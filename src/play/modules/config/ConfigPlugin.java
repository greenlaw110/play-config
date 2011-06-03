package play.modules.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.db.jpa.JPAPlugin;
import play.modules.config.models.IConfigItem;
import play.modules.config.models.JPAConfigItem;

/**
 * <code>ConfigPlugin</code> provide admin interface to manage application level
 * Play configurations.
 * 
 * <p>
 * Upon application start, <code>ConfigPlugin</code> scan application.conf file,
 * fetch all configuration items start with "app." and "cron.", load them into
 * datastore via //(TODO update here)Config model or custom implemented config
 * model if they are not found in the datastore. If any configuration item is
 * found in the datastore, <code>ConfigPlugin</code> will use the value found in
 * the datastore to override value found in application.conf file.
 * </p>
 * 
 * <p>
 * play-config plugin provides an admin page to allow user configuring all
 * configuration items. The updated value will be stored in the datastore
 * 
 * Note <code>ConfigPlugin</code> must be load prior to <code>JobPlugin</code>
 * as it might rewrite cron/every configuration in Play properties. This plugin
 * should be load after <code>JPAPlugin</code>
 * 
 * @author greenlaw110@gmail.com
 * @version 1.0 18/12/2010
 */
public class ConfigPlugin extends PlayPlugin {
   public static final String VERSION = "1.1";
   public static final String CONF_PREFIX = "config.prefix";
   private static final String DEF_MODEL_CLS_ = JPAConfigItem.class.getName();

   private static String msg_(String msg) {
      return String.format("ConfigPlugin-" + VERSION + "> %1$s", msg);
   }

   /**
    * Return true if key start with "app.", "cron." or any other prefixies configured with {@link #CONF_PREFIX}
    * 
    * @param key
    * @return
    */
   public static boolean isAppConfig(String key) { 
       if (CONF_PREFIX.equals(key)) return true;
	   String prefixes = Play.configuration.getProperty(CONF_PREFIX, "");
	   prefixes += ",app,cron";
	   String[] sa = prefixes.split("[, :]");
	   for (String p: sa) {
	       if (!p.endsWith(".")) p = p + ".";
		   if (key.startsWith(p)) return true;
	   }
	   return false;
   }
   
   /**
    * Throw {@link IllegalArgumentException} if key specified does not start
    * with "app." or "cron."
    * 
    * @param key
    * @throws IllegalArgumentException
    */
   public static void assertValidKey(String key)
         throws IllegalArgumentException {
      if (!isAppConfig(key))
         throw new IllegalArgumentException(
               "Key must start with 'app.' or 'cron.'");
   }

   private Class<? extends Object> modelClass_ = null;
   private IConfigItem factory_ = null;

   @Override
   public void onConfigurationRead() {
      if (!isJPAModel_()) {
         return; // config item class is not JPAConfigItem, no need for additional configuration
      }
      String jpaEntities = Play.configuration.getProperty("jpa.entities", "");
      if ("".equals(jpaEntities)) {
         jpaEntities = DEF_MODEL_CLS_;
      } else if (!jpaEntities.contains(DEF_MODEL_CLS_)) {
         jpaEntities += ", " + DEF_MODEL_CLS_;
      }
      Play.configuration.put("jpa.entities", jpaEntities);
      Logger.debug("jpaEntities property updated to: %1$s", jpaEntities);
   }

   @Override
   public void onApplicationStart() {
      String clsName = Play.configuration.getProperty("config.modelClass");
      try {
         modelClass_ = null == clsName ? JPAConfigItem.class : Class
               .forName(clsName);
         Object o = modelClass_.newInstance();
         if (!(o instanceof IConfigItem)) {
            throw new RuntimeException(
                  String.format(
                        "Model class[%1$s] is not an implementation of IConfigItem interface",
                        modelClass_.getName()));
         }
         factory_ = (IConfigItem) o;
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
      instance_ = this;
      Logger.info(msg_("initialized with modelClass: " + modelClass_));
   }

   @Override
   public void afterApplicationStart() {
      startTx_();
      try {
         if (Boolean.parseBoolean(Play.configuration.getProperty("config.reset", "false"))) {
            factory_.pc_clear();
         }
         loadProperties();
         commitTx_();
      } catch (RuntimeException e) {
         rollbackTx_();
         throw e;
      }
      Logger.debug(msg_("configuration loaded"));
   }

   /**
    * Load configuration from Play properties into data store. If one configuration
    * item already exists in the data store, then it will be used to override Play.configuration
    * 
    * @return Return a list of configuration items found in the data store
    */
   public List<IConfigItem> loadProperties() {
      Properties p = Play.configuration;
      if (!p.containsKey(CONF_PREFIX)) p.setProperty(CONF_PREFIX, "");
      List<IConfigItem> l = new ArrayList<IConfigItem>();
      for (Object o : p.keySet()) {
         String key = o.toString();
         if (!isAppConfig(key))
            continue;
         IConfigItem ci = factory_.pc_findByKey(key);
         if (null == ci) {
            ci = factory_.pc_new(key, p.getProperty(key)).pc_save();
         } else {
            p.setProperty(key, ci.pc_value());
         }
         l.add(ci);
      }
      return l;
   }
   
   /**
    * Reset configuration to application.conf
    */
   public void reset() {
      factory_.pc_clear();
      Play.start();      
   }

   public List<IConfigItem> all() {
      return factory_.pc_all();
   }
   
   public IConfigItem findByKey(String key) {
      return factory_.pc_findByKey(key);
   }
   
   /**
    * Remove an {@link IConfigItem}} from data store by key
    * @param key
    * @return the config item removed or <code>null</code> if no config item found by the key specified
    */
   public IConfigItem delete(String key) {
      IConfigItem ci = factory_.pc_findByKey(key);
      if (null != ci) {
         ci.pc_delete();
      }
      return ci;
   }
   
   private static boolean isJPAModel_() {
      String clsName = Play.configuration.getProperty("config.modelClass", DEF_MODEL_CLS_);
      return DEF_MODEL_CLS_.equals(clsName);
   }
   
   private static void startTx_() {
      if (isJPAModel_()) {
         JPAPlugin.startTx(false);
      }
   }
   
   private static void commitTx_() {
      if (isJPAModel_()) {
         JPAPlugin.closeTx(false);
      }
   }
   
   private static void rollbackTx_() {
      if (isJPAModel_()) {
         JPAPlugin.closeTx(true);
      }
   }

   private static ConfigPlugin instance_ = null;

   public static ConfigPlugin instance() {
      return instance_;
   }
}
