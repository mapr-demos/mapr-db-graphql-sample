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
}

type Mutation {
  createAlbum(album: AlbumInput!): Album
  updateAlbum(album: AlbumInput!): Album
  deleteAlbum(id: String!): Boolean

  createArtist(artist: ArtistInput!): Artist
  updateArtist(artist: ArtistInput!): Artist
  deleteArtist(id: String!): Boolean
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
}

input ArtistInput {
  _id: String
  name: String
  gender: String
  rating: Float
  profileImageUrl: String
  imagesUrls: [String]
  disambiguationComment: String
  mbid: String
  area: String
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
`graphql-java-servlet`. It loads [MapR Music GraphQL schema](mapr-rest/src/main/webapp/WEB-INF/classes/schema.graphqls) 
and declares data fetchers for each GraphQL type.

Method, which loads GraphQL schema:
```
  private static GraphQLSchema createGraphQLSchema(AlbumService albumService, ArtistService artistService) {

    SchemaParser schemaParser = new SchemaParser();
    InputStream schemaFile = GraphQLEndpoint.class.getClassLoader().getResourceAsStream(SCHEMA_FILENAME);
    TypeDefinitionRegistry typeRegistry = schemaParser.parse(new InputStreamReader(schemaFile));

    return new SchemaGenerator().makeExecutableSchema(typeRegistry, buildRuntimeWiring(albumService, artistService));
  }
```

Declaring Data Fetchers for each GraphQL type:
```
  private static RuntimeWiring buildRuntimeWiring(AlbumService albumService, ArtistService artistService) {
    return RuntimeWiring.newRuntimeWiring()
      .type("Query", typeWiring -> typeWiring
        .dataFetcher("album", (env) -> albumService.getAlbumById(env.getArgument("id")))
        .dataFetcher("albums", (env) -> {
          Integer offset = env.getArgument("offset");
          Integer limit = env.getArgument("limit");
          return albumService.getAlbums(offset, limit);
        })
        .dataFetcher("artist", (env) -> artistService.getArtistById(env.getArgument("id")))
        .dataFetcher("artists", (env) -> {
          Integer offset = env.getArgument("offset");
          Integer limit = env.getArgument("limit");
          return artistService.getArtists(offset, limit);
        })
      )
      .type("Mutation", typeWiring -> typeWiring
        .dataFetcher("createAlbum",
          (env) -> albumService.createAlbum(MAPPER.convertValue(env.getArgument("album"), AlbumDto.class)))
        .dataFetcher("updateAlbum",
          (env) -> albumService.updateAlbum(MAPPER.convertValue(env.getArgument("album"), AlbumDto.class)))
        .dataFetcher("deleteAlbum", (env) -> {
          albumService.deleteAlbumById(env.getArgument("id"));
          return true;
        })
        .dataFetcher("createArtist",
          (env) -> artistService.createArtist(MAPPER.convertValue(env.getArgument("artist"), ArtistDto.class)))
        .dataFetcher("updateArtist",
          (env) -> artistService.updateArtist(MAPPER.convertValue(env.getArgument("artist"), ArtistDto.class)))
        .dataFetcher("deleteArtist", (env) -> {
          artistService.deleteArtistById(env.getArgument("id"));
          return true;
        })
      )
      .type("Album", typeWiring -> typeWiring
        .dataFetcher("artists", (env) -> {
          AlbumDto source = env.getSource();
          if (source.getArtists() == null || source.getArtists().isEmpty()) {
            return source.getArtists();
          } else {
            return source.getArtists().stream().map(ArtistDto::getId).map(artistService::getArtistById)
              .collect(Collectors.toList());
          }
        })
      )
      .type("Artist", typeWiring -> typeWiring
        .dataFetcher("albums", (env) -> {
          ArtistDto source = env.getSource();
          if (source.getAlbums() == null || source.getAlbums().isEmpty()) {
            return source.getAlbums();
          } else {
            return source.getAlbums().stream().map(AlbumDto::getId).map(albumService::getAlbumById)
              .collect(Collectors.toList());
          }
        })
      )
      .build();
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
