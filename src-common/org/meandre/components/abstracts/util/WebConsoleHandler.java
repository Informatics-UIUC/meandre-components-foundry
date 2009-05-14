/**
 *
 */
package org.meandre.components.abstracts.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Boris Capitanu
 *
 */
public class WebConsoleHandler extends Handler {

    private final OutputStream _outputStream;

    public WebConsoleHandler(OutputStream outputStream) {
        this(outputStream, null);
    }

    public WebConsoleHandler(OutputStream outputStream, Formatter formatter) {
        _outputStream = outputStream;

        if (formatter != null)
            setFormatter(formatter);
    }

    @Override
    public synchronized void publish(LogRecord record) {
        if (!isLoggable(record)) return;

        String formattedRecord = getFormatter().format(record);
        try {
            _outputStream.write(formattedRecord.getBytes());
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws SecurityException {
       try {
           _outputStream.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    @Override
    public void flush() {
        try {
            _outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
