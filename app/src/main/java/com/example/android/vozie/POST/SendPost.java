package com.example.android.vozie.POST;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by python490 on 1/22/16.
 */
public class SendPost{

    String url = "http://android.com";
    DataOutputStream printout;
    public void PostData(String token) throws MalformedURLException {
        URL site = new URL(url);
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) site.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("stripeToken",token);

            OutputStream os = urlConnection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os,"UTF-8");
            osw.write(jsonParam.toString());
            osw.flush();
            osw.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally{
            urlConnection.disconnect();
        }
    }

    public Map<String,String> chargeParams(String token){
        Map<String,String> param = new HashMap<String,String>();
        param.put("stripeToken",token);

        return param;
    }


}
