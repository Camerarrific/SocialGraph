/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camerarrific.socialgraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ServerResponses {
    private Errors error;
    
    public enum Errors {
        
        AlreadyFollowing(1),
        CannotFollowYourself(2),
        CannotUnfollowYourself(3),
        FollowerMissing(4),
        TokenInvalid(5),
        TokenMissing (6),
        InvalidLoginCredentials(7),
        PasswordHashFailure(8),
        NotFollowing(9),
        AlreadyRegistered(10),
        AccountBanned(11),
        AccountDisabled(12),
        TemporaryLockout(13),
        AccountNotActivated(14),
        CannotPerformActionPostNotFound(15),
        CannotFollowUnknownUser(16),
        CannotUnfollowUnknownUser(17);
        
        private final Integer value;

        private Errors(Integer value) {
                this.value = value;
        }
    }
    
    public String GenerateError(Errors error){
        JsonObject jsonObj = new JsonObject();
        
        switch (error.value) {
            case 1: jsonObj.addProperty("error", "cannot_follow");
                    jsonObj.addProperty("error_description", "already following");
                    break;
            case 2: jsonObj.addProperty("error", "cannot_follow");
                    jsonObj.addProperty("error_description", "cannot follow yourself");
                    break;
            case 3: jsonObj.addProperty("error", "cannot_unfollow");
                    jsonObj.addProperty("error_description", "cannot unfollow yourself");
                    break;
            case 4: jsonObj.addProperty("error", "incomplete_request");
                    jsonObj.addProperty("error_description", "follower missing from request");
                    break;
            case 5: jsonObj.addProperty("error", "auth_error");
                    jsonObj.addProperty("error_description", "token invalid");
                    break;
            case 6: jsonObj.addProperty("error", "incomplete_request");
                    jsonObj.addProperty("error_description", "token missing from request");
                    break;
            case 7: jsonObj.addProperty("error", "invalid_grant");
                    jsonObj.addProperty("error_description", "invalid username or password");
                    break;
            case 8: jsonObj.addProperty("error", "internal_server_error");
                    jsonObj.addProperty("error_description", "password hash failure");
                    break;
            case 9: jsonObj.addProperty("error", "cannot_unfollow");
                    jsonObj.addProperty("error_description", "not following");
                    break;
            case 10: jsonObj.addProperty("error", "cannot_register");
                     jsonObj.addProperty("error_description", "already registered");
                     break;
            case 11: jsonObj.addProperty("error", "cannot_login");
                     jsonObj.addProperty("error_description", "user banned");
                     break;
            case 12: jsonObj.addProperty("error", "cannot_login");
                     jsonObj.addProperty("error_description", "account disabled");
                     break;
            case 13: jsonObj.addProperty("error", "cannot_login");
                     jsonObj.addProperty("error_description", "temporary lockout");
                     break;
            case 14: jsonObj.addProperty("error", "cannot_login");
                     jsonObj.addProperty("error_description", "account not activated");
                     break;
            case 15: jsonObj.addProperty("error", "cannot_perform_action");
                     jsonObj.addProperty("error_description", "post not found");
                     break;
            case 16: jsonObj.addProperty("error", "cannot_follow");
                    jsonObj.addProperty("error_description", "cannot follow unknown user");
                    break;
            case 17: jsonObj.addProperty("error", "cannot_unfollow");
                    jsonObj.addProperty("error_description", "cannot unfollow unknown user");
                    break;
        }
        return jsonObj.toString();
    }   
}