package play.modules.config.models;

import java.util.List;

import play.modules.config.ConfigPlugin;

/**
 * Define config item interface 
 * @author greenlaw110@gmail.com
 */
public interface IConfigItem {
   /**
    * Return key name of this config item
    * @return
    */
   String pc_key();
   /**
    * Return value of this config item
    * @return
    */
   String pc_value();
   /**
    * Set new value to the config item
    * @param value
    * @return this config item with new value set
    */
   IConfigItem pc_value(String value);
   /**
    * Save the config item into datastore and return saved instance
    * @return
    */
   IConfigItem pc_save();
   /**
    * Factory method to return all configuration items
    * @return all configuration item in a list
    */
   List<IConfigItem> pc_all();
   /**
    * Factory method to create new <code>IConfigItem</code> instance with given key and value.
    * 
    * <p>Note the instance returned by this method will not be persist into the datastore until
    * {@link #pc_save()} method called upon the instance.
    * 
    * @param key
    * @param value
    * @return new IConfigItem instance
    * @throws IllegalArgumentException if {@link ConfigPlugin#isAppConfig(String)} return false
    * with key specified
    */
   IConfigItem pc_new(String key, String value);
   /**
    * Factory method to load <code>IConfigItem</code> instance from data store by key
    * @param key to load the config item from data store
    * @return the config item find by key or <code>null</code> if cannot locate a config item with the key
    */
   IConfigItem pc_findByKey(String key);
   /**
    * Remove all configurations from data store. This is useful when user want to reset configuration to
    * application.conf
    */
   void pc_clear();
   /**
    * Remove this config item from data store
    */
   void pc_delete();
}
