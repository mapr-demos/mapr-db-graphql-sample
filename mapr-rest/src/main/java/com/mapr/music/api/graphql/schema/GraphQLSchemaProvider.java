package com.mapr.music.api.graphql.schema;

import com.mapr.music.api.graphql.GraphQLEndpoint;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GraphQLSchemaProvider {

  private static final String SCHEMA_FILENAME = "schema.graphqls";

  private final AlbumDataFetcher albumDataFetcher;
  private final ArtistDataFetcher artistDataFetcher;
  private final UserDataFetcher userDataFetcher;

  @Inject
  public GraphQLSchemaProvider(AlbumDataFetcher albumDataFetcher,
                               ArtistDataFetcher artistDataFetcher,
                               UserDataFetcher userDataFetcher) {

    this.albumDataFetcher = albumDataFetcher;
    this.artistDataFetcher = artistDataFetcher;
    this.userDataFetcher = userDataFetcher;
  }

  public GraphQLSchema schema() {

    SchemaParser schemaParser = new SchemaParser();
    InputStream schemaFile = GraphQLEndpoint.class.getClassLoader().getResourceAsStream(SCHEMA_FILENAME);
    TypeDefinitionRegistry typeRegistry = schemaParser.parse(new InputStreamReader(schemaFile));

    return new SchemaGenerator().makeExecutableSchema(typeRegistry, buildRuntimeWiring());
  }

  private RuntimeWiring buildRuntimeWiring() {
    return RuntimeWiring.newRuntimeWiring()
      .type("Query", typeWiring -> typeWiring
        .dataFetcher("currentUser", userDataFetcher.currentUser())
        .dataFetcher("album", albumDataFetcher.album())
        .dataFetcher("albums", albumDataFetcher.albums())
        .dataFetcher("artist", artistDataFetcher.artist())
        .dataFetcher("artists", artistDataFetcher.artists())
      )
      .type("Mutation", typeWiring -> typeWiring
        .dataFetcher("createAlbum", albumDataFetcher.createAlbum())
        .dataFetcher("updateAlbum", albumDataFetcher.updateAlbum())
        .dataFetcher("deleteAlbum", albumDataFetcher.deleteAlbum())
        .dataFetcher("createArtist", artistDataFetcher.createArtist())
        .dataFetcher("updateArtist", artistDataFetcher.updateArtist())
        .dataFetcher("deleteArtist", artistDataFetcher.deleteArtist())
      )
      .type("Album", typeWiring -> typeWiring
        .dataFetcher("artists", albumDataFetcher.artists())
      )
      .type("Artist", typeWiring -> typeWiring
        .dataFetcher("albums", artistDataFetcher.albums())
      )
      .build();
  }

}
