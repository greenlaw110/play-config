package controllers.play_config;

import java.util.List;

import play.Play;
import play.libs.Time;
import play.modules.config.ConfigPlugin;
import play.modules.config.models.IConfigItem;
import play.mvc.Controller;
import play.mvc.With;
import controllers.Secure;

@With(Secure.class)
public class Console extends Controller {
   public static void index() {
      List<IConfigItem> configuration = ConfigPlugin.instance().all();
      render(configuration);
   }
   public static void update(String key, String value) {
      if (params.get("_delete") != null) {
         ConfigPlugin.instance().delete(key);
      } else {
         if (!isValid_(key, value)) {
            flash.error("Invalid value [%1$s] for key [%2$s]", value, key);
         } else {
            IConfigItem item = ConfigPlugin.instance().findByKey(key);
            item.pc_value(value);
            item.pc_save();
            Play.configuration.setProperty(key, value);
            flash.success("key [%1$s] successfully updated to value [%1$s]", key, value);
         }
      }
      flash.keep();
      index();
   }
   private static boolean isValid_(String key, String value) {
      if (!key.startsWith("cron.")) {
         return true;
      }
      
      try {
         Time.parseDuration(value);
         return true;
      } catch (IllegalArgumentException e) {
         // not duration, let's continue to try CRON
      }

      try {
         Time.parseCRONExpression(value);
         return true;
      } catch (IllegalArgumentException e) {
         return false;
      }
   }
   public static void reset() {
      ConfigPlugin.instance().reset();
      index();
   }
   public static void restart() {
      Play.start();
      index();
   }
}
