/*
 * Copyright (c) 2010 INSciTE.  All rights reserved
 * INSciTE is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.web.playoff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.icepush.PushContext;

import com.itextpdf.text.DocumentException;

import fll.db.Queries;
import fll.util.FLLRuntimeException;
import fll.util.LogUtils;
import fll.web.ApplicationAttributes;
import fll.web.BaseFLLServlet;
import fll.xml.ChallengeDescription;
import net.mtu.eggplant.util.sql.SQLFunctions;

@WebServlet("/playoff/ScoresheetServlet")
public class ScoresheetServlet extends BaseFLLServlet {

  private static final Logger LOGGER = LogUtils.getLogger();

  @Override
  protected void processRequest(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext application,
                                final HttpSession session) throws IOException, ServletException {
    Connection connection = null;
    try {
      final DataSource datasource = ApplicationAttributes.getDataSource(application);
      connection = datasource.getConnection();
      final ChallengeDescription challengeDescription = ApplicationAttributes.getChallengeDescription(application);
      final int tournament = Queries.getCurrentTournament(connection);
      response.reset();
      response.setContentType("application/pdf");
      response.setHeader("Content-Disposition", "filename=scoreSheet.pdf");

      PushContext pc = PushContext.getInstance(application);
      pc.push("playoffs");

      final boolean orientationIsPortrait = ScoresheetGenerator.guessOrientation(challengeDescription);

      // Create the scoresheet generator - must provide correct number of
      // scoresheets
      final ScoresheetGenerator gen = new ScoresheetGenerator(request, connection, tournament, challengeDescription);

      
      //PrintStream printer = new PrintStream(out)
      //gen.wri
      if(request.getParameter("print") != null){
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        gen.writeFile(byteOut, orientationIsPortrait);
        byteOut.flush();
        HashDocAttributeSet attributes = new HashDocAttributeSet();
        attributes.add(javax.print.attribute.standard.MediaSizeName.NA_LETTER);
        Doc PDFOut = new SimpleDoc(byteOut.toByteArray(), DocFlavor.BYTE_ARRAY.PDF, attributes);
        
        PrintService service = PrintServiceLookup.lookupDefaultPrintService();
        LOGGER.info(service + "Name: " + service.getName());
        DocPrintJob printJob = service.createPrintJob();
        LOGGER.info(Arrays.toString(printJob.getAttributes().toArray()));
        try {
          LOGGER.info("Printer Attributes" + Arrays.toString(service.getAttributes().toArray()));
          PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
          aset.add(OrientationRequested.PORTRAIT);
          aset.add(new JobName("Test - Document", null));
          aset.add(new javax.print.attribute.standard.Copies(1));
          aset.add(MediaSizeName.NA_LETTER);
          //aset.add(new javax.print.attribute.standard.MediaSize())
          
          printJob.addPrintJobListener(new PJListen());
          printJob.print(PDFOut, aset);
          System.out.println("Printed?");
        } catch (PrintException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }else{
        gen.writeFile(response.getOutputStream(), orientationIsPortrait);
      }

    } catch (final SQLException e) {
      final String errorMessage = "There was an error talking to the database";
      LOGGER.error(errorMessage, e);
      throw new FLLRuntimeException(errorMessage, e);
    } catch (final DocumentException e) {
      final String errorMessage = "There was an error creating the PDF document - perhaps you didn't select any scoresheets to print?";
      LOGGER.error(errorMessage, e);
      throw new FLLRuntimeException(errorMessage, e);
    } finally {
      SQLFunctions.close(connection);
    }
  }

}
