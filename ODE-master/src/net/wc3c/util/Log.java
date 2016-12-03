package net.wc3c.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Log {
    private static final Logger logger;
    
    static {
        logger = LogManager.getLogger();
    }
    
    public static final void debug(final String msg, final Object... params) {
        logger.debug(msg, params);
    }
    
    public static final void info(final String msg, final Object... params) {
        logger.info(msg, params);
    }
    
    public static final void trace(final String msg, final Object... params) {
        logger.trace(msg, params);
    }
    
    public static final void warn(final String msg, final Object... params) {
        logger.warn(msg, params);
    }
    
    public static final void error(final String msg, final Object... params) {
        logger.error(msg, params);
    }
    
    public static final void fatal(final String msg, final Object... params) {
        logger.fatal(msg, params);
    }
    
    public static final void exception(final Throwable t) {
        logger.catching(t);
    }
    
    public static final void entry(final Object... params) {
        logger.entry(params);
    }
    
    public static final <R> R exit(final R returnValue) {
        return logger.exit(returnValue);
    }
    
    public static final void exit() {
        logger.exit();
    }
}
