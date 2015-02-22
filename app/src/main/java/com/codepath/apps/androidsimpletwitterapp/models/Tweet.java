package com.codepath.apps.androidsimpletwitterapp.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by suraj on 16/02/15.
 */
@Table(name="tweets")
public class Tweet extends Model implements Serializable {
    @Column(name = "tweetId", unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    private long tweetId;
    @Column(name="createdAt")
    private String createdTime;
    @Column(name="text")
    private String text;

    public String getTweetImage() {
        return tweetImage;
    }

    @Column(name="tweetImage")
    private String tweetImage;
    @Column(name = "user", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    private User user;

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public void setFavioriteCount(int favioriteCount) {
        this.favioriteCount = favioriteCount;
    }

    @Column(name="retweetCount")
    private int retweetCount;
    @Column(name="favioriteCount")
    private int favioriteCount;
    @Column(name="userMentioned")
    private String userMentioned = "";

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
    @Column(name="isFavorite")
    private boolean isFavorite = false;

    public long getRetweetId() {
        return retweetId;
    }

    public void setRetweetId(long retweetId) {
        this.retweetId = retweetId;
    }

    private long retweetId;

    public boolean isRetweeted() {
        return isRetweeted;
    }

    public void setRetweeted(boolean isRetweeted) {
        this.isRetweeted = isRetweeted;
    }

    @Column(name="isRetweeted")
    private boolean isRetweeted = false;

    public Tweet() {
        super();
    }

    public static Tweet getTweet(JSONObject object) {
        Tweet tweet = new Tweet();
        try {
            tweet.tweetId = object.getLong("id");
            tweet.createdTime = object.getString("created_at");
            tweet.text = object.getString("text");
            tweet.user = User.getUser(object.getJSONObject("user"));
            tweet.retweetCount = object.getInt("retweet_count");
            tweet.isFavorite = object.getBoolean("favorited");
            tweet.isRetweeted = object.getBoolean("retweeted");
            tweet.favioriteCount = object.getInt("favorite_count");
            try {
                StringBuilder sb = new StringBuilder();
                JSONArray userMentionedArray = object.getJSONObject("entities").getJSONArray("user_mentions");
                for (int i = 0; i < userMentionedArray.length(); ++i) {
                    JSONObject obj = userMentionedArray.getJSONObject(i);
                    if (sb.length() > 0)
                        sb.append(" ");
                    sb.append("@" + obj.getString("screen_name"));
                }
                tweet.userMentioned = sb.toString();
            } catch (JSONException e) {

            }
            try {
                JSONArray mediaArray = object.getJSONObject("entities").getJSONArray("media");
                if (mediaArray.length() > 0)
                    tweet.tweetImage = ((JSONObject)mediaArray.get(0)).getString("media_url");
            } catch (JSONException e) {
                tweet.tweetImage = "";
                e.printStackTrace();
            }


            Log.i("Info","Recently saved " + tweet.getTweetId());
            tweet.save();
            List<Tweet> tweets = Tweet.getAllSavedTweet();
            for (Tweet t : tweets) {
                Log.i("Info", String.valueOf(t.getTweetId()));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public static ArrayList<Tweet> getTweetList(JSONArray array) {
        ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
        for (int i = 0; i < array.length(); ++i) {
            try {
                tweetList.add(Tweet.getTweet(array.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return tweetList;
    }

    public long getTweetId() {
        return tweetId;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getText() {
        return text;
    }

    public User getUser() {
        return user;
    }

    public void setId(long id) {
        this.tweetId = id;
    }


    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public static void deleteAllSavedTweet() {
        new Delete().from(Tweet.class).execute();
    }

    public static List<Tweet> getAllSavedTweet() {
        return new Select()
                .from(Tweet.class)
                .orderBy("tweetId DESC")
                .execute();

    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public int getFavioriteCount() {
        return favioriteCount;
    }

    public String getUserMentioned() {
        return userMentioned;
    }
}
