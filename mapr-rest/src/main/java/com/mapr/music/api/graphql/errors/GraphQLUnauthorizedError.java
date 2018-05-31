package com.mapr.music.api.graphql.errors;

public class GraphQLUnauthorizedError extends GraphQLCustomError {

  public static final String STATUS_CODE = "401";

  public GraphQLUnauthorizedError() {
    super("Unautorized", "Unautorized", STATUS_CODE);
  }

  public GraphQLUnauthorizedError(String message) {
    super(message, STATUS_CODE);
  }

  public GraphQLUnauthorizedError(String message, String error) {
    super(message, error, STATUS_CODE);
  }

}
