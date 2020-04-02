/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 ROMVoid
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package net.romvoid.crashbot.logging;

import java.util.Arrays;

/**
 * The Class Logger.
 * 
 * @implNote NOT USED YET
 */
public class Logger {
    
    /** The Constant LOG_PRINT_STACK_SOURCE. */
    private final static boolean LOG_PRINT_STACK_SOURCE = false;
    
    /** The Constant minLogLevel. */
    private final static LogLevel minLogLevel = LogLevel.DEBUG;

    /**
     * Debug.
     *
     * @param message the message
     */
    public static void debug(Object... message) {
        print(LogLevel.DEBUG, message);
    }

    /**
     * Debug formatted.
     *
     * @param msg the msg
     * @param params the params
     */
    public static void debugFormatted(String msg, Object... params) {
        print(LogLevel.DEBUG, String.format(msg, params));
    }

    /**
     * Info.
     *
     * @param message the message
     */
    public static void info(Object... message) {
        print(LogLevel.INFO, message);
    }

    /**
     * Info formatted.
     *
     * @param msg the msg
     * @param params the params
     */
    public static void infoFormatted(String msg, Object... params) {
        print(LogLevel.INFO, String.format(msg, params));
    }

    /**
     * Warn.
     *
     * @param message the message
     */
    public static void warn(Object... message) {
        print(LogLevel.WARN, message);
    }

    /**
     * Warn formatted.
     *
     * @param msg the msg
     * @param params the params
     */
    public static void warnFormatted(String msg, Object... params) {
        print(LogLevel.WARN, String.format(msg, params));
    }

    /**
     * Fatal.
     *
     * @param message the message
     */
    public static void fatal(Object... message) {
        print(LogLevel.FATAL, message);
    }

    /**
     * Fatal formatted.
     *
     * @param msg the msg
     * @param params the params
     */
    public static void fatalFormatted(String msg, Object... params) {
        print(LogLevel.FATAL, String.format(msg, params));
    }

    /**
     * Fatal.
     *
     * @param e the e
     */
    public static void fatal(Throwable e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
    }

    /**
     * Fatal.
     *
     * @param e the e
     * @param message the message
     */
    public static void fatal(Throwable e, Object... message) {
        print(LogLevel.FATAL, message);
    }

    /**
     * Prints the.
     *
     * @param logLevel the log level
     * @param message the message
     */
    private static void print(LogLevel logLevel, Object... message) {
        if (logLevel.ordinal() >= minLogLevel.ordinal()) {
            System.out.println(String.format("%5s: %s", logLevel, Arrays.toString(message)));
            if (LOG_PRINT_STACK_SOURCE) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 3; i < 6 && i < stack.length; i++) {
                    System.out.println((String.format("%40s:\t\t%s", logLevel, (stack[i].toString()))));
                }
            }
        }
    }
}
