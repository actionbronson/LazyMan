package GameObj;

import java.util.ArrayList;

public class GameStream {

    private final ArrayList<ArrayList<String>> feeds = new ArrayList<>();

    public void addFeed(String name, String id, String tv) {
        for (int i = 0; i < feeds.size(); i++) {
            if (feeds.get(i).contains(name)) {
                feeds.get(i).remove(1);
                feeds.get(i).add(1, id);
                return;
            }
        }
        ArrayList<String> feed = new ArrayList<>();
        feed.add(name);
        feed.add(id);
        feed.add(tv);
        feeds.add(feed);
    }

    public String getFeedName(int idx) {
        return feeds.get(idx).get(0);
    }

    public String getFeedID(int idx) throws java.lang.IndexOutOfBoundsException {
        return feeds.get(idx).get(1);
    }

    public String getFeedTV(int idx) {
        return feeds.get(idx).get(2);
    }

    public int getNumOfFeeds() {
        return feeds.size();
    }

    public boolean contains(String s) {
        for (int i = 0; i < feeds.size(); i++) {
            if (feeds.get(i).get(0).equals(s)) {
                return true;
            }
        }
        return false;
    }

    public int getFeedIndex(String feed) {
        for (int i = 0; i < feeds.size(); i++) {
            if (feeds.get(i).get(0).equals(feed)) {
                return i;
            }
        }
        return 0;
    }
}
