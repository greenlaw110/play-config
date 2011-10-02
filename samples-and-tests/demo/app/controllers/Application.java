package controllers;

import play.Play;
import play.jobs.JobsPlugin;
import play.modules.config.PutRenderArgs;
import play.modules.config.RenderArgResolver;
import play.mvc.Controller;

public class Application extends Controller {

   @PutRenderArgs("app.conf.*, cron.updater")
   public static void index() {
      JobsPlugin jobsPlugin = Play.plugin(JobsPlugin.class);
      RenderArgResolver.put("app.conf.url.facebook");
      render(jobsPlugin);
   }

}