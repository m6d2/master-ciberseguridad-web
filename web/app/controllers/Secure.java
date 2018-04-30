package controllers;


import helpers.HashUtils;
import models.User;
import play.Play;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.utils.Java;

import java.lang.reflect.InvocationTargetException;

public class Secure extends Controller {


    /**
     * Autenticación
     * @throws Throwable
     */
    @Before(unless={"login", "authenticate", "logout", "PublicContentBase.registerComplete", "PublicContentBase.processRegister", "PublicContentBase.register"})
    public static void checkAccess() throws Throwable {

             // Authentication
             if (!session.contains("username") || session.get("username").isEmpty()) {
                     flash.put("url", "GET".equals(request.method) ? request.url : Play.ctxPath + "/"); // seems a good default
                     login();
                 }

             // Checks
             Check check = getActionAnnotation(Check.class);
             if(check != null) {
                     check(check);
                 }
             check = getControllerInheritedAnnotation(Check.class);
             if(check != null) {
                     check(check);
                 }
     }


    /**
     * Miguel Seisdedos
     * - Control de AUTORIZACION por perfiles basado en anotaciones
     * @param check
     * @throws Throwable
     */
    private static void check(Check check) throws Throwable {
        for(String profile : check.value()) {
            boolean hasProfile = (Boolean)Security.invoke("check", profile);
            if(!hasProfile) {
                Security.invoke("onCheckFailed", profile);
            }
        }
    }


    public static void login(){
        render();
    }

    public static void logout(){
        session.remove("username");
        session.remove("password");
        login();
    }

    public static void authenticate(String username, String password){
        User u = User.loadUser(username);
        if (u != null && u.getPassword().equals(HashUtils.getMd5(password))){
            session.put("username", username);
            session.put("password", password);
            Application.index();
        }else{
            flash.put("error", Messages.get("Public.login.error.credentials"));
            login();
        }

    }


    public static class Security extends Controller {

        /**
         * @Deprecated
         *
         * @param username
         * @param password
         * @return
         */
        static boolean authentify(String username, String password) {
            throw new UnsupportedOperationException();
        }

        /**
         * This method is called during the authentication process. This is where you check if
         * the user is allowed to log in into the system. This is the actual authentication process
         * against a third party system (most of the time a DB).
         *
         * @param username
         * @param password
         * @return true if the authentication process succeeded
         */
        static boolean authenticate(String username, String password) {
            return true;
        }

        /**
         * This method checks that a profile is allowed to view this page/method. This method is called prior
         * to the method's controller annotated with the @Check method.
         *
         * @param profile
         * @return true if you are allowed to execute this controller method.
         */
        static boolean check(String profile) {
            return true;
        }

        /**
         * This method returns the current connected username
         * @return
         */
        static String connected() {
            return session.get("username");
        }

        /**
         * Indicate if a user is currently connected
         * @return  true if the user is connected
         */
        static boolean isConnected() {
            return session.contains("username");
        }

        /**
         * This method is called after a successful authentication.
         * You need to override this method if you with to perform specific actions (eg. Record the time the user signed in)
         */
        static void onAuthenticated() {
        }

        /**
         * This method is called before a user tries to sign off.
         * You need to override this method if you wish to perform specific actions (eg. Record the name of the user who signed off)
         */
        static void onDisconnect() {
        }

        /**
         * This method is called after a successful sign off.
         * You need to override this method if you wish to perform specific actions (eg. Record the time the user signed off)
         */
        static void onDisconnected() {
        }

        /**
         * This method is called if a check does not succeed. By default it shows the not allowed page (the controller forbidden method).
         * @param profile
         */
        static void onCheckFailed(String profile) {
            forbidden();
        }

        private static Object invoke(String m, Object... args) throws Throwable {

            try {
                return Java.invokeChildOrStatic(Security.class, m, args);
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }

    }

}
