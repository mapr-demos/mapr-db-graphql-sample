package com.mapr.music.api.graphql.schema;

import com.mapr.music.api.graphql.errors.GraphQLUnauthorizedError;
import com.mapr.music.service.UserService;
import graphql.schema.DataFetcher;
import java.security.Principal;
import javax.inject.Inject;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class UserDataFetcher {

  private final UserService userService;

  @Inject
  public UserDataFetcher(UserService userService) {
    this.userService = userService;
  }

  public DataFetcher currentUser() {
    return (env) -> {

      if (!isAuthenticated()) {
        throw new GraphQLUnauthorizedError();
      }

      Principal principal = ResteasyProviderFactory.getContextData(Principal.class);
      return userService.getUserByPrincipal(principal);
    };
  }

  private boolean isAuthenticated() {
    return ResteasyProviderFactory.getContextData(Principal.class) != null;
  }

}
