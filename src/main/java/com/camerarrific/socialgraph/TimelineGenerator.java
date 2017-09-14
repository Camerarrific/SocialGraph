/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camerarrific.socialgraph;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lambdaworks.redis.RedisFuture;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adam@filterzilla.camera
 */
public class TimelineGenerator {
   
    private static String uuid;
    private static Integer idx;
    private static Integer cnt;
    private static Integer counter;
    private static JsonObject timeline;
    private static JsonArray timelineEntities;
    private static final String PERSONAL = "personal";
    private static final String EVERYONE = "everyone";
    
    public enum Importance {
        PERSONAL("personal"),
        EVERYONE("everyone");
    
        private final String text;

        /**
        * @param text
        */
        private Importance(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
        * @see java.lang.Enum#toString()
        */
        @Override
        public String toString() {
            return text;
        }
    }
    
    private static void init (String authenticatedUser, Integer index, Integer count) {
        api.timerStart();
        counter = 0;
        uuid = authenticatedUser;
        idx = index;
        cnt = count;
        timeline = new JsonObject();
        timelineEntities = new JsonArray();
    }
    
    public static String fifo(String authenticatedUser, Integer index, Integer count){
        init(authenticatedUser, index, count);
        RedisFuture<Long> futureCount = api.connection.async().lrange((String value) -> {
             Logger.getLogger(api.class.getName()).log(Level.INFO, value, "post:id");
                    generatePost(value);
            }, "user:" + uuid + ":timeline",idx,idx+cnt);
            
            while (!futureCount.isDone()) {
                // do something ...
            }
            return generateJSON();
    }
    
    public static String socialImportance(String authenticatedUser, Integer index, Integer count, Importance importanceType){
        init(authenticatedUser, index, count);
        RedisFuture<Long> futureCount = api.connection.async().zrange((String value) -> {
                          generatePost(value);
            }, "user:" + uuid + ":timeline:" + importanceType.toString() + ":importance",idx,idx+cnt);
            
            while (!futureCount.isDone()) {
                // do something ...
            }
            return generateJSON();
    }
    
    private static void generatePost(String value){
        Logger.getLogger(api.class.getName()).log(Level.INFO, value, "post:id");
        Map<String, String> post = api.connection2.sync().hgetall("post:" + value);
        Logger.getLogger(api.class.getName()).log(Level.INFO, post.toString(), "post:map");
                    String username = User.key(post.get("uid"), "username");                   
                    String fullname = User.key(post.get("uid"), "fullname");
                    JsonObject postObject = new JsonObject();
                    JsonObject actorObject = new JsonObject();
                    
                    if (post.get("id") != null){
                        postObject.addProperty("uuid", post.get("id"));
                        //postObject.addProperty("isLiked", Actions.Contains(Verbs.Action.LIKE, post.get("id"), uuid));
                    }
                    if (post.get("type") != null)
                        postObject.addProperty("type", post.get("type"));
                    if (post.get("content") != null)
                        postObject.addProperty("content", post.get("content"));    
                    if (post.get("url") != null)
                        postObject.addProperty("url", post.get("url"));
                    if (post.get("created") != null)
                        postObject.addProperty("created", post.get("created"));
                    if (post.get("md5") != null)
                        postObject.addProperty("created", post.get("md5"));
                    
                    
                    
                    JsonObject rootObject = new JsonObject();
                    rootObject.add("activity", postObject);
                    
                    if (username != null){
                            actorObject.addProperty("username", username);
                            actorObject.addProperty("fullname", fullname);
                            actorObject.addProperty("uuid", post.get("uid"));
                            rootObject.add("actor", actorObject);
                    }
                    timelineEntities.add(rootObject);
                    Logger.getLogger(api.class.getName()).log(Level.INFO, rootObject.toString(), "postStatus:map");
                    counter++;
    }
    
    private static String generateJSON(){
            timeline.add("entities", timelineEntities);
            timeline.addProperty("count", counter);
            timeline.addProperty("duration", api.timerEnd());
            return timeline.toString();
    }
}