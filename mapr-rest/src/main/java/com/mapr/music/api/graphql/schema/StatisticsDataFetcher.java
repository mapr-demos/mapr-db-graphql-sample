package com.mapr.music.api.graphql.schema;

import com.mapr.music.service.StatisticService;
import graphql.schema.DataFetcher;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsDataFetcher {

  private final Logger log = LoggerFactory.getLogger(StatisticsDataFetcher.class);
  private final StatisticService statisticService;

  @Inject
  public StatisticsDataFetcher(StatisticService statisticService) {
    this.statisticService = statisticService;
  }

  public DataFetcher recomputeStatistics() {
    return (env) -> {

      try {
        statisticService.recomputeStatistics();
        return true;
      } catch (Throwable t) {
        log.warn("Failed to recompute statistics", t);
      }

      return false;
    };
  }

}
