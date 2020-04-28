package com.example.sharp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;

/**
 * This class provides following method for logs Tracer.In() - inform enter a
 * method Tracer.Out() - inform leave a method Tracer.D(...) - log message
 * Tracer.getScreenShot() - take screen shot
 *
 * log and snapshot will be record in $HOME/.local/Insyde/SupervyseIDE/log/
 */
public class Tracer {
    java.io.PrintWriter writer;
    static Tracer instance = new Tracer();
    String logDir = "";

    static String now(boolean filenameFmt) {
        return now(filenameFmt, false);
    }

    static String now(boolean filenameFmt, boolean detail) {
        Calendar calendar = Calendar.getInstance();
        if (!filenameFmt) {
            return String.format("%02d:%02d:%02d.%03d", calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE),
                                 calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND));
        }

        if (!detail) {
            return String.format("%04d_%02d_%02d_%02d", calendar.get(Calendar.YEAR), 1 + calendar.get(Calendar.MONTH),
                                 calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY));
        }

        return String.format("%04d_%02d_%02d_%02d_%02d_%02d_%03d", calendar.get(Calendar.YEAR),
                             1 + calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                             calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND),
                             calendar.get(Calendar.MILLISECOND));
    }

    Tracer() {
        String dataDir = "";
        String userhome = System.getProperty("user.home");
        String path = "log";
        File localData = new File(path);
        if (!localData.exists()) {
            localData.mkdirs();
        }
        dataDir = path;
        logDir = dataDir;
        String shortFileName = String.format("SupervyseIDE_%s.log", now(true));
        String fileName = dataDir + "/" + shortFileName;
        try {
            writer = new PrintWriter(new FileOutputStream(fileName, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void println(String msg, Object... args) {
        try {
            writer.printf(msg + "\n", args);
            writer.flush();
            System.err.printf(msg + "\n", args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static StackTraceElement getStackTrace() {
        return new Exception().getStackTrace()[2];
    }

    static Tracer getInstance() {
        return instance;
    }

    /**
     * Tracer.In records HH:mm:ss.SSS : Enter __FUNCTION__ (__FILE__
     * __LINE__)-Thread to log
     */
    public static void In() {
        StackTraceElement trace = getStackTrace();
        getInstance().println("%s : Enter %s (%s line %d)-Thread %d", now(false), trace.getMethodName(),
                              trace.getClassName(), trace.getLineNumber(), Thread.currentThread().getId());
    }

    /**
     * Tracer.Out records HH:mm:ss.SSS : Leave __FUNCTION__ (__FILE__
     * __LINE__)-Thread to log
     */
    public static void Out() {
        StackTraceElement trace = getStackTrace();
        getInstance().println("%s  : Leave %s (%s line %d)-Thread %d", now(false), trace.getMethodName(),
                              trace.getClassName(), trace.getLineNumber(), Thread.currentThread().getId());
    }

    /**
     * Tracer.D records HH:mm:ss.SSS : <Exception> __FUNCTION__ (__FILE__
     * __LINE__)-Thread Exception Body ... to log file
     */
    public static void D(Exception ee) {
        StackTraceElement trace = getStackTrace();
        String userMessage = "<Exception>";

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ee.printStackTrace(printWriter);

        getInstance().println("%s  : %s ...%s (%s line %d)-Thread %d\n%s", now(false), userMessage,
                              trace.getMethodName(), trace.getClassName(), trace.getLineNumber(), Thread.currentThread().getId(),
                              stringWriter.getBuffer().toString());
    }

    /**
     * Tracer.D records HH:mm:ss.SSS : Message __FUNCTION__ (__FILE__
     * __LINE__)-Thread to log file
     */
    public static void D(String msg, Object... args) {
        StackTraceElement trace = getStackTrace();
        String userMessage = String.format(msg, args);
        getInstance().println("%s  : %s ...%s (%s line %d)-Thread %d", now(false), userMessage, trace.getMethodName(),
                              trace.getClassName(), trace.getLineNumber(), Thread.currentThread().getId());
    }

}