package models;

import javax.persistence.Entity;

import play.Logger;
import play.db.jpa.Model;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;

@Entity
public class ModelA extends Model {
   
   @Every("cron.updater")
   public static class Updater extends Job {
      @Override
      public void doJob() {
         Logger.info("updater invoked");
      }
   }
   
   @On("cron.backup")
   public static class Backup extends Job {
      @Override
      public void doJob() {
         Logger.info("backup invoked");
      }
   }
}
