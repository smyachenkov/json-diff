package com.smyachenkov.jsondiff;

import java.util.Objects;

public class Difference {

  private Object value;

  private String path;

  private Operation operation;

  public Difference(Object value, String path, Operation operation) {
    this.value = value;
    this.path = path;
    this.operation = operation;
  }



  public Object getValue() {
    return value;
  }

  public String getPath() {
    return path;
  }

  public Operation getOperation() {
    return operation;
  }

  @Override
  public String toString() {
    return "Difference{" +
           "value=" + value +
           ", path='" + path + '\'' +
           ", operation=" + operation +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Difference that = (Difference) o;
    return Objects.equals(value, that.value) &&
           Objects.equals(path, that.path) &&
           operation == that.operation;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, path, operation);
  }
}
