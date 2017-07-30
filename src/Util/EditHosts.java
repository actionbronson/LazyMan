package Util;

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
    private final String NHLHost = "mf.svc.nhl.com", MLBHost = "mlb-ws-mf.media.mlb.com";
    private boolean NHLWrongIP = false, NHLIPNotFound = false, MLBWrongIP = false, MLBIPNotFound = false;

    public boolean hostsFileEdited(String league) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }
        if (ip == null) {
            return false;
        }

        Scanner s = null;
        boolean edited = false;
        try {
            if (league.equals("NHL")) {
                if (InetAddress.getByName(new URL("http://" + NHLHost).getHost()).getHostAddress().equals(ip)) {
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
                        if (line.startsWith(ip) && line.contains(NHLHost)) {
                            edited = true;
                            break;
                        } else if (line.contains(NHLHost)) {
                            NHLWrongIP = true;
                            break;
                        }
                    }
                }
            } else if (InetAddress.getByName(new URL("http://" + MLBHost).getHost()).getHostAddress().equals(ip)) {
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
                    if (line.startsWith(ip) && line.contains(MLBHost)) {
                        edited = true;
                        break;
                    } else if (line.contains(MLBHost)) {
                        MLBWrongIP = true;
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

    public boolean editHosts(String league) {
        if (league.equals("NHL")) {
            if (NHLIPNotFound) {
                return false;
            }
        } else if (MLBIPNotFound) {
            return false;
        }

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return editWindowsHosts(league);
        } else {
            return editUnixHosts(league);
        }
    }

    private boolean editUnixHosts(String league) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        if (league.equals("NHL")) {
            String p = "echo \'" + Props.getPW() + "\' | sudo -S ", line = "\n" + ip + " " + NHLHost;
            Process e;
            try {
                e = new ProcessBuilder(new String[]{"/bin/sh", "-c", p + "-- sh -c \"echo \'" + line + "\' >> /etc/hosts\""}).start();
                e.waitFor();
                return hostsFileEdited(league);
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();

                return false;
            }
        } else {
            String p = "echo \'" + Props.getPW() + "\' | sudo -S ", line = "\n" + ip + " " + MLBHost;
            Process e;
            try {
                e = new ProcessBuilder(new String[]{"/bin/sh", "-c", p + "-- sh -c \"echo \'" + line + "\' >> /etc/hosts\""}).start();
                e.waitFor();
                return hostsFileEdited(league);
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();

                return false;
            }
        }
    }

    private boolean editWindowsHosts(String league) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        if (league.equals("NHL")) {
            try {
                String line = "\r\n" + ip + " " + NHLHost;
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
        } else {
            try {
                String line = "\r\n" + ip + " " + MLBHost;
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
    }

    public boolean modifyHosts(String league) {
        if (league.equals("NHL")) {
            if (NHLIPNotFound) {
                return false;
            }
        } else if (MLBIPNotFound) {
            return false;
        }

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return modifyWindowsHosts(league);
        } else {
            return modifyUnixHosts(league);
        }
    }

    public boolean clearHosts() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return clearWindowsHosts();
        } else {
            return clearUnixHosts();
        }
    }

    private boolean modifyUnixHosts(String league) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        String p = "echo \'" + Props.getPW() + "\' | sudo -S ";
        Process m;
        if (league.equals("NHL")) {
            try {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    m = new ProcessBuilder("/bin/sh", "-c", p + "sed -E -i '' \"s/^ *[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+( +" + NHLHost + ")/" + ip + "\\1/\" /etc/hosts").start();
                } else {
                    m = new ProcessBuilder("/bin/sh", "-c", p + "sed -r -i \"s/^ *[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+( +" + NHLHost + ")/" + ip + "\\1/\" /etc/hosts").start();
                }
                m.waitFor();
                return hostsFileEdited(league);

            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();

                return false;
            }
        } else {
            try {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    m = new ProcessBuilder("/bin/sh", "-c", p + "sed -E -i '' \"s/^ *[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+( +" + MLBHost + ")/" + ip + "\\1/\" /etc/hosts").start();
                } else {
                    m = new ProcessBuilder("/bin/sh", "-c", p + "sed -r -i \"s/^ *[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+( +" + NHLHost + ")/" + ip + "\\1/\" /etc/hosts").start();
                }
                m.waitFor();
                return hostsFileEdited(league);

            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();

                return false;
            }
        }
    }

    private boolean modifyWindowsHosts(String league) {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        FileReader fr = null;
        BufferedReader br = null;
        FileWriter fw = null;
        boolean modified = false;
        String Host;
        if (league.equals("NHL")) {
            Host = NHLHost;
        } else {
            Host = MLBHost;
        }
        try {
            fr = new FileReader(new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts"));
            String s;
            StringBuilder totalStr = new StringBuilder();
            br = new BufferedReader(fr);
            while ((s = br.readLine()) != null) {
                if (s.contains(Host)) {
                    s = ip + " " + Host;
                }
                if (!s.contains("146.185.131.14")) {
                    totalStr.append(s).append("\r\n");
                }
            }
            fw = new FileWriter(new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts"));
            fw.write(totalStr.toString());
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

    private boolean clearUnixHosts() {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        String p = "echo \'" + Props.getPW() + "\' | sudo -S ";
        Process m;
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                m = new ProcessBuilder("/bin/sh", "-c", p + "sed -E -i '' '/" + NHLHost + "/d' /etc/hosts").start();
            } else {
                m = new ProcessBuilder("/bin/sh", "-c", p + "sed -i '/" + NHLHost + "/d' /etc/hosts").start();
            }
            m.waitFor();
            return true;
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();

            return false;
        }
    }

    private boolean clearWindowsHosts() {
        if (!Props.getIP().equals("")) {
            ip = Props.getIP();
        }

        FileWriter fw = null;
        boolean cleared = false;
        try {
            String s;
            StringBuilder totalStr = new StringBuilder();
            Scanner sc = new Scanner(new File(System.getenv("WINDIR") + "\\system32\\drivers\\etc\\hosts"));
            while (sc.hasNextLine()) {
                s = sc.nextLine();
                if (s.contains(NHLHost) || s.contains(MLBHost)) {
                    continue;
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

    public boolean isWrongIP(String league) {
        if (league.equals("NHL")) {
            return NHLWrongIP;
        } else {
            return MLBWrongIP;
        }
    }

    private String getIP() {
        try {
            return InetAddress.getByName(new URL("http://nhl.freegamez.gq").getHost()).getHostAddress();
        } catch (UnknownHostException ex) {
            MessageBox.show("It seems the server is down or blocked by a firewall.", "Error", 2);
            NHLIPNotFound = true;
            MLBIPNotFound = true;
        } catch (MalformedURLException ex) {
            MessageBox.show("If you see this message, the programmer sucks!.", "Error", 2);
            NHLIPNotFound = true;
            MLBIPNotFound = true;
        }
        return null;
    }

    public boolean isIpNotFound(String league) {
        if (league.equals("NHL")) {
        return NHLIPNotFound;
        } else {
            return MLBIPNotFound;
        }
    }
}
