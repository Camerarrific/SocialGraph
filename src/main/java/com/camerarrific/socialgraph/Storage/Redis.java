 
package com.camerarrific.socialgraph.Storage;

import com.camerarrific.socialgraph.api;
import com.lambdaworks.redis.LettuceFutures;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static spark.Spark.port;

/**
 *
 * @author adam
 */

public class Redis {
   
//    // terminating
//    pool.close();
//    clusterClient.shutdown();
   
    public static RedisCommands RedisCommands() throws Exception {
        return api.connection.sync();
}
    
    public static RedisAsyncCommands RedisAsyncCommands() throws Exception {
        return api.connection.async();
}
    
    public static class queue{
        public static void Process(List<RedisFuture> commands){
            try {
                RedisAsyncCommands<String, String> async = api.connection.async();
            } catch (Exception ex) {
                Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
            }
            LettuceFutures.awaitAll(1, TimeUnit.MINUTES, commands.toArray(new RedisFuture[commands.size()]));
        }
       
       public static class commands{
           
        public static List<RedisFuture> Follow(String uuid, String follow) throws Exception{
            return Redis.queue.commands.FollowOrUnFollow(uuid, follow, 1);
        }
        
        public static List<RedisFuture> Unfollow(String uuid, String follow){
            try {
                return Redis.queue.commands.FollowOrUnFollow(uuid, follow, -1);
            } catch (Exception ex) {
                Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
        
        private static List<RedisFuture> FollowOrUnFollow(String uuid, String follow, int direction){
            RedisAsyncCommands asyncCommands;
            try {
                asyncCommands = Redis.RedisAsyncCommands();
                List<RedisFuture> futures = new ArrayList<>();
                           if (direction == 1)
                            futures.add(asyncCommands.sadd("user:" + uuid + ":following", follow));
                           else if (direction == -1)
                               futures.add(asyncCommands.srem("user:" + uuid + ":following", follow));
                           futures.add(asyncCommands.hincrby("user:" + follow, "following", direction));
                           futures.add(asyncCommands.hincrby("user:" + follow, "followers", direction));
                           return futures;
            } catch (Exception ex) {
                Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
            }
                           
            return null;
        }
    }
   } 
    
    public static class hash{
        
        public static class map{
            public static Map<String, String> get (String key){
                try { 
                    return api.connection.sync().hgetall(key);
                } catch (Exception ex) {
                    Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
    
            public static void set (String key, Map<String, String> map){
                try {
                    api.connection.sync().hmset(key, map);
                } catch (Exception ex) {
                    Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        public static class field{
            public static void set (String key, String field, String value){
                try {
                    api.connection.sync().hset(key, field, value);
                } catch (Exception ex) {
                    Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    
            public static String get (String key, String field){
                try {
                    return api.connection.sync().hget(key, field);
                } catch (Exception ex) {
                    Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        }
        
        public static class counters{
            public static long increment(String key, String field){
                try {
                    return api.connection.sync().hincrby(key, field, 1);
                } catch (Exception ex) {
                    Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
                }
                return 0;
            }
            
            public static long decrement(String key, String field){
                try {
                    return api.connection.sync().hincrby(key, field, -1);
                } catch (Exception ex) {
                    Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
                }
                return 0;
            }
            
            public static long get(String key, String field){
                try {
                    return api.connection.sync().hincrby(key, field, 0);
                } catch (Exception ex) {
                    Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
                }
                return 0;
            }
        }
    }
    
    public static class key{
        public static String set(String key, String value){
            try {
                return api.connection.sync().set(key, value);
            } catch (Exception ex) {
                Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    
        public static String get(String key){
            try {
                return api.connection.sync().get(key);
            } catch (Exception ex) {
                Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    
        public static void setWithExpireInSeconds(String key, String value, long seconds){
            try {
                api.connection.sync().setex(key, seconds, value);
            } catch (Exception ex) {
                Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
        public static Boolean exists(String key){
            try {
                return api.connection.sync().exists(key);
            } catch (Exception ex) {
                Logger.getLogger(Redis.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        
        public static class counters{
            public static long increment(String key) throws Exception{
                return api.connection.sync().incrby(key, 1);
            }
            
            public static long decrement(String key) throws Exception{
                return api.connection.sync().incrby(key, -1);
            }
            
            public static long get(String key) throws Exception{
                return api.connection.sync().incrby(key, 0);
            }
        }
    }
    
    public static class member{
        public static long add(String key, String value) throws Exception{
            return api.connection.sync().sadd(key, value);
        }
        
        public static Boolean exists(String key, String member) throws Exception{
            return api.connection.sync().sismember(key, member);
        }
        
        public static long remove(String key, String member) throws Exception{
            return api.connection.sync().srem(key, member);
        }
        
        public static long count(String key) throws Exception{
            return api.connection.sync().scard(key);
        }
    }
}