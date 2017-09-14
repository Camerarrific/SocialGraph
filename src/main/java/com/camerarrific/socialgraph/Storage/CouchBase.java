package com.camerarrific.socialgraph.Storage;

import com.camerarrific.socialgraph.api;
import java.util.Map;
import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.*;
import static java.lang.System.err;
import java.util.HashMap;
    
/**
 *
 * @author adam
 */

public class CouchBase {
   
    Cluster cluster = CouchbaseCluster.create("localhost");
    Bucket bucket = cluster.openBucket("SocialGraph");
   
    
    public class hash{
        
        public class map{
            public void set(String key, Map<String, String> value) {
                
                bucket.upsert(JsonDocument.create(key, JsonObject.from(value)));
            }
    
            public Map<String, String> get(String key){
        
                Map<String, Object> hashMap = bucket.get(key).content().toMap();
       
                Map<String,String> newMap = new HashMap<>();
    
                for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
                    if(entry.getValue() instanceof String){
                        newMap.put(entry.getKey(), (String) entry.getValue());
                    }
                }
                return newMap;
            }
        }
    }
}