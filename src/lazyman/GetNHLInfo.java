package lazyman;

import GameObj.Game;
import Objects.Web;
import Util.MessageBox;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.net.UnknownHostException;

public class GetNHLInfo {

    public static Game[] getGames(String date) {
        try {
            JsonObject t = new JsonParser().parse(Web.getContent("https://statsapi.web.nhl.com/api/v1/schedule?startDate=" + date + "&endDate=" + date
                    + "&expand=schedule.teams,schedule.linescore,schedule.broadcasts.all,schedule.ticket,schedule.game.content.media.epg"))
                    .getAsJsonObject();

            if (t.get("totalItems").getAsInt() < 1) {
                return null;
            }

            int i = 0, iso;

            Game[] g = new Game[t.getAsJsonArray("dates").get(0).getAsJsonObject().get("totalItems").getAsInt()];
            Game g1;

            for (JsonElement jo : t.getAsJsonArray("dates").get(0).getAsJsonObject().getAsJsonArray("games")) {
                g1 = new Game();
                
                iso = 0;

                g1.setGameID(jo.getAsJsonObject().get("gamePk").getAsInt());
                g1.setTime(jo.getAsJsonObject().get("gameDate").getAsString().substring(11).replace("Z", ""));
                if (g1.getTime().equals("04:00:00")) {
                    g1.setTime("TBD");
                }
                g1.setDate(jo.getAsJsonObject().get("gameDate").getAsString().substring(0, 10));

                g1.setAwayTeamFull(jo.getAsJsonObject().getAsJsonObject("teams").getAsJsonObject("away").getAsJsonObject("team").get("name").getAsString());
                g1.setAwayTeam(jo.getAsJsonObject().getAsJsonObject("teams").getAsJsonObject("away").getAsJsonObject("team").get("abbreviation").getAsString());

                g1.setHomeTeamFull(jo.getAsJsonObject().getAsJsonObject("teams").getAsJsonObject("home").getAsJsonObject("team").get("name").getAsString());
                g1.setHomeTeam(jo.getAsJsonObject().getAsJsonObject("teams").getAsJsonObject("home").getAsJsonObject("team").get("abbreviation").getAsString());

                g1.setGameState(jo.getAsJsonObject().getAsJsonObject("status").get("detailedState").getAsString());

                if (g1.getGameState().contains("In Progress")) {
                    String period = jo.getAsJsonObject().getAsJsonObject("linescore").get("currentPeriodOrdinal").getAsString();
                    String time = jo.getAsJsonObject().getAsJsonObject("linescore").get("currentPeriodTimeRemaining").getAsString();
                    g1.setTimeRemaining(period + " " + time);
                } else if (g1.getGameState().equals("Final")) {
                    g1.setTimeRemaining("Final");
                } else {
                    g1.setTimeRemaining("n/a");
                }
                
                if (jo.getAsJsonObject().getAsJsonObject("content").getAsJsonObject("media") != null) {
                    for (JsonElement stream : jo.getAsJsonObject().getAsJsonObject("content").getAsJsonObject("media").getAsJsonArray("epg")) {
                        if (stream.getAsJsonObject().get("title").getAsString().equals("NHLTV")) {
                            for (JsonElement innerStr : stream.getAsJsonObject().getAsJsonArray("items")) {

                                OUTER:
                                switch (innerStr.getAsJsonObject().get("mediaFeedType").getAsString()) {
                                    case "HOME":
                                        g1.setHomeTVStation(innerStr.getAsJsonObject().get("callLetters").getAsString());
                                        g1.setHomeMediaID(innerStr.getAsJsonObject().get("mediaPlaybackId").getAsString());
                                        break;
                                    case "NATIONAL":
                                        g1.setNationalTVStation(innerStr.getAsJsonObject().get("callLetters").getAsString());
                                        g1.setNationalMediaID(innerStr.getAsJsonObject().get("mediaPlaybackId").getAsString());
                                        break;
                                    case "AWAY":
                                        g1.setAwayTVStation(innerStr.getAsJsonObject().get("callLetters").getAsString());
                                        g1.setAwayMediaID(innerStr.getAsJsonObject().get("mediaPlaybackId").getAsString());
                                        break;
                                    case "FRENCH":
                                        g1.setFrenchTVStation(innerStr.getAsJsonObject().get("callLetters").getAsString());
                                        g1.setFrenchMediaID(innerStr.getAsJsonObject().get("mediaPlaybackId").getAsString());
                                        break;
                                    case "COMPOSITE":
                                        switch (innerStr.getAsJsonObject().get("feedName").getAsString()) {
                                            case "Multi-Cam 1":
                                                g1.setThreeCamMID(innerStr.getAsJsonObject().get("mediaPlaybackId").getAsString());
                                                break OUTER;
                                            case "Multi-Cam 2":
                                                g1.setSixCamMID(innerStr.getAsJsonObject().get("mediaPlaybackId").getAsString());
                                                break OUTER;
                                            default:
                                                break OUTER;
                                        }
                                    case "ISO":
                                        g1.setIsoStream(innerStr.getAsJsonObject().get("mediaPlaybackId").getAsString(), iso);
                                        String name = innerStr.getAsJsonObject().get("feedName").getAsString();
                                        if (name.contains("Star Cam"))
                                            name = "Star Cam";
                                        g1.setIsoStreamNames(name, iso);
                                        iso++;
                                        break;
                                    default:
                                }
                            }
                        }
                    }
                }
                g[i] = g1;
                i++;
            }
            return g;
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            Game[] e = new Game[1];
            e[0].setAwayTeam("err");
            return e;
        } catch (UnknownHostException ex) {
            MessageBox.show("Schedule site is down.", "Error", 2);
            return null;
        }
    }
}
