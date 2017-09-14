
package com.camerarrific.socialgraph;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adam
 */


public class JSONObjects {

    
    public class OAuthObject {

	private String username;
	private String token;
	private String uid;
	private int expires_in;
        
        public OAuthObject(String username, String token, String uid, int expires_in){
            this.username = username;
            this.token = token;
            this.uid = uid;
            this.expires_in = expires_in;
        }

        public OAuthObject() {
            
        }

        public OAuthObject create(String username, String token, String uid, int expires_in){
            OAuthObject o = new OAuthObject();
            o.username = username;
            o.token = token;
            o.uid = uid;
            o.expires_in = expires_in;
            return o;
        }
        
        public void setUsername(String username){
            this.username = username;
        }
        
        public void setToken(String token){
            this.token = token;
        }
        
        public void setUid(String uid){
            this.uid = uid;
        }
        
        public void setExpiresIn(int expires_in){
            this.expires_in = expires_in;
        }
        
        
        public String serialise(){
            try {
                return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
            } catch (JsonProcessingException ex) {
                Logger.getLogger(JSONObjects.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
   
}
