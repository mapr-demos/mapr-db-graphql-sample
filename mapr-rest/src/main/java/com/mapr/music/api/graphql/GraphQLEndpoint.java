package com.mapr.music.api.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.ArtistService;
import com.mapr.music.service.UserService;
import graphql.GraphQLException;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.servlet.SimpleGraphQLServlet;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.stream.Collectors;

public class GraphQLEndpoint extends SimpleGraphQLServlet {

  private static final String SCHEMA_FILENAME = "schema.graphqls";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Inject
  public GraphQLEndpoint(AlbumService albumService, ArtistService artistService, UserService userService) {
    super(createGraphQLSchema(albumService, artistService, userService));
  }

  private static GraphQLSchema createGraphQLSchema(AlbumService albumService, ArtistService artistService, UserService userService) {

    SchemaParser schemaParser = new SchemaParser();
    InputStream schemaFile = GraphQLEndpoint.class.getClassLoader().getResourceAsStream(SCHEMA_FILENAME);
    TypeDefinitionRegistry typeRegistry = schemaParser.parse(new InputStreamReader(schemaFile));

    return new SchemaGenerator().makeExecutableSchema(typeRegistry, buildRuntimeWiring(albumService, artistService, userService));
  }

  private static RuntimeWiring buildRuntimeWiring(AlbumService albumService, ArtistService artistService, UserService userService) {
    return RuntimeWiring.newRuntimeWiring()
      .type("Query", typeWiring -> typeWiring
        .dataFetcher("currentUser", (env) -> {
          Principal principal = ResteasyProviderFactory.getContextData(Principal.class);
          if (principal == null) {
            throw new GraphQLException("Unauthorized");
          }
          return userService.getUserByPrincipal(principal);
        })
        .dataFetcher("album", (env) -> albumService.getAlbumById(env.getArgument("id")))
        .dataFetcher("albums", (env) -> {
          Integer offset = env.getArgument("offset");
          Integer limit = env.getArgument("limit");
          return albumService.getAlbums(offset, limit);
        })
        .dataFetcher("artist", (env) -> artistService.getArtistById(env.getArgument("id")))
        .dataFetcher("artists", (env) -> {
          Integer offset = env.getArgument("offset");
          Integer limit = env.getArgument("limit");
          return artistService.getArtists(offset, limit);
        })
      )
      .type("Mutation", typeWiring -> typeWiring
        .dataFetcher("createAlbum", (env) -> {
          assertAuthorized();
          return albumService.createAlbum(MAPPER.convertValue(env.getArgument("album"), AlbumDto.class));
        })
        .dataFetcher("updateAlbum", (env) -> {
          assertAuthorized();
          return albumService.updateAlbum(MAPPER.convertValue(env.getArgument("album"), AlbumDto.class));
        })
        .dataFetcher("deleteAlbum", (env) -> {
          assertAuthorized();
          albumService.deleteAlbumById(env.getArgument("id"));
          return true;
        })
        .dataFetcher("createArtist", (env) -> {
          assertAuthorized();
          return artistService.createArtist(MAPPER.convertValue(env.getArgument("artist"), ArtistDto.class));
        })
        .dataFetcher("updateArtist", (env) -> {
          assertAuthorized();
          return artistService.updateArtist(MAPPER.convertValue(env.getArgument("artist"), ArtistDto.class));
        })
        .dataFetcher("deleteArtist", (env) -> {
          assertAuthorized();
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

  private static void assertAuthorized() {
    Principal principal = ResteasyProviderFactory.getContextData(Principal.class);
    if (principal == null) {
      throw new GraphQLException("Unauthorized");
    }
  }
}
