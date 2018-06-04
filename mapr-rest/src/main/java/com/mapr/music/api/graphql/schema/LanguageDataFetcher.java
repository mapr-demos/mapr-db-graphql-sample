package com.mapr.music.api.graphql.schema;

import com.mapr.music.dao.LanguageDao;
import graphql.schema.DataFetcher;
import javax.inject.Inject;

public class LanguageDataFetcher {

  private final LanguageDao languageDao;

  @Inject
  public LanguageDataFetcher(LanguageDao languageDao) {
    this.languageDao = languageDao;
  }

  public DataFetcher languages() {
    return (env) -> languageDao.getList();
  }

}
