package com.example.android.vozie;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class MainActivity extends ActionBarActivity {

    private TextView info;
    private LoginButton loginButton;
    private CallbackManager callBackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        info = (TextView) findViewById(R.id.info);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });

        ImageView image = (ImageView) findViewById(R.id.background);

        Drawable backgrounds[] = new Drawable[4];
        backgrounds[0] = ContextCompat.getDrawable(this, R.drawable.gradient1);
        backgrounds[1] = ContextCompat.getDrawable(this, R.drawable.gradient2);
        backgrounds[2] = ContextCompat.getDrawable(this, R.drawable.gradient3);
        backgrounds[3] = ContextCompat.getDrawable(this, R.drawable.gradient4);

        Crossfade(image, backgrounds, 10000);
    }

    public void login(){
        callBackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "user_photos", "public_profile"));

        loginButton.registerCallback(callBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                            public void onCompleted(JSONObject json, GraphResponse response) {
                                if (response.getError() != null) {
                                    //handle error
                                } else {
                                    //success
                                    try {
                                        String jsonresult = String.valueOf(json);
                                        String name = json.getString("name");
                                        String email = json.getString("email");
                                        String id = json.getString("id");
                                        setText(id, name, email);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                info.setText("Login attempt canceled");
            }

            @Override
            public void onError(FacebookException e) {
                info.setText("Login attemped failed");

            }
        });

    }

    public void setText(String str1, String str2, String str3){
        info.setText("ID: " + str1 + "\n NAME:  " + str2 + "\n EMAIL: " + str3);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callBackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void Crossfade(final ImageView image, final Drawable layers[], final int speedInMs) {
        class BackgroundGradientThread implements Runnable {
            Context mainContext;
            TransitionDrawable crossFader;
            boolean first = true;

            BackgroundGradientThread(Context c) {
                mainContext = c;
            }

            public void run() {
                Handler mHandler = new Handler(mainContext.getMainLooper());
                boolean reverse = false;

                while (true) {
                    if (!reverse) {
                        for (int i = 0; i < layers.length - 1; i++) {
                            Drawable tLayers[] = new Drawable[2];
                            tLayers[0] = layers[i];
                            tLayers[1] = layers[i + 1];

                            final TransitionDrawable tCrossFader = new TransitionDrawable(tLayers);
                            tCrossFader.setCrossFadeEnabled(true);

                            Runnable transitionRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    image.setImageDrawable(tCrossFader);
                                    tCrossFader.startTransition(speedInMs);
                                }
                            };

                            mHandler.post(transitionRunnable);

                            try {
                                Thread.sleep(speedInMs);
                            } catch (Exception e) {
                            }
                        }

                        reverse = true;
                    }
                    else if (reverse) {
                        for (int i = layers.length - 1; i > 0; i--) {
                            Drawable tLayers[] = new Drawable[2];
                            tLayers[0] = layers[i];
                            tLayers[1] = layers[i - 1];

                            final TransitionDrawable tCrossFader = new TransitionDrawable(tLayers);
                            tCrossFader.setCrossFadeEnabled(true);

                            Runnable transitionRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    image.setImageDrawable(tCrossFader);
                                    tCrossFader.startTransition(speedInMs);
                                }
                            };

                            mHandler.post(transitionRunnable);

                            try {
                                Thread.sleep(speedInMs);
                            } catch (Exception e) {
                            }
                        }

                        reverse = false;
                    }
                }
            }
        }

        Thread backgroundThread = new Thread(new BackgroundGradientThread(this));
        backgroundThread.start();
    }
}
