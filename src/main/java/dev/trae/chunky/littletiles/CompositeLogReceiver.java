package dev.trae.chunky.littletiles;

import se.llbit.log.Level;
import se.llbit.log.Receiver;

/**
 * Composite log receiver that broadcasts to multiple receivers.
 * Ensures logs go to both file and console/GUI.
 */
public class CompositeLogReceiver extends Receiver {
  
  private final Receiver[] receivers;
  
  public CompositeLogReceiver(Receiver... receivers) {
    this.receivers = receivers;
  }
  
  @Override
  public void logEvent(Level level, String message) {
    for (Receiver receiver : receivers) {
      if (receiver != null) {
        try {
          receiver.logEvent(level, message);
        } catch (Exception e) {
          // Silently ignore receiver errors to prevent cascading failures
          System.err.println("CompositeLogReceiver: Failed to log to receiver: " + e.getMessage());
        }
      }
    }
  }
  
  @Override
  public void logEvent(Level level, String message, Throwable thrown) {
    for (Receiver receiver : receivers) {
      if (receiver != null) {
        try {
          receiver.logEvent(level, message, thrown);
        } catch (Exception e) {
          System.err.println("CompositeLogReceiver: Failed to log to receiver: " + e.getMessage());
        }
      }
    }
  }
  
  @Override
  public void logEvent(Level level, Throwable thrown) {
    for (Receiver receiver : receivers) {
      if (receiver != null) {
        try {
          receiver.logEvent(level, thrown);
        } catch (Exception e) {
          System.err.println("CompositeLogReceiver: Failed to log to receiver: " + e.getMessage());
        }
      }
    }
  }
}
