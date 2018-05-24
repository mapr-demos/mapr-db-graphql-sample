package com.mapr.music.api.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.ArtistService;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.servlet.SimpleGraphQLServlet;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class GraphQLEndpoint extends SimpleGraphQLServlet {

  private static final String SCHEMA_FILENAME = "schema.graphqls";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Inject
  public GraphQLEndpoint(AlbumService albumService, ArtistService artistService) {
    super(createGraphQLSchema(albumService, artistService));
  }

  private static GraphQLSchema createGraphQLSchema(AlbumService albumService, ArtistService artistService) {

    SchemaParser schemaParser = new SchemaParser();
    InputStream schemaFile = GraphQLEndpoint.class.getClassLoader().getResourceAsStream(SCHEMA_FILENAME);
    TypeDefinitionRegistry typeRegistry = schemaParser.parse(new InputStreamReader(schemaFile));

    return new SchemaGenerator().makeExecutableSchema(typeRegistry, buildRuntimeWiring(albumService, artistService));
  }

  private static RuntimeWiring buildRuntimeWiring(AlbumService albumService, ArtistService artistService) {
    return RuntimeWiring.newRuntimeWiring()
      .type("Query", typeWiring -> typeWiring
        .dataFetcher("album", (env) -> albumService.getAlbumById(env.getArgument("id")))
        .dataFetcher("artist", (env) -> artistService.getArtistById(env.getArgument("id")))
      )
      .type("Mutation", typeWiring -> typeWiring
        .dataFetcher("createAlbum",
          (env) -> albumService.createAlbum(MAPPER.convertValue(env.getArgument("album"), AlbumDto.class)))
        .dataFetcher("updateAlbum",
          (env) -> albumService.updateAlbum(MAPPER.convertValue(env.getArgument("album"), AlbumDto.class)))
        .dataFetcher("deleteAlbum", (env) -> {
          albumService.deleteAlbumById(env.getArgument("id"));
          return true;
        })
        .dataFetcher("createArtist",
          (env) -> artistService.createArtist(MAPPER.convertValue(env.getArgument("artist"), ArtistDto.class)))
        .dataFetcher("updateArtist",
          (env) -> artistService.updateArtist(MAPPER.convertValue(env.getArgument("artist"), ArtistDto.class)))
        .dataFetcher("deleteArtist", (env) -> {
          artistService.deleteArtistById(env.getArgument("id"));
          return true;
        })
      )
      .type("Album", typeWiring -> typeWiring
        .dataFetcher("artists", (env) -> {
          AlbumDto source = env.getSource();
          if (source.getArtists() == null || source.getArtists().isEmpty()) {
            return source.getArtists();
          } else {
            return source.getArtists().stream().map(ArtistDto::getId).map(artistService::getArtistById)
              .collect(Collectors.toList());
          }
        })
      )
      .type("Artist", typeWiring -> typeWiring
        .dataFetcher("albums", (env) -> {
          ArtistDto source = env.getSource();
          if (source.getAlbums() == null || source.getAlbums().isEmpty()) {
            return source.getAlbums();
          } else {
            return source.getAlbums().stream().map(AlbumDto::getId).map(albumService::getAlbumById)
              .collect(Collectors.toList());
          }
        })
      )
      .build();
  }
}
