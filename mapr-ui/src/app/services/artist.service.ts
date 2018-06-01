import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Album, Artist, ArtistsPage} from "../models/artist";
import "rxjs/add/operator/map";
import "rxjs/add/operator/toPromise";
import "rxjs/add/operator/mergeMap";
import {AppConfig} from "../app.config";
import {Observable} from "rxjs/Observable";

const PAGE_SIZE = 12;

function mapToArtist({id, name, profileImageUrl, gender, slug, area, disambiguation_comment, begin_date, end_date, ipi, isni, rating}): Artist {
  return {
    id,
    name,
    gender,
    avatarURL: profileImageUrl,
    slug,
    area,
    disambiguationComment: disambiguation_comment,
    beginDate: (begin_date) ? new Date(begin_date).toDateString() : null,
    endDate: (end_date) ? new Date(end_date).toDateString() : null,
    ipi,
    isni,
    rating,
    albums: []
  }
}

function mapToAlbum({id, name, coverImageUrl, slug}): Album {
  return {
    id,
    title: name,
    slug,
    coverImageURL: coverImageUrl
  };
}

const mapToAlbumRequest = ({
                             id,
                             title,
                             coverImageURL,
                             slug
                           }: Album) => ({
  _id: id,
  name: title,
  // slug,
  coverImageUrl: coverImageURL
});

const mapToArtistRequest = ({
                              id,
                              name,
                              avatarURL,
                              gender,
                              area,
                              beginDate,
                              endDate,
                              slug,
                              disambiguationComment,
                              ipi,
                              isni,
                              albums
                            }: Artist) => ({
  _id: id,
  name: name,
  profileImageUrl: avatarURL,
  area,
  // begin_date: (beginDate) ? Date.parse(beginDate) : null,
  // end_date: (endDate) ? Date.parse(endDate) : null,
  // slug,
  gender,
  disambiguationComment,
  ipi,
  isni,
  albums: albums.map(mapToAlbumRequest)
});

@Injectable()
export class ArtistService {

  private static SERVICE_URL = '/api/1.0/artists';

  constructor(private http: HttpClient,
              private config: AppConfig) {
  }

  getArtistByIdURL(artistId: string): string {
    return `${this.config.apiURL}${ArtistService.SERVICE_URL}/${artistId}`;
  }

  // TODO
  getArtistById(artistId: string): Promise<Artist> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query Artist($id: String) { artist(id: $id){id, name, gender, ipi, isni, area, profileImageUrl, slug, rating, albums {id, name, coverImageUrl} }}",
      variables: {
        id: artistId
      }
    })
    .map((response: any) => {
      console.log('Artist: ', response);
      const artist = mapToArtist(response.data.artist);
      artist.albums = response.data.artist.albums
        ? response.data.artist.albums.map(mapToAlbum)
        : [];
      return artist;
    })
    .toPromise();
  }

  getArtistPageURL(pageNum: number): string {
    return `${this.config.apiURL}${ArtistService.SERVICE_URL}?page=${pageNum}&per_page=${PAGE_SIZE}`;
  }

  /**
   * @desc get albums page from server side
   * */
  getArtistPage(pageNum: number): Promise<ArtistsPage> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query Artists($offset: Int, $limit: Int) { artists(offset: $offset, limit: $limit){name, profileImageUrl, slug }, totalArtists }",
      variables: {
        offset: (pageNum - 1) * PAGE_SIZE,
        limit: PAGE_SIZE
      }
    })
    .map((response: any) => {
      const artists = response.data.artists.map(mapToArtist);
      return {
        artists,
        totalNumber: response.data.totalArtists
      };
    })
    .toPromise();
  }

  /**
   * @desc get album by slug URL
   * */
  getArtistBySlugURL(artistSlug: string): string {
    return `${this.config.apiURL}${ArtistService.SERVICE_URL}/slug/${artistSlug}`;
  }

  /**
   * @desc get album by slug from server side
   * */
  getArtistBySlug(artistSlug: string): Promise<Artist> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query ArtistBySlug($slug: String) { artistBySlug(slug: $slug){id, name, gender, ipi, isni, area, profileImageUrl, slug, rating, albums {id, name, coverImageUrl} }}",
      variables: {
        slug: artistSlug
      }
    })
    .map((response: any) => {
      console.log('Artist: ', response);
      const artist = mapToArtist(response.data.artistBySlug);
      artist.albums = response.data.artistBySlug.albums
        ? response.data.artistBySlug.albums.map(mapToAlbum)
        : [];
      return artist;
    })
    .toPromise();
  }

  deleteArtist(artist: Artist): Promise<void> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "mutation Artist($id: String!) { deleteArtist(id: $id) }",
      variables: {
        id: artist.id
      }
    })
    .map(() => {
    })
    .toPromise()
  }

  searchForAlbums(query: string): Observable<Array<Album>> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "query AlbumsByNameEntry($nameEntry: String, $limit: Long) { albumsByNameEntry(nameEntry: $nameEntry, limit: $limit){id, name} }",
      variables: {
        nameEntry: query,
        limit: 5
      }
    })
    .map((response: any) => {
      console.log('Search response: ', response);
      return response.data.albumsByNameEntry.map(mapToAlbum);
    });
  }

  createNewArtist(artist: Artist): Promise<Artist> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "mutation Artist($artist: ArtistInput!) { createArtist(artist: $artist){id, name, profileImageUrl, slug, rating, albums {id, name, coverImageUrl} }}",
      variables: {
        artist: mapToArtistRequest(artist)
      }
    })
    .map((response: any) => {
      console.log('Creation response: ', response);
      return mapToArtist(response.data.createArtist);
    })
    .toPromise()
  }

  updateArtist(artist: Artist): Promise<Artist> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "mutation UpdateArtist($artist: ArtistInput!) { updateArtist(artist: $artist){id, name, profileImageUrl, slug, rating, albums {id, name, coverImageUrl} }}",
      variables: {
        artist: mapToArtistRequest(artist)
      }
    })
    .map((response: any) => {
      console.log('Updated response: ', response);
      return mapToArtist(response.data.updateArtist);
    })
    .toPromise();
  }

  getRecommendedForArtist(artist: Artist): Observable<Array<Artist>> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "query ArtistsRecommended($artistId: String, $limit: Int) { artistsRecommended(artistId: $artistId, limit: $limit){id, name, slug, profileImageUrl} }",
      variables: {
        artistId: artist.id,
        limit: 4
      }
    })
    .map((response: any) => {
      console.log('Search response: ', response);
      return response.data.artistsRecommended.map(mapToArtist);
    });
  }

  changeRating(artist: Artist, rating: number): Promise<any> {
    return this.http
    .post(`${this.config.apiURL}/graphql`, {
      query: "mutation ChangeArtistRating($artistId: String!, $rating: Float!) { changeArtistRating(artistId: $artistId, rating: $rating) }",
      variables: {
        artistId: artist.id,
        rating: rating
      }
    })
    .map((response: any) => {
      return response.data.changeArtistRating;
    })
    .toPromise();
  }
}
