package Util;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

// For the first two columns of the NHL and MLB tables
public class IconTextCellRemderer extends DefaultTableCellRenderer {

    
    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value != null) {
            int idx = value.toString().indexOf('-');
            if (!value.equals("None")) {
                String abbr = value.toString().substring(0, idx);
                if (abbr != null) {
                    setText(abbr);
                } else {
                    setText(value.toString().substring(0, idx));
                }

                try {
                    setIcon(new ImageIcon(getClass().getResource("/Logos/" + value.toString().substring(idx+1).replaceAll(" ", "").replaceAll("\\.", "").replaceAll("é", "e") + ".png")));
                } catch (Exception ex) {
                    setIcon(null);
                }
                setToolTipText(value.toString().substring(idx+1));
            } else {
                setText("None");
                setIcon(null);
            }
            
            setHorizontalAlignment(CENTER);
        }
        return this;
    }

}
