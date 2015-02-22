package com.codepath.apps.androidsimpletwitterapp;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;

import com.codepath.apps.androidsimpletwitterapp.models.Tweet;
import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {

	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "dqcIDRpVmRsLWuzi9PpCBOeNr";       // Change this
	public static final String REST_CONSUMER_SECRET = "Jo9eVe96MbWH5RA3YiIEdwPOo6gGyeur9qsyC1MKkHZXSxXY4L"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cptwitterrest"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	// CHANGE THIS
	// DEFINE METHODS for different API endpoints here
	public void getInterestingnessList(AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("?nojsoncallback=1&method=flickr.interestingness.getList");
		// Can specify query string params directly or through RequestParams.
		RequestParams params = new RequestParams();
		params.put("format", "json");
		client.get(apiUrl, params, handler);

	}

    public void getMessage(AsyncHttpResponseHandler handler, Tweet tweet) {
        String apiUrl = getApiUrl("statuses/show.json");
        RequestParams params = new RequestParams();
        params.put("id",tweet.getTweetId());
        client.get(apiUrl, params, handler);
    }

    public void updateStatus(AsyncHttpResponseHandler handler, String text, long inReplyTo) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", text);
        if (inReplyTo > 0)
            params.put("in_reply_to_status_id", inReplyTo);
        client.post(apiUrl, params, handler);
    }

    public void setFavorite(AsyncHttpResponseHandler handler, Tweet tweet) {
        String apiUlr = null;
        if (tweet.isFavorite() == true)
            apiUlr = getApiUrl("favorites/create.json");
        else
            apiUlr = getApiUrl("favorites/destroy.json");
        RequestParams params = new RequestParams();
        params.put("id", tweet.getTweetId());
        client.post(apiUlr, params, handler);
    }

    public void setRetweet(AsyncHttpResponseHandler handler, Tweet tweet) {
        String apiUlr = null;
        RequestParams params = new RequestParams();

        if (tweet.isRetweeted() == true) {
            apiUlr = getApiUrl("statuses/retweet/" + String.valueOf(tweet.getTweetId()) + ".json");
            params.put("id", tweet.getTweetId());
        }
        else {
            apiUlr = getApiUrl("statuses/destroy.json");
            params.put("id", tweet.getRetweetId());
        }
        client.post(apiUlr, params, handler);
    }


    public void getCurrentUserProfile(AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/verify_credentials.json");
        client.get(apiUrl, null, handler);
    }

    public void getHomeTimeLineList(AsyncHttpResponseHandler handler, long maxId) {
        String apiUrl = getApiUrl("statuses/home_timeline.json");
        RequestParams params = new RequestParams();
        params.put("count",25);

        if (maxId > 0)
            params.put("max_id", maxId);
        else
            params.put("since_id", 1);
        client.get(apiUrl, params, handler);

    }

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}