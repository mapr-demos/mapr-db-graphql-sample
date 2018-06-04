import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {AppConfig} from "../app.config";
import {Language} from "../models/album";

const mapToLanguage = ({
  id,
  name
}): Language => ({
  code: id,
  name
});

@Injectable()
export class LanguageService {
  constructor(
    private http: HttpClient,
    private config: AppConfig
  ) {}

  getAllLanguages(): Promise<Array<Language>> {
    return this.http.post(`${this.config.apiURL}/graphql`, {
      query: "{languages{id, name}}"
    })
      .map((response: any) => {
        console.log('Languages: ', response);
        return response.data.languages.map(mapToLanguage);
      })
      .toPromise();
  }
}
