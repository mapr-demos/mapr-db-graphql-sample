import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";
import has from 'lodash/has';

const REPORT_URL_HASH = {
  'TOP_5': 'albums/top-5-languages',
  'LAST_10_YEAR': 'albums/per-year-last-10',
  'TOP_10_AREA': 'artists/top-10-area'
};

@Injectable()
export class ReportingService {
  private static SERVICE_URL = 'api/1.0/reporting';

  constructor(private http: HttpClient,
              private config: AppConfig) {
  }

  getReportsURL(reportType: string): string {
    if (!has(REPORT_URL_HASH, reportType)) {
      throw new Error('Unknown report type');
    }
    return `${this.config.apiURL}/${ReportingService.SERVICE_URL}/${REPORT_URL_HASH[reportType]}`;
  }

  getReports(reportType: string): Promise<Array<{ key: string, value: string }>> {

    if ('TOP_5' === reportType) {
      return this.getTopLanguages(5)
    }
    if ('LAST_10_YEAR' === reportType) {
      return this.getNumberOfAlbumsPerYear(10)
    }
    if ('TOP_10_AREA' === reportType) {
      return this.getTopArtistByArea(10)
    }

    throw new Error('Unknown report type');
  }

  getTopLanguages(count: number): Promise<Array<{ key: string, value: string }>> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query GetTopLanguages($count: Int) { getTopLanguagesForAlbum(count: $count){key, value} }",
      variables: {
        count: count
      }
    })
    .map((response: any) => {
      return response.data.getTopLanguagesForAlbum as Array<{ key: string, value: string }>;
    })
    .toPromise();
  }

  getTopArtistByArea(count: number): Promise<Array<{ key: string, value: string }>> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query ArtistByArea($count: Int) { getTopArtistByArea(count: $count){key, value} }",
      variables: {
        count: count
      }
    })
    .map((response: any) => {
      return response.data.getTopArtistByArea as Array<{ key: string, value: string }>;
    })
    .toPromise();
  }

  getNumberOfAlbumsPerYear(count: number): Promise<Array<{ key: string, value: string }>> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "query AlbumsPerYear($count: Int) { getNumberOfAlbumsPerYear(count: $count){key, value} }",
      variables: {
        count: count
      }
    })
    .map((response: any) => {
      return response.data.getNumberOfAlbumsPerYear as Array<{ key: string, value: string }>;
    })
    .toPromise();
  }
}
