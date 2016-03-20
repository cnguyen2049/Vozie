package com.example.android.vozie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    /*
    NOTE TO Chris(Yourself) Think of moving this code to another class when you have a chance

     */
    public String processText(View view) throws Exception {
        CreditCard card = new CreditCard();

        EditText cardNameField = (EditText) findViewById(R.id.card_name);
        String cardName = cardNameField.getText().toString();

        EditText cardNumberField = (EditText) findViewById(R.id.card_number);
        String cardNumber = cardNumberField.getText().toString();

        EditText securityCodeField = (EditText) findViewById(R.id.security_code);
        String securityCode = securityCodeField.getText().toString();

        Spinner month_spinner = (Spinner) findViewById(R.id.month_spinner);
        String month_string = month_spinner.getSelectedItem().toString();
        Spinner year_spinner = (Spinner) findViewById(R.id.year_spinner);

        String year_string = year_spinner.getSelectedItem().toString();
        Integer year_int = Integer.parseInt(year_string);

        Integer month_int = card.getIntMonthFromString(month_string);

        if(month_int == 0){
            return null;
        }
        String output = cardName + " " + cardNumber + " " + securityCode + " " + year_int +
                " " + month_int;
        Log.v(LOG_TAG, output);


        card.processCard(cardNumber,month_int,year_int, securityCode);
        return output;
    }


}
