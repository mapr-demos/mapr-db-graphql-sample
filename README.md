# MapR-DB GraphQL Sample

## Contents

* [Overview](#overview)
* [GraphQL Schema](#graphql-schema)
* [Server side implementation](#server-side-implementation)
* [Querying GraphQL](#querying-graphql)

## Overview

This project is sample application that exposes MapR-DB JSON tables using GraphQL. 
It based on [MapR Music Catalog](https://github.com/mapr-demos/mapr-music/tree/master) application, which explains the 
key MapR-DB features, and how to use them to build a complete Web application.

### GraphQL

GraphQL is a new API standard that provides a more efficient, powerful and flexible 
alternative to REST. It was developed and open-sourced by Facebook and is now maintained by a large community of 
companies and individuals from all over the world. 

Please, follow [GraphQL tutorial](http://graphql.org/learn/) to get familiar with GraphQL specification.

## GraphQL schema

A GraphQL service is created by defining types and fields on those types, then providing functions for each field on 
each type. [MapR Music GraphQL schema](mapr-rest/src/main/webapp/WEB-INF/classes/schema.graphqls) defines `Album` and 
`Artist` types along with Queries and Mutations for that types. 

GraphQL schema must declare root `Query` and `Mutation` types:
```
schema {
  query: Query
  mutation: Mutation
}

type Query {
  album(id: String): Album
  albums(offset: Int, limit: Int): [Album]
  artist(id: String): Artist
  artists(offset: Int, limit: Int): [Artist]
  
  ...
}

type Mutation {
  createAlbum(album: AlbumInput!): Album
  updateAlbum(album: AlbumInput!): Album
  deleteAlbum(id: String!): Boolean

  createArtist(artist: ArtistInput!): Artist
  updateArtist(artist: ArtistInput!): Artist
  deleteArtist(id: String!): Boolean
  
  ...
}
```

`Query` and `Mutation` types define root fields, which can be queried by API consumers. For example, following query 
can be used to get `id`, `name` fields of Album with id `00031241-434d-4f54-b170-f64db965e1fe` along with artists' 
and tracks' names of that album:
```
{
	album(id: "00031241-434d-4f54-b170-f64db965e1fe") {
	  id, 
	  name, 
	  artists{name}, 
	  tracks{name} 
  }
}

```

Lets consider single `createAlbum` mutation, which is used to create album document:
```
createAlbum(album: AlbumInput!): Album
```

Here, `album` is field argument with type of `AlbumInput`. In QraphQL input types allows you to pass complex objects as 
arguments:
```
input AlbumInput {
  _id: String
  name: String
  style: String
  barcode: String
  status: String
  packaging: String
  language: String
  script: String
  mbid: String
  format: String
  country: String
  coverImageUrl: String
  artists: [ArtistInput]
  tracks: [TrackInput]
  released_date: Date
}

input ArtistInput {
  _id: String
  name: String
  gender: String
  rating: Float
  profileImageUrl: String
  imagesUrls: [String]
  disambiguationComment: String
  albums: [AlbumInput]
  ipi: String
  isni: String
  mbid: String
  area: String
  begin_date: Date
  end_date: Date
}
```

Also, schema contains `Album` and `Artist` types definition:
```
type Album {
  id: String!
  artists: [Artist]
  tracks: [Track]
  name: String
  style: String
  barcode: String
  coverImageUrl: String
  imagesUrls: [String]
  status: String
  packaging: String
  language: String
  script: String
  mbid: String
  format: String
  country: String
  slug: String
  rating: Float
  releasedDateDay: Date
}

type Artist {
  id: String!
  albums: [Album]
  name: String
  gender: String
  rating: Float
  profileImageUrl: String
  imagesUrls: [String]
  disambiguationComment: String
  mbid: String
  area: String
  slug: String
  ipi: String
  isni: String
  beginDateDay: Date
  endDateDay: Date
}

type Track {
  id: String!
  name: String
  length: Int
  position: Int
}
```

In line `id: String!` `id` stands by field's name and `String!` by field's type. `!` character means that field is 
required. Square brackets are used to declare lists.


### Custom scalar type

Note, that schema contains definition of custom scalar type `Date`:
```
scalar Date
```

Below you can find the explanation of how to implement such custom GraphQL scalar types.

## Server side implementation

Server side implementation is based on using `graphql-java` artifact, which is GraphQL Java implementation based on the 
specification. Also, we will use `graphql-java-tools` library and `graphql-java-servlet`, which is simple helper library 
containing a ready-made servlet for accepting GraphQL queries.

### Maven dependencies

```
  <properties>
    <graphql.version>8.0</graphql.version>
    <graphql.tools.version>5.1.0</graphql.tools.version>
    <graphql.servlet.version>5.0.0</graphql.servlet.version>
  </properties>

  ...
  <dependencies>
    
    ...
    
    <dependency>
        <groupId>com.graphql-java</groupId>
        <artifactId>graphql-java</artifactId>
        <version>${graphql.version}</version>
    </dependency>
  
    <dependency>
        <groupId>com.graphql-java</groupId>
        <artifactId>graphql-java-tools</artifactId>
        <version>${graphql.tools.version}</version>
    </dependency>
  
    <dependency>
        <groupId>com.graphql-java</groupId>
        <artifactId>graphql-java-servlet</artifactId>
        <version>${graphql.servlet.version}</version>
    </dependency>
  </dependencies>

```

### GraphQL endpoint

[GraphQLEndpoint.java](mapr-rest/src/main/java/com/mapr/music/api/graphql/GraphQLEndpoint.java) extends 
`SimpleGraphQLServlet` which is ready-made servlet for accepting GraphQL queriesis servlet, provided by 
`graphql-java-servlet`.

[GraphQLEndpoint.java :](mapr-rest/src/main/java/com/mapr/music/api/graphql/GraphQLEndpoint.java)
```
public class GraphQLEndpoint extends SimpleGraphQLServlet {

  @Inject
  public GraphQLEndpoint(GraphQLSchemaProvider schemaProvider) {
    super(schemaProvider.schema());
  }

  @Override
  protected GraphQLErrorHandler getGraphQLErrorHandler() {
    return new DefaultGraphQLErrorHandler() {

      @Override
      protected List<GraphQLError> filterGraphQLErrors(List<GraphQLError> errors) {
        return errors.stream()
            .filter(e -> e instanceof ExceptionWhileDataFetching || super.isClientError(e))
            .map(e -> e instanceof ExceptionWhileDataFetching ? new GraphQLErrorWrapper((ExceptionWhileDataFetching) e)
                : e)
            .collect(Collectors.toList());
      }
    };
  }
}
```


### GraphQL Schema Provider

GraphQL Schema Provider loads [MapR Music GraphQL schema](mapr-rest/src/main/webapp/WEB-INF/classes/schema.graphqls), 
and wires data fetchers for each GraphQL type.

[GraphQLSchemaProvider.java :](mapr-rest/src/main/java/com/mapr/music/api/graphql/schema/GraphQLSchemaProvider.java)
```
  ...
  
  private RuntimeWiring buildRuntimeWiring() {
      return RuntimeWiring.newRuntimeWiring()
          .scalar(DateScalar.DATE)
          .type("Query", typeWiring -> typeWiring
              .dataFetcher("currentUser", userDataFetcher.currentUser())
  
              // Album
              .dataFetcher("album", albumDataFetcher.album())
               
               ...
               
              .dataFetcher("getNumberOfAlbumsPerYear", reportingDataFetcher.getNumberOfAlbumsPerYear())
          )
          .type("Mutation", typeWiring -> typeWiring
  
              // Album
              .dataFetcher("createAlbum", albumDataFetcher.createAlbum())
              .dataFetcher("updateAlbum", albumDataFetcher.updateAlbum())
              .dataFetcher("deleteAlbum", albumDataFetcher.deleteAlbum())
               
               ...
               
              .dataFetcher("recomputeStatistics", statisticsDataFetcher.recomputeStatistics())
          )
          .type("Album", typeWiring -> typeWiring
              .dataFetcher("artists", albumDataFetcher.artists())
          )
          .type("Artist", typeWiring -> typeWiring
              .dataFetcher("albums", artistDataFetcher.albums())
          )
          .build();
    }
  
  ...
  
```

### GraphQL DataFetchers

Each graphql field type has a `graphql.schema.DataFetcher` associated with it. Also this type of handlers often called 
as `resolvers`. 
Package [com.mapr.music.api.graphql.schema](mapr-rest/src/main/java/com/mapr/music/api/graphql/schema/) contains data 
fetchers for all defined types.

For instance, `LanguageDataFetcher` data fetcher defines how query  on the `languages` field will be processed by 
GraphQL service:
```
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
```

### Custom scalar type

[DateScalar.java](mapr-rest/src/main/java/com/mapr/music/api/graphql/schema/DateScalar.java) class is used to define 
custom `Date` scalar type. which corresponds to the `java.util.Date` and serialized as UNIX timestamp. 

```
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
```

### Declare GraphQLEndpoint servlet mapping

To make GraphQL Endpoint available at `/graphql`, we need to declare servlet mapping in 
[web.xml](mapr-rest/src/main/webapp/WEB-INF/web.xml) :

```
    <servlet>
      <servlet-name>GraphQLEndpoint</servlet-name>
      <servlet-class>com.mapr.music.api.graphql.GraphQLEndpoint</servlet-class>
    </servlet>
    <servlet-mapping>
      <servlet-name>GraphQLEndpoint</servlet-name>
      <url-pattern>/graphql</url-pattern>
    </servlet-mapping>
```


## Querying GraphQL

GraphQL HTTP server should handle the HTTP GET and POST methods.

When receiving an HTTP GET request, the GraphQL query should be specified in the "query" query string. For example, 
if we wanted to execute the following GraphQL query:

```
{
  me {
    name
  }
}
```
This request could be sent via an HTTP GET like so:
```
http://myapi/graphql?query={me{name}}
```

A standard GraphQL POST request should use the application/json content type, and include a JSON-encoded body of the 
following form:
```
{
  "query": "...",
  "operationName": "...",
  "variables": { "myVariable": "someValue", ... }
}
```

Below you can find Album query examples.

### Get single album

Method: `POST`

URL: `http://localhost:8080/mapr-music-rest/graphql`

Request Body:
```
{
	"query":"query Album($id: String) { album(id: $id){id, name, style, barcode, status, packaging, language, script, mbid, format, country, artists{name, albums{name}}, tracks{name}} }",
	"variables": {
		"id": "00031241-434d-4f54-b170-f64db965e1fe"
	}
}
```

### Create an album

Method: `POST`

URL: `http://localhost:8080/mapr-music-rest/graphql`

Request Body:
```
{
	"query":"mutation Album($album: AlbumInput!) { createAlbum(album: $album){id, name, style, barcode, status, packaging, language, script, mbid, format, country} }",
	"variables": {
		"album": {
			"name": "new"
		}
	}
}

```

### Update an album

Method: `POST`

URL: `http://localhost:8080/mapr-music-rest/graphql`

Request Body:
```
{
	"query":"mutation Album($album: AlbumInput!) { updateAlbum(album: $album){id, name, style, barcode, status, packaging, language, script, mbid, format, country} }",
	"variables": {
		"album": {
			"_id": "e75c97bb-73ad-4bf0-886c-74af4efa0895",
			"name": "updated"
		}
	}
}

```

### Delete an album

Method: `POST`

URL: `http://localhost:8080/mapr-music-rest/graphql`

Request Body:
```
{
	"query":"mutation Album($id: String!) { deleteAlbum(id: $id) }",
	"variables": {
		"id": "812bae91-43be-43ce-a335-1e8cf8c1b177"
	}
}

```

### Album nested items

Get `id`, `name` of album with id `00031241-434d-4f54-b170-f64db965e1fe` along with `id` and `name` of it's artists. 
Note, that for each artist the list of his albums' names and list of artists' names for that album will be retrieved. 
This query demonstrates the ability to query nested objects at arbitrary nesting level.

Method: `POST`

URL: `http://localhost:8080/mapr-music-rest/graphql`

Request Body:
```
{
	"query":"query Album($id: String) { album(id: $id){id, name, artists{id, name, albums{name, artists{name}}}, tracks{name}} }",
	"variables": {
		"id": "00031241-434d-4f54-b170-f64db965e1fe"
	}
}
```
