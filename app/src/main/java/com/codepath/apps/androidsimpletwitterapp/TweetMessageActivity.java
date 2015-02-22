package com.codepath.apps.androidsimpletwitterapp;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.androidsimpletwitterapp.R;
import com.codepath.apps.androidsimpletwitterapp.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class TweetMessageActivity extends ActionBarActivity {
    private final int REQUEST_TWEET = 0;

    private ImageView profilePic;
    private TextView  userName;
    private TextView  handleName;
    private TextView  text;
    private ImageView tweetImage;
    private TextView  postTime;
    private ImageButton replyButton;
    private ImageButton retweetButton;
    private ImageButton favoriteButton;
    private TextView    favoriteCount;
    private TextView    retweetCount;
    Tweet tweet = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_logo);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00aced")));
        setContentView(R.layout.activity_tweet_message);
        initMembers();
        populateFields();
        setupListeners();
    }

    private void populateFields() {
        Picasso.with(this).load(tweet.getUser().getProfilePic()).into(profilePic);
        userName.setText(tweet.getUser().getName());
        handleName.setText("@" + tweet.getUser().getHandleName());
        text.setText(tweet.getText());
        if (!tweet.getTweetImage().isEmpty())
            Picasso.with(this).load(tweet.getTweetImage()).into(tweetImage);
        else
            tweetImage.setVisibility(View.GONE);
        postTime.setText(tweet.getCreatedTime());

        if (tweet.getRetweetCount() > 0) {
            Resources res = getResources();
            String quantityString = res.getQuantityString(R.plurals.retweet_count,
                    tweet.getRetweetCount(), tweet.getRetweetCount());

            retweetCount.setText(tweet.getRetweetCount() + " " + quantityString);

        } else
            retweetCount.setVisibility(View.GONE);


        if (tweet.getFavioriteCount() > 0) {
            Resources res = getResources();
            String quantityString = res.getQuantityString(R.plurals.fav_count,
                    tweet.getFavioriteCount(), tweet.getFavioriteCount());

            favoriteCount.setText(tweet.getFavioriteCount() + " " + quantityString);

        } else
            favoriteCount.setVisibility(View.GONE);

        if (tweet.isRetweeted())
            retweetButton.setImageResource(R.drawable.ic_retweet_selected);
        if (tweet.isFavorite())
            favoriteButton.setImageResource(R.drawable.ic_faviorite_selected);


    }

    private void initMembers() {
        profilePic = (ImageView)findViewById(R.id.ivProfilePic);
        userName = (TextView)findViewById(R.id.tvUserName);
        handleName = (TextView)findViewById(R.id.tvHandleName);
        text = (TextView)findViewById(R.id.tvTweetText);
        tweetImage = (ImageView)findViewById(R.id.ivTweetPic);
        postTime = (TextView)findViewById(R.id.tvPostTime);
        favoriteCount = (TextView)findViewById(R.id.tvFavoriteCount);
        retweetCount = (TextView)findViewById(R.id.tvRetweetCount);

        Intent intent = getIntent();
        tweet = (Tweet)intent.getSerializableExtra("tweet");
        replyButton = (ImageButton)findViewById(R.id.ibReply);
        retweetButton = (ImageButton)findViewById(R.id.ibRetweet);
        favoriteButton = (ImageButton)findViewById(R.id.ibFaviorite);
    }

    private void setupListeners() {
        replyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TweetMessageActivity.this, ComposeActivity.class);
                intent.putExtra("tweet", tweet);
                startActivityForResult(intent, REQUEST_TWEET);
            }
        });

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tweet.isFavorite() == false) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_faviorite_selected);
                    tweet.setFavioriteCount(tweet.getFavioriteCount() + 1);
                    Resources res = getResources();
                    String quantityString = res.getQuantityString(R.plurals.fav_count,
                            tweet.getFavioriteCount(), tweet.getFavioriteCount());

                    TweetMessageActivity.this.favoriteCount.setText(tweet.getFavioriteCount() + " " + quantityString);
                    tweet.setFavorite(true);
                    TweetMessageActivity.this.favoriteCount.setVisibility(View.VISIBLE);
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_faviorite);
                    //int favoriteCount = Integer.parseInt(viewHolder.favoriteCount.getText().toString()) - 1;
                    tweet.setFavioriteCount(tweet.getFavioriteCount() - 1);
                    if (tweet.getFavioriteCount() > 0) {
                        Resources res = getResources();
                        String quantityString = res.getQuantityString(R.plurals.fav_count,
                                tweet.getFavioriteCount(), tweet.getFavioriteCount());

                        TweetMessageActivity.this.favoriteCount.setText(tweet.getFavioriteCount() + " " + quantityString);
                    }
                    else
                        TweetMessageActivity.this.favoriteCount.setVisibility(View.GONE);
                    tweet.setFavorite(false);
                }
                tweet.save();
                TwitterApplication.getRestClient().setFavorite(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                    }
                }, tweet);
            }
        });

        retweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tweet.isRetweeted() == false) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_retweet_selected);
                    tweet.setRetweetCount(tweet.getRetweetCount() + 1);
                    Resources res = getResources();
                    String quantityString = res.getQuantityString(R.plurals.retweet_count,
                            tweet.getRetweetCount(), tweet.getRetweetCount());

                    TweetMessageActivity.this.retweetCount.setText(tweet.getRetweetCount() + " " + quantityString);
                    tweet.setRetweeted(true);
                    TweetMessageActivity.this.retweetCount.setVisibility(View.VISIBLE);
                } else {
                        /*((ImageButton) v).setImageResource(R.drawable.ic_retweet);
                        int retweetCount = Integer.parseInt(viewHolder.retweetCount.getText().toString()) - 1;
                        tweet.setRetweetCount(retweetCount);
                        if (retweetCount > 0)
                            viewHolder.retweetCount.setText(String.valueOf(retweetCount));
                        else
                            viewHolder.retweetCount.setText("");
                        viewHolder.tweet.setRetweeted(false);*/
                    return;
                }
                tweet.save();
                TwitterApplication.getRestClient().setRetweet(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Log.i("Fail", "Failed to retweet");
                    }
                }, tweet);
            }
        });


    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("tweet", tweet);
        setResult(RESULT_OK, intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tweet_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
