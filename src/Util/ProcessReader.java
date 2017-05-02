package Util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class ProcessReader {

    public static String getProcessOutput(Process p) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((p.isAlive()) || (stdInput.ready())) {
                if (stdInput.ready()) {
                    while (stdInput.ready()) {
                        sb.append(stdInput.readLine());
                    }
                }
            }
            return sb.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return "";
    }

    public static void putProcessOutput(Process p, JTextPane tp) {
        try {
            Document document = tp.getDocument();
            SimpleAttributeSet attributes = new SimpleAttributeSet();
            StyleConstants.setForeground(attributes, Color.YELLOW);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String message = "";

            while ((p.isAlive()) || (stdInput.ready())) {
                if (stdInput.ready()) {
                    try {
                        while (stdInput.ready()) {
                            int offset = document.getLength();
                            message = stdInput.readLine();
                            try {
                                if (message.contains("med.nhl") && message.contains("m3u8")) {
                                    message = message.substring(0, message.indexOf("http")) + "<censored>" + message.substring(message.indexOf("m3u8")+4, message.length());
                                }
                                if (message.contains("med.nhl") && message.contains(".ts")) {
                                    message = message.substring(0, message.indexOf("http")) + "<censored>" + message.substring(message.indexOf("ts")+2, message.length());
                                }

                                document.insertString(offset, message + "\n", attributes);
                            } catch (BadLocationException ex) {
                                ex.printStackTrace();
                            }
                            tp.setCaretPosition(document.getLength());
                        }
                    } catch (IOException ex) {
                        if (!ex.getMessage().contains("Stream closed")) {
                            ex.printStackTrace();
                        }
                    }
                }
                Thread.sleep(300);
            }
        } catch (IOException ex) {
            if (!ex.getMessage().contains("Stream closed")) {
                ex.printStackTrace();
            }
        } catch (NoSuchMethodError n) {
            String message = "Please uninstall all versions of Java and get the latest version.";
            MessageBox.show(message, "Error", 2);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
