/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camerarrific.socialgraph;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author adam@filterzilla.camera
 */
public class Mood {
    
    public enum CurrentMood {
        Angry(100),
        Excited(50),
        Happy(40),
        Peaceful(30),
        Content(20),
        Sad(10),
        Bored(5),
        Anxious(2);
        
        private final Integer value;

        private CurrentMood(Integer value) {
            this.value = value;
        }
        
        public enum EmotionalContentType {
        Funny(100),
        Joyful(50),
        Positive(40),
        Interesting(30),
        Sad(20),
        Empathy(10),
        Compassion(5),
        Loving(2);
        
        private final Integer value;
        
        private EmotionalContentType(Integer value) {
            this.value = value;
        }
        
        private Map<String, String> AnxiousMap(){
            Map<String, String> map = new LinkedHashMap<>();
            map.put(Funny.toString(), "1");
            map.put(Compassion.toString(), "2");
            map.put(Loving.toString(), "3");
            map.put(Joyful.toString(), "4");
            
            return map;
        }
        
         private Map<String, String> SadMap(){
            Map<String, String> map = new LinkedHashMap<>();
            map.put(Funny.toString(), "1");
            map.put(Joyful.toString(), "4");
            
            return map;
        }
        
        private Map<String, String> MoodConversionMap(CurrentMood currentMood){
            if (currentMood == Anxious){
                return AnxiousMap();
            }
            return null;
        }
        }
    }
}
