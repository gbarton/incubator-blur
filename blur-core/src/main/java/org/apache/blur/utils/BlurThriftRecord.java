package org.apache.blur.utils;

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
import org.apache.blur.thrift.generated.Column;
import org.apache.blur.thrift.generated.Record;

public class BlurThriftRecord extends Record implements ReaderBlurRecord {

  private static final long serialVersionUID = 1447192115360284850L;

  @Override
  public void addColumn(String name, String value) {
    addToColumns(new Column(name, value));
  }

  @Override
  public void setRecordIdStr(String value) {
    setRecordId(value);
  }

  @Override
  public void setFamilyStr(String family) {
    setFamily(family);
  }

  @Override
  public void setRowIdStr(String rowId) {
    // do nothing
  }

}
