
package GameObj;


public class GameStream {

    private String aMID, hMID, nMID, fMID, threeCamMID, sixCamMID, aTV, hTV, nTV, fTV;
    private final String[] isoStreamNames = new String[3], isoStreams = new String[3];

    
    public void setAwayMediaID(String id) {
        aMID=id;
    }

    
    public String getAwayMediaID() {
        return aMID;
    }

    
    public void setHomeMediaID(String id) {
        hMID=id;
    }

    
    public String getHomeMediaID() {
        return hMID;
    }

    
    public void setNationalMediaID(String id) {
        nMID = id;
    }

    
    public String getNationalMediaID() {
        return nMID;
    }

    
    public void setFrenchMediaID(String id) {
        fMID = id;
    }

    
    public String getFrenchMediaID() {
        return fMID;
    }

    
    public void setAwayTVStation(String station) {
        aTV=station;
    }

    
    public String getAwayTVStation() {
        return aTV;
    }

    
    public void setHomeTVStation(String station) {
        hTV=station;
    }

    
    public String getHomeTVStation() {
        return hTV;
    }

    
    public void setNationalTVStation(String station) {
        nTV=station;
    }

    
    public String getNationalTVStation() {
        return nTV;
    }

    
    public void setFrenchTVStation(String station) {
        fTV=station;
    }

    
    public String getFrenchTVStation() {
        return fTV;
    }


    public String getThreeCamMID() {
        return threeCamMID;
    }

    public void setThreeCamMID(String threeCamMID) {
        this.threeCamMID = threeCamMID;
    }

    public String getSixCamMID() {
        return sixCamMID;
    }

    public void setSixCamMID(String sixCamMID) {
        this.sixCamMID = sixCamMID;
    }

    public String getIsoStreamNames(int index) {
        try {
        return isoStreamNames[index];
        } catch (ArrayIndexOutOfBoundsException ae) {
            return null;
        }
    }

    public void setIsoStreamNames(String isoStreamName, int index) {
        try {
        this.isoStreamNames[index] = isoStreamName;
        } catch (ArrayIndexOutOfBoundsException ae) {
            ae.printStackTrace();
        }
    }
    
   public String getIsoStream(int index) {
       try {
           return isoStreams[index];
        } catch (ArrayIndexOutOfBoundsException ae) {
            return null;
        }
    }

    public void setIsoStream(String isoStream, int index) {
        this.isoStreams[index] = isoStream;
    }
    
}
