package com.example.android.vozie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.vozie.POST.SendPost;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.exception.AuthenticationException;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class CreditCard extends AppCompatActivity {
    private final String LOG_TAG = CreditCard.class.getSimpleName();
    private SendPost post;
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

    public String processText(View view) {

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

        Integer month_int = getIntMonthFromString(month_string);

        if(month_int == 0){
            return null;
        }
        String output = cardName + " " + cardNumber + " " + securityCode + " " + year_int +
                " " + month_int;
        Log.v(LOG_TAG, output);

        return output;
    }

    public int getIntMonthFromString(String month) {
        switch (month) {
            case "Jan":
                return 1;

            case "Feb":
                return 2;

            case "Mar":
                return 3;

            case "Apr":
                return 4;

            case "May":
                return 5;

            case "Jun":
                return 6;

            case "Jul":
                return 7;

            case "Aug":
                return 8;

            case "Sep":
                return 9;

            case "Oct":
                return 10;

            case "Nov":
                return 11;

            case "Dec":
                return 12;
        }

        return 0;
    }
    /*
    *
    * Handles the validation of the credit card that is imputed by the user.
    * @param number
    * @param month
    * @param year
    * @param securityCode
    * @return void
     */
    public void processCard(String number, int month, int year, String securityCode)
            throws AuthenticationException,Exception {

        Card card = new Card("4242424242424242", 12, 2017, "123");
        boolean validation = card.validateCard();
        if(validation){
            Stripe stripe = new Stripe("pk_test_GPxTanwMDDjejJrMYzQFIxWf");
            stripe.createToken(
                    card,
                    new TokenCallback() {
                        @Override
                        public void onSuccess(Token token) {
                            post = new SendPost();
                            //POST request here
                            /*
                            This needs to be done as background thread
                            Need to send data through POST request as an JSON object
                            Convert List<MAP> to JSON Object
                             */

                            Log.v("Token ID",token.getId());
                            try {

                                post.PostData(token);

                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }catch(){

                            }
                        }
                        @Override
                        public void onError(Exception error) {
                            Toast.makeText(getBaseContext(),
                                    error.getLocalizedMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        }
//        else if(!card.validateCard()){
//            throw new Exception("This card is not valid");
//        }
//        else if(!card.validateCVC()){
//            throw new Exception("This card is not valid");
//        }
//        else if(!card.validateExpiryDate()){
//            throw new Exception("This card is not valid");
//        }
        else{
            throw new Exception("This card is not valid");
        }
    }

}
