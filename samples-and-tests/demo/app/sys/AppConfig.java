package sys;

import play.modules.config.ConfigPlugin.AutoConfig;

@AutoConfig
public class AppConfig {
  public static class conf {
    public static int one = 2;
    public static boolean two = true;
    public static class url {
      public static String google = "http://www.google.com";
      public static String facebook = "http://www.facebook.com";
    }
  }
}