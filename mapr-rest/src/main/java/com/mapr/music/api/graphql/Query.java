package com.mapr.music.api.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.service.AlbumService;

import java.util.List;

public class Query implements GraphQLQueryResolver {

  private final AlbumService albumService;

  public Query(AlbumService albumService) {
    this.albumService = albumService;
  }

  public AlbumDto album(String id) {
    return albumService.getAlbumById(id);
  }

}
