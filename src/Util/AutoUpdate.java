package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.net.ssl.HttpsURLConnection;

public class AutoUpdate {

    public static boolean download(String version) {
        String url;
        String loc;
        
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            loc = Paths.get(".").toAbsolutePath().normalize().toString() + System.getProperty("file.separator") + "lm.zip";
        } else {
            loc = new java.io.File(Props.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + System.getProperty("file.separator") + "lm.zip";
        }
        
        if (System.getProperty("os.name").contains("Win")) {
            url = "https://github.com/StevensNJD4/LazyMan/releases/download/" + version + "/Windows.zip";
        } else {
            url = "https://github.com/StevensNJD4/LazyMan/releases/download/" + version + "/Mac_Linux.zip";
        }

        URL u;
        HttpsURLConnection con;

        try {
            u = new URL(url);
            con = (HttpsURLConnection) u.openConnection();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        try (InputStream stream = con.getInputStream()) {
            Files.copy(stream, new File(loc).toPath());
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean unZipIt() {

        byte[] buffer = new byte[1024];
        String loc;
        
        if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
            loc = Paths.get(".").toAbsolutePath().normalize().toString() + System.getProperty("file.separator") + "lm.zip";
        } else {
            loc = new java.io.File(Props.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent() + System.getProperty("file.separator") + "lm.zip";
        }
        try {
            //get the zip file content
            ZipInputStream zis
                    = new ZipInputStream(new FileInputStream(loc));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(new java.io.File(loc).getParent() + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
            
            new File(loc).delete();

            System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
