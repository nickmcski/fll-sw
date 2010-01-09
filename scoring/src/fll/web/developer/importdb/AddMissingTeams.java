/*
 * Copyright (c) 2000-2002 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */
package fll.web.developer.importdb;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import net.mtu.eggplant.util.sql.SQLFunctions;

import org.apache.log4j.Logger;

import fll.Team;
import fll.db.Queries;
import fll.web.BaseFLLServlet;
import fll.web.SessionAttributes;

/**
 * Add teams after promptCreateMissingTeams.jsp.
 * 
 * @author jpschewe
 */
public class AddMissingTeams extends BaseFLLServlet {

  private static final Logger LOG = Logger.getLogger(AddMissingTeams.class);

  protected void processRequest(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext application,
                                final HttpSession session) throws IOException, ServletException {
    final StringBuilder message = new StringBuilder();

    Connection sourceConnection = null;
    Connection destConnection = null;
    try {
      final String tournament = SessionAttributes.getNonNullAttribute(session, "selectedTournament", String.class);
      final DataSource sourceDataSource = SessionAttributes.getNonNullAttribute(session, "dbimport", DataSource.class);
      sourceConnection = sourceDataSource.getConnection();

      final DataSource destDataSource = SessionAttributes.getDataSource(session);
      destConnection = destDataSource.getConnection();

      final int tournamentID = Queries.getTournamentID(destConnection, tournament);

      @SuppressWarnings(value = "unchecked")
      final List<Team> missingTeams = SessionAttributes.getNonNullAttribute(session, "missingTeams", List.class);
      for (final Team team : missingTeams) {
        final String dup = Queries.addTeam(destConnection, team.getTeamNumber(), team.getTeamName(), team.getOrganization(), team.getRegion(),
                                           team.getDivision(), tournamentID);
        if (null != dup) {
          throw new RuntimeException(
                                     String
                                           .format(
                                                   "Internal error, team with number %d should not exist in the destination database, found match with team with name: %s",
                                                   team.getTeamNumber(), dup));
        }
      }
      
      session.setAttribute(SessionAttributes.REDIRECT_URL, "CheckTeamInfo");
      
    } catch (final SQLException sqle) {
      LOG.error(sqle, sqle);
      throw new RuntimeException("Error talking to the database", sqle);
    } finally {
      SQLFunctions.closeConnection(sourceConnection);
      SQLFunctions.closeConnection(destConnection);
    }

    session.setAttribute("message", message.toString());
    response.sendRedirect(response.encodeRedirectURL(SessionAttributes.getRedirectURL(session)));
  }
}
