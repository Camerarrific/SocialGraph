/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camerarrific.socialgraph;
import static com.camerarrific.socialgraph.Util.UUID;
import static com.camerarrific.socialgraph.api.connection;
import java.math.BigInteger;
import java.util.Set;

/**
 *
 * @author adam@filterzilla.camera
 */

public class Devices {
    
    public static String Register(String username, String device){
      BigInteger deviceBigInt = new BigInteger(UUID(),16);
      if (api.connection.sync().sadd("user:" + username + ":devices", deviceBigInt.toString()) == 1)
          return "Device Registed";
      else
          return "Device Already Registered";
  }
  
  public static String Deregister(String username, String device){
      BigInteger deviceBigInt = new BigInteger(UUID(),16);
      if (api.connection.sync().srem("user:" + username + ":devices", deviceBigInt.toString()) == 1)
          return "Device Deregisted";
      else
          return "Not Removed: Device Not Registered";
  } 
  
  public static Boolean IsRegistered(String username, String device){
      if (api.connection.sync().sismember("user:" + username + ":devices", device))
          return true;
      else
          return false;
  }
  
  public static Set<String> List(String username){
      return api.connection.sync().smembers(username);
  }
}
