package com.anxpp.tinyim.client.sdk.utils;

import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.time.LocalTime;

public class Log {
    private static final int INFO = 1;
    private static final int PROMPT = 2;
    private static final int DEBUG = 3;
    private static final int WARN = 4;
    private static final int ERROR = 5;
    private static final int FETAL = 6;
    private static final int DEST_TEXT_COM = 7;
    private static final int DEST_RICH_TEXT_COM = 8;
    private static final int DEST_CONSOLE = 9;
    private static Log instance = null;
    private int logDestType = -1;
    private int logLevel = -1;
    private JTextPane richTextDest;
    private JTextArea textDest;

    public Log(Object dest, int logLevel) {
        this.logLevel = logLevel;
        setLogDest(dest);
    }

    public static Log getInstance() {
        if (instance == null)
            instance = new Log(null, -1);
        return instance;
    }

    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }

    public static void i(String tag, String msg, Exception ex) {
        getInstance().log(msg, 1, ex);
    }

    public static void p(String tag, String msg) {
        p(tag, msg, null);
    }

    public static void p(String tag, String msg, Exception ex) {
        getInstance().log(msg, 2, ex);
    }

    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    public static void d(String tag, String msg, Exception ex) {
        getInstance().log(msg, 3, ex);
    }

    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    public static void w(String tag, String msg, Exception ex) {
        getInstance().log(msg, 4, ex);
    }

    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    public static void e(String tag, String msg, Exception ex) {
        getInstance().log(msg, 5, ex);
    }

    public static void f(String tag, String msg) {
        f(tag, msg, null);
    }

    public static void f(String tag, String msg, Exception ex) {
        getInstance().log(msg, 6, ex);
    }

    public static void append(final Color c, final String s, final JTextPane p) {
        // 以下代码因会从独立的Thread中调用，所以需要借用SwingUtilities.invokeLater()，
        // 否则会报错：”Exception in thread "Thread-22" java.lang.Error: Interrupted attempt to aquire write lock“
        Runnable runnable = () -> {
            try {
                MutableAttributeSet sa = new SimpleAttributeSet();
                StyleConstants.setForeground(sa, c);

                int len = p.getDocument().getLength();
                p.getDocument().insertString(len, s, sa);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public Log setLogDest(Object dest) {
        if (dest == null) {
            this.logDestType = 9;
        } else if ((dest instanceof JTextPane)) {
            this.logDestType = 8;
            this.richTextDest = ((JTextPane) dest);
        } else if ((dest instanceof JTextArea)) {
            this.logDestType = 7;
            this.textDest = ((JTextArea) dest);
        } else {
            System.out.println("不支持的日志输出目的地!");
        }

        return this;
    }

    public int getLogLevel() {
        return this.logLevel;
    }

    public Log setLogLevel(int logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public void log(String msg, int level) {
        log(msg, level, null);
    }

    public void log(String msg, int level, Exception ex) {
        String lv = "";
        Color fc = Color.black;
        switch (level) {
            case INFO:
                fc = new Color(153, 204, 0);
                lv = "INFO";
                break;
            case PROMPT:
                fc = new Color(0, 255, 0);
                lv = "PROMPT";
                break;
            case DEBUG:
                fc = new Color(255, 204, 153);
                lv = "DEBUG";
                break;
            case WARN:
                fc = Color.pink;
                lv = "WARN";
                break;
            case ERROR:
                fc = Color.red;
                lv = "ERROR";
                break;
            case FETAL:
                fc = Color.red;
                lv = "FETAL";
                break;
        }

        if (level > logLevel) {
            String txt = " " + lv + " - " + msg + (ex == null ? "" : "(" + ex.getMessage() + ")") + " [" + (LocalTime.now()) + "]\r\n";
            if (logDestType == DEST_RICH_TEXT_COM) {
                try {
                    Log.append(fc, txt, richTextDest);
                    richTextDest.setCaretPosition(richTextDest.getDocument().getLength());
                } catch (Exception e) {
                    //					e.printStackTrace();
                }
            } else if (logDestType == DEST_TEXT_COM)
                textDest.append(txt);
            else if (logDestType == DEST_CONSOLE)
                System.out.print(txt);
            else {
                //未知日志目的地类型
            }

            if (ex != null)
                ex.printStackTrace();
        }
    }
}