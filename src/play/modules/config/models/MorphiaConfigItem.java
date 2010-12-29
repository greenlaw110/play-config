package play.modules.config.models;

import java.util.ArrayList;
import java.util.List;

import play.modules.morphia.Model;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity("_conf")
public class MorphiaConfigItem extends Model implements IConfigItem {
   
   @Id
   private String key;
   
   private String value;
   
   @Override
   public Object getId() {
      return key;
   }
   
   @Override
   protected void setId_(Object id) {
      key = id.toString();
   }
   
   protected static Object processId_(Object id) {
      return id.toString();
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

   @Override
   public IConfigItem pc_save() {
      this.save();
      return this;
   }

   @Override
   public List<IConfigItem> pc_all() {
      List<IConfigItem> l = new ArrayList();
      List<MorphiaConfigItem> l0 = new MorphiaQuery(MorphiaConfigItem.class).order("_id").asList();
      for (MorphiaConfigItem i: l0) {
         l.add(i);
      }
      return l;
   }

   @Override
   public IConfigItem pc_new(String key, String value) {
      MorphiaConfigItem ci = new MorphiaConfigItem();
      ci.key = key;
      ci.value = value;
      return ci;
   }

   @Override
   public IConfigItem pc_findByKey(String key) {
      return new MorphiaQuery(MorphiaConfigItem.class).filter("_id", key).first();
   }

   @Override
   public void pc_clear() {
      new MorphiaQuery(MorphiaConfigItem.class).delete();
   }

   @Override
   public void pc_delete() {
      delete();
   }

}
