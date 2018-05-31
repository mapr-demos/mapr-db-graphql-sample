import {Injectable} from "@angular/core";
import {Album, AlbumsPage, Artist, Track} from "../models/album";
import identity from "lodash/identity";
import {HttpClient} from "@angular/common/http";
import "rxjs/add/operator/toPromise";
import "rxjs/add/operator/map";
import {AppConfig} from "../app.config";
import {Observable} from "rxjs";
import {LanguageService} from "./language.service";
import find from "lodash/find";

const PAGE_SIZE = 12;

export const SORT_OPTIONS = [
  {
    label: 'No sorting',
    value: 'NO_SORTING'
  },
  {
    label: 'Title A-z',
    value: 'TITLE_ASC'
  },
  {
    label: 'Title z-A',
    value: 'TITLE_DESC'
  },
  {
    label: 'Newest first',
    value: 'RELEASE_DESC'
  },
  {
    label: 'Oldest first',
    value: 'RELEASE_ASC'
  },
  {
    label: 'Newest First, Title A-z',
    value: 'RELEASE_DESC_TITLE_ASC'
  },
  {
    label: 'Newest First, Title z-A',
    value: 'RELEASE_DESC_TITLE_DESC'
  },
  {
    label: 'Oldest first, Title A-z',
    value: 'RELEASE_ASC_TITLE_ASC'
  },
  {
    label: 'Oldest first, Title z-A',
    value: 'RELEASE_ASC_TITLE_DESC'
  }
];

const SORT_HASH = {
  'NO_SORTING': identity,
  'RELEASE_DESC': (url) => `${url}&sort=desc,released_date`,
  'RELEASE_ASC': (url) => `${url}&sort=asc,released_date`,
  'TITLE_ASC': (url) => `${url}&sort=asc,name`,
  'TITLE_DESC': (url) => `${url}&sort=desc,name`,
  'RELEASE_DESC_TITLE_ASC': (url) => SORT_HASH.TITLE_ASC(SORT_HASH.RELEASE_DESC(url)),
  'RELEASE_DESC_TITLE_DESC': (url) => SORT_HASH.TITLE_DESC(SORT_HASH.RELEASE_DESC(url)),
  'RELEASE_ASC_TITLE_ASC': (url) => SORT_HASH.TITLE_ASC(SORT_HASH.RELEASE_ASC(url)),
  'RELEASE_ASC_TITLE_DESC': (url) => SORT_HASH.TITLE_DESC(SORT_HASH.RELEASE_ASC(url))
};

interface PageRequest {
  pageNumber: number,
  sortType: string,
  lang: string
}

const mapToArtist = ({
                       id,
                       name,
                       slug,
                       profileImageUrl
                     }): Artist => ({
  id: id,
  name,
  slug,
  avatarURL: profileImageUrl
});

const mapToTrack = ({
                      id,
                      name,
                      length,
                      position
                    }): Track => ({
  id,
  duration: length ? `${length}` + '' : '0',
  name,
  position
});

const mapToAlbum = ({
                      id,
                      name,
                      coverImageUrl,
                      country,
                      artists,
                      format,
                      tracks,
                      slug,
                      //this property is injected on ui
                      // TODO add to document
                      language,
                      rating,
                      released_date
                    }): Album => ({
  id,
  title: name,
  coverImageURL: coverImageUrl,
  country,
  format,
  slug,
  rating,
  releasedDate: (released_date) ? new Date(released_date) : null,
  language,
  trackList: tracks
    ? tracks.map(mapToTrack)
    : [],
  artists: artists
    ? artists.map(mapToArtist)
    : []
});

const mapToTrackRequest = ({
                             id,
                             name,
                             duration,
                             position
                           }: Track) => ({
  _id: id,
  length: (duration) ? Number(duration) : null,
  name,
  position: (position) ? Number(position) : null
});

const mapToArtistRequest = ({
                              id,
                              name,
                              slug,
                              avatarURL
                            }: Artist) => ({
  _id: id,
  name,
  // slug,
  profileImageUrl: avatarURL
});

const mapToAlbumRequest = ({
                             id,
                             title,
                             coverImageURL,
                             country,
                             format,
                             slug,
                             trackList,
                             artists,
                             language,
                             releasedDate
                           }: Album) => ({
  _id: id,
  name: title,
  coverImageUrl: coverImageURL,
  country,
  format,
  // released_date: (releasedDate) ? releasedDate.getTime() : null,
  language: (language) ? language.code : null,
  artists: artists.map(mapToArtistRequest),
  tracks: trackList.map(mapToTrackRequest)
});

@Injectable()
export class AlbumService {

  private static SERVICE_URL = '/api/1.0/albums';

  constructor(private http: HttpClient,
              private config: AppConfig,
              private languageService: LanguageService) {
  }

  /**
   * @desc returns URL for albums page request
   * */
  getAlbumsPageURL({pageNumber, sortType, lang}: PageRequest): string {
    let url = `${this.config.apiURL}${AlbumService.SERVICE_URL}?page=${pageNumber}&per_page=${PAGE_SIZE}`;
    if (lang !== null) {
      url += `&language=${lang}`;
    }
    return SORT_HASH[sortType](url);
  }

  /**
   * @desc get albums page from server side
   * */
  getAlbumsPage(request: PageRequest): Promise<AlbumsPage> {

    console.log(request);

    return this.http.post(`${this.config.apiURL}/graphql`,
      {
        query: "query Albums($offset: Int, $limit: Int) { albums(offset: $offset, limit: $limit){name, artists{id, name, slug}, coverImageUrl, language, slug }, totalAlbums }",
        variables: {
          offset: (request.pageNumber - 1) * PAGE_SIZE,
          limit: PAGE_SIZE
        }
      }
    )
    .flatMap((response: any) => {
      return this.languageService.getAllLanguages().then((languages) => ({languages, response}))
    })
    .map(({response, languages}) => {
      console.log('Albums: ', response);
      const albums = response.data.albums
      .map((album) => {
        album.language = find(languages, (language) => language.code === album.language);
        return album;
      })
      .map(mapToAlbum);

      return {
        albums,
        totalNumber: response.data.totalAlbums
      };
    })
    .toPromise();
  }

  /**
   * @desc get album by slug URL
   * */
  getAlbumBySlugURL(albumSlug: string): string {
    return `${this.config.apiURL}${AlbumService.SERVICE_URL}/slug/${albumSlug}`;
  }

  /**
   * @desc get album by slug from server side
   * */
  getAlbumBySlug(albumSlug: string): Promise<Album> {
    return this.http.post(`${this.config.apiURL}/graphql`,
      {
        query: "query AlbumBySlug($slug: String) { albumBySlug(slug: $slug){id, slug, name, rating, style, coverImageUrl, barcode, status, packaging, language, script, mbid, format, country, artists{id, name}, tracks{id, name, length, position}} }",
        variables: {
          slug: albumSlug
        }
      }
    )
    .flatMap((response: any) => {
      return this.languageService.getAllLanguages().then((languages) => ({languages, response}))
    })
    .map(({response, languages}) => {
      console.log('Album: ', response.data.albumBySlug);
      response.data.albumBySlug.language = find(languages, (language) => language.code === response.data.albumBySlug.language);
      return mapToAlbum(response.data.albumBySlug);
    })
    .toPromise();
  }

  deleteTrackInAlbum(albumId: string, trackId: string): Promise<Object> {
    return this.http.post(`${this.config.apiURL}/graphql`,
      {
        query: "mutation DeleteAlbumTrack($albumId: String, $trackId: String!) { deleteTrackInAlbum(albumId: $albumId, trackId: $trackId){id, slug, name, rating, style, coverImageUrl, barcode, status, packaging, language, script, mbid, format, country, artists{id, name}, tracks{id, name, length, position}} }",
        variables: {
          albumId: albumId,
          trackId: trackId
        }
      })
    .toPromise()
  }

  saveAlbumTracks(albumId: string, tracks: Array<Track>): Promise<Object> {
    return this.http.post(`${this.config.apiURL}/graphql`,
      {
        query: "mutation SaveAlbumTracks($albumId: String, $tracks: [TrackInput!]!) { saveAlbumTracks(albumId: $albumId, tracks: $tracks){id, slug, name, rating, style, coverImageUrl, barcode, status, packaging, language, script, mbid, format, country, artists{id, name}, tracks{id, name, length, position}} }",
        variables: {
          albumId: albumId,
          tracks: tracks.map(mapToTrackRequest)
        }
      })
    .toPromise()
  }

  updateAlbumTrack(albumId: string, track: Track): Promise<Object> {
    return this.http.post(`${this.config.apiURL}/graphql`,
      {
        query: "mutation UpdateAlbumTrack($albumId: String, $track: TrackInput!) { updateAlbumTrack(albumId: $albumId, track: $track){id, slug, name, rating, style, coverImageUrl, barcode, status, packaging, language, script, mbid, format, country, artists{id, name}, tracks{id, name, length, position}} }",
        variables: {
          albumId: albumId,
          track: mapToTrackRequest(track)
        }
      })
    .toPromise();
  }

  addTrackToAlbum(albumId: string, track: Track): Promise<Track> {
    const request = mapToTrackRequest(track);
    return this.http.post(`${this.config.apiURL}/graphql`,
      {
        query: "mutation AddTrackToAlbum($albumId: String, $track: TrackInput!) { addTrackToAlbum(albumId: $albumId, track: $track){id, name, length, position} }",
        variables: {
          albumId: albumId,
          track: request
        }
      }
    )
    .map((response: any) => {
      return mapToTrack(response.data.addTrackToAlbum);
    })
    .toPromise();
  }

  searchForArtists(query: string): Observable<Array<Artist>> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "query ArtistsByNameEntry($nameEntry: String, $limit: Long) { artistsByNameEntry(nameEntry: $nameEntry, limit: $limit){id, name} }",
      variables: {
        nameEntry: query,
        limit: 5
      }
    })
    .map((response: any) => {
      console.log('Search response: ', response);
      return response.data.artistsByNameEntry.map(mapToArtist);
    });
  }

  createNewAlbum(album: Album): Promise<Album> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "mutation Album($album: AlbumInput!) { createAlbum(album: $album){id, slug, name, rating, style, coverImageUrl, barcode, status, packaging, language, script, mbid, format, country, artists{id, name}, tracks{id, name, length, position}} }",
      variables: {
        album: mapToAlbumRequest(album)
      }
    })
    .map((response: any) => {
      console.log('Creation response: ', response);
      return mapToAlbum(response.data.createAlbum);
    })
    .toPromise()
  }

  updateAlbum(album: Album): Promise<Album> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "mutation Album($album: AlbumInput!) { updateAlbum(album: $album){id, slug, name, rating, style, coverImageUrl, barcode, status, packaging, language, script, mbid, format, country, artists{id, name}, tracks{id, name, length, position}} }",
      variables: {
        // _id: album.id,
        album: mapToAlbumRequest(album)
      }
    })
    .map((response: any) => {
      console.log('Updated response: ', response);
      return mapToAlbum(response.data.updateAlbum);
    })
    .toPromise();
  }

  deleteAlbum(album: Album): Promise<void> {
    return this.http.post(`${this.config.apiURL}/graphql`,
      {
        query: "mutation Album($id: String!) { deleteAlbum(id: $id) }",
        variables: {
          id: album.id
        }
      })
    .map(() => {
    })
    .toPromise()
  }

  getRecommendedForAlbum(album: Album): Observable<Array<Album>> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "query AlbumsRecommended($albumId: String, $limit: Int) { albumsRecommended(albumId: $albumId, limit: $limit){id, name, slug, coverImageUrl} }",
      variables: {
        albumId: album.id,
        limit: 4
      }
    })
    .map((response: any) => {
      console.log('Search response: ', response);
      return response.data.albumsRecommended.map(mapToAlbum);
    });
  }

  changeRating(album: Album, rating: number): Promise<any> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "mutation ChangeRating($albumId: String!, $rating: Float!) { changeAlbumRating(albumId: $albumId, rating: $rating) }",
      variables: {
        albumId: album.id,
        rating: rating
      }
    })
    .map((response: any) => {
      return response.data.changeAlbumRating;
    })
    .toPromise();
  }
}
