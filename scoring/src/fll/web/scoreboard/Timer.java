package fll.web.scoreboard;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import fll.Team;
import fll.db.Queries;
import fll.web.ApplicationAttributes;
import fll.web.BaseFLLServlet;

/**
 * Servlet implementation class Last4
 */
@WebServlet("/scoreboard/Timer")
public class Timer extends BaseFLLServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see fll.web.BaseFLLServlet#processRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse, javax.servlet.ServletContext,
   *      javax.servlet.http.HttpSession)
   */
  
  public static int version = 1;
  @Override
  protected void processRequest(HttpServletRequest request,
                                HttpServletResponse response,
                                ServletContext application,
                                HttpSession session)
      throws IOException, ServletException {
    // TODO Auto-generated method stub
    response.setContentType("text/plain");
    Formatter out = new Formatter(response.getWriter());
//    out.format("The GO attribute is %s" , request.getAttribute("go"));
    if(request.getParameter("go") != null){
      Timer.version++;
    }else{
      out.format("%s", version);
    }

  }
  

}
