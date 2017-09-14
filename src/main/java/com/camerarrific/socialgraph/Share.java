/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camerarrific.socialgraph;

import com.google.gson.Gson;
import com.lambdaworks.redis.RedisFuture;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.camerarrific.socialgraph.Storage.*;
/**
 *
 * @author adam@filterzilla.camera
 */

public class Share {
    
    public static String Photo (String authenticatedUser, String content, String url){
        return statusUpdate(authenticatedUser, content, "photo", url);
    }
    
    public static String Photo (String authenticatedUser, String content, byte[] bytes){
        
        String url = null;
        String md5 = null;
        
        if (bytes.length > 0){
                md5 = Util.getMD5(bytes);
                url = Blobs.upload(bytes);
            }
        
        return statusUpdate(authenticatedUser, content, "photo", url, md5);
    }
    
    public static String Video (String authenticatedUser, String content, String url){
        return statusUpdate(authenticatedUser, content, "video", url);
    }
    
    public static String share (String authenticatedUser, String postID){
        if (authenticatedUser != null){
                api.timerStart();
                Map<String, String> map = Redis.hash.map.get("post:" + postID);
                List<String> keywords = api.getWords(map.get("content"));
                pushGraph(authenticatedUser, postID, keywords);
                map.put("duration", String.valueOf(api.timerEnd()));
            return new Gson().toJson(map);
            }
            return null;    
    }
    
    private static String statusUpdate (String authenticatedUser, String content, String type, String url) {
        return statusUpdate(authenticatedUser, content, type, url, null);
    }
    
    private static String statusUpdate (String authenticatedUser, String content, String type, String url, String MD5) {
            
            if (authenticatedUser != null){
                api.timerStart();
                String postID = Util.UUID();
                            
                Map<String, String> map = new LinkedHashMap<>();
                                    map.put("id", postID);
                                    map.put("type", type);
                                    map.put("uid", authenticatedUser);
                                    map.put("content", content);
                                    map.put("url", url);
                                    map.put("created", api.unixtime());
                                    
               if (MD5 != null){
                   map.put("md5", MD5);
               }
            
            List<String> keywords = api.getWords(content);                             
            
            Logger.getLogger(api.class.getName()).log(Level.INFO, map.toString(), "postStatus:map");
            
            api.connection.sync().multi();
            api.connection.sync().hmset("post:" + postID, map); 
            api.connection.sync().lpush("user:" + authenticatedUser + ":timeline", postID);
            
            if (map.get("type") == "photo"){
                api.connection.sync().hincrby("photos", authenticatedUser, 1);
            } else if (map.get("type") == "videos"){
                api.connection.sync().hincrby("videos", authenticatedUser, 1);            
            }
            api.connection.sync().exec();
            
            pushGraph(authenticatedUser, postID, keywords);
            map.put("duration", String.valueOf(api.timerEnd()));
            
            return new Gson().toJson(map);
            }
            return null;
    }
    
    // intersected push lists, topics, interest lists.
    
    
    public static void pushGraph(String authenticatedUser, String postID, List<String> keywords){
        
        RedisFuture<Long> count = api.connection.async().smembers((String value) -> {
             if (api.negativeKeywordNotFound(value, keywords)){
//                if (api.negativeKeywordNotFound(value, keywords) && api.blockedImageNotFound(value, keywords)){
                    api.connection.sync().lpush("user:" + value + ":timeline", postID);
                    api.connection.sync().zadd("user:"  + value + ":timeline:personal:importance", api.getConnectionZScore(authenticatedUser, value));
                    api.connection.sync().zadd("user:"  + value + ":timeline:everyone:importance", api.getSocialImportance(value), value);
                }
            }, "user:" + authenticatedUser + ":followers");
            
            while (!count.isDone()) {
                // do nothing ...
                }
    }
}