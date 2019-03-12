package com.example.ibmhyperlegerblockchain.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ibmhyperlegerblockchain.constants.Constants;
import com.example.ibmhyperlegerblockchain.R;
import com.example.ibmhyperlegerblockchain.Webservice;
import com.example.ibmhyperlegerblockchain.model.Customer;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.ibmhyperlegerblockchain.constants.Constants.LOG_TAG;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mCustomerEtd, mFirstNameEtd, mLastNameEtd;
    private Button mCreateBtn, mBackBtn;

    private String mCustomerId, mFirstName, mLastName;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        dialog = new ProgressDialog(this);

        mCustomerEtd = (EditText) findViewById(R.id.customer_id_etd);
        mFirstNameEtd = (EditText) findViewById(R.id.customer_fname_etd);
        mLastNameEtd = (EditText) findViewById(R.id.customer_lname_etd);
        mCreateBtn = (Button) findViewById(R.id.create_btn);
        mBackBtn = (Button) findViewById(R.id.back_btn);

        mCreateBtn.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.create_btn:
                mCustomerId = mCustomerEtd.getText().toString();
                mFirstName = mFirstNameEtd.getText().toString();
                mLastName = mLastNameEtd.getText().toString();

                if(mCustomerId.length() > 0 && mFirstName.length() > 0 && mLastName.length() > 0){
                    new CreateAccountTask().execute();
                }else{
                    Toast.makeText(getApplicationContext(), "Field Cannot be blank", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.back_btn:
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
                default:
                    break;
        }
    }

    private class CreateAccountTask extends AsyncTask<String, String, String> {

        private String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(Constants.PLEASE_WAIT);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("$class", Constants.CUSTOMER);
                jsonObject.put("customerId", mCustomerId);
                jsonObject.put("firstName", mFirstName);
                jsonObject.put("lastName", mLastName);

                Webservice Webservice = new Webservice();
                response = Webservice.postRequest(Webservice.BASE_URL+Webservice.CUSTOMER_SIGN_UP, jsonObject.toString());

            } catch (JSONException e) {
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.d(LOG_TAG, "onPostExecute : "+result);

            String response = result.replaceAll("\r", "");

            try{
                Gson gson = new Gson();
                Customer customer = gson.fromJson(response, Customer.class);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.CUSTOMER_CLASS, customer.get$class());
                editor.putString(Constants.CUSTOMER_ID, customer.getCustomerId());
                editor.putString(Constants.CUSTOMER_NAME, customer.getFirstName()+" "+customer.getLastName());
                editor.commit();

                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }catch (Exception e){
            }
        }
    }
}
