package Objects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Time {

    public static String formatDateSched(String d) {
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        try {
            Date date = df.parse(d);
            df = new SimpleDateFormat("yyyy-MM-dd");
            return df.format(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
            
            return d;
        }
    }

    public static String getPSTDate(String formatType) {
        Date today = new Date();
        DateFormat df = new SimpleDateFormat(formatType);

        df.setTimeZone(TimeZone.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        try {
            today = df.parse(df.format(today));
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
        return df.format(today);
    }
    
    public static Date getPSTDate1(String formatType) {
        Date today = new Date();
        DateFormat df = new SimpleDateFormat(formatType);

        df.setTimeZone(TimeZone.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        try {
            return df.parse(df.format(today));
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String toLocalTZ(String time, String timezone, String format) {
        if (time.contains("TBD")) {
            return "TBD";
        }
        try {
            SimpleDateFormat from = new SimpleDateFormat(format);
            SimpleDateFormat to = new SimpleDateFormat("h:mm a");

            from.setTimeZone(TimeZone.getTimeZone(timezone));
            Date d1 = from.parse(time);

            from.setTimeZone(TimeZone.getDefault());

            return to.format(d1);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String toLocalTZ(String time, String timezoneFrom, String formatFrom, String formatTo) {
        if (time.contains("TBD")) {
            return "TBD";
        }
        try {
            SimpleDateFormat from = new SimpleDateFormat(formatFrom);
            SimpleDateFormat to = new SimpleDateFormat(formatTo);

            from.setTimeZone(TimeZone.getTimeZone(timezoneFrom));
            Date d1 = from.parse(time);

            from.setTimeZone(TimeZone.getDefault());

            return to.format(d1);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static long nextDay() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        Date now, threeAM;
        df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        try {
            threeAM = df.parse(getPSTDate("yyyy-MM-dd") + " 3:00 am");
            String three = df.format(threeAM);
            df.setTimeZone(TimeZone.getDefault());
            threeAM = df.parse(three);
            
            now = new Date();
            
            Calendar c = Calendar.getInstance(), c2 = Calendar.getInstance();
            c.setTime(now);
            c2.setTime(threeAM);
            
            if (c.after(c2))
                c2.add(Calendar.DAY_OF_MONTH, 1);
            
            return c2.getTimeInMillis() - c.getTimeInMillis();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return -1;
        
    }

    public static String getPSTTime() {
        Date today = new Date();
        DateFormat df = new SimpleDateFormat("h:mm a");

        df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        return df.format(today);
    }

    public static String getPSTDateTime(String dt) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd h:mm a");
        Date d = null;
        try {
            d = df.parse(dt);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

        return df.format(d);
    }

    public static boolean isXMinBeforeGame(String t, int x) {
        if (t.contains("TBD")) {
            return false;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd h:mm a");
            Date d1 = format.parse(getDate());
            Date d2 = format.parse(t);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d2);
            cal.add(12, -x);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(d1);
            return cal2.after(cal);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static String getDate() {
        Date today = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd h:mm a");

        return df.format(today);
    }
    
    public static boolean isToday(String date) {
        Date today = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        try {
            today = df.parse(getPSTDate(df.format(today)));
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        cal.setTime(today);

        Date tdy = null;
        df.setTimeZone(TimeZone.getDefault());
        try {
            tdy = df.parse(getPSTDate(date));
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        cal2.setTime(tdy);
        return cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                  cal.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    public static String getPrevDay(String day) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date d2 = format.parse(day);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d2);
            cal.add(Calendar.DATE, -1);
            return format.format(cal.getTime());
        } catch (ParseException ex) {
            ex.printStackTrace();
            return "";
        }
    }
    
    public static String getNextDay(String day) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date d2 = format.parse(day);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d2);
            cal.add(Calendar.DATE, 1);
            return format.format(cal.getTime());
        } catch (ParseException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static Date getDate(String date, String format) {
        DateFormat df = new SimpleDateFormat(format);

        try {
            return df.parse(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static int getMinutesPassed(String startTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd H:mm");
        Date d = null, now = new Date();
        try {
            startTime = toLocalTZ(startTime, "UTC", "yyyy-MM-dd H:mm", "yyyy-MM-dd H:mm");
            d = df.parse(startTime);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return -1;
        }
        
        return (int) TimeUnit.MILLISECONDS.toMinutes(now.getTime() - d.getTime());
    }
    
    public static boolean isPastGameTime(String startTime)  {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd H:mm");
        Date d = null, now = new Date();
        try {
            startTime = toLocalTZ(startTime, "UTC", "yyyy-MM-dd H:mm", "yyyy-MM-dd H:mm");
            d = df.parse(startTime);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return false;
        }
        Calendar cal = Calendar.getInstance(), cal2 = Calendar.getInstance();
        cal.setTime(d);
        cal2.setTime(now);
        
        return cal2.after(cal);
    }
}
