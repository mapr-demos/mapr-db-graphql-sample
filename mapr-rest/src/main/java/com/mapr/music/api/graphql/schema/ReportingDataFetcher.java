package com.mapr.music.api.graphql.schema;

import com.mapr.music.service.ReportingService;
import graphql.schema.DataFetcher;
import javax.inject.Inject;

public class ReportingDataFetcher {

  private final ReportingService reportingService;

  @Inject
  public ReportingDataFetcher(ReportingService reportingService) {
    this.reportingService = reportingService;
  }

  public DataFetcher getTopArtistByArea() {
    return (env) -> reportingService.getTopArtistByArea(env.getArgument("count"));
  }

  public DataFetcher getTopLanguagesForAlbum() {
    return (env) -> reportingService.getTopLanguagesForAlbum(env.getArgument("count"));
  }

  public DataFetcher getNumberOfAlbumsPerYear() {
    return (env) -> reportingService.getNumberOfAlbumsPerYear(env.getArgument("count"));
  }

}
