package fll.web.scoreboard;

import fll.Team;
import fll.db.Queries;
import fll.util.SQLUtil;
import fll.web.ApplicationAttributes;
import fll.web.BaseFLLServlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Servlet implementation class Pit
 */
@WebServlet("/scoreboard/Pit")
public class Pit extends BaseFLLServlet {
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
    Formatter format = new Formatter(response.getWriter());
    
    format.format("<table class=\"pit_scores\"><tbody>");
    format.format("<tr><th>Rank</th><th>Team</th><th>Name</th><th>run</th><th>Score</th></tr>");
    Random rand = new Random();
    for(int i = 1; i< 20; i++){
      
    }
    
    PreparedStatement prep = null;
    ResultSet rs = null;
    Connection connection = null;
    final DataSource datasource = ApplicationAttributes.getDataSource(application);
    try {
      connection = datasource.getConnection();

      final int currentTournament = Queries.getCurrentTournament(connection);
      prep = connection.prepareStatement("select * from PERFORMANCE where verified = TRUE ORDER by COMPUTEDTOTAL desc");
      rs = prep.executeQuery();
      int place = 0;
      List<Integer> alreadySeen = new ArrayList<Integer>();
      while(rs.next()){
        Team team = Team.getTeamFromDatabase(connection, rs.getInt("TEAMNUMBER"));
        if(alreadySeen.contains(team.getTeamNumber()))
          continue;
        alreadySeen.add(team.getTeamNumber());
        //out.format("" + rs.getString("teamnumber"));
        //printTeam(rs, connection, out);
        place++;
        printTeam(format,place , team.getTeamNumber(), team.getTeamName(), rs.getInt("RUNNUMBER"), rs.getInt("COMPUTEDTOTAL"));
      }
//      for(int i = 1; i<=4; i++){
//        printTeam(rs, out);
//      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    
    format.format("</tbody></table>");
    
    SQLUtil.close(connection, prep,rs);
  }
  
  private static void printTeam(Formatter formater, int rank, int number, String name, int run, int score){
    formater.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>", rank, number, name, run, score);
  }

}
