/*
 * Copyright (c) 2000-2002 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */
package fll.web.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import fll.Tournament;
import fll.Utilities;
import fll.db.Queries;
import fll.util.CSVCellReader;
import fll.util.CellFileReader;
import fll.util.ExcelCellReader;
import fll.util.FLLInternalException;
import fll.util.FLLRuntimeException;
import fll.util.LogUtils;
import fll.web.ApplicationAttributes;
import fll.web.BaseFLLServlet;
import fll.web.SessionAttributes;
import net.mtu.eggplant.util.sql.SQLFunctions;

/**
 * Assign teams to tournaments, creating tournaments if needed.
 */
@WebServlet("/admin/ProcessTeamTournamentAssignmentsUpload")
public final class ProcessTeamTournamentAssignmentsUpload extends BaseFLLServlet {

  private static final Logger LOGGER = LogUtils.getLogger();

  protected void processRequest(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext application,
                                final HttpSession session) throws IOException, ServletException {

    final StringBuilder message = new StringBuilder();
    final String advanceFile = SessionAttributes.getNonNullAttribute(session, "advanceFile", String.class);
    final File file = new File(advanceFile);
    Connection connection = null;
    try {
      if (!file.exists()
          || !file.canRead()) {
        throw new RuntimeException("Cannot read file: "
            + advanceFile);
      }

      final String teamNumberColumnName = request.getParameter("teamNumber");
      if (null == teamNumberColumnName) {
        throw new FLLRuntimeException("Cannot find 'teamNumber' request parameter");
      }

      final String tournamentColumnName = request.getParameter("tournament");
      if (null == tournamentColumnName) {
        throw new FLLRuntimeException("Cannot find 'tournament' request parameter");
      }

      final DataSource datasource = ApplicationAttributes.getDataSource(application);
      connection = datasource.getConnection();

      final String sheetName = SessionAttributes.getAttribute(session, "sheetName", String.class);

      processFile(connection, message, file, sheetName, teamNumberColumnName, tournamentColumnName);

      session.setAttribute("message", message.toString());
      response.sendRedirect(response.encodeRedirectURL("index.jsp"));

      if (!file.delete()) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Error deleting file, will need to wait until exit. Filename: "
              + file.getAbsolutePath());
        }
      }

    } catch (final SQLException sqle) {
      message.append("<p class='error'>Error saving team assignmentsinto the database: "
          + sqle.getMessage() + "</p>");
      LOGGER.error(sqle, sqle);
      throw new RuntimeException("Error saving team assignments into the database", sqle);
    } catch (final ParseException e) {
      message.append("<p class='error'>Error saving team assignments into the database: "
          + e.getMessage() + "</p>");
      LOGGER.error(e, e);
      throw new RuntimeException("Error saving team assignments into the database", e);
    } catch (final Exception e) {
      message.append("<p class='error'>Error saving team assignments into the database: "
          + e.getMessage() + "</p>");
      LOGGER.error(e, e);
      throw new RuntimeException("Error saving team assignments into the database", e);
    } finally {
      session.setAttribute(SessionAttributes.MESSAGE, message.toString());

      if (!file.delete()) {
        file.deleteOnExit();
      }
      SQLFunctions.close(connection);
    }
  }

  /**
   * Make the changes
   * 
   * @throws InvalidFormatException
   */
  public static void processFile(final Connection connection,
                                 final StringBuilder message,
                                 final File file,
                                 final String sheetName,
                                 final String teamNumberColumnName,
                                 final String tournamentColumnName)
                                     throws SQLException, IOException, ParseException, InvalidFormatException {

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("File name: "
          + file.getName());
    }

    final CellFileReader reader;
    if (ExcelCellReader.isExcelFile(file)) {
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(file);
        reader = new ExcelCellReader(fis, sheetName);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Created excel reader");
        }
      } finally {
        if (null != fis) {
          fis.close();
        }
      }
    } else {
      // determine if the file is tab separated or comma separated, check the
      // first line for tabs and if they aren't found, assume it's comma
      // separated
      final BufferedReader breader = new BufferedReader(new InputStreamReader(new FileInputStream(file),
                                                                              Utilities.DEFAULT_CHARSET));
      try {
        final String firstLine = breader.readLine();
        if (null == firstLine) {
          LOGGER.error("Empty file");
          return;
        }
        if (firstLine.indexOf('\t') != -1) {
          reader = new CSVCellReader(file, '\t');
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Created csv tab reader");
          }
        } else {
          reader = new CSVCellReader(file);
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Created csv comma reader");
          }
        }
      } finally {
        breader.close();
      }
    }

    // parse out the first non-blank line as the names of the columns
    String[] columnNames = reader.readNext();
    while (columnNames.length < 1) {
      columnNames = reader.readNext();
    }
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Column names size: "
          + columnNames.length //
          + " names: " + Arrays.asList(columnNames).toString() //
          + " teamNumber column: " + teamNumberColumnName);
    }

    int teamNumColumnIdx = -1;
    int tournamentColumnIdx = -1;
    int index = 0;
    while (-1 == teamNumColumnIdx
        || -1 == tournamentColumnIdx) {
      if (-1 == teamNumColumnIdx
          && teamNumberColumnName.equals(columnNames[index])) {
        teamNumColumnIdx = index;
      }

      if (-1 == tournamentColumnIdx
          && tournamentColumnName.equals(columnNames[index])) {
        tournamentColumnIdx = index;
      }

      ++index;
    }

    int rowsProcessed = 0;
    String[] data = reader.readNext();
    while (null != data) {
      if (teamNumColumnIdx < data.length
          && tournamentColumnIdx < data.length) {
        final String teamNumStr = data[teamNumColumnIdx];
        if (null != teamNumStr
            && !"".equals(teamNumStr.trim())) {
          final int teamNumber = Utilities.NUMBER_FORMAT_INSTANCE.parse(teamNumStr).intValue();

          final String division = Queries.getDivisionOfTeam(connection, teamNumber);

          final String tournamentName = data[tournamentColumnIdx];
          Tournament tournament = Tournament.findTournamentByName(connection, tournamentName);
          if (null == tournament) {
            // create the tournament
            Tournament.createTournament(connection, tournamentName, tournamentName);
            tournament = Tournament.findTournamentByName(connection, tournamentName);
            if (null == tournament) {
              throw new FLLInternalException("Created tournament '"
                  + tournamentName + "', but can't find it.");
            } else {
              message.append("<p>Created tournament '"
                  + tournamentName + "'</p>");
            }
          } else {
            Queries.addTeamToTournament(connection, teamNumber, tournament.getTournamentID(), division, division);
            ++rowsProcessed;
          }
        }
      }

      data = reader.readNext();
    }

    message.append("<p>Successfully processed "
        + rowsProcessed + " rows of data</p>");

  }

}