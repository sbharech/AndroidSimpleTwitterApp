package com.codepath.apps.androidsimpletwitterapp.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by suraj on 16/02/15.
 */
@Table(name = "users")
public class User extends Model implements Serializable {
    @Column(name = "userId", unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    private long userId;
    @Column(name = "name")
    private String name;
    @Column(name = "handleName")
    private String handleName;
    @Column(name = "profilePic")
    private String profilePic;

    public User() {
        super();
    }

    public String getHandleName() {
        return handleName;
    }


    public static User getUser(JSONObject object) {

        try {
            User user = new Select().from(User.class).where("userId = ?", object.getLong("id")).executeSingle();
            if (user == null) {
                user = new User();
                user.userId = object.getLong("id");
                user.name = object.getString("name");
                user.profilePic = object.getString("profile_image_url");
                user.handleName = object.getString("screen_name");

                user.save();
            }

            return user;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Tweet> items() {
        return getMany(Tweet.class, "User");
    }

    public String getName() {
        return name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public static User getUser(long userId) {
        return new Select()
                .from(User.class)
                .where("id = ?", userId)
                .orderBy("id DESC")
                .executeSingle();
    }
}
