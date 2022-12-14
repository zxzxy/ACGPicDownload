package xyz.zcraft.acgpicdownload.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private final String name;
    private final Logger parentLogger;

    private final PrintStream[] out;

    public Logger(String name, Logger parentLogger, PrintStream... out) {
        this.name = name;
        this.parentLogger = parentLogger;
        this.out = out;
    }

    public Logger(String name, PrintStream... out) {
        this.name = name;
        this.out = out;
        parentLogger = null;
    }

    public String getName() {
        if (parentLogger != null) {
            return parentLogger.getName() + "|" + name;
        }
        return name;
    }

    public Logger getParentLogger() {
        return parentLogger;
    }

    public String getOutputName() {
        return "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "][" + getName() + "] ";
    }

    public void info(String message) {
        for (PrintStream t : out) {
            t.println(getOutputName() + message);
            t.flush();
        }
    }

    public void printr(String str) {
        for (PrintStream t : out) {
            t.print("\r");
            t.print(getOutputName());
            t.print(str);
            t.flush();
        }
    }

    public void err(String message) {
        System.err.println(getOutputName() + message);
    }

    public void printf(String format, String... arg) {
        for (PrintStream t : out) {
            t.print(getOutputName());
            t.printf(format, (Object[]) arg);
            t.flush();
        }
    }

    public void printlnf(String format, String... arg) {
        printf(format, arg);
        for (PrintStream t : out) {
            t.println();
            t.flush();
        }
    }
}
