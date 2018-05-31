package com.mapr.music.api.graphql.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.api.graphql.errors.GraphQLUnauthorizedError;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.ArtistService;
import graphql.schema.DataFetcher;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.inject.Inject;
import java.security.Principal;
import java.util.stream.Collectors;

public class AlbumDataFetcher {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final AlbumService albumService;
  private final ArtistService artistService;

  @Inject
  public AlbumDataFetcher(AlbumService albumService, ArtistService artistService) {
    this.albumService = albumService;
    this.artistService = artistService;
  }

  public DataFetcher album() {
    return (env) -> albumService.getAlbumById(env.getArgument("id"));
  }

  public DataFetcher albums() {
    return (env) -> {
      Integer offset = env.getArgument("offset");
      Integer limit = env.getArgument("limit");
      return albumService.getAlbums(offset, limit);
    };
  }

  public DataFetcher createAlbum() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      return albumService.createAlbum(MAPPER.convertValue(env.getArgument("album"), AlbumDto.class));
    };
  }

  public DataFetcher updateAlbum() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      return albumService.updateAlbum(MAPPER.convertValue(env.getArgument("album"), AlbumDto.class));
    };
  }

  public DataFetcher deleteAlbum() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      albumService.deleteAlbumById(env.getArgument("id"));
      return true;
    };
  }

  public DataFetcher artists() {
    return (env) -> {
      AlbumDto source = env.getSource();
      if (source.getArtists() == null || source.getArtists().isEmpty()) {
        return source.getArtists();
      } else {
        return source.getArtists().stream().map(ArtistDto::getId).map(artistService::getArtistById)
          .collect(Collectors.toList());
      }
    };
  }

  private boolean isAuthenticated() {
    return ResteasyProviderFactory.getContextData(Principal.class) != null;
  }

}
