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
  private final LanguageDataFetcher languageDataFetcher;
  private final SearchDataFetcher searchDataFetcher;
  private final StatisticsDataFetcher statisticsDataFetcher;
  private final ReportingDataFetcher reportingDataFetcher;

  @Inject
  public GraphQLSchemaProvider(AlbumDataFetcher albumDataFetcher,
      ArtistDataFetcher artistDataFetcher,
      UserDataFetcher userDataFetcher,
      LanguageDataFetcher languageDataFetcher,
      SearchDataFetcher searchDataFetcher,
      StatisticsDataFetcher statisticsDataFetcher,
      ReportingDataFetcher reportingDataFetcher) {

    this.albumDataFetcher = albumDataFetcher;
    this.artistDataFetcher = artistDataFetcher;
    this.userDataFetcher = userDataFetcher;
    this.languageDataFetcher = languageDataFetcher;
    this.searchDataFetcher = searchDataFetcher;
    this.statisticsDataFetcher = statisticsDataFetcher;
    this.reportingDataFetcher = reportingDataFetcher;
  }

  public GraphQLSchema schema() {

    SchemaParser schemaParser = new SchemaParser();
    InputStream schemaFile = GraphQLEndpoint.class.getClassLoader().getResourceAsStream(SCHEMA_FILENAME);
    TypeDefinitionRegistry typeRegistry = schemaParser.parse(new InputStreamReader(schemaFile));

    return new SchemaGenerator().makeExecutableSchema(typeRegistry, buildRuntimeWiring());
  }

  private RuntimeWiring buildRuntimeWiring() {
    return RuntimeWiring.newRuntimeWiring()
        .scalar(DateScalar.DATE)
        .type("Query", typeWiring -> typeWiring
            .dataFetcher("currentUser", userDataFetcher.currentUser())

            // Album
            .dataFetcher("album", albumDataFetcher.album())
            .dataFetcher("albumBySlug", albumDataFetcher.albumBySlug())
            .dataFetcher("albums", albumDataFetcher.albums())
            .dataFetcher("albumsPage", albumDataFetcher.albumsPage())
            .dataFetcher("albumsRecommended", albumDataFetcher.albumsRecommended())
            .dataFetcher("albumsByNameEntry", albumDataFetcher.albumsByNameEntry())
            .dataFetcher("totalAlbums", albumDataFetcher.totalAlbums())

            // Artist
            .dataFetcher("artist", artistDataFetcher.artist())
            .dataFetcher("artistBySlug", artistDataFetcher.artistBySlug())
            .dataFetcher("artists", artistDataFetcher.artists())
            .dataFetcher("artistsByNameEntry", artistDataFetcher.artistsByNameEntry())
            .dataFetcher("artistsRecommended", artistDataFetcher.artistsRecommended())
            .dataFetcher("totalArtists", artistDataFetcher.totalArtists())

            .dataFetcher("languages", languageDataFetcher.languages())

            // Search
            .dataFetcher("findByNameEntry", searchDataFetcher.findByNameEntry())
            .dataFetcher("findArtistsByNameEntry", searchDataFetcher.findArtistsByNameEntry())
            .dataFetcher("findAlbumsByNameEntry", searchDataFetcher.findAlbumsByNameEntry())

            // reporting
            .dataFetcher("getTopArtistByArea", reportingDataFetcher.getTopArtistByArea())
            .dataFetcher("getTopLanguagesForAlbum", reportingDataFetcher.getTopLanguagesForAlbum())
            .dataFetcher("getNumberOfAlbumsPerYear", reportingDataFetcher.getNumberOfAlbumsPerYear())
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
            .dataFetcher("changeArtistRating", artistDataFetcher.changeArtistRating())

            .dataFetcher("recomputeStatistics", statisticsDataFetcher.recomputeStatistics())
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
