package com.example.ibmhyperlegerblockchain;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.ibmhyperlegerblockchain.constants.Constants.LOG_TAG;

public class Webservice {
    public static final String BASE_URL = "http://YOUR_LOCAL_ADDRESS/api";

    public static final String CUSTOMER_SIGN_UP = "/Customer";
    public static final String ADD_BALANCE = "/bankAccount";
    public static final String TRANSFER_AMOUNT = "/transferAmount";


    public String postRequest(String basUrl, String jsonParam){

        Log.d(LOG_TAG , "postRequest : "+basUrl+" -- "+jsonParam);

        String response = "";

        try {
            URL url = new URL(basUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches (false);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
            wr.writeBytes(jsonParam.toString());

            wr.flush();
            wr.close();

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));

            String output;
            StringBuilder stringBuilder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
                stringBuilder.append('\r');
            }
            response = stringBuilder.toString();

            Log.d(LOG_TAG , String.valueOf(connection.getResponseCode())+" -- "+response);

            connection.disconnect();


        } catch (Exception e) {
            Log.d(LOG_TAG , "Exception : "+e.toString());
        }

        return response;
    }

    public String putRequest(String basUrl, String jsonParam){

        Log.d(LOG_TAG , "putRequest : "+basUrl+" -- "+jsonParam);

        String response = "";

        try {
            URL url = new URL(basUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches (false);

            DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
            wr.writeBytes(jsonParam.toString());

            wr.flush();
            wr.close();

            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));

            String output;
            StringBuilder stringBuilder = new StringBuilder();
            while ((output = br.readLine()) != null) {
                stringBuilder.append(output);
                stringBuilder.append('\r');
            }
            response = stringBuilder.toString();

            Log.d(LOG_TAG , String.valueOf(connection.getResponseCode())+" -- "+response);

            connection.disconnect();


        } catch (Exception e) {
            Log.d(LOG_TAG , "Exception : "+e.toString());
        }

        return response;
    }


    public String getRequest(String basUrl){

        Log.d(LOG_TAG , "getRequest : "+basUrl);

        String response = "Wrong Customer Id";

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(basUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = urlConnection.getInputStream();

            InputStreamReader isw = new InputStreamReader(in);

            int data = isw.read();
            StringBuilder stringBuilder = new StringBuilder();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                stringBuilder.append(current);
                stringBuilder.append('\r');
            }
            response = stringBuilder.toString();
            Log.d(LOG_TAG , "getRequest : "+stringBuilder.toString());
        } catch (Exception e) {
            Log.d(LOG_TAG , "Exception getRequest : "+e.toString());
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return response;
    }
}