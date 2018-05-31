package com.mapr.music.api.graphql.errors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class GraphQLCustomError extends RuntimeException implements GraphQLError {

  protected String error;
  protected String code;

  public GraphQLCustomError(String message, String code) {
    super(message);
    this.code = code;
  }

  public GraphQLCustomError(String message, String error, String code) {
    super(message);
    this.error = error;
    this.code = code;
  }

  @Override
  public String getMessage() {
    return super.getMessage();
  }

  public String getStatusCode() {
    return code;
  }

  public String getError() {
    return error;
  }

  @Override
  @JsonIgnore
  public List<SourceLocation> getLocations() {
    return null;
  }

  @Override
  @JsonIgnore
  public ErrorType getErrorType() {
    return null;
  }

  @Override
  @JsonIgnore
  public List<Object> getPath() {
    return null;
  }

  @Override
  @JsonIgnore
  public Map<String, Object> toSpecification() {
    return null;
  }

  @Override
  @JsonIgnore
  public Map<String, Object> getExtensions() {
    return null;
  }

  @Override
  @JsonIgnore
  public String getLocalizedMessage() {
    return super.getLocalizedMessage();
  }

  @Override
  @JsonIgnore
  public StackTraceElement[] getStackTrace() {
    return super.getStackTrace();
  }

}
