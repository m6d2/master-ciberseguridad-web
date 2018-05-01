package controllers;

import models.Constants;
import models.User;
import play.mvc.*;

import java.util.List;

@With(Secure.class)
public class Application extends Controller {

    private static void checkTeacher() throws Throwable {
        checkUser();

        User u = (User) renderArgs.get("user");
        if (!u.getType().equals(Constants.User.TEACHER)){
            return;
        }
    }

    private static void checkUser() throws Throwable {
        if (session.contains("username")){
            User u = User.loadUser(session.get("username"));
            if (u != null){
                renderArgs.put("user", u);
                return;
            }
        }
        Secure.login();
    }

    public static void index() throws Throwable {
        checkUser();

        User u = (User) renderArgs.get("user");

        if (u.getType().equals(Constants.User.TEACHER)){
            List<User> students = User.loadStudents();
            render("Application/teacher.html", u, students);
        }else{
            render("Application/student.html", u);
        }
    }


    public static void removeStudent(String student) throws Throwable {
        checkTeacher();

        User.remove(student);
        index();
    }


    public static void setMark(String student) {
        User u = User.loadUser(student);
        render(u);
    }

    public static void doSetMark(String student, Integer mark) throws Throwable {
        User u = User.loadUser(student);
        u.setMark(mark);
        u.save();
        index();
    }
}