package models;

import play.Logger;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import play.modules.morphia.Model;

import com.google.code.morphia.annotations.Entity;

@Entity
public class ModelA extends Model {
   
   /**
     * 
     */
    private static final long serialVersionUID = 1L;

@Every("cron.updater")
   public static class Updater extends Job<Object> {
      @Override
      public void doJob() {
         Logger.info("updater invoked");
      }
   }
   
   @On("cron.backup")
   public static class Backup extends Job<Object> {
      @Override
      public void doJob() {
         Logger.info("backup invoked");
      }
   }
}
