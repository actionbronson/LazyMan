package Util;

import java.awt.Component;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

// For the first two columns of the NHL table
public class IconTextCellRemderer extends DefaultTableCellRenderer {

    private final Map<String, String> nhl = new java.util.TreeMap();

    @Override
    public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setTeams();
        if (value != null) {
            if (!value.equals("None")) {
                String abbr = value.toString().substring(0, 3);
                if (abbr != null) {
                    setText(abbr);
                } else {
                    setText(value.toString().substring(0, 3));
                }

                try {
                    setIcon(new ImageIcon(getClass().getResource("/Logos/" + value.toString().substring(3).replaceAll(" ", "").replaceAll("\\.", "").replaceAll("é", "e") + ".png")));
                } catch (Exception ex) {
                    setIcon(null);
                }
                setToolTipText(value.toString());
            } else {
                setText("None");
                setIcon(null);
            }
            
            setHorizontalAlignment(CENTER);
        }
        return this;
    }

    private void setTeams() {
        nhl.put("Ottawa Senators", "OTT");
        nhl.put("Philadelphia Flyers", "PHI");
        nhl.put("New York Rangers", "NYR");
        nhl.put("Washington Capitals", "WSH");
        nhl.put("Calgary Flames", "CGY");
        nhl.put("Winnipeg Jets", "WPG");
        nhl.put("San Jose Sharks", "SJS");
        nhl.put("Los Angeles Kings", "LAK");
        nhl.put("Minnesota Wild", "MIN");
        nhl.put("St. Louis Blues", "STL");
        nhl.put("Pittsburgh Penguins", "PIT");
        nhl.put("Buffalo Sabres", "BUF");
        nhl.put("Montréal Canadiens", "MTL");
        nhl.put("Toronto Maple Leafs", "TOR");
        nhl.put("New Jersey Devils", "NJD");
        nhl.put("Florida Panthers", "FLA");
        nhl.put("Columbus Blue Jackets", "CBJ");
        nhl.put("New York Islanders", "NYI");
        nhl.put("Detroit Red Wings", "DET");
        nhl.put("Carolina Hurricanes", "CAR");
        nhl.put("Boston Bruins", "BOS");
        nhl.put("Tampa Bay Lightning", "TBL");
        nhl.put("Nashville Predators", "NSH");
        nhl.put("Dallas Stars", "DAL");
        nhl.put("Chicago Blackhawks", "CHI");
        nhl.put("Colorado Avalanche", "COL");
        nhl.put("Anaheim Ducks", "ANA");
        nhl.put("Arizona Coyotes", "ARI");
        nhl.put("Edmonton Oilers", "EDM");
        nhl.put("Vancouver Canucks", "VAN");
    }
}
