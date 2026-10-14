package dev.trae.chunky.littletiles;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.llbit.log.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileLogReceiverTest {
  
  @Test
  void testLogToFile(@TempDir Path tempDir) throws IOException {
    Path logFile = tempDir.resolve("test.log");
    
    FileLogReceiver receiver = new FileLogReceiver(logFile);
    receiver.logEvent(Level.INFO, "Test info message");
    receiver.logEvent(Level.WARNING, "Test warning message");
    receiver.close();
    
    // Verify file was created
    assertTrue(Files.exists(logFile), "Log file should exist");
    
    // Verify content
    List<String> lines = Files.readAllLines(logFile);
    assertTrue(lines.size() > 0, "Log file should not be empty");
    
    String content = String.join("\n", lines);
    assertTrue(content.contains("Test info message"), "Should contain info message");
    assertTrue(content.contains("Test warning message"), "Should contain warning message");
    assertTrue(content.contains("[INFO]"), "Should contain INFO level");
    assertTrue(content.contains("[WARNING]"), "Should contain WARNING level");
  }
  
  @Test
  void testLogException(@TempDir Path tempDir) throws IOException {
    Path logFile = tempDir.resolve("exception.log");
    
    FileLogReceiver receiver = new FileLogReceiver(logFile);
    Exception testException = new RuntimeException("Test exception");
    receiver.logEvent(Level.ERROR, "Error occurred", testException);
    receiver.close();
    
    // Verify exception stack trace is logged
    List<String> lines = Files.readAllLines(logFile);
    String content = String.join("\n", lines);
    assertTrue(content.contains("RuntimeException"), "Should contain exception type");
    assertTrue(content.contains("Test exception"), "Should contain exception message");
  }
  
  @Test
  void testSessionHeader(@TempDir Path tempDir) throws IOException {
    Path logFile = tempDir.resolve("session.log");
    
    FileLogReceiver receiver = new FileLogReceiver(logFile);
    receiver.close();
    
    List<String> lines = Files.readAllLines(logFile);
    String content = String.join("\n", lines);
    assertTrue(content.contains("Session started"), "Should contain session header");
    assertTrue(content.contains("Session ended"), "Should contain session footer");
  }
}
