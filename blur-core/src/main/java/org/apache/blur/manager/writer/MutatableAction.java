/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.blur.manager.writer;

import static org.apache.blur.metrics.MetricsConstants.BLUR;
import static org.apache.blur.metrics.MetricsConstants.ORG_APACHE_BLUR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.blur.analysis.FieldManager;
import org.apache.blur.manager.IndexManager;
import org.apache.blur.server.IndexSearcherClosable;
import org.apache.blur.server.ShardContext;
import org.apache.blur.server.TableContext;
import org.apache.blur.thrift.BException;
import org.apache.blur.thrift.MutationHelper;
import org.apache.blur.thrift.generated.BlurException;
import org.apache.blur.thrift.generated.Column;
import org.apache.blur.thrift.generated.FetchResult;
import org.apache.blur.thrift.generated.FetchRowResult;
import org.apache.blur.thrift.generated.Record;
import org.apache.blur.thrift.generated.RecordMutation;
import org.apache.blur.thrift.generated.RecordMutationType;
import org.apache.blur.thrift.generated.Row;
import org.apache.blur.thrift.generated.RowMutation;
import org.apache.blur.thrift.generated.RowMutationType;
import org.apache.blur.thrift.generated.Selector;
import org.apache.blur.utils.BlurConstants;
import org.apache.blur.utils.RowDocumentUtil;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;

public class MutatableAction extends IndexAction {

  private static final Meter _writeRecordsMeter;
  private static final Meter _writeRowMeter;

  static {
    MetricName metricName1 = new MetricName(ORG_APACHE_BLUR, BLUR, "Write Records/s");
    MetricName metricName2 = new MetricName(ORG_APACHE_BLUR, BLUR, "Write Row/s");
    _writeRecordsMeter = Metrics.newMeter(metricName1, "Records/s", TimeUnit.SECONDS);
    _writeRowMeter = Metrics.newMeter(metricName2, "Row/s", TimeUnit.SECONDS);
  }

  static class UpdateRow extends InternalAction {

    static abstract class UpdateRowAction {
      abstract Row performAction(Row row);
    }

    private final List<UpdateRowAction> _actions = new ArrayList<UpdateRowAction>();
    private final String _rowId;
    private final String _table;
    private final String _shard;
    private final int _maxHeap;
    private final TableContext _tableContext;
    private final FieldManager _fieldManager;

    UpdateRow(String rowId, String table, String shard, int maxHeap, TableContext tableContext) {
      _rowId = rowId;
      _table = table;
      _shard = shard;
      _maxHeap = maxHeap;
      _tableContext = tableContext;
      _fieldManager = _tableContext.getFieldManager();
    }

    void deleteRecord(final String recordId) {
      _actions.add(new UpdateRowAction() {
        @Override
        Row performAction(Row row) {
          if (row == null) {
            return null;
          } else {
            if (row.getRecords() == null) {
              return row;
            }
            Row result = new Row();
            result.setId(row.getId());
            for (Record record : row.getRecords()) {
              if (!record.getRecordId().equals(recordId)) {
                result.addToRecords(record);
              }
            }
            return result;
          }
        }
      });
    }

    void appendColumns(final Record record) {
      _actions.add(new UpdateRowAction() {
        @Override
        Row performAction(Row row) {
          if (row == null) {
            row = new Row(_rowId, null);
            row.addToRecords(record);
            return row;
          } else {
            Row result = new Row();
            result.setId(row.getId());
            String recordId = record.getRecordId();
            boolean found = false;
            if (row.getRecords() != null) {
              for (Record r : row.getRecords()) {
                if (!r.getRecordId().equals(recordId)) {
                  result.addToRecords(r);
                } else {
                  found = true;
                  // Append columns
                  r.getColumns().addAll(record.getColumns());
                  result.addToRecords(r);
                }
              }
            }
            if (!found) {
              result.addToRecords(record);
            }
            return result;
          }
        }
      });
    }

    void replaceColumns(final Record record) {
      _actions.add(new UpdateRowAction() {
        @Override
        Row performAction(Row row) {
          if (row == null) {
            row = new Row(_rowId, null);
            row.addToRecords(record);
            return row;
          } else {
            Row result = new Row();
            result.setId(row.getId());
            String recordId = record.getRecordId();
            boolean found = false;
            if (row.getRecords() != null) {
              for (Record r : row.getRecords()) {
                if (!r.getRecordId().equals(recordId)) {
                  result.addToRecords(r);
                } else {
                  found = true;
                  // Replace columns
                  result.addToRecords(replaceColumns(r, record));
                }
              }
            }
            if (!found) {
              result.addToRecords(record);
            }
            return result;
          }
        }
      });
    }

    protected Record replaceColumns(Record existing, Record newRecord) {
      Map<String, List<Column>> existingColumns = getColumnMap(existing.getColumns());
      Map<String, List<Column>> newColumns = getColumnMap(newRecord.getColumns());
      existingColumns.putAll(newColumns);
      Record record = new Record();
      record.setFamily(existing.getFamily());
      record.setRecordId(existing.getRecordId());
      record.setColumns(toList(existingColumns.values()));
      return record;
    }

    private List<Column> toList(Collection<List<Column>> values) {
      ArrayList<Column> list = new ArrayList<Column>();
      for (List<Column> v : values) {
        list.addAll(v);
      }
      return list;
    }

    private Map<String, List<Column>> getColumnMap(List<Column> columns) {
      Map<String, List<Column>> columnMap = new TreeMap<String, List<Column>>();
      for (Column column : columns) {
        String name = column.getName();
        List<Column> list = columnMap.get(name);
        if (list == null) {
          list = new ArrayList<Column>();
          columnMap.put(name, list);
        }
        list.add(column);
      }
      return columnMap;
    }

    void replaceRecord(final Record record) {
      _actions.add(new UpdateRowAction() {
        @Override
        Row performAction(Row row) {
          if (row == null) {
            row = new Row(_rowId, null);
            row.addToRecords(record);
            return row;
          } else {
            Row result = new Row();
            result.setId(row.getId());
            String recordId = record.getRecordId();
            if (row.getRecords() != null) {
              for (Record r : row.getRecords()) {
                if (!r.getRecordId().equals(recordId)) {
                  result.addToRecords(r);
                }
              }
            }
            // Add replacement
            result.addToRecords(record);
            return result;
          }
        }
      });
    }

    @Override
    void performAction(IndexSearcherClosable searcher, IndexWriter writer) throws IOException {
      Selector selector = new Selector();
      selector.setRowId(_rowId);
      IndexManager.populateSelector(searcher, _shard, _table, selector);
      Row row = null;
      if (!selector.getLocationId().equals(IndexManager.NOT_FOUND)) {
        FetchResult fetchResult = new FetchResult();
        IndexManager.fetchRow(searcher.getIndexReader(), _table, _shard, selector, fetchResult, null, null, _maxHeap,
            _tableContext, null);
        FetchRowResult rowResult = fetchResult.getRowResult();
        if (rowResult != null) {
          row = rowResult.getRow();
        }
      }
      for (UpdateRowAction action : _actions) {
        row = action.performAction(row);
      }
      Term term = createRowId(_rowId);
      if (row != null && row.getRecords() != null && row.getRecords().size() > 0) {
        List<List<Field>> docsToUpdate = RowDocumentUtil.getDocs(row, _fieldManager);
        writer.updateDocuments(term, docsToUpdate);
        _writeRecordsMeter.mark(docsToUpdate.size());
      } else {
        writer.deleteDocuments(term);
      }
      _writeRowMeter.mark();
    }

  }

  static abstract class InternalAction {
    abstract void performAction(IndexSearcherClosable searcher, IndexWriter writer) throws IOException;
  }

  private final List<InternalAction> _actions = new ArrayList<InternalAction>();
  private final Map<String, UpdateRow> _rowUpdates = new HashMap<String, UpdateRow>();
  private final FieldManager _fieldManager;
  private final String _shard;
  private final String _table;
  private final int _maxHeap = Integer.MAX_VALUE;
  private TableContext _tableContext;

  public MutatableAction(ShardContext context) {
    _tableContext = context.getTableContext();
    _shard = context.getShard();
    _table = _tableContext.getTable();
    _fieldManager = _tableContext.getFieldManager();
  }

  public void deleteRow(final String rowId) {
    _actions.add(new InternalAction() {
      @Override
      void performAction(IndexSearcherClosable searcher, IndexWriter writer) throws IOException {
        writer.deleteDocuments(createRowId(rowId));
        _writeRowMeter.mark();
      }
    });
  }

  public void replaceRow(final Row row) {
    _actions.add(new InternalAction() {
      @Override
      void performAction(IndexSearcherClosable searcher, IndexWriter writer) throws IOException {
        List<List<Field>> docs = RowDocumentUtil.getDocs(row, _fieldManager);
        Term rowId = createRowId(row.getId());
        writer.updateDocuments(rowId, docs);
        _writeRecordsMeter.mark(docs.size());
        _writeRowMeter.mark();
      }
    });
  }

  public void deleteRecord(final String rowId, final String recordId) {
    UpdateRow updateRow = getUpdateRow(rowId);
    updateRow.deleteRecord(recordId);
  }

  public void replaceRecord(final String rowId, final Record record) {
    UpdateRow updateRow = getUpdateRow(rowId);
    updateRow.replaceRecord(record);
  }

  public void appendColumns(final String rowId, final Record record) {
    UpdateRow updateRow = getUpdateRow(rowId);
    updateRow.appendColumns(record);
  }

  public void replaceColumns(final String rowId, final Record record) {
    UpdateRow updateRow = getUpdateRow(rowId);
    updateRow.replaceColumns(record);
  }

  @Override
  public void performMutate(IndexSearcherClosable searcher, IndexWriter writer) throws IOException {
    try {
      for (InternalAction internalAction : _actions) {
        internalAction.performAction(searcher, writer);
      }
    } finally {
      _actions.clear();
    }
  }

  public static Term createRowId(String id) {
    return new Term(BlurConstants.ROW_ID, id);
  }

  public static Term createRecordId(String id) {
    return new Term(BlurConstants.RECORD_ID, id);
  }

  private synchronized UpdateRow getUpdateRow(String rowId) {
    UpdateRow updateRow = _rowUpdates.get(rowId);
    if (updateRow == null) {
      updateRow = new UpdateRow(rowId, _table, _shard, _maxHeap, _tableContext);
      _rowUpdates.put(rowId, updateRow);
      _actions.add(updateRow);
    }
    return updateRow;
  }

  @Override
  public void doPreCommit(IndexSearcherClosable indexSearcher, IndexWriter writer) {

  }

  @Override
  public void doPostCommit(IndexWriter writer) {

  }

  @Override
  public void doPreRollback(IndexWriter writer) {

  }

  @Override
  public void doPostRollback(IndexWriter writer) {

  }

  public void mutate(RowMutation mutation) {
    RowMutationType type = mutation.rowMutationType;
    switch (type) {
    case REPLACE_ROW:
      Row row = MutationHelper.getRowFromMutations(mutation.rowId, mutation.recordMutations);
      replaceRow(row);
      break;
    case UPDATE_ROW:
      doUpdateRowMutation(mutation, this);
      break;
    case DELETE_ROW:
      deleteRow(mutation.rowId);
      break;
    default:
      throw new RuntimeException("Not supported [" + type + "]");
    }
  }

  private void doUpdateRowMutation(RowMutation mutation, MutatableAction mutatableAction) {
    String rowId = mutation.getRowId();
    for (RecordMutation recordMutation : mutation.getRecordMutations()) {
      RecordMutationType type = recordMutation.recordMutationType;
      Record record = recordMutation.getRecord();
      switch (type) {
      case DELETE_ENTIRE_RECORD:
        mutatableAction.deleteRecord(rowId, record.getRecordId());
        break;
      case APPEND_COLUMN_VALUES:
        mutatableAction.appendColumns(rowId, record);
        break;
      case REPLACE_ENTIRE_RECORD:
        mutatableAction.replaceRecord(rowId, record);
        break;
      case REPLACE_COLUMNS:
        mutatableAction.replaceColumns(rowId, record);
        break;
      default:
        throw new RuntimeException("Unsupported record mutation type [" + type + "]");
      }
    }
  }

  public void mutate(List<RowMutation> mutations) {
    for (int i = 0; i < mutations.size(); i++) {
      mutate(mutations.get(i));
    }
  }

  public static List<RowMutation> reduceMutates(List<RowMutation> mutations) throws BlurException {
    Map<String, RowMutation> mutateMap = new TreeMap<String, RowMutation>();
    for (RowMutation mutation : mutations) {
      RowMutation rowMutation = mutateMap.get(mutation.getRowId());
      if (rowMutation != null) {
        mutateMap.put(mutation.getRowId(), merge(rowMutation, mutation));
      } else {
        mutateMap.put(mutation.getRowId(), mutation);
      }
    }
    return new ArrayList<RowMutation>(mutateMap.values());
  }

  private static RowMutation merge(RowMutation mutation1, RowMutation mutation2) throws BlurException {
    RowMutationType rowMutationType1 = mutation1.getRowMutationType();
    RowMutationType rowMutationType2 = mutation2.getRowMutationType();
    if (!rowMutationType1.equals(rowMutationType2)) {
      throw new BException(
          "RowMutation conflict, cannot perform 2 different operations on the same row in the same batch. [{0}] [{1}]",
          mutation1, mutation2);
    }
    if (rowMutationType1.equals(RowMutationType.DELETE_ROW)) {
      // Since both are trying to delete the same row, just pick one and move
      // on.
      return mutation1;
    } else if (rowMutationType1.equals(RowMutationType.REPLACE_ROW)) {
      throw new BException(
          "RowMutation conflict, cannot perform 2 different REPLACE_ROW mutations on the same row in the same batch. [{0}] [{1}]",
          mutation1, mutation2);
    } else {
      // Now this is a row update, so try to merge the record mutations
      List<RecordMutation> recordMutations1 = mutation1.getRecordMutations();
      List<RecordMutation> recordMutations2 = mutation2.getRecordMutations();
      List<RecordMutation> mergedRecordMutations = merge(recordMutations1, recordMutations2);
      mutation1.setRecordMutations(mergedRecordMutations);
      return mutation1;
    }
  }

  private static List<RecordMutation> merge(List<RecordMutation> recordMutations1, List<RecordMutation> recordMutations2)
      throws BException {
    Map<String, RecordMutation> recordMutationMap = new TreeMap<String, RecordMutation>();
    merge(recordMutations1, recordMutationMap);
    merge(recordMutations2, recordMutationMap);
    return new ArrayList<RecordMutation>(recordMutationMap.values());
  }

  private static void merge(List<RecordMutation> recordMutations, Map<String, RecordMutation> recordMutationMap)
      throws BException {
    for (RecordMutation recordMutation : recordMutations) {
      Record record = recordMutation.getRecord();
      String recordId = record.getRecordId();
      RecordMutation existing = recordMutationMap.get(recordId);
      if (existing != null) {
        recordMutationMap.put(recordId, merge(recordMutation, existing));
      } else {
        recordMutationMap.put(recordId, recordMutation);
      }
    }
  }

  private static RecordMutation merge(RecordMutation recordMutation1, RecordMutation recordMutation2) throws BException {
    RecordMutationType recordMutationType1 = recordMutation1.getRecordMutationType();
    RecordMutationType recordMutationType2 = recordMutation2.getRecordMutationType();
    if (!recordMutationType1.equals(recordMutationType2)) {
      throw new BException(
          "RecordMutation conflict, cannot perform 2 different operations on the same record in the same row in the same batch. [{0}] [{1}]",
          recordMutation1, recordMutation2);
    }

    if (recordMutationType1.equals(RecordMutationType.DELETE_ENTIRE_RECORD)) {
      // Since both are trying to delete the same record, just pick one and move
      // on.
      return recordMutation1;
    } else if (recordMutationType1.equals(RecordMutationType.REPLACE_ENTIRE_RECORD)) {
      throw new BException(
          "RecordMutation conflict, cannot perform 2 different replace record operations on the same record in the same row in the same batch. [{0}] [{1}]",
          recordMutation1, recordMutation2);
    } else if (recordMutationType1.equals(RecordMutationType.REPLACE_COLUMNS)) {
      throw new BException(
          "RecordMutation conflict, cannot perform 2 different replace columns operations on the same record in the same row in the same batch. [{0}] [{1}]",
          recordMutation1, recordMutation2);
    } else {
      Record record1 = recordMutation1.getRecord();
      Record record2 = recordMutation2.getRecord();
      String family1 = record1.getFamily();
      String family2 = record2.getFamily();

      if (isSameFamily(family1, family2)) {
        record1.getColumns().addAll(record2.getColumns());
        return recordMutation1;
      } else {
        throw new BException("RecordMutation conflict, cannot merge records with different family. [{0}] [{1}]",
            recordMutation1, recordMutation2);
      }
    }
  }

  private static boolean isSameFamily(String family1, String family2) {
    if (family1 == null && family2 == null) {
      return true;
    }
    if (family1 != null && family1.equals(family2)) {
      return true;
    }
    return false;
  }
}
