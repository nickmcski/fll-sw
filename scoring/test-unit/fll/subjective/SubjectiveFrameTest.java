/*
 * Copyright (c) 2011 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.subjective;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import net.mtu.eggplant.xml.NodelistElementCollectionAdapter;

import org.fest.swing.edt.FailOnThreadViolationRepaintManager;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JTabbedPaneFixture;
import org.fest.swing.fixture.JTableFixture;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fll.TestUtils;
import fll.Utilities;
import fll.db.ImportDB;
import fll.db.ImportDBTest;
import fll.db.Queries;
import fll.util.LogUtils;
import fll.web.admin.DownloadSubjectiveData;

/**
 * 
 */
public class SubjectiveFrameTest {

  @BeforeClass
  public static void setUpOnce() {
    FailOnThreadViolationRepaintManager.install();
  }

  private FrameFixture window;
  private String database;
  private File subjectiveScores;
  private Document document;
  
  @Before
  public void setUp() throws IOException, SQLException {
    LogUtils.initializeLogging();
    
    // create a database
    final InputStream dumpFileIS = ImportDBTest.class.getResourceAsStream("data/plymouth-2009-11-21.zip");
    Assert.assertNotNull("Cannot find test data", dumpFileIS);

    final File tempFile = File.createTempFile("flltest", null);
    database = tempFile.getAbsolutePath();

    ImportDB.loadFromDumpIntoNewDB(new ZipInputStream(dumpFileIS), database);

    final Connection connection = Utilities.createDataSource(database).getConnection();

    document = Queries.getChallengeDocument(connection);

    // set the right tournament
    final String tournamentName = "11-21 Plymouth Middle";
    int tournamentID = -1;
    final Statement stmt = connection.createStatement();
    final ResultSet rs = stmt.executeQuery("Select tournament_id,Name from Tournaments ORDER BY Name");
    while (rs.next()) {
      final int id = rs.getInt(1);
      final String name = rs.getString(2);
      if (tournamentName.equals(name)) {
        tournamentID = id;
      }
    }
    Assert.assertTrue("Could find tournament "
        + tournamentName + " in database dump", tournamentID != -1);
    Queries.setCurrentTournament(connection, tournamentID);

    // create the subjective datafile
    subjectiveScores = File.createTempFile("testStartupState", ".fll");
    subjectiveScores.deleteOnExit();
    final FileOutputStream fileStream = new FileOutputStream(subjectiveScores);
    DownloadSubjectiveData.writeSubjectiveScores(connection, document, fileStream);
    fileStream.close();

    final SubjectiveFrame frame = GuiActionRunner.execute(new GuiQuery<SubjectiveFrame>() {
      protected SubjectiveFrame executeInEDT() throws IOException {
        return new SubjectiveFrame(subjectiveScores);
      }
    });
   window = new FrameFixture(frame);
  }

  @After
  public void tearDown() {
    window.cleanUp();
    subjectiveScores.delete();
    TestUtils.deleteDatabase(database);
  }
  
  @Test
  public void testStartupState() throws SQLException, IOException {
    window.show(); // shows the frame to test

    final JTabbedPaneFixture tabbedPane = window.tabbedPane();

    final Map<String, Integer> expectedRowCounts = new HashMap<String, Integer>();
    expectedRowCounts.put("Teamwork", 29);
    expectedRowCounts.put("Design", 41);
    expectedRowCounts.put("Programming", 29);
    expectedRowCounts.put("Research Project Assessment", 58);
    for (final Element categoryDescription : new NodelistElementCollectionAdapter(
                                                                                  document
                                                                                          .getDocumentElement()
                                                                                          .getElementsByTagName(
                                                                                                                "subjectiveCategory"))) {
      final String title = categoryDescription.getAttribute("title");

      tabbedPane.selectTab(title);
      final JTableFixture table = window.table();
      if (expectedRowCounts.containsKey(title)) {
        final int expected = expectedRowCounts.get(title);
        Assert.assertEquals("Category "
            + title, expected, table.rowCount());
      } else {
        Assert.fail("Unknow category '" + title + "'");
      }
    }
  }
  
  // TODO create test for invalid values
  
  // TODO make sure that the totals add up, take weights into account
  
  // TODO make sure that clicking on cell does overwrite
  
  // TODO make sure that backspace on cell clears
  

}