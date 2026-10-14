package dev.trae.chunky.littletiles;

import se.llbit.log.Level;
import se.llbit.log.Receiver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * File-based log receiver for LittleTiles plugin debugging.
 * Writes all log events to ~/.chunky/plugins/littletiles-plugin.log
 */
public class FileLogReceiver extends Receiver {
  
  private static final DateTimeFormatter TIMESTAMP_FORMAT = 
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  
  private final Path logFile;
  private PrintWriter writer;
  
  public FileLogReceiver(Path logFile) {
    this.logFile = logFile;
    try {
      // Create parent directories if needed
      Files.createDirectories(logFile.getParent());
      
      // Open file in append mode
      this.writer = new PrintWriter(
          new BufferedWriter(new FileWriter(logFile.toFile(), true)), 
          true  // auto-flush
      );
      
      // Write session header
      writer.println();
      writer.println("=".repeat(80));
      writer.println("LittleTiles Plugin Log - Session started: " + 
          LocalDateTime.now().format(TIMESTAMP_FORMAT));
      writer.println("=".repeat(80));
      
    } catch (IOException e) {
      System.err.println("Failed to initialize FileLogReceiver: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  @Override
  public void logEvent(Level level, String message) {
    if (writer == null) {
      return;
    }
    
    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    String logLine = String.format("[%s] [%s] %s", timestamp, level, message);
    
    writer.println(logLine);
  }
  
  @Override
  public void logEvent(Level level, String message, Throwable thrown) {
    if (writer == null) {
      return;
    }
    
    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    writer.println(String.format("[%s] [%s] %s", timestamp, level, message));
    thrown.printStackTrace(writer);
  }
  
  /**
   * Close the log file and flush any remaining data.
   */
  public void close() {
    if (writer != null) {
      writer.println("=".repeat(80));
      writer.println("Session ended: " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
      writer.println("=".repeat(80));
      writer.close();
      writer = null;
    }
  }
  
  /**
   * Create a default FileLogReceiver that logs to ~/.chunky/plugins/littletiles-plugin.log
   */
  public static FileLogReceiver createDefault() {
    String userHome = System.getProperty("user.home");
    Path logFile = Paths.get(userHome, ".chunky", "plugins", "littletiles-plugin.log");
    return new FileLogReceiver(logFile);
  }
  
  /**
   * Get the log file path for reporting to the user.
   */
  public String getLogFilePath() {
    return logFile.toString();
  }
}
