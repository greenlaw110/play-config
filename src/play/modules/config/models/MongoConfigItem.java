package play.modules.config.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import play.Play;
import play.modules.config.ConfigPlugin;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoConfigItem implements IConfigItem {
    private String key;
    private String val;
    
    public MongoConfigItem() {
    }
    
    private MongoConfigItem(String key, String val) {
        this.key = key;
        this.val = val;
    }

    @Override
    public String pc_key() {
        return key;
    }

    @Override
    public String pc_value() {
        return val;
    }

    @Override
    public IConfigItem pc_value(String value) {
        val = value;
        return this;
    }

    @Override
    public IConfigItem pc_save() {
        DBObject o = toDBObject_(this);
        MongoDB.save(o);
        return this;
    }

    @Override
    public List<IConfigItem> pc_all() {
        List<DBObject> all = MongoDB.all();
        List<IConfigItem> lst = new ArrayList<IConfigItem>();
        for (DBObject o: all) {
            lst.add(fromDBObject_(o));
        }
        return lst;
    }

    @Override
    public IConfigItem pc_new(String key, String value) {
        return new MongoConfigItem(key, value);
    }

    @Override
    public IConfigItem pc_findByKey(String key) {
        DBObject o = MongoDB.find(key);
        return null == o ? null : fromDBObject_(o);
    }

    @Override
    public void pc_clear() {
        MongoDB.purge();
    }

    @Override
    public void pc_delete() {
        MongoDB.delete(toDBObject_(this));
    }

    @Override
    public void onApplicationStart() throws Exception {
        MongoDB.init(Play.configuration);
    }
    
    private static DBObject toDBObject_(MongoConfigItem ci) {
        DBObject o = new BasicDBObject();
        o.put("key", ci.key);
        o.put("val", ci.val);
        return o;
    }
    
    private static MongoConfigItem fromDBObject_(DBObject o) {
//        String app_id = (String)o.get("app_id");
//        if (StringUtil.isEqual(app_id, ConfigPlugin.appId())) {
//            throw new IllegalStateException("app_id does not match");
//        }
        String key = (String)o.get("key");
        String val = (String)o.get("val");
        if (null == val) val = (String)o.get("value"); //also load value for back compatibility
        MongoConfigItem ci = new MongoConfigItem(key, val);
        return ci;
    }
    
    private static class MongoDB {
        private static DBCollection col;
        private static String getProp_(Properties p, String conf, String morphia, String mongo, String defVal) {
            String val = p.getProperty(conf);
            if (null == val) val = p.getProperty(morphia);
            if (null == val) val = p.getProperty(mongo);
            if (null == val) val = defVal;
            return val;
        }
        static void init(Properties p) throws Exception {
            String host = getProp_(p, "config.mongo.db.host", "morphia.db.host", "mongo.host", "127.0.0.1");
            int port = Integer.parseInt(getProp_(p, "config.mongo.db.port", "morphia.db.port", "mongo.port", "27017"));
            Mongo m = new Mongo(host, port);
            
            String dbname = getProp_(p, "config.mongo.db.name", "morphia.db.name", "mongo.database", "test");
            DB db = m.getDB(dbname);
            
            String username = getProp_(p, "config.mongo.db.username", "morphia.db.username", "mongo.username", null);
            String password = getProp_(p, "config.mongo.db.password", "morphia.db.password", "mongo.password", null);
            if (null != username && null != password) {
                db.authenticate(username, password.toCharArray());
            }
            
            String colName = p.getProperty("config.mongo.col.name", "play_config");
            col = db.getCollection(colName);
            
            col.ensureIndex("app_id");
            col.ensureIndex("key");
        }
        static DBObject save(DBObject o) {
            o.put("app_id", ConfigPlugin.appId());
            DBObject ref = ref_();
            ref.put("key", o.get("key"));
            return col.findAndModify(ref, null, null, false, o, true, true);
        }
        private static DBObject ref_() {
            DBObject ref = new BasicDBObject();
            ref.put("app_id", ConfigPlugin.appId());
            return ref;
        }
        static List<DBObject> all() {
            DBObject ref = ref_();
            DBCursor c = col.find(ref);
            return c.toArray();
        }
        static DBObject find(String key) {
            DBObject ref = ref_();
            ref.put("key", key);
            DBCursor c = col.find(ref);
            if (c.hasNext()) return c.next();
            return null;
        }
        static void purge() {
            DBObject ref = ref_();
            col.remove(ref);
        }
        static void delete(DBObject o) {
            o.put("app_id", ConfigPlugin.appId());
            col.remove(o);
        }
    }

}
