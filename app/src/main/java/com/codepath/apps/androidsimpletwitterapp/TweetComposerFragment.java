package com.codepath.apps.androidsimpletwitterapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.androidsimpletwitterapp.models.Tweet;
import com.codepath.apps.androidsimpletwitterapp.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by suraj on 18/02/15.
 */
public class TweetComposerFragment extends DialogFragment {

    private ImageButton cancelButton;
    private ImageButton tweetButton;
    private TextView    remainingCharText;
    private ImageView   profilePic;
    private TextView    userName;
    private TextView    handleName;
    private EditText    tweetText;
    private User        user;

    private Activity parentActivity = null;

    public static TweetComposerFragment getNewInstance() {
        TweetComposerFragment tf = new TweetComposerFragment();
        return tf;
    }

    public interface TweetComposerFragmentResultListener {
        void setResult(Intent intent);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setLayout(Utility.getDisplayWidth(getActivity().getBaseContext()) - 100, Utility.getDisplayHeight(getActivity().getBaseContext())-200);

        parentActivity = getActivity();
        CurrentUser.populateCurrentUser();

        View view = inflater.inflate(R.layout.fragment_tweet_composer, container);

        initMembers(view);
        setAdaptersAndListeners();

        return view;
    }

    private void initMembers(View view) {
        user = CurrentUser.getCurrentUser();
        if (user == null)
            user = new User();

        cancelButton = (ImageButton)view.findViewById(R.id.ibCancel);
        tweetButton = (ImageButton)view.findViewById(R.id.ibTweet);
        remainingCharText = (TextView)view.findViewById(R.id.tvRemainingcCharacters);
        profilePic = (ImageView)view.findViewById(R.id.ivProfilePic);
        Picasso.with(getActivity().getBaseContext()).load(user.getProfilePic()).into(profilePic);
        userName = (TextView)view.findViewById(R.id.tvUserName);
        userName.setText(user.getName());
        handleName = (TextView)view.findViewById(R.id.tvHandleName);
        handleName.setText("@" + user.getHandleName());
        tweetText = (EditText)view.findViewById(R.id.etText);
        remainingCharText.setText(R.string.max_text_count);
    }

    private void setAdaptersAndListeners() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tweetText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                remainingCharText.setText(String.valueOf(140 - tweetText.getText().toString().length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterApplication.getRestClient().updateStatus(new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("Info", response.toString());
                        Tweet tweet = new Tweet();
                        try {
                            tweet.setId(response.getLong("id"));
                            Intent intent = new Intent();
                            intent.putExtra("tweet", tweet);
                            //Log.i("erer", TweetComposerFragment.this.getActivity().toString());

                            TweetComposerFragmentResultListener l = (TweetComposerFragmentResultListener)TweetComposerFragment.this.parentActivity;
                            l.setResult(intent);
                        } catch (JSONException e) {
                            tweet.setId(0);
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                    }

                }, tweetText.getText().toString(), 0);
                dismiss();
            }
        });
    }

}
