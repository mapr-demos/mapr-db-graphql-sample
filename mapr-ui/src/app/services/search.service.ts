import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";
import has from "lodash/has";
import {SearchResult, SearchResultsPage} from "../models/search-result";

const SEARCHING_URL_HASH = {
  'EVERYTHING': 'name',
  'ALBUMS': 'albums/name',
  'ARTISTS': 'artists/name'
};

const mapToSearchResult = ({
                             id,
                             type,
                             name,
                             imageUrl,
                             slug
                           }): SearchResult => ({
  id,
  type,
  name,
  slug,
  imageURL: imageUrl
});

@Injectable()
export class SearchService {
  private static SERVICE_URL = 'api/1.0/search';
  private static PER_PAGE_DEFAULT = 9;

  constructor(private http: HttpClient,
              private config: AppConfig) {
  }

  getSearchURL(searchType: string, nameEntry: string, page: number): string {
    if (!has(SEARCHING_URL_HASH, searchType)) {
      throw new Error('Unknown search type');
    }
    return `${this.config.apiURL}/${SearchService.SERVICE_URL}/${SEARCHING_URL_HASH[searchType]}?entry=${nameEntry}&page=${page}&per_page=${SearchService.PER_PAGE_DEFAULT}`;
  }

  find(searchType: string, nameEntry: string, page: number): Promise<SearchResultsPage> {
    if ('EVERYTHING' === searchType) {
      return this.findEverything(nameEntry, page)
    }

    if ('ALBUMS' === searchType) {
      return this.findAlbums(nameEntry, page)
    }

    if ('ARTISTS' === searchType) {
      return this.findArtists(nameEntry, page)
    }

    throw new Error('Unknown search type');
  }

  findEverything(nameEntry: string, page: number): Promise<SearchResultsPage> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query FindByNameEntry($nameEntry: String, $page: Int, $perPage: Int) { findByNameEntry(nameEntry: $nameEntry, page: $page, perPage: $perPage){ results {id, name, slug, imageUrl, type}, pagination{items}  }}",
      variables: {
        nameEntry: nameEntry,
        page: page
      }
    })
    .map((response: any) => {
      const searchResults = response.data.findByNameEntry.results.map(mapToSearchResult);
      return {
        searchResults,
        totalNumber: response.data.findByNameEntry.pagination.items
      };
    })
    .toPromise();
  }

  findAlbums(nameEntry: string, page: number): Promise<SearchResultsPage> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query FindAlbumsByNameEntry($nameEntry: String, $page: Int, $perPage: Int) { findAlbumsByNameEntry(nameEntry: $nameEntry, page: $page, perPage: $perPage){ results {id, name, slug, imageUrl, type}, pagination{items}  }}",
      variables: {
        nameEntry: nameEntry,
        page: page
      }
    })
    .map((response: any) => {
      const searchResults = response.data.findAlbumsByNameEntry.results.map(mapToSearchResult);
      return {
        searchResults,
        totalNumber: response.data.findAlbumsByNameEntry.pagination.items
      };
    })
    .toPromise();
  }

  findArtists(nameEntry: string, page: number): Promise<SearchResultsPage> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query FindArtistsByNameEntry($nameEntry: String, $page: Int, $perPage: Int) { findArtistsByNameEntry(nameEntry: $nameEntry, page: $page, perPage: $perPage){ results {id, name, slug, imageUrl, type}, pagination{items}  }}",
      variables: {
        nameEntry: nameEntry,
        page: page
      }
    })
    .map((response: any) => {
      const searchResults = response.data.findArtistsByNameEntry.results.map(mapToSearchResult);
      return {
        searchResults,
        totalNumber: response.data.findArtistsByNameEntry.pagination.items
      };
    })
    .toPromise();
  }
}
