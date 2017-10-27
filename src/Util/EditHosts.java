package Util;

import GameObj.League;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class EditHosts {

    private String ip = getIP();
    
    private boolean wrongIP = false, ipNotFound = false;

    public boolean hostsFileEdited(String url) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }
        if (ip == null) {
            return false;
        }

        Scanner s = null;
        boolean edited = false;
        try {
            if (InetAddress.getByName(new URL("http://" + url).getHost()).getHostAddress().equals(ip)) {
                return true;
            } else {
                File hosts;

                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    hosts = new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts");
                } else {
                    hosts = new File("/etc/hosts");
                }

                s = new Scanner(hosts);
                while (s.hasNext()) {
                    String line = s.nextLine();
                    if (line.startsWith(ip) && line.contains(url)) {
                        edited = true;
                        break;
                    } else if (line.contains(url)) {
                        wrongIP = true;
                        break;
                    }
                }
            }
        } catch (FileNotFoundException | UnknownHostException | MalformedURLException ex) {
            ex.printStackTrace();
        } finally {
            if (s != null) {
                s.close();
            }
        }
        return edited;
    }

    public boolean editHosts(String url) {
        if (ipNotFound) {
            return false;
        }

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return editWindowsHosts(url);
        } else {
            return editUnixHosts(url);
        }
    }

    private boolean editUnixHosts(String url) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        String p = "echo \'" + Props.getPW() + "\' | sudo -S ", line = "\n" + ip + " " + url;
        Process e;
        try {
            e = new ProcessBuilder(new String[]{"/bin/sh", "-c", p + "-- sh -c \"echo \'" + line + "\' >> /etc/hosts\""}).start();
            e.waitFor();
            return hostsFileEdited(url);
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();

            return false;
        }
    }

    private boolean editWindowsHosts(String url) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        try {
            String line = "\r\n" + ip + " " + url;
            File f = new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts");
            if (!f.exists()) {
                f.createNewFile();
            }

            Files.write(Paths.get(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts"), line.getBytes(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean modifyHosts(String url) {
        if (ipNotFound) {
            return false;
        }

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return modifyWindowsHosts(url);
        } else {
            return modifyUnixHosts(url);
        }
    }

    public boolean clearHosts(League[] lg) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return clearWindowsHosts(lg);
        } else {
            return clearUnixHosts(lg);
        }
    }

    private boolean modifyUnixHosts(String url) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        String p = "echo \'" + Props.getPW() + "\' | sudo -S ";
        Process m;
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                m = new ProcessBuilder("/bin/sh", "-c", p + "sed -E -i '' \"s/^ *[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+( +" + url + ")/" + ip + "\\1/\" /etc/hosts").start();
            } else {
                m = new ProcessBuilder("/bin/sh", "-c", p + "sed -r -i \"s/^ *[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+( +" + url + ")/" + ip + "\\1/\" /etc/hosts").start();
            }
            m.waitFor();
            return hostsFileEdited(url);

        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();

            return false;
        }
    }

    private boolean modifyWindowsHosts(String url) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        FileReader fr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        boolean modified = false;
        try {
            fr = new FileReader(new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts"));
            String s;
            StringBuilder totalStr = new StringBuilder();
            br = new BufferedReader(fr);
            while ((s = br.readLine()) != null) {
                if (s.contains(url)) {
                    s = ip + " " + url;
                }
                totalStr.append(s).append("\r\n");
            }
            fw = new FileWriter(new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts"));
            fw.write(totalStr.toString().trim());
            modified = true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return modified;
    }

    private boolean clearUnixHosts(League[] lg) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        String p = "echo \'" + Props.getPW() + "\' | sudo -S ";
        Process m;
        try {
            for (League l : lg) {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    m = new ProcessBuilder("/bin/sh", "-c", p + "sed -E -i '' '/" + l.getKeyURL() + "/d' /etc/hosts").start();
                } else {
                    m = new ProcessBuilder("/bin/sh", "-c", p + "sed -i '/" + l.getKeyURL() + "/d' /etc/hosts").start();
                }
                m.waitFor();
            }
            return true;
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();

            return false;
        }
    }

    private boolean clearWindowsHosts(League[] lg) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        FileWriter fw = null;
        boolean cleared = false;
        try {
            String s;
            StringBuilder totalStr = new StringBuilder();
            Scanner sc = new Scanner(new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts"));
            outer:
            while (sc.hasNextLine()) {
                s = sc.nextLine();
                for (League l : lg) {
                    if (s.contains(l.getKeyURL())) {
                        continue outer;
                    }
                }
                if (sc.hasNextLine()) {
                    totalStr.append(s).append("\r\n");
                }
            }
            fw = new FileWriter(new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts"));
            fw.write(totalStr.toString().trim());
            cleared = true;
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {

            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return cleared;
    }

    public boolean isWrongIP() {
        return wrongIP;
    }

    private String getIP() {
        try {
            return InetAddress.getByName(new URL("http://nhl.freegamez.gq").getHost()).getHostAddress();
        } catch (UnknownHostException ex) {ex.printStackTrace();
            MessageBox.show("It seems the server is down or blocked by a firewall.", "Error", 2);
            ipNotFound = true;
        } catch (MalformedURLException ex) {
            MessageBox.show("If you see this message, the programmer sucks!.", "Error", 2);
            ipNotFound = true;
        }
        return null;
    }

    public boolean isIpNotFound() {
        return ipNotFound;
    }
}
