/*
 * Copyright (c) 2017 High Tech Kids.  All rights reserved
 * HighTechKids is on the web at: http://www.hightechkids.org
 * This code is released under GPL; see LICENSE.txt for details.
 */

package fll.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 
 */
public class SQLUtil {
  public static void close (Object... objects){
    for(Object obj: objects){
      if(obj instanceof ResultSet){
        try {
          ((ResultSet) obj).close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    for(Object obj: objects){
      if(obj instanceof PreparedStatement){
        try {
          ((PreparedStatement) obj).close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    
    for(Object obj: objects){
      if(obj instanceof Connection){
        try {
          ((Connection) obj).close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
