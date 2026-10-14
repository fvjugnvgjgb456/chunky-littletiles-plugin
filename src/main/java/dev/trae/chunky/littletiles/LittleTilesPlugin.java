package dev.trae.chunky.littletiles;

import se.llbit.chunky.Plugin;
import se.llbit.chunky.main.Chunky;
import se.llbit.log.Level;
import se.llbit.log.Log;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Chunky plugin entry point for LittleTiles rendering support.
 */
public class LittleTilesPlugin implements Plugin {
  
  private static final DateTimeFormatter TIMESTAMP_FORMAT = 
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
  
  @Override
  public void attach(Chunky chunky) {
    try {
      // Set log level to INFO
      Log.setLevel(Level.INFO);
      
      // Setup System.out and System.err redirection to capture ALL output
      String userHome = System.getProperty("user.home");
      Path logFile = Paths.get(userHome, ".chunky", "plugins", "littletiles-plugin.log");
      Files.createDirectories(logFile.getParent());
      
      // Save original streams
      PrintStream originalOut = System.out;
      PrintStream originalErr = System.err;
      
      // Create file output stream (append mode)
      FileOutputStream fileOut = new FileOutputStream(logFile.toFile(), true);
      PrintStream fileStream = new PrintStream(fileOut, true);
      
      // Write session header to file
      fileStream.println();
      fileStream.println("=".repeat(80));
      fileStream.println("LittleTiles Plugin Log - Session started: " + 
          LocalDateTime.now().format(TIMESTAMP_FORMAT));
      fileStream.println("=".repeat(80));
      
      // Add shutdown hook to print performance report
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        PerformanceMonitor.printReport();
      }));
      
      // Create Tee streams that write to both console and file
      TeeOutputStream teeOut = new TeeOutputStream(originalOut, fileOut);
      TeeOutputStream teeErr = new TeeOutputStream(originalErr, fileOut);
      
      PrintStream teeOutStream = new PrintStream(teeOut, true);
      PrintStream teeErrStream = new PrintStream(teeErr, true);
      
      // Redirect both System.out and System.err
      System.setOut(teeOutStream);
      System.setErr(teeErrStream);
      
      System.out.println("LittleTiles plugin loaded");
      System.out.println("System.out and System.err redirected to: " + logFile);
      
      // Register BlockProvider
      chunky.registerBlockProvider(new LTBlockProvider());
      System.out.println("LTBlockProvider registered");
      
    } catch (Exception e) {
      System.err.println("Failed to setup logging: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
