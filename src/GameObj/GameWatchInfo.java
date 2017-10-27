package GameObj;

import Objects.Time;
import Objects.Web;
import java.net.UnknownHostException;

public class GameWatchInfo {
    
    private String cdn, quality, url, date;
    
    public GameWatchInfo(String cdn, String quality, String date, String mediaID) {
        this.cdn = cdn;
        this.quality = quality;
        try {
            url = Web.getContent("http://freegamez.gq/m3u8/" + date + "/" + mediaID + cdn);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
    
    public GameWatchInfo() {
        
    }

    /**
     * @return the cdn
     */
    public String getCdn() {
        return cdn;
    }

    /**
     * @param cdn the cdn to set
     */
    public void setCdn(String cdn) {
        this.cdn = cdn;
    }

    /**
     * @return the quality
     */
    public String getQuality() {
        return quality;
    }

    /**
     * @param quality the quality to set
     */
    public void setQuality(String quality) {
        this.quality = quality;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String mediaID, String league) {
        if (league.equals("NHL")) {
            if (!Time.isToday(date)) {
                if (Web.testURL("http://nhl.freegamez.gq/m3u8/" + date + "/" + mediaID + "akc")) {
                    try {
                        url = Web.getContent("http://nhl.freegamez.gq/m3u8/" + date + "/" + mediaID + "akc");
                    } catch (UnknownHostException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        url = Web.getContent("http://nhl.freegamez.gq/m3u8/" + date + "/" + mediaID);
                    } catch (UnknownHostException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    url = Web.getContent("http://nhl.freegamez.gq/m3u8/" + date + "/" + mediaID + cdn);
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            if (!Time.isToday(date)) {
                if (Web.testURL("http://nhl.freegamez.gq/mlb/m3u8/" + date + "/" + mediaID + "akc")) {
                    try {
                        url = Web.getContent("http://nhl.freegamez.gq/mlb/m3u8/" + date + "/" + mediaID + "akc");
                    } catch (UnknownHostException ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    url = Web.getContent("http://nhl.freegamez.gq/mlb/m3u8/" + date + "/" + mediaID + cdn);
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
            }
            
        }
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
}
