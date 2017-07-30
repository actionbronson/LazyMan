
package lazyman;

import GameObj.Game;
import Objects.Web;
import Util.MessageBox;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;


public class GetMLBInfo {
    public static Game[] getGames(String date) {
        try {
            JsonObject t = new JsonParser().parse(Web.getContent("https://statsapi.mlb.com/api/v1/schedule?sportId=1&date=" + date + "&hydrate=team,linescore,flags,liveLookin,person,stats,probablePitcher,game(content(summary,media(epg)),tickets)&language=en"))
                    .getAsJsonObject();

            if (t.get("totalItems").getAsInt() < 1) {
                return null;
            }

            int i = 0;

            Game[] g = new Game[t.getAsJsonArray("dates").get(0).getAsJsonObject().get("totalItems").getAsInt()];
            Game g1;

            for (JsonElement jo : t.getAsJsonArray("dates").get(0).getAsJsonObject().getAsJsonArray("games")) {
                g1 = new Game();
               
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
                    String period = jo.getAsJsonObject().getAsJsonObject("linescore").get("currentInningOrdinal").getAsString();
                    String time = jo.getAsJsonObject().getAsJsonObject("linescore").get("inningHalf").getAsString();
                    g1.setTimeRemaining(period + " " + time);
                } else if (g1.getGameState().equals("Final")) {
                    g1.setTimeRemaining("Final");
                } else if (g1.getGameState().equals("Postponed")) {
                    g1.setTimeRemaining("PPD");
                } else {
                    g1.setTimeRemaining("n/a");
                }
                
                if (jo.getAsJsonObject().getAsJsonObject("content").getAsJsonObject("media") != null) {
                    for (JsonElement stream : jo.getAsJsonObject().getAsJsonObject("content").getAsJsonObject("media").getAsJsonArray("epg")) {
                        if (stream.getAsJsonObject().get("title").getAsString().equals("MLBTV")) {
                            for (JsonElement innerStr : stream.getAsJsonObject().getAsJsonArray("items")) {
                                g1.addFeed(innerStr.getAsJsonObject().get("mediaFeedType").getAsString(), innerStr.getAsJsonObject().get("id").getAsString(), innerStr.getAsJsonObject().get("callLetters").getAsString());
                            }
                        }
                    }
                }
                g[i] = g1;
                i++;
            }
            Arrays.sort(g, (Game o1, Game o2) -> {
                if (o1.getGameState().equals(o2.getGameState()))
                    return 0;
                else if ((o1.getGameState().equals("In Progress") || o1.getGameState().equals("Delayed")) && !(o2.getGameState().equals("In Progress") || o2.getGameState().equals("Delayed")))
                    return -1;
                else if ((o1.getGameState().equals("Final") || o1.getGameState().equals("Postponed")) && !(o2.getGameState().equals("Final") || o2.getGameState().equals("Postponed")))
                    return 1;
                else if (o1.getGameState().equals("Pre-Game") && o2.getGameState().equals("Final"))
                    return -1;
                else if (o1.getGameState().equals("Warmup") && o2.getGameState().equals("Final"))
                    return -1;
                else if (o1.getGameState().equals("Pre-Game") && o2.getGameState().equals("Warmup"))
                    return 1;
                else if (o1.getGameState().equals("Warmup") && o2.getGameState().equals("Pre-Game"))
                    return -1;
                else if (o1.getGameState().equals("Scheduled") && o2.getGameState().equals("Final"))
                    return -1;
                else if (o1.getGameState().equals("Scheduled") && !o2.getGameState().equals("Scheduled"))
                    return 1;
                return 0;
            });
            return g;
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            Game[] e = new Game[1];
            e[0].setAwayTeam("err");
            return e;
        } catch (UnknownHostException ex) {
            MessageBox.show("Schedule site is down for MLB.", "Error", 2);
            return null;
        }
    }
}
