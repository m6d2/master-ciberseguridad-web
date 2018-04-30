package controllers;


import com.google.gson.JsonObject;
import models.User;
import play.mvc.Controller;

public class Api extends Secure {

    public static void removeAllUsers(){
        User.removeAll();
        renderJSON(new JsonObject());
    }
}
