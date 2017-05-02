
package Util;

import javax.swing.JOptionPane;


public class MessageBox {
    
    public static void show(String msg, String title, int type) {
        int t;
        
        switch (type) {
            case 2:
                t = JOptionPane.ERROR_MESSAGE;
                break;
            default:
                t = JOptionPane.INFORMATION_MESSAGE;
        }
        
        JOptionPane.showMessageDialog(null, msg, title, t);
    }
    
    public static int ask(String question, String title) {
        return JOptionPane.showConfirmDialog(null, question, title, JOptionPane.YES_NO_OPTION);
    }
    
    public static int yesOption() {
        return JOptionPane.YES_OPTION;
    }
}
