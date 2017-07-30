
package Objects;

import Util.Encryption;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;


public class Web {

    public static String getContent(String url) throws UnknownHostException {

        String c = "";

        try (Scanner s = new Scanner(new URL(url).openStream(), "UTF-8")) {
            c = s.useDelimiter("\\A").next();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        catch (Exception x) {
            x.printStackTrace();
        }
        return c;
    }

    public static boolean testURL(String url) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            
            HttpURLConnection con
                    = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean testM3U8(String url) throws UnknownHostException{
        try {
            HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
            huc.setRequestMethod("HEAD");
            huc.setRequestProperty("User-Agent", "Mozilla/5.0 Gecko Firefox");//User-Agent, Mozilla/5.0 Gecko Firefox
            huc.setRequestProperty("Cookie", "mediaAuth="+"\""+ Encryption.getSaltString() +"\"");//Cookie, mediaAuth
            huc.connect();
            
            return huc.getResponseCode() == 200;
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
