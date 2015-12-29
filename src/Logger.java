/* *****************************************************************************
 * $Archive: $
 * $Revision: $
 * $Date:  $
 *
 * Copyright (c) 2012 De Clercq Solutions BVBA, Belgium
 * All rights reserved
 *
 * This software is the confidential and proprietary information
 * of De Clercq Solutions BVBA. You shall not disclose this
 * confidential information and shall use it only in accordance
 * with the terms of the license agreement you entered into with
 * De Clercq Solutions BVBA.
 * ****************************************************************************
 */

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger
{
  public static final String DATE_FORMAT = "yyyyMMdd.HHmmss.SSS";

  public static final String DEBUG = "DEBUG";
  public static final String ERROR = "ERROR";
  public static final String WARNING = "WARNING";

  public static SimpleDateFormat getSimpleDateFormat()
  {
    return new SimpleDateFormat(DATE_FORMAT);
  }

  public static String getCurrentDate()
  {
    return getSimpleDateFormat().format(new Date());
  }

  public static void warning(Class pClass, String pMethod, String pMessage)
  {
    writeLog(WARNING, pClass.getName(), pMethod, pMessage);
  }

  public static void error(Class pClass, String pMethod, String pMessage)
  {
    writeLog(ERROR, pClass.getName(), pMethod, pMessage);
  }

  public static void debug(Class pClass, String pMethod, String pMessage)
  {
    writeLog(DEBUG, pClass.getName(), pMethod, pMessage);
  }

  public static void warning(String pClassName, String pMethod, String pMessage)
  {
    writeLog(WARNING, pClassName, pMethod, pMessage);
  }

  public static void error(String pClassName, String pMethod, String pMessage)
  {
    writeLog(ERROR, pClassName, pMethod, pMessage);
  }

  public static void debug(String pClassName, String pMethod, String pMessage)
  {
    writeLog(DEBUG, pClassName, pMethod, pMessage);
  }

  public static void writeLog(String pCategory, String pClassName, String pMethod, String pMessage)
  {
    System.out.println(getCurrentDate() + " | " + pCategory + " | " + pClassName + " | " + pMethod + " | " + pMessage);
  }
}
