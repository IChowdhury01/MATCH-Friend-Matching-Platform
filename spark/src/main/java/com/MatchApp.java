package com;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import static com.MatchJDBC.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import static spark.Spark.*;

public class MatchApp {

    // Declare dependencies (i.e. classes, interfaces) [OPTIONAL]

    static BiMap<String, String> cookielist = HashBiMap.create();

    public static void main(String[] args) {

        // Instantiate dependencies (create objects) [OPTIONAL]

        /**
         * This directory will contain all static files that will be served
         * These are files that aren't generated by the server, but are preset and loaded
         * e.g. HTML forms, images, CSS style sheets, static javascript files
         * Home page, login, register pages can be static?
         * Profile/Friendslist, Friend profile pages can not?
         */
        String staticFilesDir = "src/main/resources/";


        // Configure Spark's embedded Jetty Web Server
        port(8080);     // To test routes: localhost:8080/<routeURL>
        staticFiles.location(staticFilesDir);   // Set static files directory

        staticFiles.externalLocation(staticFilesDir);


        // Set up routing
        get("/ping", (req, res)->"Pong\n");


        get("/", (req,res)-> {
            String value = req.cookie("match");
            String username = userFromCookie(req,res);
            if (username == null)
                res.redirect("/home.html");
            else
                res.redirect("/welcome.html");
            return username;
        });

        get("/user/:name", (req,res)-> {
            String username = req.params(":name");
            res.type("application/json");
            JsonObject jres = new JsonObject();
            jres.addProperty("DisplayName", getDisplayName(username));
            jres.addProperty("AboutMe", getAboutMe(username));
            return jres;
        });
        get("/friends", (req,res)-> {
            String username = userFromCookie(req,res);
            res.type("application/json");
            ArrayList<String> friends = findFriends(username);
            JsonArray juser = new JsonArray();
            JsonArray jname = new JsonArray();
            for (String user : friends) {
                juser.add(user);
                jname.add(getDisplayName(user));
            }
            JsonObject jres = new JsonObject();
            jres.add("DisplayName",jname);
            jres.add("UserName",juser);
            return jres;
        });

        get("logout", (req,res)-> {
            String username = userFromCookie(req,res);
            if (username != null) {
                cookielist.remove("match");
                res.removeCookie("match");
            }
            res.redirect("/");
            return "logged out";
        });

        post("/login",(req,res)-> {
            String name = req.queryParams("username");
            String pass = req.queryParams("password");
            if (pass.equals("") || name.equals(""))
                res.redirect("/login.html");

            if(getPassword(name).equals(pass)) {
                //Set the cookie with their session id. If it already exists in the list use that, otherwise get a new random value
                if (cookielist.containsKey(name)){
                    res.cookie("match",String.valueOf(cookielist.get(name)));
                }
                else {
                    String cookieid =  String.valueOf( (int) (Math.random() * 9999999));
                    cookielist.put(name, cookieid);
                    res.cookie("match",cookieid);
                }
                res.redirect("/");

            }
            else
                res.redirect("/login.html");
            return 1;
        });

        post("/register",(req,res)-> {
            createUser (
                    req.queryParams("username"),
                    req.queryParams("password"),
                    req.queryParams("displayName"),
                    req.queryParams("aboutMe"),

                    Double.parseDouble(req.queryParams("maxTravelDistance")),   // queryParams only returns strings

                    // var latitude, longitude - get from register.html script, parse to Double.
                    Double.parseDouble(req.queryParams("latitude")),
                    Double.parseDouble(req.queryParams("longitude")),

                    // survey questions - get from radio buttons, parse to Boolean
                    req.queryParams("swimming")+req.queryParams("reading")+req.queryParams("bike")+req.queryParams("hiking")+req.queryParams("camp")+req.queryParams("dance")+req.queryParams("run")+req.queryParams("games")+req.queryParams("bowl")+req.queryParams("basketball")+req.queryParams("football")+req.queryParams("baseball")+req.queryParams("program")+req.queryParams("TV")+req.queryParams("movies"));
            res.redirect("/login.html");
            return "Registration Successful";
        });

            // Upload profile pic route - http://sparkjava.com/documentation#javadoc

        // Set up after filters [OPTIONAL]

        // Initialize and test database
        createSchema();
        createUser("test1","jamessmith","James Smith","I like to party",100,40.75,-74.2,"101100000000000");
        createUser("test2","jennygoldstein","Jenny Goldstein","I am sporty",95,40.70,-73.7,"110010000000000");
        createUser("test3","rachelberry","Rachel Berry","I am destined to be a star",105,40.72,-73.9,"010000010001000");
        createUser("test4","willschuester","Will Schuester","I am a high school teacher",150,40.83,-74.8,"000001000001100");
        createUser("test5","quinnfabray","Quinn Fabray","I am head of the Cheerios cheerleading squad",50,40.23,-73.2,"000001010001000");
        createUser("test6","mikechang","Mike Chang","I am the quarterback of the football team",120,40.6,-74.1,"000000100001001");
        createUser("test7","ryujinkang","Ryujin Kang","I am the main dancer in ITZY",110,40.73,-74.5,"000101000001000");
        createUser("test8","suesylvester","Sue Sylvester","I am a cheerleading coach",70,40.83,-73.9,"000001001100000");
        createUser("test9","beckyjackson","Becky Jackson","I am a cheerleader",100,41.23,-73.9,"000011000010000");
        createUser("test10","tomholland","Tom Holland","I am Spiderman",160,41.03,-74.1,"010001010000000");

    }
    private static String userFromCookie(spark.Request req,spark.Response res) {

        String value = req.cookie("match");
        if (value == null)
            return null;
        else
        {
            try {
                if (cookielist.containsValue(value))
                    return cookielist.inverse().get(value);
                else
                    return null;
            }
            catch(NumberFormatException e) {
                return null;
            }
        }
    }

    private static double distance(double lat1, double lat2, double long1, double long2) {
        //https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(long2 - long1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 0.621371; // convert to miles
    }

    private static ArrayList<String> findFriends(String username) throws java.sql.SQLException{ //TODO Fix this
        double lat1 = getLatitude(username);
        double long1 = getLongitude(username);
        System.out.println(lat1+ " "+ long1);
        double max1 = getMaxTravelDistance(username);
        String hob1 = getHobbies(username);
        ArrayList<String> friends = getUserList();
        Iterator<String> it = friends.iterator();
        while (it.hasNext()) {
            String user = it.next();
            if (distance(lat1, getLatitude(user), long1, getLongitude(user)) > Math.min(max1, getMaxTravelDistance(user)))
                it.remove();
            else {
                String hob2 = getHobbies(user);
                int shared = 0;
                for (int i = 0; i < hob1.length(); i++) {
                    if (hob1.charAt(i) + hob2.charAt(i)  == '1'+'1')
                        shared++;
                }
                if (shared < 1)  //change to make tighter matches
                    it.remove();
            }
        }
        return friends;
    }
}