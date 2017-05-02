
package Util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class OpenURL {
    
    public static void open(String url) {
        try {
            URI uri = new URL(url).toURI();
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (MalformedURLException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }
}
