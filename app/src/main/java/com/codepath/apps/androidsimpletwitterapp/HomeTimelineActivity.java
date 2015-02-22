package com.codepath.apps.androidsimpletwitterapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.codepath.apps.androidsimpletwitterapp.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class HomeTimelineActivity extends ActionBarActivity implements TweetComposerFragment.TweetComposerFragmentResultListener {
    public static final int REQUEST_NEW_TWEET = 0;
    public static final int REQUEST_MESSAGE_DETAIL = 1;
    private TwitterClient client;
    private ArrayList<Tweet> tweetList;
    private TweetArrayAdapter arrayAdaptor;
    private long maxId = 0;
    private PullToRefreshLayout pullToRefreshLayout;
    private ProgressBar progressLoading;
    private int positionSelected = 0;
    private View viewSelected = null;
    private ListView lvTweetList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_timeline);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_logo);
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00aced")));
        tweetList = new ArrayList<Tweet>();
        arrayAdaptor = new TweetArrayAdapter(this, tweetList);
        pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptrTweetListLayout);
        lvTweetList = (ListView)findViewById(R.id.lvTweetList);
        progressLoading = (ProgressBar)findViewById(R.id.pbLoading);
        lvTweetList.setAdapter(arrayAdaptor);
        client = TwitterApplication.getRestClient();
        CurrentUser.populateCurrentUser();

        arrayAdaptor.addAll(Tweet.getAllSavedTweet());


        populateTimeLine(maxId);
        lvTweetList.setOnScrollListener(new CustomEndlessScrollListener(5, 25));

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this)
                // Mark All Children as pullable
                .allChildrenArePullable()
                        // Set a OnRefreshListener
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        progressLoading.setVisibility(View.VISIBLE);
                        populateTimeLine(0);
                        pullToRefreshLayout.setRefreshComplete();
                    }
                })
                        // Finally commit the setup to our PullToRefreshLayout
                .setup(pullToRefreshLayout);

        lvTweetList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Tweet tweet = (Tweet)parent.getItemAtPosition(position);
                Intent intent = new Intent(HomeTimelineActivity.this, TweetMessageActivity.class);
                intent.putExtra("tweet", tweet);
                startActivityForResult(intent, REQUEST_MESSAGE_DETAIL);
                positionSelected = position;
                viewSelected = view;
                return true;
            }
        });



    }

    @Override
    public void setResult(Intent intent) {
        final Tweet tweet = (Tweet)intent.getSerializableExtra("tweet");
        Log.i("New Tweet", "Created a new one with id " + tweet.getTweetId());
        if (tweet.getTweetId() > 0) {
            TwitterApplication.getRestClient().getMessage(new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Tweet newTweet = Tweet.getTweet(response);
                    tweetList.add(0, newTweet);
                    arrayAdaptor.notifyDataSetInvalidated();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.i("Error....", "Error");
                }


            },tweet);
        }
    }

    class CustomEndlessScrollListener extends EndlessScrollListener {
        public CustomEndlessScrollListener(int visibleThreshold, int itemPerPage) {
            super(visibleThreshold, itemPerPage);
        }

        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            progressLoading.setVisibility(View.VISIBLE);
            populateTimeLine(maxId - 1);
        }
    }

    private void setMaxId(long maxId) {
        this.maxId = maxId;
    }



    private void populateTimeLine(final long maxId) {
        client.getHomeTimeLineList(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                progressLoading.setVisibility(View.GONE);
                if (maxId == 0) {
                    arrayAdaptor.clear();
                    Tweet.deleteAllSavedTweet();
                }
                arrayAdaptor.addAll(Tweet.getTweetList(response));
                setMaxId(tweetList.get(tweetList.size() - 1).getTweetId());

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                progressLoading.setVisibility(View.GONE);
                NetworkErrorDialog errorDialog = new NetworkErrorDialog(HomeTimelineActivity.this);
                errorDialog.createNetworkErrorDialog();
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                NetworkErrorDialog errorDialog = new NetworkErrorDialog(HomeTimelineActivity.this);
                errorDialog.createNetworkErrorDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                NetworkErrorDialog errorDialog = new NetworkErrorDialog(HomeTimelineActivity.this);
                errorDialog.createNetworkErrorDialog();
            }


        }, maxId);
        //since_id = arrayAdaptor.getItem(0).getId();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_NEW_TWEET) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                final Tweet tweet = (Tweet)data.getSerializableExtra("tweet");
                Log.i("New Tweet", "Created a new one with id " + tweet.getTweetId());
                if (tweet.getTweetId() > 0) {
                    TwitterApplication.getRestClient().getMessage(new JsonHttpResponseHandler(){
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Tweet newTweet = Tweet.getTweet(response);
                            tweetList.add(0, newTweet);
                            arrayAdaptor.notifyDataSetInvalidated();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Log.i("Error....", "Error");
                        }


                    },tweet);
                }
            }
        } else if (requestCode == REQUEST_MESSAGE_DETAIL) {
            if (resultCode == RESULT_OK) {
                final Tweet tweet = (Tweet)data.getSerializableExtra("tweet");
                tweetList.set(positionSelected, tweet);
                View view = lvTweetList.getChildAt(positionSelected);
                lvTweetList.getAdapter().getView(positionSelected, view, lvTweetList);
                //TweetArrayAdapter.updateView(this, viewSelected, tweet);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_compose) {
            //FragmentManager fm = getFragmentManager();
            //TweetComposerFragment tf = TweetComposerFragment.getNewInstance();
            //tf.show(fm, "tweetcomposer");
            Intent i = new Intent(this, ComposeActivity.class);
            startActivityForResult(i, REQUEST_NEW_TWEET);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
