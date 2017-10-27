package Util;

import java.awt.Component;
import javax.swing.BorderFactory;
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
            try {
                setIcon(new ImageIcon(getClass().getResource("/Logos/" + value.toString().replaceAll(" ", "").replaceAll("\\.", "").replaceAll("é", "e") + ".png")));
                setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
                setHorizontalAlignment(LEFT);
            } catch (Exception ex) {
                setHorizontalAlignment(CENTER);
                setIcon(null);
            }
        } else {
            setHorizontalAlignment(CENTER);
            setText("None");
            setIcon(null);
        }

        return this;
    }

}
