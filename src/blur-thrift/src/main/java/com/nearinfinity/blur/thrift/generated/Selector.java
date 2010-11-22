/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package com.nearinfinity.blur.thrift.generated;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.protocol.*;

public class Selector implements TBase<Selector, Selector._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("Selector");

  private static final TField ID_FIELD_DESC = new TField("id", TType.STRING, (short)1);
  private static final TField LOCATION_ID_FIELD_DESC = new TField("locationId", TType.STRING, (short)2);
  private static final TField COLUMN_FAMILIES_FIELD_DESC = new TField("columnFamilies", TType.SET, (short)3);
  private static final TField COLUMNS_FIELD_DESC = new TField("columns", TType.MAP, (short)4);

  public String id;
  public String locationId;
  public Set<String> columnFamilies;
  public Map<String,Set<String>> columns;

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements TFieldIdEnum {
    ID((short)1, "id"),
    LOCATION_ID((short)2, "locationId"),
    COLUMN_FAMILIES((short)3, "columnFamilies"),
    COLUMNS((short)4, "columns");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ID
          return ID;
        case 2: // LOCATION_ID
          return LOCATION_ID;
        case 3: // COLUMN_FAMILIES
          return COLUMN_FAMILIES;
        case 4: // COLUMNS
          return COLUMNS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments

  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new FieldMetaData("id", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.LOCATION_ID, new FieldMetaData("locationId", TFieldRequirementType.DEFAULT, 
        new FieldValueMetaData(TType.STRING)));
    tmpMap.put(_Fields.COLUMN_FAMILIES, new FieldMetaData("columnFamilies", TFieldRequirementType.DEFAULT, 
        new SetMetaData(TType.SET, 
            new FieldValueMetaData(TType.STRING))));
    tmpMap.put(_Fields.COLUMNS, new FieldMetaData("columns", TFieldRequirementType.DEFAULT, 
        new MapMetaData(TType.MAP, 
            new FieldValueMetaData(TType.STRING), 
            new SetMetaData(TType.SET, 
                new FieldValueMetaData(TType.STRING)))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(Selector.class, metaDataMap);
  }

  public Selector() {
  }

  public Selector(
    String id,
    String locationId,
    Set<String> columnFamilies,
    Map<String,Set<String>> columns)
  {
    this();
    this.id = id;
    this.locationId = locationId;
    this.columnFamilies = columnFamilies;
    this.columns = columns;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Selector(Selector other) {
    if (other.isSetId()) {
      this.id = other.id;
    }
    if (other.isSetLocationId()) {
      this.locationId = other.locationId;
    }
    if (other.isSetColumnFamilies()) {
      Set<String> __this__columnFamilies = new HashSet<String>();
      for (String other_element : other.columnFamilies) {
        __this__columnFamilies.add(other_element);
      }
      this.columnFamilies = __this__columnFamilies;
    }
    if (other.isSetColumns()) {
      Map<String,Set<String>> __this__columns = new HashMap<String,Set<String>>();
      for (Map.Entry<String, Set<String>> other_element : other.columns.entrySet()) {

        String other_element_key = other_element.getKey();
        Set<String> other_element_value = other_element.getValue();

        String __this__columns_copy_key = other_element_key;

        Set<String> __this__columns_copy_value = new HashSet<String>();
        for (String other_element_value_element : other_element_value) {
          __this__columns_copy_value.add(other_element_value_element);
        }

        __this__columns.put(__this__columns_copy_key, __this__columns_copy_value);
      }
      this.columns = __this__columns;
    }
  }

  public Selector deepCopy() {
    return new Selector(this);
  }

  @Deprecated
  public Selector clone() {
    return new Selector(this);
  }

  public String getId() {
    return this.id;
  }

  public Selector setId(String id) {
    this.id = id;
    return this;
  }

  public void unsetId() {
    this.id = null;
  }

  /** Returns true if field id is set (has been asigned a value) and false otherwise */
  public boolean isSetId() {
    return this.id != null;
  }

  public void setIdIsSet(boolean value) {
    if (!value) {
      this.id = null;
    }
  }

  public String getLocationId() {
    return this.locationId;
  }

  public Selector setLocationId(String locationId) {
    this.locationId = locationId;
    return this;
  }

  public void unsetLocationId() {
    this.locationId = null;
  }

  /** Returns true if field locationId is set (has been asigned a value) and false otherwise */
  public boolean isSetLocationId() {
    return this.locationId != null;
  }

  public void setLocationIdIsSet(boolean value) {
    if (!value) {
      this.locationId = null;
    }
  }

  public int getColumnFamiliesSize() {
    return (this.columnFamilies == null) ? 0 : this.columnFamilies.size();
  }

  public java.util.Iterator<String> getColumnFamiliesIterator() {
    return (this.columnFamilies == null) ? null : this.columnFamilies.iterator();
  }

  public void addToColumnFamilies(String elem) {
    if (this.columnFamilies == null) {
      this.columnFamilies = new HashSet<String>();
    }
    this.columnFamilies.add(elem);
  }

  public Set<String> getColumnFamilies() {
    return this.columnFamilies;
  }

  public Selector setColumnFamilies(Set<String> columnFamilies) {
    this.columnFamilies = columnFamilies;
    return this;
  }

  public void unsetColumnFamilies() {
    this.columnFamilies = null;
  }

  /** Returns true if field columnFamilies is set (has been asigned a value) and false otherwise */
  public boolean isSetColumnFamilies() {
    return this.columnFamilies != null;
  }

  public void setColumnFamiliesIsSet(boolean value) {
    if (!value) {
      this.columnFamilies = null;
    }
  }

  public int getColumnsSize() {
    return (this.columns == null) ? 0 : this.columns.size();
  }

  public void putToColumns(String key, Set<String> val) {
    if (this.columns == null) {
      this.columns = new HashMap<String,Set<String>>();
    }
    this.columns.put(key, val);
  }

  public Map<String,Set<String>> getColumns() {
    return this.columns;
  }

  public Selector setColumns(Map<String,Set<String>> columns) {
    this.columns = columns;
    return this;
  }

  public void unsetColumns() {
    this.columns = null;
  }

  /** Returns true if field columns is set (has been asigned a value) and false otherwise */
  public boolean isSetColumns() {
    return this.columns != null;
  }

  public void setColumnsIsSet(boolean value) {
    if (!value) {
      this.columns = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((String)value);
      }
      break;

    case LOCATION_ID:
      if (value == null) {
        unsetLocationId();
      } else {
        setLocationId((String)value);
      }
      break;

    case COLUMN_FAMILIES:
      if (value == null) {
        unsetColumnFamilies();
      } else {
        setColumnFamilies((Set<String>)value);
      }
      break;

    case COLUMNS:
      if (value == null) {
        unsetColumns();
      } else {
        setColumns((Map<String,Set<String>>)value);
      }
      break;

    }
  }

  public void setFieldValue(int fieldID, Object value) {
    setFieldValue(_Fields.findByThriftIdOrThrow(fieldID), value);
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return getId();

    case LOCATION_ID:
      return getLocationId();

    case COLUMN_FAMILIES:
      return getColumnFamilies();

    case COLUMNS:
      return getColumns();

    }
    throw new IllegalStateException();
  }

  public Object getFieldValue(int fieldId) {
    return getFieldValue(_Fields.findByThriftIdOrThrow(fieldId));
  }

  /** Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    switch (field) {
    case ID:
      return isSetId();
    case LOCATION_ID:
      return isSetLocationId();
    case COLUMN_FAMILIES:
      return isSetColumnFamilies();
    case COLUMNS:
      return isSetColumns();
    }
    throw new IllegalStateException();
  }

  public boolean isSet(int fieldID) {
    return isSet(_Fields.findByThriftIdOrThrow(fieldID));
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Selector)
      return this.equals((Selector)that);
    return false;
  }

  public boolean equals(Selector that) {
    if (that == null)
      return false;

    boolean this_present_id = true && this.isSetId();
    boolean that_present_id = true && that.isSetId();
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (!this.id.equals(that.id))
        return false;
    }

    boolean this_present_locationId = true && this.isSetLocationId();
    boolean that_present_locationId = true && that.isSetLocationId();
    if (this_present_locationId || that_present_locationId) {
      if (!(this_present_locationId && that_present_locationId))
        return false;
      if (!this.locationId.equals(that.locationId))
        return false;
    }

    boolean this_present_columnFamilies = true && this.isSetColumnFamilies();
    boolean that_present_columnFamilies = true && that.isSetColumnFamilies();
    if (this_present_columnFamilies || that_present_columnFamilies) {
      if (!(this_present_columnFamilies && that_present_columnFamilies))
        return false;
      if (!this.columnFamilies.equals(that.columnFamilies))
        return false;
    }

    boolean this_present_columns = true && this.isSetColumns();
    boolean that_present_columns = true && that.isSetColumns();
    if (this_present_columns || that_present_columns) {
      if (!(this_present_columns && that_present_columns))
        return false;
      if (!this.columns.equals(that.columns))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(Selector other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    Selector typedOther = (Selector)other;

    lastComparison = Boolean.valueOf(isSetId()).compareTo(typedOther.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {      lastComparison = TBaseHelper.compareTo(this.id, typedOther.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetLocationId()).compareTo(typedOther.isSetLocationId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetLocationId()) {      lastComparison = TBaseHelper.compareTo(this.locationId, typedOther.locationId);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetColumnFamilies()).compareTo(typedOther.isSetColumnFamilies());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumnFamilies()) {      lastComparison = TBaseHelper.compareTo(this.columnFamilies, typedOther.columnFamilies);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetColumns()).compareTo(typedOther.isSetColumns());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetColumns()) {      lastComparison = TBaseHelper.compareTo(this.columns, typedOther.columns);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: // ID
          if (field.type == TType.STRING) {
            this.id = iprot.readString();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: // LOCATION_ID
          if (field.type == TType.STRING) {
            this.locationId = iprot.readString();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 3: // COLUMN_FAMILIES
          if (field.type == TType.SET) {
            {
              TSet _set34 = iprot.readSetBegin();
              this.columnFamilies = new HashSet<String>(2*_set34.size);
              for (int _i35 = 0; _i35 < _set34.size; ++_i35)
              {
                String _elem36;
                _elem36 = iprot.readString();
                this.columnFamilies.add(_elem36);
              }
              iprot.readSetEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 4: // COLUMNS
          if (field.type == TType.MAP) {
            {
              TMap _map37 = iprot.readMapBegin();
              this.columns = new HashMap<String,Set<String>>(2*_map37.size);
              for (int _i38 = 0; _i38 < _map37.size; ++_i38)
              {
                String _key39;
                Set<String> _val40;
                _key39 = iprot.readString();
                {
                  TSet _set41 = iprot.readSetBegin();
                  _val40 = new HashSet<String>(2*_set41.size);
                  for (int _i42 = 0; _i42 < _set41.size; ++_i42)
                  {
                    String _elem43;
                    _elem43 = iprot.readString();
                    _val40.add(_elem43);
                  }
                  iprot.readSetEnd();
                }
                this.columns.put(_key39, _val40);
              }
              iprot.readMapEnd();
            }
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();

    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.id != null) {
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeString(this.id);
      oprot.writeFieldEnd();
    }
    if (this.locationId != null) {
      oprot.writeFieldBegin(LOCATION_ID_FIELD_DESC);
      oprot.writeString(this.locationId);
      oprot.writeFieldEnd();
    }
    if (this.columnFamilies != null) {
      oprot.writeFieldBegin(COLUMN_FAMILIES_FIELD_DESC);
      {
        oprot.writeSetBegin(new TSet(TType.STRING, this.columnFamilies.size()));
        for (String _iter44 : this.columnFamilies)
        {
          oprot.writeString(_iter44);
        }
        oprot.writeSetEnd();
      }
      oprot.writeFieldEnd();
    }
    if (this.columns != null) {
      oprot.writeFieldBegin(COLUMNS_FIELD_DESC);
      {
        oprot.writeMapBegin(new TMap(TType.STRING, TType.SET, this.columns.size()));
        for (Map.Entry<String, Set<String>> _iter45 : this.columns.entrySet())
        {
          oprot.writeString(_iter45.getKey());
          {
            oprot.writeSetBegin(new TSet(TType.STRING, _iter45.getValue().size()));
            for (String _iter46 : _iter45.getValue())
            {
              oprot.writeString(_iter46);
            }
            oprot.writeSetEnd();
          }
        }
        oprot.writeMapEnd();
      }
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Selector(");
    boolean first = true;

    sb.append("id:");
    if (this.id == null) {
      sb.append("null");
    } else {
      sb.append(this.id);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("locationId:");
    if (this.locationId == null) {
      sb.append("null");
    } else {
      sb.append(this.locationId);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("columnFamilies:");
    if (this.columnFamilies == null) {
      sb.append("null");
    } else {
      sb.append(this.columnFamilies);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("columns:");
    if (this.columns == null) {
      sb.append("null");
    } else {
      sb.append(this.columns);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
  }

}

