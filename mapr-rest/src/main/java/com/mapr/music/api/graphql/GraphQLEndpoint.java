package com.mapr.music.api.graphql;

import com.mapr.music.api.graphql.errors.GraphQLErrorWrapper;
import com.mapr.music.api.graphql.schema.GraphQLSchemaProvider;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.servlet.DefaultGraphQLErrorHandler;
import graphql.servlet.GraphQLErrorHandler;
import graphql.servlet.SimpleGraphQLServlet;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class GraphQLEndpoint extends SimpleGraphQLServlet {

  @Inject
  public GraphQLEndpoint(GraphQLSchemaProvider schemaProvider) {
    super(schemaProvider.schema());
  }

  @Override
  protected GraphQLErrorHandler getGraphQLErrorHandler() {
    return new DefaultGraphQLErrorHandler() {

      @Override
      protected List<GraphQLError> filterGraphQLErrors(List<GraphQLError> errors) {
        return errors.stream()
          .filter(e -> e instanceof ExceptionWhileDataFetching || super.isClientError(e))
          .map(e -> e instanceof ExceptionWhileDataFetching ? new GraphQLErrorWrapper((ExceptionWhileDataFetching) e) : e)
          .collect(Collectors.toList());
      }
    };
  }
}
