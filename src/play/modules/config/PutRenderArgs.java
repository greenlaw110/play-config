package play.modules.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class or a method to be enhanced by play-config to put configuration items
 * into renderArgs automatically
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PutRenderArgs {
    /**
     * Could be something like 
     * "foo.bar1" or "foo.*" or {"foo.bar1", "foo.bar2"}
     * @return
     */
    String[] value();
}
