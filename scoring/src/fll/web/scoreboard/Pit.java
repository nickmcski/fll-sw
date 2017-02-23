package fll.web.scoreboard;

import fll.web.BaseFLLServlet;

import java.io.IOException;
import java.util.Formatter;
import java.util.Random;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
    
    format.format("Hello");
    format.format("<table class=\"pit_scores\">");
    format.format("<tr><th>Rank</th><th>Team</th><th>Name</th><th>run</th><th>Score</th></tr>");
    Random rand = new Random();
    for(int i = 1; i< 20; i++){
      printTeam(format, i, i*100, "Team " + (i*100), rand.nextInt(3) +1, rand.nextInt(600));
    }
    
    format.format("</table>");
  }
  
  private static void printTeam(Formatter formater, int rank, int number, String name, int run, int score){
    formater.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>", rank, number, name, run, score);
  }

}
