/*
 * Copyright (c) 2011 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.web.scoreEntry;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import net.mtu.eggplant.util.sql.SQLFunctions;
import fll.Team;
import fll.TournamentTeam;
import fll.Utilities;
import fll.db.Queries;
import fll.db.TournamentParameters;
import fll.web.ApplicationAttributes;
import fll.web.BaseFLLServlet;
import fll.web.SessionAttributes;
import fll.web.playoff.Playoff;
import fll.xml.ChallengeDescription;

/**
 * Gather the data required for scoreEntry.jsp.
 */
@WebServlet("/scoreEntry/GatherScoreEntryData")
public class GatherScoreEntryData extends BaseFLLServlet {

  @Override
  protected void processRequest(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext application,
                                final HttpSession session)
      throws IOException, ServletException {
    Connection connection = null;
    try {
      final ChallengeDescription challengeDescription = ApplicationAttributes.getChallengeDescription(application);

      session.setAttribute("EditFlag", request.getParameter("EditFlag"));

      // support the unverified runs select box
      final String lTeamNum = request.getParameter("TeamNumber");
      if (null == lTeamNum) {
        session.setAttribute(SessionAttributes.MESSAGE,
                             "<p name='error' class='error'>Attempted to load score entry page without providing a team number.</p>");
        response.sendRedirect(response.encodeRedirectURL("select_team.jsp"));
        return;
      }
      final int dashIndex = lTeamNum.indexOf('-');
      final int teamNumber;
      final String runNumberStr;
      if (dashIndex > 0) {
        // teamNumber - runNumber
        final String teamStr = lTeamNum.substring(0, dashIndex);
        teamNumber = Integer.parseInt(teamStr);
        runNumberStr = lTeamNum.substring(dashIndex
            + 1);
      } else {
        runNumberStr = request.getParameter("RunNumber");
        teamNumber = Utilities.NUMBER_FORMAT_INSTANCE.parse(lTeamNum).intValue();
      }
      final DataSource datasource = ApplicationAttributes.getDataSource(application);
      connection = datasource.getConnection();
      final int tournament = Queries.getCurrentTournament(connection);
      final int numSeedingRounds = TournamentParameters.getNumSeedingRounds(connection, tournament);
      final Map<Integer, TournamentTeam> tournamentTeams = Queries.getTournamentTeams(connection);
      if (!tournamentTeams.containsKey(Integer.valueOf(teamNumber))) {
        throw new RuntimeException("Selected team number is not valid: "
            + teamNumber);
      }
      final Team team = tournamentTeams.get(Integer.valueOf(teamNumber));
      session.setAttribute("team", team);

      // the next run the team will be competing in
      final int nextRunNumber = Queries.getNextRunNumber(connection, team.getTeamNumber());

      // what run number we're going to edit/enter
      int lRunNumber;
      if ("1".equals(request.getParameter("EditFlag"))) {
        if (null == runNumberStr) {
          session.setAttribute(SessionAttributes.MESSAGE,
                               "<p name='error' class='error'>Please choose a run number when editing scores</p>");
          response.sendRedirect(response.encodeRedirectURL("select_team.jsp"));
          return;
        }
        final int runNumber = Utilities.NUMBER_FORMAT_INSTANCE.parse(runNumberStr).intValue();
        if (runNumber == 0) {
          lRunNumber = nextRunNumber
              - 1;
          if (lRunNumber < 1) {
            session.setAttribute(SessionAttributes.MESSAGE,
                                 "<p name='error' class='error'>Selected team has no performance score for this tournament.</p>");
            response.sendRedirect(response.encodeRedirectURL("select_team.jsp"));
            return;
          }
        } else {
          if (!Queries.performanceScoreExists(connection, teamNumber, runNumber)) {
            session.setAttribute(SessionAttributes.MESSAGE,
                                 "<p name='error' class='error'>Team has not yet competed in run "
                                     + runNumber + ".  Please choose a valid run number.</p>");
            response.sendRedirect(response.encodeRedirectURL("select_team.jsp"));
            return;
          }
          lRunNumber = runNumber;
        }
      } else {
        if (nextRunNumber > numSeedingRounds) {
          if (null == Playoff.involvedInUnfinishedPlayoff(connection, tournament,
                                                          Collections.singletonList(teamNumber))) {
            session.setAttribute(SessionAttributes.MESSAGE, "<p name='error' class='error'>Selected team ("
                + teamNumber
                + ") is not involved in an unfinished head to head bracket. Please double check that the head to head brackets were properly initialized"
                + " If you were intending to double check a score, you probably just forgot to check"
                + " the box for doing so.</p>");
            response.sendRedirect(response.encodeRedirectURL("select_team.jsp"));
            return;
          } else if (!Queries.didTeamReachPlayoffRound(connection, nextRunNumber, teamNumber)) {
            session.setAttribute(SessionAttributes.MESSAGE,
                                 "<p name='error' class='error'>Selected team has not advanced to the next head to head round.</p>");
            response.sendRedirect(response.encodeRedirectURL("select_team.jsp"));
            return;
          }
        }
        lRunNumber = nextRunNumber;
      }
      session.setAttribute("lRunNumber", lRunNumber);

      final String roundText;
      if (lRunNumber > numSeedingRounds) {
        final String division = Playoff.getPlayoffDivision(connection, teamNumber, lRunNumber);
        final int playoffRun = Playoff.getPlayoffRound(connection, division, lRunNumber);
        roundText = "Playoff&nbsp;Round&nbsp;"
            + playoffRun;
      } else {
        roundText = "Run&nbsp;Number&nbsp;"
            + lRunNumber;
      }
      session.setAttribute("roundText", roundText);

      final double minimumAllowedScore = challengeDescription.getPerformance().getMinimumScore();
      session.setAttribute("minimumAllowedScore", minimumAllowedScore);

      // check if this is the last run a team has completed
      final int maxRunCompleted = Queries.getMaxRunNumber(connection, teamNumber);
      session.setAttribute("isLastRun", Boolean.valueOf(lRunNumber == maxRunCompleted));

      // check if the score being edited is a bye
      session.setAttribute("isBye", Boolean.valueOf(Queries.isBye(connection, tournament, teamNumber, lRunNumber)));
      session.setAttribute("isNoShow",
                           Boolean.valueOf(Queries.isNoShow(connection, tournament, teamNumber, lRunNumber)));

      // check if previous run is verified
      final boolean previousVerified;
      if (lRunNumber > 1) {
        previousVerified = Queries.isVerified(connection, tournament, teamNumber, lRunNumber
            - 1);
      } else {
        previousVerified = true;
      }
      session.setAttribute("previousVerified", previousVerified);

      if (lRunNumber <= numSeedingRounds) {
        if ("1".equals(request.getParameter("EditFlag"))) {
          session.setAttribute("top_info_color", "yellow");
        } else {
          session.setAttribute("top_info_color", "#e0e0e0");
        }
      } else {
        session.setAttribute("top_info_color", "#00ff00");
      }

      if ("1".equals(request.getParameter("EditFlag"))) {
        session.setAttribute("body_background", "yellow");
      } else {
        session.setAttribute("body_background", "transparent");
      }

      response.sendRedirect(response.encodeRedirectURL("scoreEntry.jsp"));
    } catch (final ParseException pe) {
      throw new RuntimeException(pe);
    } catch (final SQLException e) {
      throw new RuntimeException(e);
    } finally {
      SQLFunctions.close(connection);
    }
  }

}
