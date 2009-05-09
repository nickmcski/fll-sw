/*
 * Copyright (c) 2008
 *      Jon Schewe.  All rights reserved
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * I'd appreciate comments/suggestions on the code jpschewe@mtu.net
 */
package fll.web.scoreboard;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Formatter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import net.mtu.eggplant.util.sql.SQLFunctions;

import org.apache.log4j.Logger;

import fll.Utilities;
import fll.db.Queries;
import fll.web.Init;
import fll.web.SessionAttributes;

/**
 * @author jpschewe
 */
public class Last8 extends HttpServlet {

  private static Logger LOGGER = Logger.getLogger(Last8.class);

  /**
   * @param request
   * @param response
   */
  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    doPost(request, response);
  }

  /**
   * @param request
   * @param response
   */
  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Entering doPost");
    }

    try {
      Init.initialize(request, response);
    } catch (final SQLException e) {
      throw new RuntimeException("Error in initialization", e);
    }

    final HttpSession session = request.getSession();
    final DataSource datasource = SessionAttributes.getDataSource(session);
    final Formatter formatter = new Formatter(response.getWriter());
    final String showOrgStr = request.getParameter("showOrganization");
    final boolean showOrg = null == showOrgStr ? true : Boolean.parseBoolean(showOrgStr);

    PreparedStatement prep = null;
    ResultSet rs = null;
    try {
      final Connection connection = datasource.getConnection();
      
      final String currentTournament = Queries.getCurrentTournament(connection);

      formatter.format("<html>");
      formatter.format("<head>");
      formatter.format("<link rel='stylesheet' type='text/css' href='score_style.css' />");
      formatter.format("<meta http-equiv='refresh' content='30' />");
      formatter.format("</head>");

      formatter.format("<body>");
      formatter.format("<center>");
      formatter.format("<table border='1' cellpadding='0' cellspacing='0' width='98%%'>");
      formatter.format("<tr>");
      int numColumns = 6;
      if (!showOrg) {
        --numColumns;
      }
      formatter.format("<th colspan='%d'>Most Recent Performance Scores</th>", numColumns);
      formatter.format("</tr>");

      // scores here
      prep = connection.prepareStatement("SELECT Teams.TeamNumber"
          + ", Teams.Organization, Teams.TeamName, current_tournament_teams.event_division"
          + ", verified_performance.Tournament, verified_performance.RunNumber"
          + ", verified_performance.Bye, verified_performance.NoShow, verified_performance.TimeStamp, verified_performance.ComputedTotal"
          + " FROM Teams,verified_performance,current_tournament_teams WHERE verified_performance.Tournament = ?"
          + "  AND Teams.TeamNumber = verified_performance.TeamNumber AND Teams.TeamNumber = current_tournament_teams.TeamNumber"
          + "  AND verified_performance.Bye = False ORDER BY verified_performance.TimeStamp DESC, Teams.TeamNumber ASC LIMIT 8");
      prep.setString(1, currentTournament);
      rs = prep.executeQuery();

      while (rs.next()) {
        formatter.format("<tr>");
        formatter.format("<td class='left' width='10%%'>%d</td>", rs.getInt("TeamNumber"));
        String teamName = rs.getString("TeamName");
        teamName = null == teamName ? "&nbsp;" : teamName.substring(0, Math.min(20, teamName.length()));
        formatter.format("<td class='left' width='28%%'>%s</td>", teamName);
        if (showOrg) {
          String organization = rs.getString("Organization");
          organization = null == organization ? "&nbsp;" : organization.substring(0, Math.min(35, organization.length()));
          formatter.format("<td class='left'>%s</td>", organization);
        }
        formatter.format("<td class='right' width='5%%'>%s</td>", rs.getString("event_division"));
        formatter.format("<td class='right' width='5%%'>%d</td>", rs.getInt("RunNumber"));

        formatter.format("<td class='right' width='8%%'>");
        if (rs.getBoolean("NoShow")) {
          formatter.format("No Show");
        } else if (rs.getBoolean("Bye")) {
          formatter.format("Bye");
        } else {
          formatter.format("%s", Utilities.NUMBER_FORMAT_INSTANCE.format(rs.getDouble("ComputedTotal")));
        }
        formatter.format("</td>");
        formatter.format("</tr>");

      }// end while next

      formatter.format("</table>");
      formatter.format("</body>");
      formatter.format("</html>");
    } catch (final SQLException e) {
      throw new RuntimeException("Error talking to the database", e);
    } finally {
      SQLFunctions.closeResultSet(rs);
      SQLFunctions.closePreparedStatement(prep);
    }

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Exiting doPost");
    }
  }
}