/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camerarrific.socialgraph;

import com.camerarrific.socialgraph.Verbs.Action;
import static com.camerarrific.socialgraph.api.connection;
import static com.camerarrific.socialgraph.api.timerEnd;
import static com.camerarrific.socialgraph.api.timerStart;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lambdaworks.redis.RedisFuture;
import static spark.Spark.get;
import static spark.Spark.post;

//{
//  "@context": "http://www.w3.org/ns/activitystreams",
//  "@type": "Like",
//  "actor": {
//    "@type": "Person",
//    "displayName": "Sally"
//  },
//  "object": "http://example.org/notes/1"
//}

public class Actions {
    
    private static Integer actionsCounter;
    
    public static String List (Action action, String post, Integer index, Integer count){
            
        if (connection.sync().exists("post:" + post)){
            timerStart();
            actionsCounter = 0;
            JsonObject rootObject  = new JsonObject();
            JsonArray timelineEntities = new JsonArray();
            RedisFuture<Long> futureCount = api.connection.async().lrange((String value) -> {
            actionsCounter++;
            
            timelineEntities.add(actionObject(action, User.key(value, "username"), User.key(value, "fullname"), value));
            
            }, "post:" + post + action.key(), index, index + count);
            
            while (!futureCount.isDone()) {
                   // do something ...
            }
            rootObject.add(action.plural(), timelineEntities);
            rootObject.addProperty("object", post);
            rootObject.addProperty("count", actionsCounter);
            rootObject.addProperty("duration", timerEnd());
            return rootObject.toString();
        } else {
            return new ServerResponses().GenerateError(ServerResponses.Errors.CannotPerformActionPostNotFound);
        }
    }
    
    public static String Contains (Action action, String id, String authenticatedUser){   
        return ((connection.sync().hget("post:" + id + ":" + authenticatedUser + ":",
                     action.noun())) != null) ? "true" : "false";  
    }
      
    public static String Perform (Action action, String id, String authenticatedUser){
        
        if (connection.sync().exists("post:" + id)){    
            JsonObject jsonObj = new JsonObject();
            if (connection.sync().lpush("post:" + id + action.key(), authenticatedUser) == 1){
                connection.sync().hsetnx("post:" + id + ":" + authenticatedUser + ":", action.noun(), "1");
                jsonObj.addProperty(action.pastTense() + " Post", id);
            } else {
                jsonObj.addProperty("Already " + action.pastTense() + " Post", id);
            }
            return jsonObj.toString();
        } else {
            return new ServerResponses().GenerateError(ServerResponses.Errors.CannotPerformActionPostNotFound);
        } 
    }
    
    public static String Reverse (Action action, String id, String authenticatedUser){
        if (connection.sync().exists("post:" + id)){ 
            JsonObject jsonObj = new JsonObject();
            if (connection.sync().lrem("post:" + id + action.key(), 0, authenticatedUser) == 1){
                connection.sync().hdel("post:" + id + ":" + authenticatedUser + ":", action.noun());
                jsonObj.addProperty("Un" + action.pastTense() + " Post", id);
            } else {
                jsonObj.addProperty("Cannot un" + action.noun() + ", not " + action.noun(), id);
            }
            return jsonObj.toString();
        } else {
            return new ServerResponses().GenerateError(ServerResponses.Errors.CannotPerformActionPostNotFound);
        } 
    }
       
    private static JsonObject actionObject (Action action, String username, String fullname, String value){
        JsonObject likeObject  = new JsonObject();
        JsonObject actorObject = new JsonObject();
                   actorObject.addProperty("@type", "person");
                   actorObject.addProperty("username", username);
                   actorObject.addProperty("displayName", fullname);
                   actorObject.addProperty("uuid", value);
                   likeObject.add("actor", actorObject);
    return likeObject;
    }
}
