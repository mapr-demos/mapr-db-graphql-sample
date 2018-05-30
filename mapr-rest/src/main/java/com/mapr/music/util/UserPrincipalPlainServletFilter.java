package com.mapr.music.util;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;

/**
 * Class which has access to the SecurityContext. Uses the RESTeasy utility class to push the User Principal into the
 * context. This allows to access User Principal at other components.
 */
@WebFilter(filterName = "UserPrincipalPlainServletFilter")
public class UserPrincipalPlainServletFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    ResteasyProviderFactory.pushContext(Principal.class, request.getUserPrincipal());

    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {

  }
}
