package com.camerarrific.socialgraph;

import com.camerarrific.socialgraph.HashMaps.JSONHashMap;
import com.camerarrific.socialgraph.Storage.Redis;
import static com.camerarrific.socialgraph.api.connection;
import static com.camerarrific.socialgraph.api.timerEnd;
import static com.camerarrific.socialgraph.api.timerStart;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lambdaworks.redis.LettuceFutures;
import static com.lambdaworks.redis.LettuceStrings.string;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.api.sync.RedisCommands;
import java.rmi.server.UID;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.hash;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.RandomStringUtils;



public class User {
        static Map<String, String> users = new LinkedHashMap<>();
        
	static Properties mailServerProperties;
	static Session getMailSession;
	static MimeMessage generateMailMessage;
        
        private static final String FOLLOWING = "following";
        private static final String FOLLOWERS = "followers";
        private static final String BLOCKED = "blocked";
        private static final String BLOCKERS = "blockers";
        
        public class map {
        
        private Map<String,String> user;  
        private String username;
           
            public map(String username) {
                this.user = User.getHashMap(username);
                this.username = username;
            }
            
            public String uid(){
                return this.user.get("uuid");
            }
  
            public String fullname(){
                return this.user.get("fullname");
            }
            
            public String email(){
                return this.user.get("email");
            }
            
            public String followers(){
                return this.user.get("followers");
            }
            
            public String following(){
                return this.user.get("following");
            }
            
            public String salt(){
                return this.user.get("salt");
            }
            
            public String passwordHash(){
                return this.user.get("passwordHash");
            }
            
            public String poly(){
                return this.user.get("poly");
            }
                        
        }
        
	public static void sendActivationEmail(String uid, String mailto) throws AddressException, MessagingException {
                
                String activationUUID = Util.UUID();
                
                api.connection.sync().multi();
                api.connection.sync().set("user:" + uid + ":activation:uuid", activationUUID);
                api.connection.sync().set("user:activations:" + activationUUID + ":uid",uid);
                api.connection.sync().exec();
                
                String activationLink = "http://localhost:4567/api/activate?token=" + activationUUID;
                String mailServerIntranet = "mailserver.activator.a9.internal.cloudapp.net";
                String mailServerInternet = "activator.cloudapp.net";
                String username = "support";
                String password = "8eB!B4$7D4F&491a9$69%78!C8038*F#";
                
                String emailBody = "Please use the link below to activate your account."
                                 + "<br><br>" + activationLink 
                                 + "<br><br>" + "<a href=" + activationLink + "\" target=\"_blank\">activationLink</a>"
                                 + "<br><br> Regards, <br>FilterZilla Admin";
                                 
		mailServerProperties = new Properties();
                mailServerProperties.put("mail.smtp.host", mailServerInternet);
		mailServerProperties.put("mail.smtp.port", "587");
		mailServerProperties.put("mail.smtp.auth", "no");
		mailServerProperties.put("mail.smtp.starttls.enable", "no");
                mailServerProperties.put("mail.smtp.debug", "true");
                
		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		generateMailMessage = new MimeMessage(getMailSession);
                generateMailMessage.setFrom(new InternetAddress("adam@enigmaticflare.co.uk"));
		generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress("adam@enigmaticflare.co.uk"));
		generateMailMessage.setSubject("Greetings from FilterZilla");
		generateMailMessage.setContent(emailBody, "text/html");
                
		Transport transport = getMailSession.getTransport("smtp");
                transport.connect();
//		transport.connect(mailServerInternet, username, password);
		transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
		transport.close();
	}
  
        
  public static String members (String uuid, String set){
      api.timerStart();
      api.postCount = 0;
            
            JsonObject rootObject = new JsonObject();
            JsonArray followingEntities = new JsonArray();
              
            StatefulRedisConnection<String, String> redis = api.redisConnection();
            StatefulRedisConnection<String, String> redis2 = api.redisConnection();

            RedisFuture<Long> fc = redis.async().smembers((String value) -> {
                String username = redis2.sync().hget("user:uid", value);
                JsonObject actorObject = new JsonObject();
                           actorObject.addProperty("username", username);
                           actorObject.addProperty("fullname", redis2.sync().hget("user:" + username, "fullname"));
                           actorObject.addProperty("uid", value);
                     followingEntities.add(actorObject);
                     api.postCount++;
                 }, "user:" + uuid + ":" + set);
                
                 while (!fc.isDone()) {
                    // do nothing ...
                 }
                 redis.close();
                 redis2.close();
                rootObject.add(set, followingEntities);
                rootObject.addProperty("count", api.postCount);
                rootObject.addProperty("duration", api.timerEnd());
                return rootObject.toString();
  }

  public static Boolean Follow (String uuid, String follow) throws Exception{
      
      if (Redis.member.add("user:" + follow + ":followers", uuid) == 1){
          Redis.queue.Process(Redis.queue.commands.Follow(uuid, follow));
          return true;
      } else 
          return false;
  }
  
  public static Boolean Unfollow (String uuid, String unfollow){
      if(connection.sync().srem("user:" + unfollow + ":followers", uuid) == 1){
                           Redis.queue.Process(Redis.queue.commands.Unfollow(uuid, unfollow));
                           
                           return true;
      } else
          return false;
  }

public static Map<String, String> getHashMap(String username){ 
    return Redis.hash.map.get("username:" + username);
}
  
public static Boolean exists (String username){
    return Redis.key.exists("user:" + username);
}

  public static String authenticatedUser (String token){
      return Redis.key.get("tokens:" + token);
  }
  
  public static String salt (String username){
      return Redis.hash.field.get("user:" + username, "salt");
  }
  
  public static String passwordHash (String username){
      return Redis.hash.field.get("user:" + username, "passwordHash");
  }
  
  public static String poly (String username){
      return Redis.hash.field.get("user:" + username, "poly");
  }
  
  public static String username (String uid){
      return Redis.hash.field.get("user:uid", uid);
  }
  
  public static String uid (String username){
      return Redis.hash.field.get("user:" + username, "uuid");
  }
  
  public static String publicRSAKey (String uid){
      return Redis.hash.field.get("user:" + uid + ":crypto", "publicKey");
  }
  
  public static String key (String uid, String key){
      return Redis.hash.field.get("user:" + uid, key);
  }      
  
  public static String cacheHitMiss (String uid, String key){
      
      if (users.containsKey("user:" + uid + ":" + key)){
           Logger.getLogger(api.class.getName()).log(Level.INFO, "Hit Key:" + key + ":" + uid, key);
           return users.get((key + ":" + uid));
      } else {
          Logger.getLogger(api.class.getName()).log(Level.INFO, "Miss Key:" + key + ":" + uid, key);
          String value = api.connection.sync().get("user:" + uid + ":" + key);
          users.put(("user:" + uid + ":" + key), value);
          return value;
      }
  }
  
  
  public static String register(String username, String password, String email) throws NoSuchAlgorithmException{
      // create salt
            String salt = PasswordHash.createSalt();
            
            String hash = PasswordHash.createArgon2Hash(salt + password);
            
            String uid = Util.UUID();
            String poly = Util.UUID();
            // let's generate a OAUTH token
            String token = Util.UUID();
            
            JSONHashMap userHashMap = new JSONHashMap();
                                userHashMap.put("passwordHash", hash);
                                userHashMap.put("uuid", uid);
                                userHashMap.put("email", email);
                                userHashMap.put("salt", salt);
                                userHashMap.put("poly", poly);
            
            
            Redis.hash.map.set("user:" + username, userHashMap);
            Redis.hash.field.set("user:uid", uid, username);
            Redis.key.setWithExpireInSeconds("tokens:" + token, uid, 86400);
                    
            
            JsonObject jsonObj = new JsonObject();
                       jsonObj.addProperty("username", username);
                       jsonObj.addProperty("token", token);
                       jsonObj.addProperty("uid", uid);
                       jsonObj.addProperty("expires_in", 86400);
            return jsonObj.toString();
  }
  
  public static String login (String username, String password) throws NoSuchAlgorithmException, InvalidKeySpecException, Exception{
  
            timerStart(); 
//            r..hash.counters.increment("user:" + username, "polyCount");

//            String passwordHash = User.passwordHash(username);
//            
//            String salt = User.salt(username);
//            String poly = User.poly(username);
            //Logger.getLogger(api.class.getName()).log(Level.INFO, "login:get:hashes:duration:" + String.valueOf(timerEnd()),"");
            
            List<String> list = connection.sync().hmget("user:" + username, "passwordHash","salt","poly");
            
            Logger.getLogger(api.class.getName()).log(Level.INFO, "login:get:list:duration:" + String.valueOf(timerEnd()),"");
            
//            Map<String, String> userMap = connection.sync().hgetall("user:" + username);
//            Logger.getLogger(api.class.getName()).log(Level.INFO, "login:get:hgetall:duration:" + String.valueOf(timerEnd()),"");
            
            if (PasswordHash.validateArgon2Hash(list.get(1) + password, list.get(0))){
                Logger.getLogger(api.class.getName()).log(Level.INFO, "login:auth:success:duration:" + String.valueOf(timerEnd()),"");
                // Correct username + password, let's generate a OAUTH token                
                String token = Util.UUID();
                
                List<String> secondList = connection.sync().hmget("user:" + username, "uuid","followers","following");
                Logger.getLogger(api.class.getName()).log(Level.INFO, "login:get:secondList:duration:" + String.valueOf(timerEnd()),"");
                
                //Logger.getLogger(api.class.getName()).log(Level.INFO, "newToken:duration:" + String.valueOf(timerEnd()),"");
                //String uid = User.uid(username);
                //Logger.getLogger(api.class.getName()).log(Level.INFO, "uid:duration:" + String.valueOf(timerEnd()),"");
                
//                Number followers = connection.sync().hincrby("user:" + username, "followers", 0);
//                Number following = connection.sync().hincrby("user:" + username, "following", 0);
                
                
                HashMaps.JSONHashMap mapper = new HashMaps.JSONHashMap();             
                                mapper.put("username", username);
                                mapper.put("token", token);
                                mapper.put("uid", secondList.get(0));
                                mapper.put("expires_in", 86400);
                                mapper.put("followers",secondList.get(1));
                                mapper.put("following",secondList.get(2));
                
                //String poly = RandomStringUtils.random(32, "!@£$%^&*()_+=|.,<>ABCDEFGHIJKLMNOPRSTUVXYZabcdefghijklmnopqrstuvxyz1234567890~`€#ı◊ÌÊÂ‰„ŒÍÅÔÓÈØ");
                //passwordHash = PasswordHash.createArgon2Hash(poly + salt + password);

                ;                
                  
               connection.reactive().hsetnx("tokens:" + token, secondList.get(0), "86400").debounce(200, TimeUnit.MINUTES);
                 Logger.getLogger(api.class.getName()).log(Level.INFO, "reactive:duration:" + String.valueOf(timerEnd()),"");
                //Redis.key.setWithExpireInSeconds("tokens:" + token, secondList.get(0), 86400);
//                Redis.hash.field.set("user:" + username, "poly", poly);
//                Redis.hash.field.set("user:" + username, "passwordHash", passwordHash);
                    
                return mapper.toJSON();
            } else {
                return new ServerResponses().GenerateError(ServerResponses.Errors.InvalidLoginCredentials);
            }
  }
}