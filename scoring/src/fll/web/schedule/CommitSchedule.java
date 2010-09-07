/*
 * Copyright (c) 2010 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.web.schedule;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

import fll.db.Queries;
import fll.scheduler.TournamentSchedule;
import fll.util.FLLRuntimeException;
import fll.web.BaseFLLServlet;
import fll.web.SessionAttributes;
import fll.web.WebUtils;

/**
 * Commit the schedule in uploadSchedule_schedule to the database for the
 * current tournament.
 * 
 * @web.servlet name="CommitSchedule"
 * @web.servlet-mapping url-pattern="/schedule/CommitSchedule"
 */
public class CommitSchedule extends BaseFLLServlet {

  private static final Logger LOGGER = Logger.getLogger(CommitSchedule.class);

  @Override
  protected void processRequest(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext application,
                                final HttpSession session) throws IOException, ServletException {
    final DataSource datasource = SessionAttributes.getDataSource(session);

    try {
      final Connection connection = datasource.getConnection();
      final int tournamentID = Queries.getCurrentTournament(connection);

      final TournamentSchedule schedule = SessionAttributes.getNonNullAttribute(session, "uploadSchedule_schedule",
                                                                                TournamentSchedule.class);

      schedule.storeSchedule(connection, tournamentID);
      
      session.setAttribute(SessionAttributes.MESSAGE, "<p>Schedule successfully stored in the database</p>");
      WebUtils.sendRedirect(application, response, "/admin/index.jsp");
      return;
    } catch (final SQLException e) {
      LOGGER.error("There was an error talking to the database", e);
      throw new FLLRuntimeException("There was an error talking to the database", e);
    }

  }

}