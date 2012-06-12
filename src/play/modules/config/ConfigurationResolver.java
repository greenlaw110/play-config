package play.modules.config;

import java.lang.reflect.Modifier;
import java.util.List;

import play.Play;
import play.mvc.Scope.RenderArgs;

/**
 * TODO Change Object to String, do type casting intelligently
 * @author luog
 *
 */
public class ConfigurationResolver {
    
    private static IConfigurationResolver resolver_ = new IConfigurationResolver() {
        @Override
        public Object get(String key) {
            return Play.configuration.get(key);
        }
    };
    
    public static Object get(String key) {
        return resolver_.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        Object val = get(key);
        return (T)val;
    }

    public static Object get(String key, Object def) {
        Object val = get(key);
        return null == val ? def : val;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T def, Class<T> clazz) {
        Object val = get(key);
        return (T)(null == val ? def : val);
    }
    
    public static void put(String... keys) {
        for (String key: keys) {
            RenderArgs.current().put(key.replace('.', '_'), get(key));
        }
    }

    @SuppressWarnings("rawtypes")
    static void loadConfigurationResolver() {
        List<Class> ca = Play.classloader.getAssignableClasses(IConfigurationResolver.class);
        for (Class c: ca) {
            int mod = c.getModifiers();
            if (Modifier.isAbstract(mod) || Modifier.isInterface(mod)) continue;
            try {
                resolver_ = (IConfigurationResolver)c.newInstance();
            } catch (Exception e) {
                continue;
            }
        }
    }
}
