package dev.trae.chunky.littletiles.c7;

import dev.trae.chunky.littletiles.LittleBox;

/**
 * Box parser strategy interface.
 * 
 * Allows multiple parsing strategies with fallback chain.
 * Each parser attempts to handle the data and throws BoxParseException if it cannot.
 */
public interface BoxParser {
  
  /**
   * Try to parse the box array into a LittleBox.
   * 
   * @param data NBT int array
   * @return parsed LittleBox (or subclass)
   * @throws BoxParseException if this parser cannot handle the data
   */
  LittleBox parse(int[] data) throws BoxParseException;
  
  /**
   * Exception thrown when a parser cannot handle the data.
   */
  class BoxParseException extends Exception {
    public BoxParseException(String message) {
      super(message);
    }
  }
}
