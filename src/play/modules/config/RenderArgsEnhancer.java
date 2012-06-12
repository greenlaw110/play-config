package play.modules.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import play.Logger;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.ControllersEnhancer.ControllerSupport;
import play.classloading.enhancers.Enhancer;
import play.exceptions.UnexpectedException;

public class RenderArgsEnhancer extends Enhancer {

    private boolean isAnon_(ApplicationClass app) {
        return app.name.contains("$anonfun$") || app.name.contains("$anon$");
    }

    @Override
    public void enhanceThisClass(ApplicationClass applicationClass)
            throws Exception {

        if (isAnon_(applicationClass)) {
            return;
        }

        CtClass ctClass = makeClass(applicationClass);

        if (!ctClass.subtypeOf(classPool.get(ControllerSupport.class.getName()))) {
            return;
        }

        for (final CtMethod ctMethod : ctClass.getDeclaredMethods()) {

            // is there PutRenderArgs annotation ?
            Object o = ctMethod.getAnnotation(PutRenderArgs.class);
            //Object o = JavassistPatch.getAnnotation(ctMethod, PutRenderArgs.class);
            if (null == o) continue;
            PutRenderArgs putRenderArgs = (PutRenderArgs)o;
            String[] keys = putRenderArgs.value();
            if (keys.length == 0) continue;

            Set<String> ss = new HashSet<String>();
            for (String key: keys) {
                String sa[] = key.split("[\\s\\t,;:]+");
                for (String s: sa) {
                    if (s.indexOf("*") != -1) {
                        boolean found = false;
                        for (Object confKey: Play.configuration.keySet()) {
                            String sConfKey = confKey.toString();
                            if (sConfKey.matches(s)) {
                                found = true;
                                ss.add(sConfKey);
                            }
                        }
                        if (!found) Logger.warn("conf key not recognized: %s", s);
                    } else {
                        ss.add(s);
                    }
                }
            }
            List<String> sl = new ArrayList<String>(ss);

            if (Modifier.isPublic(ctMethod.getModifiers()) && Modifier.isStatic(ctMethod.getModifiers()) && ctMethod.getReturnType().equals(CtClass.voidType)) {
                StringBuilder sb = new StringBuilder("String[] _sa = {\"").append(sl.get(0)).append("\"");
                for (int i = 1; i < sl.size(); ++i) {
                    sb.append(", \"").append(sl.get(i)).append("\"");
                }
                sb.append("}; play.modules.config.ConfigurationResolver.put(_sa);");
                try {
                    ctMethod.insertBefore(sb.toString());
                } catch (Exception e) {
                    Logger.error(e, "Error in RenderArgEnhancer. %s.%s has not been properly enhanced (put-renderargs).", applicationClass.name, ctMethod.getName());
                    throw new UnexpectedException(e);
                }
            }

            applicationClass.enhancedByteCode = ctClass.toBytecode();
            ctClass.defrost();
        }
    }

}
