package com.mapr.music.api.graphql.schema;

import graphql.language.IntValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.util.Date;

public class DateScalar {

  private DateScalar() {
  }

  public static final GraphQLScalarType DATE = new GraphQLScalarType("Date",
      "A custom scalar that handles dates",

      new Coercing<Date, Long>() {
        @Override
        public Long serialize(Object dataFetcherResult) throws CoercingSerializeException {
          return serializeDate(dataFetcherResult);
        }

        @Override
        public Date parseValue(Object input) throws CoercingParseValueException {
          return parseDateFromVariable(input);
        }

        @Override
        public Date parseLiteral(Object input) throws CoercingParseLiteralException {
          return parseDateFromAstLiteral(input);
        }
      });

  private static Long serializeDate(Object dataFetcherResult) {

    if (dataFetcherResult instanceof Date) {
      Date date = (Date) dataFetcherResult;
      return date.getTime();
    }

    throw new CoercingSerializeException("Unable to serialize " + dataFetcherResult + " as a date");
  }

  private static Date parseDateFromVariable(Object input) {
    if (input instanceof Long) {
      Long dateInMillis = (Long) input;
      return new Date(dateInMillis);
    }

    throw new CoercingParseValueException("Unable to parse variable value " + input + " as a date");
  }

  private static Date parseDateFromAstLiteral(Object input) {
    if (input instanceof IntValue) {
      Long dateInMillis = ((IntValue) input).getValue().longValue();
      return new Date(dateInMillis);
    }

    throw new CoercingParseLiteralException("Value is not any date: '" + input + "'");
  }
}
