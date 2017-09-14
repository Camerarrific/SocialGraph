/*
 * (c) 2016 - 2017 Camerarrific Ltd
 * All rights reserved
 * This code will be Open Sourced in the future.
 */

package com.camerarrific.socialgraph;

import com.camerarrific.crypto.AESEncryption;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static spark.Spark.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lambdaworks.redis.*;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.cluster.RedisClusterClient;

import java.text.BreakIterator;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class api {

public static final String storageConnectionString =
          "DefaultEndpointsProtocol=https;"
        + "AccountName=photoblobs;"
        + "AccountKey=<azure storage key>";
    public static Integer postCount = 0;
   
    public static RedisClient redisClient;
    public static StatefulRedisConnection<String, String>  connection;
    public static StatefulRedisConnection<String, String>  connection2;
    public static RedisClusterClient clusterClient;
    public static PasswordHash passwordHash;
    public static long APITimer;
    private static Object attrs;
    private static Object txt;
    
    
    public static void main(String[] args) {
     
     RedisURI redisURL = RedisURI.create("localhost", 6379);
    
     
       redisClient = RedisClient.create(redisURL);     
       connection = redisClient.connect();
       connection2 = redisClient.connect();
       
       before((request, response) -> {
           response.type("application/json");
           if (!"/api/login".equals(request.pathInfo()) &&
               !"/api/register".equals(request.pathInfo()) &&
               !"/api/ping".equals(request.pathInfo()) &&
               !"/api/session".equals(request.pathInfo())){
                String token = request.headers("Authorization").replaceFirst("Bearer ", "");
                
                if (token == null)
                    halt (400, new ServerResponses().GenerateError(ServerResponses.Errors.TokenMissing));
                String authenticatedUser = User.authenticatedUser(token);
                
                if (authenticatedUser == null)
                    halt(401, new ServerResponses().GenerateError(ServerResponses.Errors.TokenInvalid));
                 else {
                    request.attribute("authenticatedUser", authenticatedUser);
                    String username = User.username(authenticatedUser);
                    request.attribute("authenticatedUsername",username);
                    connection.sync().hincrby("user:" + username, "polyCount", 1);
//                    if ("/api/follow".equals(request.pathInfo())){
//                        if (request.queryParams("uid") == null)
//                            halt(400, new ServerResponses().GenerateError(ServerResponses.Errors.FollowerMissing));
//                    }
                }
            }
        });     
        
//        after((request, response) -> {
//            String session = request.headers("session");
//            
//            Session s = new Session(session);
//             
//            String body = response.body().toString();
//            
//            String encryptBody = s.encrypt(body);
//            response.body(encryptBody);
//            
//        });    
  
//       get("/api/get/image", (request, response) -> {
//
//        String key = request.queryParams("filepath");
//        Path path = Paths.get(key);
//        byte[] data = null;
//        try {
//            data = Files.readAllBytes(path);
//        } catch (Exception e1) {
//
//            e1.printStackTrace();
//        }
//
//        HttpServletResponse raw = response.raw();
//        response.header("Content-Disposition", "attachment; filename=image.jpg");
//        response.type("application/force-download");
//        try {
//            raw.getOutputStream().write(data);
//            raw.getOutputStream().flush();
//            raw.getOutputStream().close();
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        }
//        return raw;
//        
//   });
       
       get("/api/session", (request, response) -> {
           
           String uuid = request.attribute("uuid");
           Session s = new Session(uuid);

           JsonObject jsonObj = new JsonObject(); 
           JsonObject serverObj = new JsonObject();


           if (s.newKey)
                serverObj.addProperty("pubKey", s.publicKey);
           
           jsonObj.addProperty("uuid", s.uuid);
           jsonObj.add("response", serverObj);
           
           return jsonObj.toString();
       });
        
        post("/api/login", (request, response) -> {
             
            return User.login(request.queryParams("username"), request.queryParams("password"));      
        });
        
        post("/api/register", (request, response) -> {
            
            String hash = null;
            String username = request.queryParams("username");
            String password = request.queryParams("password");
            String email = request.queryParams("email");
            
            if (!connection.sync().exists("user:" + username)){
            } else {
                response.status(400);
                return new ServerResponses().GenerateError(ServerResponses.Errors.AlreadyRegistered);
            }
            
            // create salt
            
            String salt = PasswordHash.createSalt();
            
            hash = PasswordHash.createArgon2Hash(salt + password);
            
            String uid = Util.UUID();
            String poly = Util.UUID();
            // let's generate a OAUTH token
            String token = Util.UUID();
            
            HashMaps.User userHash = HashMaps.User.create(uid, email, hash, salt, poly);
            
            
            
//            Map<String, String> userHashMap = new LinkedHashMap<>();
//                                userHashMap.put("passwordHash", hash);
//                                userHashMap.put("uuid", uid);
//                                userHashMap.put("email", email);
//                                userHashMap.put("salt", salt);
//                                userHashMap.put("poly", poly);
            
            connection.sync().multi();
            connection.sync().hmset("user:" + username, userHash.getHashMap());
            connection.sync().hset("user:uid", uid, username);
            connection.sync().setex("tokens:" + token, 86400, uid);
            connection.sync().hincrby("user:" + username, "polyCount", 1);
            connection.sync().exec();
            
           
//           String json = JSONObjects.OAuthObject.class.newInstance().create(username, token, uid, 86400).serialise();
//           
//            return json;
//            OAuthObject oauthObject = new JSONObjects.OAuthObject (username, token, uid, 86400);
            
//            JSONObjects.OAuthObject();
            
            return userHash.toJSON();
            
//            JsonObject jsonObj = new JsonObject();
//            jsonObj.addProperty("username", username);
//            jsonObj.addProperty("token", token);
//            jsonObj.addProperty("uid", uid);
//            jsonObj.addProperty("expires_in", 86400);
//            //User.sendActivationEmail(uid,connection.get("user:" + uid + "profile:email"));
//            return jsonObj.toString();
        });
        
        get("/api/activate", (request, response) -> {
            
            String token = request.queryParams("token");
            String uid = connection.sync().get("user:activations:" + token + ":uid");
            String username = connection.sync().get("user:" + uid + ":username");
            
            JsonObject jsonObj = new JsonObject();
            
            if (username != null){
                jsonObj.addProperty("username", username);
                jsonObj.addProperty("uid", uid);
                jsonObj.addProperty("activated", "true");
            } else {
                response.status(400);
                jsonObj.addProperty("activated", "false");
            }
            return jsonObj.toString();
            
        });
        
        post("/api/status", (request, response) -> {
            String content = request.queryParams("content");
            String type = request.queryParams("type");
            String url = request.queryParams("url");
           
            if ("photo".equals(type)){   
                if (url != null){
                        return Share.Photo(request.attribute("authenticatedUser"), content, url);
                } else {
                    if (request.bodyAsBytes().length > 0){
                        return Share.Photo(request.attribute("authenticatedUser"), content, request.bodyAsBytes());
                    } 
                }
                    
            } else if ("video".equals(type)){
                return Share.Video(request.attribute("authenticatedUser"), content, url);
            }
            return null;
        });
        
        get("/api/timeline", (request, response) -> {

           return TimelineGenerator.fifo(request.attribute("authenticatedUser"),
                            Integer.parseInt(request.queryParams("index")),
                            Integer.parseInt(request.queryParams("count")));
        });
        
        get("/api/likes", (request, response) -> {
            return Actions.List(Verbs.Action.LIKE, request.queryParams("uuid"),
                    Integer.parseInt(request.queryParams("index")),
                    Integer.parseInt(request.queryParams("count")));
        });
        
        post("/api/like", (request, response) -> {
            return Actions.Perform(Verbs.Action.LIKE, request.queryParams("uuid"), request.attribute("authenticatedUser"));
        });
              
        post("/api/unlike", (request, response) -> {
            return Actions.Reverse(Verbs.Action.LIKE, request.queryParams("uuid"), request.attribute("authenticatedUser"));
        });
        
        get("/api/loves", (request, response) -> {
            return Actions.List(Verbs.Action.LOVE, request.queryParams("uuid"),
                    Integer.parseInt(request.queryParams("index")),
                    Integer.parseInt(request.queryParams("count")));
        });
        
        post("/api/love", (request, response) -> {
            return Actions.Perform(Verbs.Action.LOVE, request.queryParams("uuid"), request.attribute("authenticatedUser"));
        });
              
        post("/api/unlove", (request, response) -> {
            return Actions.Reverse(Verbs.Action.LOVE, request.queryParams("uuid"), request.attribute("authenticatedUser"));
        });
        
        get("/api/faves", (request, response) -> {
            return Actions.List(Verbs.Action.FAV, request.queryParams("uuid"),
                    Integer.parseInt(request.queryParams("index")),
                    Integer.parseInt(request.queryParams("count")));
        });
        
        post("/api/fav", (request, response) -> {
            return Actions.Perform(Verbs.Action.FAV, request.queryParams("uuid"), request.attribute("authenticatedUser"));
        });
              
        post("/api/unfav", (request, response) -> {
            return Actions.Reverse(Verbs.Action.FAV, request.queryParams("uuid"), request.attribute("authenticatedUser"));
        });
        
        post("/api/share", (request, response) -> {
            return Actions.Perform(Verbs.Action.SHARE, request.queryParams("uuid"), request.attribute("authenticatedUser"));
        });
        
        post("/api/unshare", (request, response) -> {
            return Actions.Reverse(Verbs.Action.SHARE, request.queryParams("uuid"), request.attribute("authenticatedUser"));
        });
        
        get("/api/shares", (request, response) -> {
            return Actions.List(Verbs.Action.SHARE, request.queryParams("uuid"),
                    Integer.parseInt(request.queryParams("index")),
                    Integer.parseInt(request.queryParams("count")));
        });
        
        post("/api/follow", (request, response) -> {
            JsonObject jsonObj = new JsonObject();
                String follow = request.queryParams("uid");
                String username = request.queryParams("username");
                String authenticatedUser = request.attribute("authenticatedUser");
                String userName = request.attribute("userName");
                Boolean unknownUser = false;
                
                if (follow == null){
                    String followUser = User.uid(username);
                    if (followUser == null){
                        unknownUser = true;
                    } else {
                        follow = followUser;
                    }
                } else{
                    if (User.username(follow) == null){
                        unknownUser = true;
                    }
                }
                
                if (unknownUser){
                    response.status(400);
                        return new ServerResponses().GenerateError(ServerResponses.Errors.CannotFollowUnknownUser);
                }
                //incrementSocialImportance(token, follow);
                
                
                if (authenticatedUser.equals(follow)){
                           response.status(400);
                           return new ServerResponses().GenerateError(ServerResponses.Errors.CannotFollowYourself);
                 } else if(User.Follow(authenticatedUser, follow)){
                           jsonObj.addProperty("success", "true");
                           jsonObj.addProperty("following.UID", follow);
                           jsonObj.addProperty("following.Username",username);
                    } else {
                           response.status(400);
                           return new ServerResponses().GenerateError(ServerResponses.Errors.AlreadyFollowing);
                    }
             return jsonObj;
        });
        
        post("/api/unfollow", (request, response) -> {
            JsonObject jsonObj = new JsonObject();
            
            String unfollow = request.queryParams("uid");
            
            if (unfollow == null){
                response.status(400);
                return new ServerResponses().GenerateError(ServerResponses.Errors.FollowerMissing);
            }
            
            String authenticatedUser = request.attribute("authenticatedUser");
                 //incrementSocialImportance(token, follow);
                 if (authenticatedUser.equals(unfollow)){
                           response.status(400);
                           return new ServerResponses().GenerateError(ServerResponses.Errors.CannotUnfollowYourself);
                 } else if(User.Unfollow(authenticatedUser, unfollow)){
                           jsonObj.addProperty("success", unfollow);
                           jsonObj.addProperty("unfollowed", unfollow);
                    } else {
                           response.status(400);
                           return new ServerResponses().GenerateError(ServerResponses.Errors.NotFollowing);
                    }
             return jsonObj;
        });
          
        get("/api/followers", (request, response) -> {
            return User.members(request.attribute("authenticatedUser"), "followers");
        });
        
        get("/api/following", (request, response) -> {
             return User.members(request.attribute("authenticatedUser"), "following");
        });
        
        get("/api/friends", (request, response) -> {
             return User.members(request.attribute("authenticatedUser"), "friends");
        }); 
        
        get("/api/blocked", (request, response) -> {
            return User.members(request.attribute("authenticatedUser"), "blocked");
        });
        
        get("/api/blockers", (request, response) -> {
             return User.members(request.attribute("authenticatedUser"), "blockers");
        }); 
        
        get("/api/muted", (request, response) -> {
             return User.members(request.attribute("authenticatedUser"), "muted");
        });
        
        get("/api/muters", (request, response) -> {
             return User.members(request.attribute("authenticatedUser"), "muters");
        }); 
        
        get("/api/me/rsa/public/key", (request, response) -> {
            return User.publicRSAKey(request.attribute("authenticatedUser"));
        });
        
        get("/api/rsa/public/key", (request, response) -> {
            String uid = request.queryParams("uid");
            if (uid != null)
                return User.publicRSAKey(uid);
            else
                return User.publicRSAKey(request.attribute("authenticatedUser"));
        });
        
        get("/api/aes/key", (request, response) -> {
            String authenticatedUser = request.attribute("authenticatedUser");
            String password = request.queryParams("password");
//            int keysize = Integer.parseInt(request.queryParams("keysize"));
            return AESEncryption.generateKeys(request.attribute("authenticatedUser"), password, 256);
        });
        
        get("/api/devices/registered", (request, response) -> {
            timerStart();
            String token = request.queryParams("token");
            Integer index = Integer.parseInt(request.queryParams("index"));
            Integer count = Integer.parseInt(request.queryParams("count"));
            postCount = 0;
            
            JsonObject rootObject = new JsonObject();
            JsonArray deviceEntities = new JsonArray();
              
            RedisFuture<Long> fc = connection.async().smembers((String value) -> {
                JsonObject deviceObject = new JsonObject();
                           deviceObject.addProperty("device", value);
                     deviceEntities.add(deviceObject);
                     postCount++;
                 }, "user:" + request.attribute("authenticatedUser") + ":devices");
                
                 while (!fc.isDone()) {
                    // do nothing ...
                 }
                rootObject.add("devices", deviceEntities);
                rootObject.addProperty("count", postCount);
                rootObject.addProperty("duration", timerEnd());
                return rootObject.toString();
        }); 
        
        post("/api/request/storage/key", (request, response) -> {
            return Blobs.getSasKey();
        });
       
        post("/api/add/keyword/negative", (request, response) -> {
            
            String keyword = request.params("keyword");
            
            JsonObject rootObj = new JsonObject();
                       rootObj.addProperty("keyword", keyword);
            
                if (connection.sync().hsetnx("user:" + request.attribute("authenticatedUser") + ":negative:keywords", keyword, keyword)){
                    RedisFuture<Long> futureCount = connection.async().lrange((String value) -> {
                        Map<String, String> post = connection.sync().hgetall("post:" + value);
                        String content = connection.sync().hget("post:" + value, "content");
                        
                        if (!negativeKeywordNotFound(value,getWords(content))){
                            connection.sync().lrem("user:" + post.get("id") + ":timeline", 1, value);
                        }
                    }, "user:" + request.attribute("authenticatedUser") + ":timeline", 0, 2000);
                
                    while (!futureCount.isDone()) {
                        // do something ...
                    }
                    rootObj.addProperty("added", "true");
            } else {
                rootObj.addProperty("added", "false");
            }
            return rootObj;
        });
        
         post("/api/add/image/block", (request, response) -> {
            
            String md5 = request.params("md5");
            
            JsonObject rootObj = new JsonObject();
                       rootObj.addProperty("md5", md5);
            
                if (connection.sync().hsetnx("user:" + request.attribute("authenticatedUser") + ":images:blocked:md5", md5, md5)){
                    RedisFuture<Long> futureCount = connection.async().lrange((String value) -> {
                        Map<String, String> post = connection.sync().hgetall("post:" + value);
                        String content = connection.sync().hget("post:" + value, "content");
                        
                        if (!negativeKeywordNotFound(value,getWords(content))){
                            connection.sync().lrem("user:" + post.get("id") + ":timeline", 1, value);
                        }
                    }, "user:" + request.attribute("authenticatedUser") + ":timeline", 0, 2000);
                
                    while (!futureCount.isDone()) {
                        // do something ...
                    }
                    rootObj.addProperty("added", "true");
            } else {
                rootObj.addProperty("added", "false");
            }
            return rootObj;
        });
        
        
        get("/api/ping", (request, response) -> {
            return "hello";
        });
        
        post("/api/lq/upload", (request, response) -> {
            
            String url = request.queryParams("url");
            Integer cutSize = Integer.parseInt(request.queryParams("cut"));
            
           return Blobs.uploadWithRescale(url, cutSize); 
        });
}    
    
public static String requestTagReservation (String tag, String domain) {
    
    if (!"true".equals(connection.sync().hget("tag:" + tag, "confirmed"))){
        
        Map<String, String> map = new LinkedHashMap<>();
                            map.put("domain", domain);
                            map.put("uuid", Util.UUID());
                            map.put("confirmed", "false");

        connection.sync().hmset("tag:" + tag, map);               
        return map.get("uuid");
    } else {
        return null;
    }
}    
    
public static boolean requestTagReservation (String tag, String domain, String uuid) throws NamingException{
    
    Map<String, String> map = connection.sync().hgetall("tag:" + tag);
    
    Hashtable<String, String> env = new Hashtable<>();
                              env.put("java.naming.factory.initial","com.sun.jndi.dns.DnsContextFactory");
                              
    DirContext dirContext = new InitialDirContext(env);
    
    javax.naming.directory.Attributes attributes = dirContext.getAttributes(domain, new String[] { "TXT" });
    attributes.get("TXT");
    NamingEnumeration e = attributes.getAll();
    while (e.hasMore()) {
        if (e.toString() == null ? uuid == null : e.toString().equals(uuid)){
            map.replace("confirmed", "true");
            connection.sync().hmset("tag", map);
            return true;
        }
        e.next();
    }
    return false;
}

public static boolean blockedImageFound (String uuid, String md5) {
  return connection.sync().hexists("user:" + uuid + "blocked:images", md5);  
};

public static boolean blockedImageNotFound (String uuid, String md5) {
    return !blockedImageFound(uuid, md5);
};  

public static boolean negativeKeywordFound (String uuid, List<String> words) {
  return words.stream().anyMatch((keyword) -> (connection.sync().hexists("user:" + uuid + "negative:keywords", keyword)));  
};

public static boolean negativeKeywordNotFound (String uuid, List<String> words) {
    return !negativeKeywordFound(uuid, words);
};

public static List<String> getWords(String text) {
    List<String> words = new ArrayList<>();
    BreakIterator breakIterator = BreakIterator.getWordInstance();
    breakIterator.setText(text);
    int lastIndex = breakIterator.first();
    while (BreakIterator.DONE != lastIndex) {
        int firstIndex = lastIndex;
        lastIndex = breakIterator.next();
        if (lastIndex != BreakIterator.DONE && Character.isLetterOrDigit(text.charAt(firstIndex))) {
            if (!words.contains(text.substring(firstIndex, lastIndex))){
                words.add(text.substring(firstIndex, lastIndex));
            }
        }
    }
    return words;
}

public static List<String> getHashTags(String text) {
    List<String> hashTags = new ArrayList<>();
    
    getWords(text).stream().filter((s) -> (s.startsWith("#"))).forEach((s) -> {
        hashTags.add(s);
    });
    return hashTags;
}

public static Boolean isAuthenticated (String token) {
    return connection.sync().get ("user:" + token + ":uid") != null;
}

public static Boolean isFollowing (String token, String uuid) {
    
    if (isAuthenticated(token))    
        return connection.sync().sismember("user:" + connection.sync().get ("user:" + token + ":uid") + ":following", uuid);
    else
        return false;
}

public static Boolean sendFriendRequest (String token, String friendRequestUUID) {
    String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null) {
        if (!connection.sync().sismember (authenticatedUser, friendRequestUUID)){
            connection.sync().multi();
            connection.sync().incr("user:" + authenticatedUser + ":friend:requests:outbound:count");
            connection.sync().incr("user:" + friendRequestUUID + ":friend:requests:inbound:count");
            connection.sync().sadd("user:" + authenticatedUser + ":friend:requests:outbound", friendRequestUUID);
            connection.sync().exec();
            return connection.sync().sadd("user:" + friendRequestUUID + ":friend:requests:inbound", authenticatedUser) == 1;
        } else
            return false;
    }
    else
        return false;
}

public static void setConnectionEdgeScore (String token, String uuid, EdgeScore.Groups g, EdgeScore.Multipliers m){
    String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null) {
        connection.sync().multi();
        connection.sync().hset("user:" + uuid, "group", g.toString());
        connection.sync().hset("user:" + uuid, "multiplier", m.toString());
        connection.sync().exec();
    }
}

public static Boolean deleteFriendRequest (String token, String friendRequestUUID) {
    
   String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null) {
        if (connection.sync().sismember ("user:" + token + ":uid", friendRequestUUID)){
            connection.sync().multi();
            connection.sync().decr("user:" + authenticatedUser + ":friend:requests:outbound:count");
            connection.sync().decr("user:" + friendRequestUUID + ":friend:requests:inbound:count");
            connection.sync().srem("user:" + authenticatedUser + ":friend:requests:outbound", friendRequestUUID);
            connection.sync().exec();
            return connection.sync().srem("user:" + friendRequestUUID + ":friend:requests:inbound", authenticatedUser) == 1;
        } else
            return false;
    }
    else
        return false;
}

public static void setConnectionZScore (String token, String UUID, Integer edgeScore, EdgeScore e) {
    
    String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null)
        connection.sync().set ("user:" + token + ":uid:" + authenticatedUser + ":connection:edgescore:" + UUID, edgeScore.toString());
}

public static Integer getConnectionZScore (String authenticatedUser, String UUID) {
    String edgeScore = "0";
    if (authenticatedUser != null)
      edgeScore = connection.sync().get ("user:" + authenticatedUser + ":connection:edgescore:" + UUID);
    
    return Integer.parseInt(edgeScore);
}

public static double getSocialImportance (String UUID) {
    return connection.sync().zscore("user:social:importance", UUID);
}

public static double getSocialIsolationFactor (String UUID) {
    return connection.sync().zscore("user:social:isolation", UUID);
}

public static void incrementSocialIsolationFactor (String token, String UUID) {
    String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null){
        connection.sync().zincrby("user:social:isolation", getConnectionZScore(token, UUID),UUID);
    }
}

public static void decrementSocialIsolationFactor (String token, String UUID) {
    String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null){
        connection.sync().zincrby("user:social:isolation",Integer.parseInt("-" + getConnectionZScore(token, UUID).toString()),UUID);
    }
}

public static void incrementSocialImportance (String token, String UUID) {
    String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null){
        connection.sync().zincrby("user:social:importance",getConnectionZScore(token, UUID),UUID);
    }
}

public static void decrementSocialImportance (String token, String UUID) {
    String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null){
        connection.sync().zincrby("user:social:importance",Integer.parseInt("-" + getConnectionZScore(token, UUID).toString()),UUID);
    }
}

public static void setProfilePicture (String token, String url) {
    String authenticatedUser = connection.sync().get ("user:" + token + ":uid");
    if (authenticatedUser != null){
        connection.sync().set("user:" + authenticatedUser + ":profile:picture",url);
    }
}

public static Boolean isAzure() throws UnknownHostException{  
  return InetAddress.getLocalHost().getHostAddress().startsWith("172.16.0");
}

public static void timerStart() {
    APITimer = System.currentTimeMillis();
}

public static long timerEnd() {
    long ms = System.currentTimeMillis()-APITimer;
    APITimer  = System.currentTimeMillis();
    return ms;
}

public static String unixtime() {
    return Long.toString(System.currentTimeMillis() / 1000L);
}

public static StatefulRedisConnection<String, String> redisConnection() {
    return redisClient.connect();
}

}

