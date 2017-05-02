package Util;

import java.util.Random;

public class Encryption
{
  private static final String key = "99lhkj";
  
  public static String encrypt(String str)
  {
    StringBuilder sb = new StringBuilder(str);
    
    int lenStr = str.length();
    int lenKey = key.length();
    
    int i = 0;
    for (int j = 0; i < lenStr; j++)
    {
      if (j >= lenKey) {
        j = 0;
      }
      sb.setCharAt(i, (char)(str.charAt(i) ^ key.charAt(j)));i++;
    }
    return sb.toString();
  }
  
  public static String decrypt(String str)
  {
    return encrypt(str);
  }
  
  public static String getSaltString() {
        String SALTCHARS = garble("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 241) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();

    }

    private static String garble(String SALTCHARS) {
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < SALTCHARS.length()) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }
}
