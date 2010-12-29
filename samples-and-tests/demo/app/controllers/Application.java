package controllers;

import java.util.SortedMap;
import java.util.TreeMap;

import play.Play;
import play.PlayPlugin;
import play.jobs.JobsPlugin;
import play.modules.config.ConfigPlugin;
import play.mvc.Controller;

public class Application extends Controller {

   public static void index() {
      SortedMap<String, String> configuration = new TreeMap();
      for (Object o: Play.configuration.keySet()) {
         String key = (String)o;
         if (ConfigPlugin.isAppConfig(key)) {
            configuration.put(key, Play.configuration.getProperty(key));
         }
      }
      JobsPlugin jobsPlugin = null;
      for (PlayPlugin pp: Play.plugins) {
         if (pp instanceof JobsPlugin) {
            jobsPlugin = (JobsPlugin)pp;
            break;
         }
      }
      render(configuration, jobsPlugin);
   }

}