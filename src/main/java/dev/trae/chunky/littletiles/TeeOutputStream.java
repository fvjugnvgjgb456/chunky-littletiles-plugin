package dev.trae.chunky.littletiles;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Tee output stream that writes to multiple output streams simultaneously.
 * Similar to Unix tee command - duplicates output to multiple destinations.
 */
public class TeeOutputStream extends OutputStream {
  
  private final OutputStream[] streams;
  
  public TeeOutputStream(OutputStream... streams) {
    this.streams = streams;
  }
  
  @Override
  public void write(int b) throws IOException {
    for (OutputStream stream : streams) {
      if (stream != null) {
        stream.write(b);
      }
    }
  }
  
  @Override
  public void write(byte[] b) throws IOException {
    for (OutputStream stream : streams) {
      if (stream != null) {
        stream.write(b);
      }
    }
  }
  
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    for (OutputStream stream : streams) {
      if (stream != null) {
        stream.write(b, off, len);
      }
    }
  }
  
  @Override
  public void flush() throws IOException {
    for (OutputStream stream : streams) {
      if (stream != null) {
        stream.flush();
      }
    }
  }
  
  @Override
  public void close() throws IOException {
    for (OutputStream stream : streams) {
      if (stream != null) {
        stream.close();
      }
    }
  }
}
