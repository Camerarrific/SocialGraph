package com.camerarrific.socialgraph;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 *
 * @author adam
 */
public class HashMaps {

    public static class JSONHashMap extends LinkedHashMap {

        public String toJSON() throws IOException {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        }

        public static Map<String, String> fromJSON(String json) throws IOException {
            JSONHashMap jsonHashMap = new JSONHashMap();
            Map<String, String> map = new ObjectMapper().readValue(json, new TypeReference<Map<String, String>>() {});
            jsonHashMap.putAll(map);
            return jsonHashMap;
        }
    }

    public static class User extends JSONHashMap{

        static User create(String uid, String email, String hash, String salt, String poly) {
            return new User(uid, email, hash, salt, poly);
        }
        
        private Map<String, String> userHashMap = new LinkedHashMap<>();

        private User(String uid, String email, String hash, String salt, String poly) {
            userHashMap = new JSONHashMap();
            userHashMap.put("passwordHash", hash);
            userHashMap.put("uuid", uid);
            userHashMap.put("email", email);
            userHashMap.put("salt", salt);
            userHashMap.put("poly", poly);
        }

        public Map<String, String> getHashMap() {
            return this.userHashMap;
        }
    }

}
