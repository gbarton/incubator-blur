package org.apache.blur.thrift;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.blur.MiniCluster;
import org.apache.blur.manager.IndexManager;
import org.apache.blur.thirdparty.thrift_0_9_0.TException;
import org.apache.blur.thrift.generated.Blur;
import org.apache.blur.thrift.generated.Blur.Iface;
import org.apache.blur.thrift.generated.BlurException;
import org.apache.blur.thrift.generated.BlurQuery;
import org.apache.blur.thrift.generated.BlurResult;
import org.apache.blur.thrift.generated.BlurResults;
import org.apache.blur.thrift.generated.ErrorType;
import org.apache.blur.thrift.generated.Query;
import org.apache.blur.thrift.generated.RecordMutation;
import org.apache.blur.thrift.generated.RowMutation;
import org.apache.blur.thrift.generated.TableDescriptor;
import org.apache.blur.thrift.util.BlurThriftHelper;
import org.apache.blur.utils.GCWatcher;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.zookeeper.KeeperException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlurClusterTest {

  private static final int _1MB = 1000 * 1000;
  private static final int _20MB = 20 * _1MB;
  private static final File TMPDIR = new File(System.getProperty("blur.tmp.dir", "./target/tmp_BlurClusterTest"));

  @BeforeClass
  public static void startCluster() throws IOException {
    GCWatcher.init(0.80);
    LocalFileSystem localFS = FileSystem.getLocal(new Configuration());
    File testDirectory = new File(TMPDIR, "blur-cluster-test");
    testDirectory.mkdirs();

    Path directory = new Path(testDirectory.getPath());
    FsPermission dirPermissions = localFS.getFileStatus(directory).getPermission();
    FsAction userAction = dirPermissions.getUserAction();
    FsAction groupAction = dirPermissions.getGroupAction();
    FsAction otherAction = dirPermissions.getOtherAction();

    StringBuilder builder = new StringBuilder();
    builder.append(userAction.ordinal());
    builder.append(groupAction.ordinal());
    builder.append(otherAction.ordinal());
    String dirPermissionNum = builder.toString();
    System.setProperty("dfs.datanode.data.dir.perm", dirPermissionNum);
    testDirectory.delete();

    MiniCluster.startBlurCluster("target/cluster", 2, 3);
  }

  @AfterClass
  public static void shutdownCluster() {
    MiniCluster.shutdownBlurCluster();
  }

  private Iface getClient() {
    return BlurClient.getClient(MiniCluster.getControllerConnectionStr());
  }

  @Test
  public void testCreateTable() throws BlurException, TException, IOException {
    Blur.Iface client = getClient();
    TableDescriptor tableDescriptor = new TableDescriptor();
    tableDescriptor.setName("test");
    tableDescriptor.setShardCount(5);
    tableDescriptor.setTableUri(MiniCluster.getFileSystemUri().toString() + "/blur/test");
    client.createTable(tableDescriptor);
    List<String> tableList = client.tableList();
    assertEquals(Arrays.asList("test"), tableList);
  }

  @Test
  public void testLoadTable() throws BlurException, TException, InterruptedException {
    Iface client = getClient();
    int length = 100;
    List<RowMutation> mutations = new ArrayList<RowMutation>();
    for (int i = 0; i < length; i++) {
      String rowId = UUID.randomUUID().toString();
      RecordMutation mutation = BlurThriftHelper.newRecordMutation("test", rowId,
          BlurThriftHelper.newColumn("test", "value"));
      RowMutation rowMutation = BlurThriftHelper.newRowMutation("test", rowId, mutation);
      rowMutation.setWaitToBeVisible(true);
      mutations.add(rowMutation);
    }
    long s = System.nanoTime();
    client.mutateBatch(mutations);
    long e = System.nanoTime();
    System.out.println("mutateBatch took [" + (e - s) / 1000000.0 + "]");
    BlurQuery blurQueryRow = new BlurQuery();
    Query queryRow = new Query();
    queryRow.setQuery("test.test:value");
    blurQueryRow.setQuery(queryRow);
    blurQueryRow.setUseCacheIfPresent(false);
    blurQueryRow.setCacheResult(false);
    BlurResults resultsRow = client.query("test", blurQueryRow);
    assertRowResults(resultsRow);
    assertEquals(length, resultsRow.getTotalResults());

    BlurQuery blurQueryRecord = new BlurQuery();
    Query queryRecord = new Query();
    queryRecord.rowQuery = false;
    queryRecord.setQuery("test.test:value");
    blurQueryRecord.setQuery(queryRecord);
    BlurResults resultsRecord = client.query("test", blurQueryRecord);
    assertRecordResults(resultsRecord);
    assertEquals(length, resultsRecord.getTotalResults());
  }

  @Test
  public void testQueryCancel() throws BlurException, TException, InterruptedException {
    // This will make each collect in the collectors pause 250 ms per collect
    // call
    IndexManager.DEBUG_RUN_SLOW.set(true);

    final Iface client = getClient();
    final BlurQuery blurQueryRow = new BlurQuery();
    Query queryRow = new Query();
    queryRow.setQuery("test.test:value");
    blurQueryRow.setQuery(queryRow);
    blurQueryRow.setUseCacheIfPresent(false);
    blurQueryRow.setCacheResult(false);
    blurQueryRow.setUuid(1234l);

    final AtomicReference<BlurException> error = new AtomicReference<BlurException>();
    final AtomicBoolean fail = new AtomicBoolean();

    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // This call will take several seconds to execute.
          client.query("test", blurQueryRow);
          fail.set(true);
        } catch (BlurException e) {
          error.set(e);
        } catch (TException e) {
          e.printStackTrace();
          fail.set(true);
        }
      }
    }).start();
    Thread.sleep(500);
    client.cancelQuery("test", blurQueryRow.getUuid());
    BlurException blurException = pollForError(error, 10, TimeUnit.SECONDS, null);
    if (fail.get()) {
      fail("Unknown error, failing test.");
    }
    assertEquals(blurException.getErrorType(), ErrorType.QUERY_CANCEL);
  }

  @Test
  public void testBackPressureViaQuery() throws BlurException, TException, InterruptedException {
    // This will make each collect in the collectors pause 250 ms per collect
    // call
    IndexManager.DEBUG_RUN_SLOW.set(true);
    runBackPressureViaQuery();
    Thread.sleep(1000);
    System.gc();
    System.gc();
    Thread.sleep(1000);
  }

  private void runBackPressureViaQuery() throws InterruptedException {
    final Iface client = getClient();
    final BlurQuery blurQueryRow = new BlurQuery();
    Query queryRow = new Query();
    queryRow.setQuery("test.test:value");
    blurQueryRow.setQuery(queryRow);
    blurQueryRow.setUseCacheIfPresent(false);
    blurQueryRow.setCacheResult(false);
    blurQueryRow.setUuid(1234l);

    final AtomicReference<BlurException> error = new AtomicReference<BlurException>();
    final AtomicBoolean fail = new AtomicBoolean();

    System.gc();
    System.gc();
    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage usage = memoryMXBean.getHeapMemoryUsage();
    long max = usage.getMax();
    long used = usage.getUsed();
    long limit = (long) (max * 0.80);
    long difference = limit - used;
    byte[] bufferToFillHeap = new byte[(int) (difference - _20MB)];
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // This call will take several seconds to execute.
          client.query("test", blurQueryRow);
          fail.set(true);
        } catch (BlurException e) {
          error.set(e);
        } catch (TException e) {
          e.printStackTrace();
          fail.set(true);
        }
      }
    }).start();
    Thread.sleep(500);
    List<byte[]> bufferToPutGcWatcherOverLimitList = new ArrayList<byte[]>();
    BlurException blurException = pollForError(error, 10, TimeUnit.SECONDS, bufferToPutGcWatcherOverLimitList);
    if (fail.get()) {
      fail("Unknown error, failing test.");
    }
    System.out.println(bufferToFillHeap.hashCode());
    System.out.println(bufferToPutGcWatcherOverLimitList.hashCode());
    assertEquals(blurException.getErrorType(), ErrorType.BACK_PRESSURE);
    bufferToPutGcWatcherOverLimitList.clear();
    bufferToPutGcWatcherOverLimitList = null;
    bufferToFillHeap = null;
  }

  private BlurException pollForError(AtomicReference<BlurException> error, long period, TimeUnit timeUnit,
      List<byte[]> bufferToPutGcWatcherOverLimitList) throws InterruptedException {
    long s = System.nanoTime();
    long totalTime = timeUnit.toNanos(period) + s;
    if (bufferToPutGcWatcherOverLimitList != null) {
      bufferToPutGcWatcherOverLimitList.add(new byte[_1MB * 5]);
    }
    while (totalTime > System.nanoTime()) {
      BlurException blurException = error.get();
      if (blurException != null) {
        return blurException;
      }
      Thread.sleep(500);
      if (bufferToPutGcWatcherOverLimitList != null) {
        bufferToPutGcWatcherOverLimitList.add(new byte[_1MB * 5]);
      }
    }
    return null;
  }

  @Test
  public void testTestShardFailover() throws BlurException, TException, InterruptedException, IOException,
      KeeperException {
    Iface client = getClient();
    int length = 100;
    BlurQuery blurQuery = new BlurQuery();
    blurQuery.setUseCacheIfPresent(false);
    Query query = new Query();
    query.setQuery("test.test:value");
    blurQuery.setQuery(query);
    BlurResults results1 = client.query("test", blurQuery);
    assertEquals(length, results1.getTotalResults());
    assertRowResults(results1);

    MiniCluster.killShardServer(1);

    // make sure the WAL syncs
    Thread.sleep(TimeUnit.SECONDS.toMillis(1));

    // This should block until shards have failed over
    client.shardServerLayout("test");

    assertEquals(length, client.query("test", blurQuery).getTotalResults());

  }

  private void assertRowResults(BlurResults results) {
    for (BlurResult result : results.getResults()) {
      assertNull(result.locationId);
      assertNull(result.fetchResult.recordResult);
      assertNull(result.fetchResult.rowResult.row.records);
      assertNotNull(result.fetchResult.rowResult.row.id);
    }
  }

  private void assertRecordResults(BlurResults results) {
    for (BlurResult result : results.getResults()) {
      assertNull(result.locationId);
      assertNotNull(result.fetchResult.recordResult);
      assertNotNull(result.fetchResult.recordResult.rowid);
      assertNotNull(result.fetchResult.recordResult.record.recordId);
      assertNotNull(result.fetchResult.recordResult.record.family);
      assertNull("Not null [" + result.fetchResult.recordResult.record.columns + "]",
          result.fetchResult.recordResult.record.columns);
      assertNull(result.fetchResult.rowResult);
    }
  }

  @Test
  public void testCreateDisableAndRemoveTable() throws IOException, BlurException, TException {
    Iface client = getClient();
    String tableName = UUID.randomUUID().toString();
    TableDescriptor tableDescriptor = new TableDescriptor();
    tableDescriptor.setName(tableName);
    tableDescriptor.setShardCount(5);
    tableDescriptor.setTableUri(MiniCluster.getFileSystemUri().toString() + "/blur/" + tableName);

    for (int i = 0; i < 3; i++) {
      client.createTable(tableDescriptor);
      client.disableTable(tableName);
      client.removeTable(tableName, true);
    }

    assertFalse(client.tableList().contains(tableName));

  }
}
