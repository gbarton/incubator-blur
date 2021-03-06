package org.apache.blur.jdbc.abstractimpl;

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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.apache.blur.jdbc.util.NotImplemented;

/**
 * This Statement implementation is simply to provide the major of the
 * method implementations that only throw not implemented exceptions. That way
 * it's easier to see what has been implemented in the real class.
 */
public class AbstractBlurStatement implements Statement {

  private Statement throwExceptionDelegate;

  public AbstractBlurStatement() {
    throwExceptionDelegate = (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(),
        new Class[] { Statement.class }, new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            throw new NotImplemented(method.getName());
          }
        });
  }

  public void addBatch(String sql) throws SQLException {
    throwExceptionDelegate.addBatch(sql);
  }

  public void cancel() throws SQLException {
    throwExceptionDelegate.cancel();
  }

  public void clearBatch() throws SQLException {
    throwExceptionDelegate.clearBatch();
  }

  public void clearWarnings() throws SQLException {
    throwExceptionDelegate.clearWarnings();
  }

  public void close() throws SQLException {
    throwExceptionDelegate.close();
  }

  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    return throwExceptionDelegate.execute(sql, autoGeneratedKeys);
  }

  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    return throwExceptionDelegate.execute(sql, columnIndexes);
  }

  public boolean execute(String sql, String[] columnNames) throws SQLException {
    return throwExceptionDelegate.execute(sql, columnNames);
  }

  public boolean execute(String sql) throws SQLException {
    return throwExceptionDelegate.execute(sql);
  }

  public int[] executeBatch() throws SQLException {
    return throwExceptionDelegate.executeBatch();
  }

  public ResultSet executeQuery(String sql) throws SQLException {
    return throwExceptionDelegate.executeQuery(sql);
  }

  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    return throwExceptionDelegate.executeUpdate(sql, autoGeneratedKeys);
  }

  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    return throwExceptionDelegate.executeUpdate(sql, columnIndexes);
  }

  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    return throwExceptionDelegate.executeUpdate(sql, columnNames);
  }

  public int executeUpdate(String sql) throws SQLException {
    return throwExceptionDelegate.executeUpdate(sql);
  }

  public Connection getConnection() throws SQLException {
    return throwExceptionDelegate.getConnection();
  }

  public int getFetchDirection() throws SQLException {
    return throwExceptionDelegate.getFetchDirection();
  }

  public int getFetchSize() throws SQLException {
    return throwExceptionDelegate.getFetchSize();
  }

  public ResultSet getGeneratedKeys() throws SQLException {
    return throwExceptionDelegate.getGeneratedKeys();
  }

  public int getMaxFieldSize() throws SQLException {
    return throwExceptionDelegate.getMaxFieldSize();
  }

  public int getMaxRows() throws SQLException {
    return throwExceptionDelegate.getMaxRows();
  }

  public boolean getMoreResults() throws SQLException {
    return throwExceptionDelegate.getMoreResults();
  }

  public boolean getMoreResults(int current) throws SQLException {
    return throwExceptionDelegate.getMoreResults(current);
  }

  public int getQueryTimeout() throws SQLException {
    return throwExceptionDelegate.getQueryTimeout();
  }

  public ResultSet getResultSet() throws SQLException {
    return throwExceptionDelegate.getResultSet();
  }

  public int getResultSetConcurrency() throws SQLException {
    return throwExceptionDelegate.getResultSetConcurrency();
  }

  public int getResultSetHoldability() throws SQLException {
    return throwExceptionDelegate.getResultSetHoldability();
  }

  public int getResultSetType() throws SQLException {
    return throwExceptionDelegate.getResultSetType();
  }

  public int getUpdateCount() throws SQLException {
    return throwExceptionDelegate.getUpdateCount();
  }

  public SQLWarning getWarnings() throws SQLException {
    return throwExceptionDelegate.getWarnings();
  }

  public boolean isClosed() throws SQLException {
    return throwExceptionDelegate.isClosed();
  }

  public boolean isPoolable() throws SQLException {
    return throwExceptionDelegate.isPoolable();
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return throwExceptionDelegate.isWrapperFor(iface);
  }

  public void setCursorName(String name) throws SQLException {
    throwExceptionDelegate.setCursorName(name);
  }

  public void setEscapeProcessing(boolean enable) throws SQLException {
    throwExceptionDelegate.setEscapeProcessing(enable);
  }

  public void setFetchDirection(int direction) throws SQLException {
    throwExceptionDelegate.setFetchDirection(direction);
  }

  public void setFetchSize(int rows) throws SQLException {
    throwExceptionDelegate.setFetchSize(rows);
  }

  public void setMaxFieldSize(int max) throws SQLException {
    throwExceptionDelegate.setMaxFieldSize(max);
  }

  public void setMaxRows(int max) throws SQLException {
    throwExceptionDelegate.setMaxRows(max);
  }

  public void setPoolable(boolean poolable) throws SQLException {
    throwExceptionDelegate.setPoolable(poolable);
  }

  public void setQueryTimeout(int seconds) throws SQLException {
    throwExceptionDelegate.setQueryTimeout(seconds);
  }

  public <T> T unwrap(Class<T> iface) throws SQLException {
    return throwExceptionDelegate.unwrap(iface);
  }

  // java 7

  public void closeOnCompletion() throws SQLException {
    throw new SQLException("No supported");
  }

  public boolean isCloseOnCompletion() throws SQLException {
    throw new SQLException("No supported");
  }

}