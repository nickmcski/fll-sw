package fll.web;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

import org.apache.log4j.Logger;

import fll.util.LogUtils;

/**
 * Ensure to include the necessary CSS files in the header
 */
@WebFilter("/*")
public class HeaderFilter implements Filter {

  private static final Logger LOGGER = LogUtils.getLogger();

  /**
   * Default constructor.
   */
  public HeaderFilter() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @see Filter#destroy()
   */
  public void destroy() {
    // TODO Auto-generated method stub
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
          final int headIndex = origStr.indexOf("</head>");

          if (-1 != headIndex && !noHeader(url)) {
            caw.write(origStr.substring(0, headIndex - 1));
            addHeader(caw, httpRequest);
            caw.write(origStr.substring(headIndex));
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
  private boolean noHeader(String url) {
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
   * Writer the header to the char array writer.
   */
  private static void addHeader(final CharArrayWriter caw,
                                final HttpServletRequest request)
      throws IOException {
    //final String contextPath = request.getContextPath();
    final Formatter formatter = new Formatter(caw);
    formatter.format("<!-- Hello world --> \n");
    formatter.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fll-sw/css/bootstrap.min.css\"/>");
    formatter.format("<link rel=\"stylesheet\" type=\"text/css\" href=\"/fll-sw/style/fll-sw.css\"/> \n");
    
  }

  /**
   * @see Filter#init(FilterConfig)
   */
  public void init(FilterConfig fConfig) throws ServletException {
    // nothing
  }

}
