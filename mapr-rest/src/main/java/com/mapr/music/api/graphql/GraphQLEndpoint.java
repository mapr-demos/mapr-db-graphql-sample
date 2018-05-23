package com.mapr.music.api.graphql;

import com.coxautodev.graphql.tools.SchemaParser;
import com.mapr.music.service.AlbumService;
import graphql.servlet.SimpleGraphQLServlet;

import javax.inject.Inject;

public class GraphQLEndpoint extends SimpleGraphQLServlet {

  @Inject
  public GraphQLEndpoint(AlbumService albumService) {
    super(SchemaParser.newParser()
      .file("schema.graphqls")
      .resolvers(new Query(albumService), new Mutation(albumService))
      .build()
      .makeExecutableSchema());

  }
}
