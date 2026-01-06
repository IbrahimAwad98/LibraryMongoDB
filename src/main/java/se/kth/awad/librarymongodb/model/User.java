package se.kth.awad.librarymongodb.model;

import java.util.Objects;

/*
* Representerar en användare
 */
public class User {
    private int userID;
    private String username;
    private String password;

    public User(){}

    public User(int userID, String username) {
        this.userID = userID;
        this.username = username;
    }

    public User(int userID, String username, String password) {
        this.userID = userID;
        this.username = username;
        this.password = password;
    }

    //getters
    public int getUserID() {return userID;}
    public String getUsername() {return username;}
    public String getPassword() {return password;}

    //setters
    public void setUserID(int userID) {this.userID = userID;}
    public void setUsername(String username){this.username = username;}
    public void setPassword(String password){this.password = password;}

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        return userID == other.userID;
    }

    public int hashCode() {
        return Objects.hash(userID);
    }

    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", username='" + username + '\'' +
                ", password='" + (password != null ? "***" : "null") + '\'' +
                '}';
    }
}
