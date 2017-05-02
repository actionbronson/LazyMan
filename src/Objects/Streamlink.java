package Objects;

import GameObj.Game;
import GameObj.GameWatchInfo;
import Util.Props;
import Util.Encryption;
import Util.MessageBox;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Streamlink {

    private String location = "";
    public boolean record = false;

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    public Process run(Game g, GameWatchInfo gwi) {
        if (gwi.getUrl().equals("")) {
            MessageBox.show("Could not get the m3u8 URL. The server may be down.", "Error", 2);
            return null;
        }
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, Like Gecko) Chrome/48.0.2564.82 Safari/537.36 Edge/14.14316";
        ProcessBuilder pb;

        List<String> args = new ArrayList<>(Arrays.asList(new String[]{location, getURLFormat(gwi.getUrl(), gwi.getQuality().equals("best")), gwi.getQuality(),
            "--http-header", "\"User-Agent=" + ua + "\"", "--http-cookie=mediaAuth=" + Encryption.getSaltString()}));

        if (record) {
            String saveLoc = Props.getSaveStreamLoc() + File.separator + gwi.getDate();
            File saveDir = new File(saveLoc);
            if (!saveDir.isDirectory()) {
                saveDir.mkdir();
            }

            String o = saveLoc + File.separator + g.getAwayTeam() + "_" + g.getHomeTeam() + ".mp4";

            File f = new File(o);
            if (f.exists()) {
                if (MessageBox.ask("A recording of this game already exists. Overwrite?", "Overwrite?") == MessageBox.yesOption()) {
                    f.delete();
                } else {
                    return null;
                }
            }
            args.add("-o");
            args.add(o);
        } else {
            args.add("--player");
            if (!Props.getVlcloc().toLowerCase().contains("mpv")) {
                String arg = "";

                if (!Props.getMediaPlayerrArgs().equals("")) {
                    arg = " " + Props.getMediaPlayerrArgs().replaceAll("\\{homeAbbr\\}", g.getHomeTeam()).replaceAll("\\{awayAbbr\\}", g.getAwayTeam()).replaceAll("\\{homeFull\\}", g.getHomeTeamFull()).replaceAll("\\{awayFull\\}", g.getAwayTeamFull());
                }
                if (!System.getProperty("os.name").toLowerCase().contains("win"))
                    args.add(Props.getVlcloc() + arg);
                else
                    args.add("\"" + Props.getVlcloc() + arg + "\"");
            } else {
                args.add(Props.getVlcloc() + " --http-header-fields='Cookie: mediaAuth=" + Encryption.getSaltString() + "' --user-agent='" + ua + "' " + Props.getMediaPlayerrArgs().replaceAll("\\{homeAbbr\\}", g.getHomeTeam()).replaceAll("\\{awayAbbr\\}", g.getAwayTeam()).replaceAll("\\{homeFull\\}", g.getHomeTeamFull()).replaceAll("\\{awayFull\\}", g.getAwayTeamFull()));
                args.add("--player-passthrough");
                args.add("hls");
            }
        }

        if (!Props.getStreamLinkArgs().equals("")) {
            args.addAll(Arrays.asList(Props.getStreamlinkArgsArray()));
        }

        pb = new ProcessBuilder(args).redirectErrorStream(true);

        try {
            return pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String getURLFormat(String url, boolean best) {
        if (System.getProperty("os.name").contains("Win")) {
            if (best) {
                return "\"hlsvariant://" + url + " name_key=bitrate verify=False\"";
            }
            return "\"hlsvariant://" + url + " verify=False\"";
        }

        if (best) {
            return "hlsvariant://" + url + " name_key=bitrate verify=False";
        }
        return "hlsvariant://" + url + " verify=False";
    }
}
