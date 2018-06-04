package com.mapr.music;

import com.mapr.music.util.CORSFilter;
import com.mapr.music.util.CommonExceptionMapper;
import com.mapr.music.util.ConstraintViolationExceptionMapper;
import com.mapr.music.util.DefaultOptionsExceptionMapper;
import com.mapr.music.util.ResourceNotFoundExceptionMapper;
import com.mapr.music.util.UserPrincipalContextFilter;
import com.mapr.music.util.ValidationExceptionMapper;
import io.swagger.jaxrs.config.BeanConfig;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


@ApplicationPath("/api/1.0/")
public class MaprMusicApp extends Application {

  private Set<Object> singletons = new HashSet<>();

  public MaprMusicApp() {
    // Configure and Initialize Swagger
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1.0.0");
    beanConfig.setSchemes(new String[]{"http"});
    beanConfig.setHost("localhost:8080");
    beanConfig.setBasePath("/mapr-music-rest/api/1.0/");
    beanConfig.setResourcePackage("com.mapr.music.api");
    beanConfig.setScan(true);
  }

  @Override
  public Set<Object> getSingletons() {
    return singletons;
  }

  @Override
  public Set<Class<?>> getClasses() {

    Set<Class<?>> resources = new HashSet<>();
    resources.add(CORSFilter.class);
    resources.add(ValidationExceptionMapper.class);
    resources.add(ResourceNotFoundExceptionMapper.class);
    resources.add(ConstraintViolationExceptionMapper.class);
    resources.add(DefaultOptionsExceptionMapper.class);
    resources.add(CommonExceptionMapper.class);
    resources.add(UserPrincipalContextFilter.class);

    // Hooking up Swagger-Core
    resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
    resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    return resources;
  }

}
