package com.mapr.music.api.graphql.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapr.music.api.graphql.errors.GraphQLUnauthorizedError;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.dto.TrackDto;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.ArtistService;
import com.mapr.music.service.RateService;
import com.mapr.music.service.RecommendationService;
import com.mapr.music.service.UserService;
import graphql.schema.DataFetcher;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class AlbumDataFetcher {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final AlbumService albumService;
  private final ArtistService artistService;
  private final RecommendationService recommendationService;
  private final RateService rateService;
  private final UserService userService;

  @Inject
  public AlbumDataFetcher(
      AlbumService albumService,
      ArtistService artistService,
      RecommendationService recommendationService,
      RateService rateService,
      UserService userService) {

    this.albumService = albumService;
    this.artistService = artistService;
    this.recommendationService = recommendationService;
    this.rateService = rateService;
    this.userService = userService;
  }

  public DataFetcher album() {
    return (env) -> albumService.getAlbumById(env.getArgument("id"));
  }

  public DataFetcher albumBySlug() {
    return (env) -> albumService.getAlbumBySlugName(env.getArgument("slug"));
  }

  public DataFetcher albums() {
    return (env) -> {
      Integer offset = env.getArgument("offset");
      Integer limit = env.getArgument("limit");
      return albumService.getAlbums(offset, limit);
    };
  }

  public DataFetcher albumsRecommended() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      Principal principal = ResteasyProviderFactory.getContextData(Principal.class);
      String albumId = env.getArgument("albumId");
      Integer limit = env.getArgument("limit");

      return recommendationService.getRecommendedAlbums(albumId, principal, limit);
    };
  }

  public DataFetcher albumsByNameEntry() {
    return (env) -> {
      String nameEntry = env.getArgument("nameEntry");
      Long limit = env.getArgument("limit");
      return albumService.searchAlbums(nameEntry, limit);
    };
  }

  public DataFetcher totalAlbums() {
    return (env) -> albumService.getTotalNum();
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

  public DataFetcher deleteTrackInAlbum() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      albumService.deleteAlbumTrack(env.getArgument("albumId"), env.getArgument("trackId"));
      return albumService.getAlbumById(env.getArgument("albumId"));
    };
  }

  public DataFetcher saveAlbumTracks() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      List<TrackDto> tracks = MAPPER.convertValue(env.getArgument("tracks"), new TypeReference<List<TrackDto>>() {
      });
      albumService.setAlbumTrackList(env.getArgument("albumId"), tracks);

      return albumService.getAlbumById(env.getArgument("albumId"));
    };
  }

  public DataFetcher updateAlbumTrack() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      TrackDto track = MAPPER.convertValue(env.getArgument("track"), TrackDto.class);
      albumService.updateAlbumTrack(env.getArgument("albumId"), track.getId(), track);

      return albumService.getAlbumById(env.getArgument("albumId"));
    };
  }

  public DataFetcher addTrackToAlbum() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      TrackDto track = MAPPER.convertValue(env.getArgument("track"), TrackDto.class);
      return albumService.addTrackToAlbumTrackList(env.getArgument("albumId"), track);
    };
  }

  public DataFetcher changeAlbumRating() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      Principal principal = ResteasyProviderFactory.getContextData(Principal.class);
      String userId = userService.getUserByPrincipal(principal).getUsername();
      String albumId = env.getArgument("albumId");
      Double rating = env.getArgument("rating");

      return rateService.rateAlbum(userId, albumId, rating).getRating();
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
