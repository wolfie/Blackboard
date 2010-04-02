package com.github.wolfie.blackboard;

import java.io.PrintStream;

class Log {
  private static boolean logging = false;
  private static PrintStream logTo = System.out;
  
  private Log() {
    // not instantiable
  }
  
  static void logTo(final PrintStream printWriter) {
    logTo = printWriter;
  }
  
  static void setLogging(final boolean enableLogging) {
    logging = enableLogging;
  }
  
  static void log(final String string) {
    if (logging) {
      logTo.println("[BB] " + string);
    }
  }
  
  static void logEmptyLine() {
    log("");
  }
}
