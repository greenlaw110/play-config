package play.modules.config.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.Logger;
import play.db.jpa.GenericModel;
import play.db.jpa.JPQL;
import play.modules.config.ConfigPlugin;

/**
 * Default {@link IConfigItem} implementation on JPA
 * @author greenlaw110@gmail.com
 * @version 1.0 18/12/2010
 */
@Entity
@Table(name = "_conf")
public class JPAConfigItem extends GenericModel implements IConfigItem {

   @Id
   public String key;
   
   public String value;
   
   public JPAConfigItem() {}
   
   private JPAConfigItem(String key, String value) {
      this.key = key;
      this.value = value;
   }
   
   @Override
   public String pc_key() {
      return key;
   }

   @Override
   public String pc_value() {
      return value;
   }
   
   @Override
   public IConfigItem pc_value(String value) {
      this.value = value;
      return this;
   }
   
   public IConfigItem pc_save() {
      return (IConfigItem)save();
   }
   
   @Override
   public String toString() {
      return String.format("%1$s=%2$s", key, value);
   }

   @Override
   public List<IConfigItem> pc_all() {
      List<IConfigItem> l = new ArrayList<IConfigItem>();
      List<JPAConfigItem> l0 = JPQL.instance.findBy(JPAConfigItem.class.getName(), "order by key", new Object[]{});
      for (JPAConfigItem ci: l0) {
         l.add(ci);
      }
      return l;
   }
   
   @Override
   public IConfigItem pc_new(String key, String value) {
      ConfigPlugin.assertValidKey(key);
      return new JPAConfigItem(key, value);
   }
   
   @Override
   public IConfigItem pc_findByKey(String key) {
      try {
         return (IConfigItem)JPQL.instance.findById(JPAConfigItem.class.getName(), key);
      } catch (Exception e) {
         Logger.warn(e, "Error fetch configuration item by key: %1$s", key);
         return null;
      }
   }
   
   @Override
   public void pc_clear() {
      JPQL.instance.deleteAll(JPAConfigItem.class.getName());
   }
   
   @Override
   public void pc_delete() {
      delete();
   }
   
}
