package com.store;

import com.model.User;
import com.model.UserBuilder;
import com.typesafe.config.Config;

import java.sql.*;

public class MatchJdbc implements UserStore {

    private static final String GET_USER_STATEMENT =
            "SELECT username FROM users WHERE username = ?";

    private final Config config;

    public MatchJdbc(final Config config) {
        this.config = config;
    }

    // getUser - Finds all info of a user with the specified username in the DB. If there is no user, returns null.
    @Override
    public User getUser(final String username) {
        Connection connection;
        try {
            connection =
                    DriverManager.getConnection(
                            config.getString("mysql.jdbc"),
                            config.getString("mysql.user"),
                            config.getString("mysql.password"));

            PreparedStatement preparedStatement = connection.prepareStatement(GET_USER_STATEMENT);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.first()) {
                return new UserBuilder()
                        .username(resultSet.getString("username"))
                        .password(resultSet.getString("password"))
                        .displayName(resultSet.getString("displayName"))
                        .aboutMe(resultSet.getString("aboutMe"))
//                        .hobbyList(resultSet.getBoolean[]("hobbyList"))
                        .maxTravelDistance(resultSet.getInt("maxTravelDistance"))
                        .longitude(resultSet.getDouble("longitude"))
                        .latitude(resultSet.getDouble("latitude"))
                        .oldFriendCount(resultSet.getInt("oldFriendCount"))
//                        .availableHobbies(resultSet.getString[]("availableHobbies"))
//                        .friendsList(resultSet.getString[]("friendsList"))
                        .build();
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("error fetching user", e);
        }
    }
<<<<<<< HEAD

    @Override
    public Boolean createUser(User newUser) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("SELECT username FROM users WHERE username = ?");
            statement.setString(1,newUser.username());

            statement.executeQuery();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        try {
            connection = DriverManager.getConnection(
                config.getString("mysql.jdbc"),
                config.getString("mysql.user"),
                config.getString("mysql.password"));

            System.out.println("Connected to database successfully");
            System.out.println("Inserting new record into table");

            String sqlinsert = "INSERT INTO users (userid, username, userdisplayname, userpassword, userhobbylist, usermaxtraveldistance,userlatitude, userlongitude)" + "VALUES (?,?,?,?,?,?,?,?)";

            PreparedStatement psinsert = connection.prepareStatement(sqlinsert, Statement.RETURN_GENERATED_KEYS);
            psinsert.setString(2,newUser.username());
            psinsert.setString(3,newUser.displayName());
            psinsert.setString(4,newUser.password());
            // psinsert.setBoolean(5,newuser.hobbyList());  How to insert boolean array to database
            psinsert.setInt(6,newUser.maxTravelDistance());
            psinsert.setDouble(7,newUser.latitude());
            psinsert.setDouble(8,newUser.longitude());
=======
}

@NewTest
public boolean createUser(User newuser) {
        Connection conn = null;
        PreparedStatement stmt = null;
    try {
        stmt = connection.prepareStatement("SELECT username FROM users WHERE username = ?");
        stmt.setString(1,newuser.username());
    }
    catch (SQLException e) {
        e.printStackTrace();
    } //check existence
    try {
        stmt.executeQuery();
    }
    catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
    try {
        Connection conn = DriverManager.getConnection(
            config.getString("mysql.jdbc"),
            config.getString("mysql.user"),
            config.getString("mysql.password));
        System.out.println("Connected to database successfully...");
        System.out.println("Inserting new record into table...");
        stmt = conn.createStatement();                     
        String sqlinsert = "INSERT INTO users (userid, username, userdisplayname, userpassword, userhobbylist, usermaxtraveldistance,userlatitude, userlongitude)" + "VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement psinsert = conn.prepareStatement(sqlinsert,Statement.RETURN_GENERATED_KEYS);
        psinsert.setInt(1,newuser.userid);
        psinsert.setString(2,newuser.username());
        psinsert.setString(3,newuser.userdisplayname());
        psinsert.setString(4,newuser.userpassword());
        psinsert.setString(5,newuser.userhobbylist());
        psinsert.setString(6,newuser.usermaxtraveldistance());
        psinsert.setString(7,newuser.userlatitude());
        psinsert.setString(8,newuser.userlongitude());
       } 
       catch (SQLException e) {
           e.printStackTrace();
       }
                             
        try{
            stmt.execute(sqlinsert);
            return true;
         }
           catch (SQLException se) {
               se.printStackTrace();
               return false;
>>>>>>> 1ed2288dce7a6195ebb676d56f6d0d3288acc6c8
           }

           catch (SQLException e) {
               e.printStackTrace();
           }

            try{
                psinsert.execute(sqlinsert);
                return true;
             }
               catch (SQLException se) {
                   se.printStackTrace();
                   return false;
               }
    }
}