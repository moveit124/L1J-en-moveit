package l1j.server;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;

/**
 * A simple file handler that allows separate configuration for detailed FINEST-level
 * logging via log.properties. Reads its configuration (pattern, limit, count,
 * formatter, level) from the LogManager, defaulting to the "l1j.server.DebugTraceHandler"
 * namespace.
 */
public class DebugTraceHandler extends FileHandler {

    /**
     * Default constructor. Reads configuration from LogManager.
     * @throws IOException if an I/O error occurs.
     * @throws SecurityException if a security manager exists and doesn't allow the operation.
     */
    public DebugTraceHandler() throws IOException, SecurityException {
        super(); // The super constructor reads properties from LogManager
    }

    // Optionally, you could add constructors that allow programmatic configuration,
    // but for use with log.properties, the default constructor is sufficient.
    // e.g.:
    // public DebugTraceHandler(String pattern) throws IOException, SecurityException {
    //     super(pattern);
    // }
    // public DebugTraceHandler(String pattern, boolean append) throws IOException, SecurityException {
    //     super(pattern, append);
    // }
    // public DebugTraceHandler(String pattern, int limit, int count) throws IOException, SecurityException {
    //     super(pattern, limit, count);
    // }
    // public DebugTraceHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
    //     super(pattern, limit, count, append);
    // }
} 