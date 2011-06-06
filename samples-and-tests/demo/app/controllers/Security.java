package controllers;

import play.Play;


public class Security extends Secure.Security {
    static boolean authentify(String username, String password) {
        return isEqual(username, Play.configuration.getProperty("app.admin.username", "admin")) &&
        	isEqual(password, Play.configuration.getProperty("app.admin.password", "admin"));
    }
    static boolean isEqual(String s0, String s1) {
        if (null == s0) {
            return s1 == null;
        }
        if (null == s1)
            return false;
        return s0.equals(s1);
    }
}

