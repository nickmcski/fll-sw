/*
 * Copyright (c) 2000-2002 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */
package fll.web.admin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.mtu.eggplant.util.sql.SQLFunctions;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fll.db.Queries;
import fll.web.UploadProcessor;
import fll.xml.XMLUtils;

/**
 * Java code behind uploading subjective scores
 * 
 * @version $Revision$
 */
public final class UploadSubjectiveData extends HttpServlet {

  private static final Logger LOGGER = Logger.getLogger(UploadSubjectiveData.class);
  
  /**
   * @param request
   * @param response
   */
  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    final StringBuilder message = new StringBuilder();
    final ServletContext application = getServletContext();
    final HttpSession session = request.getSession();

    try {
      // must be first to ensure the form parameters are set
      UploadProcessor.processUpload(request);

      final FileItem subjectiveFileItem = (FileItem) request.getAttribute("subjectiveFile");
      final File file = File.createTempFile("fll", null);
      subjectiveFileItem.write(file);

      final Connection connection = (Connection) application.getAttribute("connection");
      saveSubjectiveData(file, Queries.getCurrentTournament(connection), (Document) application.getAttribute("challengeDocument"), connection);
      file.delete();

      message.append("Subjective data uploaded successfully");
    } catch (final SQLException sqle) {
      message.append("<p class='error'>Error saving subjective data into the database: "
          + sqle.getMessage() + "</p>");
      LOGGER.error(sqle, sqle);
      throw new RuntimeException("Error saving subjective data into the database", sqle);
    } catch (final ParseException e) {
      message.append("<p class='error'>Error saving subjective data into the database: "
          + e.getMessage() + "</p>");
      LOGGER.error(e, e);
      throw new RuntimeException("Error saving subjective data into the database", e);
    } catch (final FileUploadException e) {
      message.append("<p class='error'>Error processing subjective data upload: "
          + e.getMessage() + "</p>");
      LOGGER.error(e, e);
      throw new RuntimeException("Error processing subjective data upload", e);
    } catch (final Exception e) {
      message.append("<p class='error'>Error saving subjective data into the database: "
          + e.getMessage() + "</p>");
      LOGGER.error(e, e);
      throw new RuntimeException("Error saving subjective data into the database", e);
    }

    session.setAttribute("message", message.toString());
    response.sendRedirect(response.encodeRedirectURL("index.jsp"));
  }

  /**
   * Save the data stored in file to the database and update the subjective
   * score totals.
   * 
   * @param file the file to read the data from
   * @param challengeDocument the already parsed challenge document. Used to get
   *          information about the subjective categories.
   * @param connection the database connection to write to
   */
  public static void saveSubjectiveData(final File file, final String currentTournament, final Document challengeDocument, final Connection connection)
      throws SQLException, IOException, ParseException {
    final ZipFile zipfile = new ZipFile(file);
    // read in score data
    final ZipEntry scoreZipEntry = zipfile.getEntry("score.xml");
    if (null == scoreZipEntry) {
      throw new RuntimeException("Zipfile does not contain score.xml as expected");
    }
    final InputStream scoreStream = zipfile.getInputStream(scoreZipEntry);
    final Document scoreDocument = XMLUtils.parseXMLDocument(scoreStream);
    scoreStream.close();
    zipfile.close();

    final Element scoresElement = scoreDocument.getDocumentElement();
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("first element: "
          + scoresElement);
    }

    for (final Element scoreCategoryNode : XMLUtils.filterToElements(scoresElement.getChildNodes())) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("An element: "
            + scoreCategoryNode);
      }
      final Element scoreCategoryElement = (Element) scoreCategoryNode;
      final String categoryName = scoreCategoryElement.getNodeName();
      final Element categoryElement = XMLUtils.getSubjectiveCategoryByName(challengeDocument, categoryName);
      final List<Element> goalDescriptions = XMLUtils.filterToElements(categoryElement.getElementsByTagName("goal"));

      if (null == categoryElement) {
        throw new RuntimeException("Cannot find subjective category description for category in score document");
      } else {

        PreparedStatement insertPrep = null;
        PreparedStatement updatePrep = null;
        try {
          // prepare statements for update and insert

          final StringBuffer updateStmt = new StringBuffer();
          final StringBuffer insertSQLColumns = new StringBuffer();
          insertSQLColumns.append("INSERT INTO "
              + categoryName + " (TeamNumber, Tournament, Judge, NoShow");
          final StringBuffer insertSQLValues = new StringBuffer();
          insertSQLValues.append(") VALUES ( ?, ?, ?, ?");
          updateStmt.append("UPDATE "
              + categoryName + " SET NoShow = ? ");
          final int numGoals = goalDescriptions.size();
          for (final Element goalDescription : goalDescriptions) {
            insertSQLColumns.append(", "
                + goalDescription.getAttribute("name"));
            insertSQLValues.append(", ?");
            updateStmt.append(", "
                + goalDescription.getAttribute("name") + " = ?");
          }

          updateStmt.append(" WHERE TeamNumber = ? AND Tournament = ? AND Judge = ?");
          updatePrep = connection.prepareStatement(updateStmt.toString());
          insertPrep = connection.prepareStatement(insertSQLColumns.toString()
              + insertSQLValues.toString() + ")");
          // initialze the tournament
          insertPrep.setString(2, currentTournament);
          updatePrep.setString(numGoals + 3, currentTournament);

          for (final Element scoreElement : XMLUtils.filterToElements(scoreCategoryElement.getElementsByTagName("score"))) {

            if (scoreElement.hasAttribute("modified")
                && "true".equalsIgnoreCase(scoreElement.getAttribute("modified"))) {
              final int teamNumber = NumberFormat.getInstance().parse(scoreElement.getAttribute("teamNumber")).intValue();
              final String judge = scoreElement.getAttribute("judge");
              final boolean noShow = Boolean.parseBoolean(scoreElement.getAttribute("NoShow"));
              updatePrep.setBoolean(1, noShow);
              insertPrep.setBoolean(4, noShow);

              insertPrep.setInt(1, teamNumber);
              updatePrep.setInt(numGoals + 2, teamNumber);
              insertPrep.setString(3, judge);
              updatePrep.setString(numGoals + 4, judge);

              for (int goalIndex = 0; goalIndex < numGoals; goalIndex++) {
                final Element goalDescription = goalDescriptions.get(goalIndex);
                final String goalName = goalDescription.getAttribute("name");
                final String value = scoreElement.getAttribute(goalName);
                if (null != value
                    && !"".equals(value.trim())) {
                  insertPrep.setString(goalIndex + 5, value.trim());
                  updatePrep.setString(goalIndex + 2, value.trim());
                } else {
                  insertPrep.setNull(goalIndex + 5, Types.DOUBLE);
                  updatePrep.setNull(goalIndex + 2, Types.DOUBLE);
                }
              }

              // attempt the update first
              final int modifiedRows = updatePrep.executeUpdate();
              if (modifiedRows < 1) {
                // do insert
                insertPrep.executeUpdate();
              }
            }
          }

        } finally {
          SQLFunctions.closePreparedStatement(insertPrep);
        }
      }
    }

    Queries.updateSubjectiveScoreTotals(challengeDocument, connection);
  }

}
