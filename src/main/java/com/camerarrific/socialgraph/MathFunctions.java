/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camerarrific.socialgraph;
import static com.camerarrific.socialgraph.Util.BigIntegerUUIDArray;
import static com.camerarrific.socialgraph.Util.UUID;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigInteger;
/**
 *
 * @author adam@filterzilla.camera
 */
public class MathFunctions {
     
//    private Operations mathOperations;
        
        public enum Operations {
        
        Add(1),
        Subtract(2),
        Multiply(3),
        Divide(4),
        ShiftLeft(5),
        ShiftRight(6);
        
        private final Integer value;
        
        private Operations(Integer value) {
                this.value = value;
        }
         
        public Integer toInt(){
        return this.value;
    }
 }
        
   
}


//public String OperationString(Operations operation){
//    
//    String operationValue = null;
//    
//    switch (operation.value){
//        case 1:
//            operationValue = "+";
//            break;
//        case 2:
//            operationValue = "-";
//            break;
//        case 3:
//            operationValue = "*";
//            break;
//        case 4:
//            operationValue = "/";
//            break;
//        case 5:
//            operationValue = "<";
//            break;
//        case 6:
//            operationValue = ">";
//            break;
//    }
//    
//    return operationValue;
//    
//}
     
//public String PerformOperation(Operations operation, BigInteger[] series){
//    
//    BigInteger deviceBigInt = new BigInteger(UUID(),16);
//    
//    BigInteger[] newSeries = BigIntegerUUIDArray(10);
//    
//    switch (operation.value){
//        case 1:
//            operationValue = "+";
//            break;
//        case 2:
//            operationValue = "-";
//            break;
//        case 3:
//            operationValue = "*";
//            break;
//        case 4:
//            operationValue = "/";
//            break;
//        case 5:
//            operationValue = "<";
//            break;
//        case 6:
//            operationValue = ">";
//            break;
//    }
//    
//    return operationValue;
//    
//}
        
//    public String GenerateError(Operations error){
//        JsonObject jsonObj = new JsonObject();
//        
//        switch (error.value) {
//            case 1: jsonObj.addProperty("error", "cannot_follow");
//                    jsonObj.addProperty("error_description", "already following");
//                    break;
//            case 2: jsonObj.addProperty("error", "cannot_follow");
//                    jsonObj.addProperty("error_description", "cannot follow yourself");
//                    break;
//            case 3: jsonObj.addProperty("error", "cannot_unfollow");
//                    jsonObj.addProperty("error_description", "cannot unfollow yourself");
//                    break;
//            case 4: jsonObj.addProperty("error", "incomplete_request");
//                    jsonObj.addProperty("error_description", "follower missing from request");
//                    break;
//            case 5: jsonObj.addProperty("error", "auth_error");
//                    jsonObj.addProperty("error_description", "token invalid");
//                    break;
//            case 6: jsonObj.addProperty("error", "incomplete_request");
//                    jsonObj.addProperty("error_description", "token missing from request");
//                    break;
//            case 7: jsonObj.addProperty("error", "invalid_grant");
//                    jsonObj.addProperty("error_description", "invalid username or password");
//                    break;
//            case 8: jsonObj.addProperty("error", "internal_server_error");
//                    jsonObj.addProperty("error_description", "password hash failure");
//                    break;
//            case 9: jsonObj.addProperty("error", "cannot_unfollow");
//                    jsonObj.addProperty("error_description", "not following");
//                    break;
//            case 10: jsonObj.addProperty("error", "cannot_register");
//                     jsonObj.addProperty("error_description", "already registered");
//                     break;
//            case 11: jsonObj.addProperty("error", "cannot_login");
//                     jsonObj.addProperty("error_description", "user banned");
//                     break;
//            case 12: jsonObj.addProperty("error", "cannot_login");
//                     jsonObj.addProperty("error_description", "account disabled");
//                     break;
//            case 13: jsonObj.addProperty("error", "cannot_login");
//                     jsonObj.addProperty("error_description", "temporary lockout");
//                     break;
//            case 14: jsonObj.addProperty("error", "cannot_login");
//                     jsonObj.addProperty("error_description", "account not activated");
//                     break;
//            case 15: jsonObj.addProperty("error", "cannot_perform_action");
//                     jsonObj.addProperty("error_description", "post not found");
//                     break;
//        }
//        return jsonObj.toString();
//    }   
     
     
