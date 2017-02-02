package fll.web;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Formatter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import fll.db.Queries;
import fll.util.LogUtils;

/**
 * Servlet Filter implementation class NavbarFilter
 */
@WebFilter(description = "Add a navbar to the desired pages", urlPatterns = { "/*" })
public class NavbarFilter implements Filter {

  private static final Logger LOGGER = LogUtils.getLogger();

  /**
   * Default constructor.
   */
  public NavbarFilter() {
  }

  /**
   * @see Filter#destroy()
   */
  public void destroy() {
  }

  /**
   * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
   */
  public void doFilter(ServletRequest request,
                       ServletResponse response,
                       FilterChain chain)
      throws IOException, ServletException {
    if (response instanceof HttpServletResponse && request instanceof HttpServletRequest) {
      final HttpServletResponse httpResponse = (HttpServletResponse) response;
      final HttpServletRequest httpRequest = (HttpServletRequest) request;
      final ByteResponseWrapper wrapper = new ByteResponseWrapper(httpResponse);
      chain.doFilter(request, wrapper);

      final String path = httpRequest.getRequestURI();

      final String contentType = wrapper.getContentType();
      if (wrapper.isStringUsed()) {
        if (null != contentType && contentType.startsWith("text/html")) {
          final String url = httpRequest.getRequestURI();

          final String origStr = wrapper.getString();
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(new Formatter().format("html page: %s content type: %s ###%s###", httpRequest.getRequestURL(),
                                                contentType, origStr));
          }

          final PrintWriter writer = response.getWriter();

          final CharArrayWriter caw = new CharArrayWriter();
          final int headIndex = origStr.indexOf("<body>");

          if (-1 != headIndex && !noNavbar(url)) {
            caw.write(origStr.substring(0, headIndex + 6));
            addNavbar(caw, httpRequest);
            caw.write(origStr.substring(headIndex + 6));
            response.setContentLength(caw.toString().length());
            writer.print(caw.toString());
          } else {
            writer.print(origStr);
          }
          writer.close();
        } else {
          final String origStr = wrapper.getString();
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(new Formatter().format("non-html text page: %s content type: %s ###%s###",
                                                httpRequest.getRequestURL(), contentType, origStr));
          }

          final PrintWriter writer = response.getWriter();
          writer.print(origStr);
          writer.close();
        }
      } else if (wrapper.isBinaryUsed()) {
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace(new Formatter().format("binary page: %s content type: %s", httpRequest.getRequestURL(),
                                              contentType));
        }

        final byte[] origData = wrapper.getBinary();
        final ServletOutputStream out = response.getOutputStream();
        out.write(origData);
        out.close();
      } else {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("No output stream used, just returning page: " + httpRequest.getRequestURL());
        }
      }
    } else {
      chain.doFilter(request, response);
    }
  }

  /**
   * @param url
   * @return
   */
  private boolean noNavbar(String url) {
    if (url.endsWith("welcome.jsp")) {
      return true;
    } else if (url.indexOf("scoreboard") != -1
        && !url.endsWith("index.jsp")) {
      return true;
    } else if (url.indexOf("playoff/remoteMain.jsp") != -1) {
      return true;
    } else if (url.indexOf("playoff/title.jsp") != -1) {
      return true;
    } else if (url.indexOf("playoff/remoteControlBrackets.jsp") != -1) {
      return true;
    } else if (url.indexOf("playoff/sponsors.jsp") != -1) {
      return true;
    } else if (url.indexOf("report/finalist/FinalistTeams.jsp") != -1) {
      return true;
    } else if (url.indexOf("report/finalist/PublicFinalistDisplaySchedule.jsp") != -1) {
      return true;
    } else if(url.indexOf("slideshow/index.jsp") != -1) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * @param caw
   * @param httpRequest
   */
  private void addNavbar(CharArrayWriter caw,
                         HttpServletRequest httpRequest)
      throws IOException {
    // final String contextPath = request.getContextPath();
    boolean loggedIn = WebUtils.checkAuthenticated(httpRequest);
    final Formatter ft = new Formatter(caw);
    String cp = httpRequest.getContextPath();
    
    //Navbar preamble
    ft.format("\n<nav class=\"navbar navbar-default navbar-static-top\">");
    ft.format("<div class=\"container\">");
    ft.format("<div class=\"navbar-header\">");
    ft.format("<a class=\"navbar-brand\" href=\"/fll-sw/\">%s</a>","2016 FIRST LEGO League - Animal Allies");
    ft.format("</div>"); //end nav header
    
    
    //Start navbar content
    ft.format("<div id=\"navbar\" class=\"navbar-collapse collapse\">");
    ft.format("<ul class=\"nav navbar-nav\">");
      //Links
    ft.format("<li class=\"active\"><a href=\"%s\">Home</a></li>", cp);
    
    
      //End links
    ft.format("</ul>");
    ft.format("</div>");
    ft.format("</div>");
    ft.format("</nav>");
//    
//    ft.format("<p>NAVBAR HERE</p>");
//    if(loggedIn){
//      ft.format("<p>Admin logged in</p>");
//    }
    

  }

  /**
   * @see Filter#init(FilterConfig)
   */
  public void init(FilterConfig fConfig) throws ServletException {
  }

}
