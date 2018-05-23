package com.mapr.music.api.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.service.AlbumService;

public class Mutation implements GraphQLMutationResolver {

  private final AlbumService albumService;

  public Mutation(AlbumService albumService) {
    this.albumService = albumService;
  }

  public AlbumDto createAlbum(AlbumDto album) {
    return albumService.createAlbum(album);
  }

  public AlbumDto updateAlbum(AlbumDto album) {
    return albumService.updateAlbum(album);
  }

  public Boolean deleteAlbum(String id) {
    albumService.deleteAlbumById(id);
    return true;// FIXME
  }

}
