package controllers.play_config;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import play.Play;
import play.Play.Mode;
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
        Collections.sort(configuration, new Comparator<IConfigItem>(){

            @Override
            public int compare(IConfigItem o1, IConfigItem o2) {
                if (null == o1) return -1;
                if (null == o2) return 1;
                return o1.pc_key().compareTo(o2.pc_key());
            }
            
        });
        render(configuration);
    }

    public static void update(String key, String value) {
        if (!isValid_(key, value)) {
            value = Play.configuration.getProperty(key);
            renderJSON(String.format("{\"error\": \"invalid value\", \"old_value\": \"%s\"}", value));
        }
        if (ConfigPlugin.CONF_PREFIX.equals(key)) {
            ConfigPlugin.instance().reload();
            IConfigItem item = ConfigPlugin.instance().findByKey(key);
            item.pc_value(value);
            item.pc_save();
            renderJSON("{\"script\": \"window.location.href=window.location.href;\"}");
        }
        IConfigItem item = ConfigPlugin.instance().findByKey(key);
        item.pc_value(value);
        item.pc_save();
        Play.configuration.setProperty(key, value);
        ok();
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
        if (Mode.DEV == Play.mode) Play.start();
        index();
    }
}
