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
@WebServlet("/scoreboard/Last4")
public class Last4 extends BaseFLLServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see fll.web.BaseFLLServlet#processRequest(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse, javax.servlet.ServletContext,
   *      javax.servlet.http.HttpSession)
   */
  @Override
  protected void processRequest(HttpServletRequest request,
                                HttpServletResponse response,
                                ServletContext application,
                                HttpSession session)
      throws IOException, ServletException {
    // TODO Auto-generated method stub
    response.setContentType("text/plain");
    Formatter out = new Formatter(response.getWriter());

    PreparedStatement prep = null;
    ResultSet rs = null;
    Connection connection = null;
    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    try {
      connection = datasource.getConnection();

      final int currentTournament = Queries.getCurrentTournament(connection);
      prep = connection.prepareStatement("select * from PERFORMANCE where verified = TRUE ORDER by TIMESTAMP desc limit 4");
      rs = prep.executeQuery();
      while(rs.next()){
        //out.format("" + rs.getString("teamnumber"));
        printTeam(rs, connection, out);
      }
//      for(int i = 1; i<=4; i++){
//        printTeam(rs, out);
//      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  private void printTeam(ResultSet rs, Connection conn, Formatter formatter) throws SQLException{
    Random rand = new Random();
    //Queries.getTeamCurrentTournament(conn, rs.getInt("TEAMNUMBER"));
    Team team = Team.getTeamFromDatabase(conn, rs.getInt("TEAMNUMBER"));
    // style=\"display:none\"
    formatter.format("<div class=\"entry\" style=\"display: inline-block;\">");
    formatter.format("<p class=\"team\">Team : %s %s</p>", team.getTeamNumber(),team.getTeamName());
    formatter.format("<p class=\"score\">Run %s: %s points</p>", rs.getInt("RUNNUMBER"), (int) (rs.getDouble("COMPUTEDTOTAL")));
    //formatter.format("<p class=\"score\">%s</p>", "Run 2: " + (rand.nextInt(150)+1) + "points");
    //if(rand.nextBoolean()) formatter.format("<p class=\"score\">%s</p>", "Run 3: " + (rand.nextInt(150)+1) + " points");
    formatter.format("</div>");
    
    
  }

}
