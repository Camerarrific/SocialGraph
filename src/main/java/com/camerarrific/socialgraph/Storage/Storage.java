    
package com.camerarrific.socialgraph.Storage;

import com.camerarrific.socialgraph.api;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import java.util.Map;

/**
 *
 * @author adam
 */


public class Storage {
    
//    public static RedisPool redis;
    public static CouchBase couchBase;
    
    public enum StorageDestination {
        StorageDestinationRedis(1),
        StorageDestinationCouchBase(2),
        StorageDestinationRedisAndCouchBase(3);
        
        private final Integer value;

        private StorageDestination(Integer value) {
            this.value = value;
        }
    }
    
    StorageDestination storageType;
    Cluster cluster = CouchbaseCluster.create("localhost");
    Bucket bucket = cluster.openBucket("SocialGraph");
    
    Storage(){
        this.cluster = CouchbaseCluster.create("localhost");
        this.bucket = cluster.openBucket("SocialGraph");
    }
    
    Storage(StorageDestination storageType){
        this.storageType = storageType;
        
        if (this.storageType == StorageDestination.StorageDestinationCouchBase){
            this.cluster = CouchbaseCluster.create("localhost");
            this.bucket = cluster.openBucket("SocialGraph");
        }
    }
   
//    public void save(String key, Map<String, String> value) {
//        if (this.storageType == StorageDestination.StorageDestinationRedis){
//            
//             api.connection.sync().hmset(key, value); 
//        } else if (this.storageType == StorageDestination.StorageDestinationCouchBase){
////            // Store the Document
//            this.bucket.upsert(JsonDocument.create(key, JsonObject.from(value)));
//        }
//    }
//    
//   
//    public Map<String, Object> get(String key){
//        return this.bucket.get(key).content().toMap();
//    }
    
}
