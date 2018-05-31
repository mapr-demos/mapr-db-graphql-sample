package com.mapr.music.api.graphql.schema;

import com.mapr.music.api.graphql.GraphQLEndpoint;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.inject.Inject;

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

            // Album
            .dataFetcher("album", albumDataFetcher.album())
            .dataFetcher("albumBySlug", albumDataFetcher.albumBySlug())
            .dataFetcher("albums", albumDataFetcher.albums())
            .dataFetcher("albumsRecommended", albumDataFetcher.albumsRecommended())
            .dataFetcher("totalAlbums", albumDataFetcher.totalAlbums())

            // Artist
            .dataFetcher("artist", artistDataFetcher.artist())
            .dataFetcher("artists", artistDataFetcher.artists())
            .dataFetcher("artistsByNameEntry", artistDataFetcher.artistsByNameEntry())
            .dataFetcher("totalArtists", artistDataFetcher.totalArtists())
        )
        .type("Mutation", typeWiring -> typeWiring

            // Album
            .dataFetcher("createAlbum", albumDataFetcher.createAlbum())
            .dataFetcher("updateAlbum", albumDataFetcher.updateAlbum())
            .dataFetcher("deleteAlbum", albumDataFetcher.deleteAlbum())
            .dataFetcher("deleteTrackInAlbum", albumDataFetcher.deleteTrackInAlbum())
            .dataFetcher("saveAlbumTracks", albumDataFetcher.saveAlbumTracks())
            .dataFetcher("updateAlbumTrack", albumDataFetcher.updateAlbumTrack())
            .dataFetcher("addTrackToAlbum", albumDataFetcher.addTrackToAlbum())
            .dataFetcher("changeAlbumRating", albumDataFetcher.changeAlbumRating())

            // Artist
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
