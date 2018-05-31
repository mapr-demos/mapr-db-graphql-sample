package com.mapr.music.api.graphql.errors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

public class GraphQLErrorWrapper extends ExceptionWhileDataFetching {

  private final ExceptionWhileDataFetching inner;

  public GraphQLErrorWrapper(ExceptionWhileDataFetching inner) {

    super(ExecutionPath.rootPath(), new Exception(), null);
    if (inner == null) {
      throw new IllegalArgumentException("Exception can not be null");
    }

    this.inner = inner;
  }

  @JsonValue
  public Throwable getCause() {
    return inner.getException();
  }

  @Override
  @JsonIgnore
  public Throwable getException() {
    return super.getException();
  }

  @Override
  @JsonIgnore
  public String getMessage() {
    return null;
  }

  @Override
  @JsonIgnore
  public List<SourceLocation> getLocations() {
    return null;
  }

  @Override
  @JsonIgnore
  public List<Object> getPath() {
    return null;
  }

  @Override
  @JsonIgnore
  public Map<String, Object> getExtensions() {
    return null;
  }

  @Override
  @JsonIgnore
  public ErrorType getErrorType() {
    return null;
  }
}
