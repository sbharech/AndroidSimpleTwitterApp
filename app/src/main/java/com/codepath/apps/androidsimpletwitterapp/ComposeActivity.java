package com.codepath.apps.androidsimpletwitterapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codepath.apps.androidsimpletwitterapp.R;
import com.codepath.apps.androidsimpletwitterapp.models.Tweet;
import com.codepath.apps.androidsimpletwitterapp.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class ComposeActivity extends ActionBarActivity {
    private TextView remainingCharText;
    private ImageView profilePic;
    private TextView    userName;
    private TextView    handleName;
    private EditText tweetText;
    private User user;
    private ProgressBar pbTweetSendProgress;
    private Tweet tweet = null;
    private MenuItem sendItem = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_logo);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00aced")));

        initMembers();
        setListeners();
    }

    private void initMembers() {
        user = CurrentUser.getCurrentUser();
        if (user == null)
            user = new User();

        remainingCharText = (TextView) findViewById(R.id.tvRemainingcCharacters);
        profilePic = (ImageView) findViewById(R.id.ivProfilePic);
        Picasso.with(this).load(user.getProfilePic()).into(profilePic);
        userName = (TextView) findViewById(R.id.tvUserName);
        userName.setText(user.getName());
        handleName = (TextView) findViewById(R.id.tvHandleName);
        handleName.setText("@" + user.getHandleName());
        tweetText = (EditText) findViewById(R.id.etText);
        tweetText.setText("");
        pbTweetSendProgress = (ProgressBar)findViewById(R.id.pbTweetProgress);
        pbTweetSendProgress.setVisibility(View.INVISIBLE);
        Intent intent = getIntent();
        tweet = (Tweet)intent.getSerializableExtra("tweet");
        if (tweet != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("@" + tweet.getUser().getHandleName());
            if (tweet.getUserMentioned() != null)
                sb.append(" " + tweet.getUserMentioned());
            sb.append(" ");
            tweetText.setText(sb.toString());

            Editable etext = tweetText.getText();
            Selection.setSelection(etext, sb.length());

        }
        remainingCharText.setText(String.valueOf(140 - tweetText.getText().toString().length()));
    }

    void setListeners() {
        tweetText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                remainingCharText.setText(String.valueOf(140 - tweetText.getText().toString().length()));
                if (tweetText.getText().toString().length() == 0 ||
                        tweetText.getText().toString().length() > 140)
                    sendItem.setIcon(R.drawable.ic_send_disabled);
                else
                    sendItem.setIcon(R.drawable.ic_send);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose, menu);
        sendItem = menu.findItem(R.id.action_tweet);
        if (tweetText != null && tweetText.getText().length() > 0)
            sendItem.setIcon(R.drawable.ic_send);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_tweet) {
            if (tweetText.length() > 0 && tweetText.length() <= 140 ) {
                pbTweetSendProgress.setVisibility(View.VISIBLE);
                TwitterApplication.getRestClient().updateStatus(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("Info", response.toString());
                        Tweet tweet = new Tweet();
                        try {
                            tweet.setId(response.getLong("id"));
                            Intent intent = new Intent();
                            intent.putExtra("tweet", tweet);
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (JSONException e) {
                            tweet.setId(0);
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        setResult(RESULT_CANCELED, new Intent());
                    }

                }, tweetText.getText().toString(), tweet != null ? tweet.getTweetId() : 0);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
