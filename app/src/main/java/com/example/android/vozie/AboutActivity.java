package com.example.android.vozie;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import mehdi.sakout.aboutpage.AboutPage;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        String aboutText = "Vozie Inc. is a pre-paid ride sharing service HQd out of " +
                "San Jose California located in the heart of the Silicon Valley. " +
                "Vozie began as a garage start up in September of 2015. The idea " +
                "was brought up due to the high surcharges and price increases when " +
                "using ride sharing services during high peak hours. The Vozie team has " +
                "made it our mission to provide an option that will be consistent and not " +
                "gauge people that depend on ride sharing to get around.";

        String aboutText2 ="Vozie has grown " +
                "into a four person team that has worked extensively to provide the best " +
                "solution in the marketplace. Two members of the team are in charge of " +
                "developing the product for the android app and will be making the app " +
                "available for the ios.";

        TextView desText = (TextView) findViewById(R.id.description_text);
        desText.setText(aboutText);
        TextView desText2 = (TextView) findViewById(R.id.description_text2);
        desText2.setText(aboutText2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
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
