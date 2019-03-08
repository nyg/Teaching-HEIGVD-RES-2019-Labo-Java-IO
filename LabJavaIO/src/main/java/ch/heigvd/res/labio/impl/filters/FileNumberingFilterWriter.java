package ch.heigvd.res.labio.impl.filters;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

/**
 * This class transforms the streams of character sent to the decorated writer.
 * When filter encounters a line separator, it sends it to the decorated writer.
 * It then sends the line number and a tab character, before resuming the write
 * process.
 *
 * Hello\n\World -> 1\tHello\n2\tWorld
 *
 * @author Olivier Liechti
 */
public class FileNumberingFilterWriter extends FilterWriter {

  private static final Logger LOG = Logger.getLogger(FileNumberingFilterWriter.class.getName());

  private int lineNumber = 1;
  private boolean shouldPrependLineNumber = true;
  private boolean shouldCheckForLineFeed = false;

  public FileNumberingFilterWriter(Writer out) {
    super(out);
  }

  @Override
  public void write(String str, int off, int len) throws IOException {
    for (char c : str.substring(off, off + len).toCharArray()) {
      write(c);
    }
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    for (int i = off; i < off + len; i++) {
      write(cbuf[i]);
    }
  }

  @Override
  public void write(int c) throws IOException {

    if (shouldPrependLineNumber) {
      shouldPrependLineNumber = false;
      super.write(getLineNumber());
    }

    // previous char was a CR
    if (shouldCheckForLineFeed) {

      shouldCheckForLineFeed = false;
      if (c == '\r') {
        shouldCheckForLineFeed = true;
      }

      // a LF follows, we write it followed by a line number
      if (c == '\n') {
        super.write(c);
        super.write(getLineNumber());
      }
      // another char follows, we write the line number before, then write the char
      else {
        super.write(getLineNumber());
        super.write(c);
      }

      // we're done
      return;
    }

    if (c == '\n') {
      super.write(c);
      super.write(getLineNumber());
      return;
    }
    if (c == '\r') {
      shouldCheckForLineFeed = true;
    }

    super.write(c);
  }

  private String getLineNumber() {
    return String.format("%d\t", lineNumber++);
  }
}
