package lazyman;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;

public class PopupListener extends MouseAdapter {
    JPopupMenu consolePM;
    
    public PopupListener(JPopupMenu pm) {
        consolePM = pm;
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            JMenuItem i = new JMenuItem("Copy");
            JMenuItem i2 = new JMenuItem("Copy for Reddit");
            consolePM.removeAll();
            if (!((JTextPane)evt.getComponent()).getText().isEmpty()) {
                i.addActionListener((ActionEvent e) -> {
                    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection selection = new StringSelection(((JTextPane)evt.getComponent()).getText());
                    c.setContents(selection, selection);
                });
                i2.addActionListener((ActionEvent ae) -> {
                    String output = "    " + ((JTextPane)evt.getComponent()).getText().replaceAll("\n", "\n    ");
                    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection selection = new StringSelection(output);
                    c.setContents(selection, selection);
                });
            } else {
                i.setEnabled(false);
                i2.setEnabled(false);
            }
            consolePM.add(i);
            consolePM.add(i2);
            consolePM.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
}