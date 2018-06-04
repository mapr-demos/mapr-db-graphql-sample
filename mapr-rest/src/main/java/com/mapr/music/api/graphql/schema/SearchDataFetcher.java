package com.mapr.music.api.graphql.schema;

import com.mapr.music.service.ESSearchService;
import graphql.schema.DataFetcher;
import javax.inject.Inject;

public class SearchDataFetcher {

  private final ESSearchService searchService;

  @Inject
  public SearchDataFetcher(ESSearchService searchService) {
    this.searchService = searchService;
  }

  public DataFetcher findByNameEntry() {
    return (env) -> {

      String nameEntry = env.getArgument("nameEntry");
      Integer perPage = env.getArgument("perPage");
      Integer page = env.getArgument("page");

      return searchService.findByNameEntry(nameEntry, perPage, page);
    };
  }

  public DataFetcher findAlbumsByNameEntry() {
    return (env) -> {

      String nameEntry = env.getArgument("nameEntry");
      Integer perPage = env.getArgument("perPage");
      Integer page = env.getArgument("page");

      return searchService.findAlbumsByNameEntry(nameEntry, perPage, page);
    };
  }

  public DataFetcher findArtistsByNameEntry() {
    return (env) -> {

      String nameEntry = env.getArgument("nameEntry");
      Integer perPage = env.getArgument("perPage");
      Integer page = env.getArgument("page");

      return searchService.findArtistsByNameEntry(nameEntry, perPage, page);
    };
  }

}
