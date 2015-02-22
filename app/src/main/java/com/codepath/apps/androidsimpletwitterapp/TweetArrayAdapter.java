package com.codepath.apps.androidsimpletwitterapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.androidsimpletwitterapp.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by suraj on 16/02/15.
 */
public class TweetArrayAdapter extends ArrayAdapter<Tweet> {
    Context context;
    public TweetArrayAdapter(Context context, ArrayList<Tweet> tweets) {
        super(context, 0, tweets);
        this.context = context;
    }

    private class ViewHolder {
        ImageView profile;
        TextView  userName;
        TextView  tweetText;
        TextView  handleName;
        TextView  postTime;
        ImageView tweetImage;
        ImageButton replyButton;
        ImageButton retweetButton;
        TextView    retweetCount;
        ImageButton favoriteButton;
        TextView    favoriteCount;
        Tweet       tweet;
    }

    private class MyJsonHttpResponseHandler extends JsonHttpResponseHandler {
        Tweet tweet;

        public MyJsonHttpResponseHandler(Tweet tweet) {
            super();
            this.tweet = tweet;
        }
    }

    public static void updateView(Context context, View convertView, Tweet tweet) {
        ViewHolder viewHolder = (ViewHolder)convertView.getTag();

        viewHolder.tweet = tweet;


        if (tweet.isFavorite())
            viewHolder.favoriteButton.setImageResource(R.drawable.ic_faviorite_selected);
        else
            viewHolder.favoriteButton.setImageResource(R.drawable.ic_faviorite);


        if (tweet.isRetweeted())
            viewHolder.retweetButton.setImageResource(R.drawable.ic_retweet_selected);
        else
            viewHolder.retweetButton.setImageResource(R.drawable.ic_retweet);



        viewHolder.profile.setImageResource(0);
        Picasso.with(context).load(tweet.getUser().getProfilePic()).into(viewHolder.profile);


        viewHolder.userName.setText(Html.fromHtml("<b>" + tweet.getUser().getName() + "</b>"));
        viewHolder.tweetText.setText(tweet.getText());
        viewHolder.handleName.setText("@" + tweet.getUser().getHandleName());

        viewHolder.tweetImage.setImageResource(0);
        if (!tweet.getTweetImage().isEmpty()) {
            viewHolder.tweetImage.setVisibility(View.VISIBLE);
            Picasso.with(context).load(tweet.getTweetImage()).into(viewHolder.tweetImage);
        }
        else
            viewHolder.tweetImage.setVisibility(View.GONE);

        viewHolder.postTime.setText(Utility.getRelativeTime(tweet.getCreatedTime()));
        if (tweet.getRetweetCount() > 0)
            viewHolder.favoriteCount.setText(String.valueOf(tweet.getFavioriteCount()));
        else
            viewHolder.favoriteCount.setText("");
        if (tweet.getRetweetCount() > 0)
            viewHolder.retweetCount.setText(String.valueOf(tweet.getRetweetCount()));
        else
            viewHolder.retweetCount.setText("");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Tweet tweet = getItem(position);
        ViewHolder viewHolder;



        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_tweet_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.profile = (ImageView)convertView.findViewById(R.id.ivProfilePic);
            viewHolder.userName = (TextView)convertView.findViewById(R.id.tvUserName);
            viewHolder.tweetText = (TextView)convertView.findViewById(R.id.tvTweetText);
            viewHolder.handleName = (TextView)convertView.findViewById(R.id.tvHandleName);
            viewHolder.postTime = (TextView)convertView.findViewById(R.id.tvPostTime);
            viewHolder.tweetImage = (ImageView)convertView.findViewById(R.id.ivTweetPic);
            viewHolder.replyButton = (ImageButton)convertView.findViewById(R.id.ibReply);
            viewHolder.retweetButton = (ImageButton)convertView.findViewById(R.id.ibRetweet);
            viewHolder.retweetCount = (TextView)convertView.findViewById(R.id.tvReTweetCount);
            viewHolder.favoriteButton = (ImageButton)convertView.findViewById(R.id.ibFaviorite);
            viewHolder.favoriteCount = (TextView)convertView.findViewById(R.id.tvFavoriteCount);

            viewHolder.replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder viewHolder = (ViewHolder)v.getTag();
                    Intent intent = new Intent(context, ComposeActivity.class);
                    intent.putExtra("tweet", viewHolder.tweet);
                    ((Activity)TweetArrayAdapter.this.context).startActivityForResult(intent, HomeTimelineActivity.REQUEST_NEW_TWEET);
                }
            });

            viewHolder.favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder viewHolder = (ViewHolder)v.getTag();
                    if (viewHolder.tweet.isFavorite() == false) {
                        ((ImageButton) v).setImageResource(R.drawable.ic_faviorite_selected);
                        int favoriteCount = viewHolder.favoriteCount.getText().length() > 0 ? Integer.parseInt(viewHolder.favoriteCount.getText().toString()) + 1 : 1;
                        tweet.setFavioriteCount(favoriteCount);
                        viewHolder.favoriteCount.setText(String.valueOf(favoriteCount));
                        viewHolder.tweet.setFavorite(true);
                    } else {
                        ((ImageButton) v).setImageResource(R.drawable.ic_faviorite);
                        int favoriteCount = Integer.parseInt(viewHolder.favoriteCount.getText().toString()) - 1;
                        tweet.setFavioriteCount(favoriteCount);
                        if (favoriteCount > 0)
                            viewHolder.favoriteCount.setText(String.valueOf(favoriteCount));
                        else
                            viewHolder.favoriteCount.setText("");
                        viewHolder.tweet.setFavorite(false);
                    }
                    viewHolder.tweet.save();
                    TwitterApplication.getRestClient().setFavorite(new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                        }
                    }, viewHolder.tweet);
                }
            });

            viewHolder.retweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewHolder viewHolder = (ViewHolder) v.getTag();
                    if (viewHolder.tweet.isRetweeted() == false) {
                        ((ImageButton) v).setImageResource(R.drawable.ic_retweet_selected);
                        int retweetCount = viewHolder.retweetCount.getText().length() > 0 ? Integer.parseInt(viewHolder.retweetCount.getText().toString()) + 1 : 1;
                        tweet.setRetweetCount(retweetCount);
                        viewHolder.retweetCount.setText(String.valueOf(retweetCount));
                        viewHolder.tweet.setRetweeted(true);
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
                    viewHolder.tweet.save();
                    TwitterApplication.getRestClient().setRetweet(new MyJsonHttpResponseHandler(viewHolder.tweet) {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.i("Success", "Retweeted successfully");
                            try {
                                this.tweet.setRetweetId(response.getLong("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.i("Fail", "Failed to retweet");
                        }
                    }, viewHolder.tweet);
                }
            });

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.replyButton.setTag(viewHolder);
        viewHolder.favoriteButton.setTag(viewHolder);
        viewHolder.retweetButton.setTag(viewHolder);



        updateView(getContext(), convertView, tweet);
        /*if (tweet.isFavorite())
            viewHolder.favoriteButton.setImageResource(R.drawable.ic_faviorite_selected);
        else
            viewHolder.favoriteButton.setImageResource(R.drawable.ic_faviorite);


        if (tweet.isRetweeted())
            viewHolder.retweetButton.setImageResource(R.drawable.ic_retweet_selected);
        else
            viewHolder.retweetButton.setImageResource(R.drawable.ic_retweet);



        viewHolder.profile.setImageResource(0);
        Picasso.with(getContext()).load(tweet.getUser().getProfilePic()).into(viewHolder.profile);


        viewHolder.userName.setText(Html.fromHtml("<b>" + tweet.getUser().getName() + "</b>"));
        viewHolder.tweetText.setText(tweet.getText());
        viewHolder.handleName.setText("@" + tweet.getUser().getHandleName());

        viewHolder.tweetImage.setImageResource(0);
        if (!tweet.getTweetImage().isEmpty()) {
            viewHolder.tweetImage.setVisibility(View.VISIBLE);
            Picasso.with(getContext()).load(tweet.getTweetImage()).into(viewHolder.tweetImage);
        }
        else
            viewHolder.tweetImage.setVisibility(View.GONE);

        viewHolder.postTime.setText(Utility.getRelativeTime(tweet.getCreatedTime()));
        if (tweet.getRetweetCount() > 0)
            viewHolder.favoriteCount.setText(String.valueOf(tweet.getFavioriteCount()));
        else
            viewHolder.favoriteCount.setText("");
        if (tweet.getRetweetCount() > 0)
            viewHolder.retweetCount.setText(String.valueOf(tweet.getRetweetCount()));
        else
            viewHolder.retweetCount.setText("");*/
        return convertView;
    }



}
