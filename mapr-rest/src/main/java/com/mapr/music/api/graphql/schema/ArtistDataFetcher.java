package com.mapr.music.api.graphql.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.api.graphql.errors.GraphQLUnauthorizedError;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.ArtistService;
import graphql.schema.DataFetcher;
import java.security.Principal;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class ArtistDataFetcher {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final ArtistService artistService;
  private final AlbumService albumService;

  @Inject
  public ArtistDataFetcher(ArtistService artistService, AlbumService albumService) {
    this.artistService = artistService;
    this.albumService = albumService;
  }

  public DataFetcher artist() {
    return (env) -> artistService.getArtistById(env.getArgument("id"));
  }

  public DataFetcher artists() {
    return (env) -> {
      Integer offset = env.getArgument("offset");
      Integer limit = env.getArgument("limit");
      return artistService.getArtists(offset, limit);
    };
  }

  public DataFetcher artistsByNameEntry() {
    return (env) -> {
      String nameEntry = env.getArgument("nameEntry");
      Long limit = env.getArgument("limit");
      return artistService.searchArtists(nameEntry, limit);
    };
  }

  public DataFetcher totalArtists() {
    return (env) -> artistService.getTotalNum();
  }

  public DataFetcher createArtist() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      return artistService.createArtist(MAPPER.convertValue(env.getArgument("artist"), ArtistDto.class));
    };
  }

  public DataFetcher updateArtist() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      return artistService.updateArtist(MAPPER.convertValue(env.getArgument("artist"), ArtistDto.class));
    };
  }

  public DataFetcher deleteArtist() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      artistService.deleteArtistById(env.getArgument("id"));
      return true;
    };
  }

  public DataFetcher albums() {
    return (env) -> {
      ArtistDto source = env.getSource();
      if (source.getAlbums() == null || source.getAlbums().isEmpty()) {
        return source.getAlbums();
      } else {
        return source.getAlbums().stream().map(AlbumDto::getId).map(albumService::getAlbumById)
            .collect(Collectors.toList());
      }
    };
  }

  private boolean isAuthenticated() {
    return ResteasyProviderFactory.getContextData(Principal.class) != null;
  }

}
