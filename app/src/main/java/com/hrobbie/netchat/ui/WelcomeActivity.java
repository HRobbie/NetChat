package com.hrobbie.netchat.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.hrobbie.netchat.MainActivity;
import com.hrobbie.netchat.R;
import com.hrobbie.netchat.ui.activity.Main2Activity;
import com.hrobbie.netchat.ui.login.LoginActivity;
import com.hrobbie.netchat.utills.cache.Preferences;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              if(canAutoLogin()){
                  startActivity(new Intent(WelcomeActivity.this, Main2Activity.class));
                  finish();
              }  else{
                  startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                  finish();
              }
            }
        },1000);
    }

    /**
     * 已经登陆过，自动登陆
     */
    private boolean canAutoLogin() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();

//        Log.i(TAG, "get local sdk token =" + token);
        return !TextUtils.isEmpty(account) && !TextUtils.isEmpty(token);
    }
}
