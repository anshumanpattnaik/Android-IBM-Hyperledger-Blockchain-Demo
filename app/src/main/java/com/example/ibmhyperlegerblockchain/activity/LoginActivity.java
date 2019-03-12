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

import static com.example.ibmhyperlegerblockchain.constants.Constants.LOG_TAG;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mCustomerIdEtd;
    private Button mLoginBtn, mSignUpBtn;

    private String mCustomerId;

    private SharedPreferences prefs;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        dialog = new ProgressDialog(this);

        mCustomerIdEtd = (EditText) findViewById(R.id.customer_id_etd);
        mLoginBtn = (Button) findViewById(R.id.login_btn);
        mSignUpBtn = (Button) findViewById(R.id.signup_btn);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(prefs.getString(Constants.CUSTOMER_NAME,null)!=null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        mLoginBtn.setOnClickListener(this);
        mSignUpBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
                mCustomerId = mCustomerIdEtd.getText().toString();
                if(mCustomerId.length() > 0){
                    new LoginTask().execute();
                }else{
                    Toast.makeText(getApplicationContext(), "Field Cannot be blank", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.signup_btn:
                Intent signUpIntent = new Intent(this, SignUpActivity.class);
                startActivity(signUpIntent);
                break;
                default:
                    break;
        }
    }

    private class LoginTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(Constants.PLEASE_WAIT);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = null;

            Webservice webservice = new Webservice();
            response = webservice.getRequest(Webservice.BASE_URL+Webservice.CUSTOMER_SIGN_UP+"/"+mCustomerId);

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            String response = result.replaceAll("\r", "");

            try{
                Gson gson = new Gson();
                Customer customer = gson.fromJson(response, Customer.class);

                Log.d(LOG_TAG , "onPostExecute : "+response+" --- "+customer.getCustomerId());

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Constants.CUSTOMER_CLASS, customer.get$class());
                editor.putString(Constants.CUSTOMER_ID, customer.getCustomerId());
                editor.putString(Constants.CUSTOMER_NAME, customer.getFirstName()+" "+customer.getLastName());
                editor.commit();

                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Wrong Customer Id", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
