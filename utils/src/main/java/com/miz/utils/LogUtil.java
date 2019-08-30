package com.miz.utils;


import org.apache.log4j.Logger;

public class LogUtil {
    private static Logger logger = Logger.getLogger("xini");

    public static void debug(String s) {
        logger.debug(s);
    }

    public static void info(String s) {
        logger.info(s);
    }

    public static void warn(String s) {
        logger.warn(s);
    }

    public static void error(String s) {
        logger.error(s);
    }

    public static void error(String s, Throwable e) {
        logger.error(s, e);
    }

}
