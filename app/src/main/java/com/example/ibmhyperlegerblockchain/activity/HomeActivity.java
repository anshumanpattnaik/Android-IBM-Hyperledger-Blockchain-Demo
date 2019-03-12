package com.example.ibmhyperlegerblockchain.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ibmhyperlegerblockchain.constants.Constants;
import com.example.ibmhyperlegerblockchain.R;
import com.example.ibmhyperlegerblockchain.Webservice;
import com.example.ibmhyperlegerblockchain.adapter.TransactionAdapter;
import com.example.ibmhyperlegerblockchain.model.BankAccount;
import com.example.ibmhyperlegerblockchain.model.Transaction;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.ibmhyperlegerblockchain.constants.Constants.LOG_TAG;
import static com.example.ibmhyperlegerblockchain.Utils.getBalance;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mTransactionListView;
    private Button mFundTransferBtn;
    private TextView mBalanceTxt, mCustomerNameTxt, mLogoutBtn, mAddBalanceBtn;

    private TransactionAdapter mAdapter;

    private ArrayList<Transaction> mTransactions;

    private SharedPreferences prefs;

    private String mTransactionId;
    private String mCustomerId;
    private String mCustomerName;
    private String mReceiverId;

    private double mBalance;
    private String mTransferAmount;

    private String mBalanceValue;

    private ProgressDialog dialog;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().hide();

        dialog = new ProgressDialog(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mTransactionListView = (RecyclerView) findViewById(R.id.transcation_list);
        mBalanceTxt = (TextView) findViewById(R.id.balance_txt);
        mCustomerNameTxt = (TextView) findViewById(R.id.customer_name);
        mAddBalanceBtn = (TextView) findViewById(R.id.add_money_btn);
        mLogoutBtn = (TextView) findViewById(R.id.logout_btn);
        mFundTransferBtn = (Button)findViewById(R.id.money_transfer_btn);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mBalance = prefs.getFloat(Constants.BALANCE, 0);

        new FetchTransactionTask().execute();

        if(mBalance > 0){
            setBalance(mBalanceTxt, mBalance);
        }else{
            new FetchBalance().execute();
        }

        mTransactionId = prefs.getString(Constants.TRANSACTION_ID, null);
        mCustomerName = prefs.getString(Constants.CUSTOMER_NAME, null);
        mCustomerId = prefs.getString(Constants.CUSTOMER_ID, null);

        mCustomerNameTxt.setText(mCustomerName);

        mTransactions = new ArrayList<>();

        setTransactionAdapter();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchBalance().execute();
                new FetchTransactionTask().execute();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mAddBalanceBtn.setOnClickListener(this);
        mLogoutBtn.setOnClickListener(this);
        mFundTransferBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_money_btn:
                final Dialog addBalanceDialog = new Dialog(this);
                addBalanceDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                addBalanceDialog.setContentView(R.layout.dialog_add_balance);

                final EditText balanceTxt = (EditText) addBalanceDialog.findViewById(R.id.add_amount_etd);
                Button addBalanceBtn = (Button) addBalanceDialog.findViewById(R.id.add_balance_btn);

                addBalanceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBalanceValue = balanceTxt.getText().toString();
                        if(mBalanceValue.length() > 0){
                            boolean isAccount = prefs.getBoolean(Constants.isAccount, false);
                            if(isAccount){
                                new UpdateBalanceTask().execute();
                            }else{
                                new AddBalanceTask().execute();
                            }
                            addBalanceDialog.dismiss();
                        }else{
                            Toast.makeText(HomeActivity.this, "Field cannot be blank", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                addBalanceDialog.show();
                break;
            case R.id.money_transfer_btn:
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_fund_transfer);

                final EditText receiverAccountEtd = (EditText) dialog.findViewById(R.id.customer_account_etd);
                final EditText transferAmountEtd = (EditText) dialog.findViewById(R.id.amount_etd);
                Button transferBtn = (Button) dialog.findViewById(R.id.transfer_btn);

                transferBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mReceiverId = receiverAccountEtd.getText().toString();
                        mTransferAmount = transferAmountEtd.getText().toString();

                        double currentBalance = prefs.getFloat(Constants.BALANCE, 0);

                        if(mReceiverId.length() > 0 && mTransferAmount.length() > 0){
                            if(!mReceiverId.contains(mCustomerId)){
                                if(currentBalance >= Double.parseDouble(mTransferAmount)){
                                    new TransferBalanceTask().execute();
                                    dialog.dismiss();
                                }else{
                                    Toast.makeText(HomeActivity.this, "Not enough balance to send", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(HomeActivity.this, "Invalid Id", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(HomeActivity.this, "Field cannot be blank", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.show();
                break;
            case R.id.logout_btn:
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.commit();

                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
                default:
                    break;
        }
    }

    private void setTransactionAdapter(){
        mAdapter = new TransactionAdapter(mTransactions);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mTransactionListView.setLayoutManager(mLayoutManager);
        mTransactionListView.setItemAnimator(new DefaultItemAnimator());
        mTransactionListView.setAdapter(mAdapter);
    }

    private class FetchTransactionTask extends AsyncTask<String, String, String> {

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

            Webservice webservice = new Webservice();
            response = webservice.getRequest(Webservice.BASE_URL+Webservice.TRANSFER_AMOUNT);

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.d(LOG_TAG, "onPostExecute : "+result);

            mTransactions.clear();

            String response = result.replaceAll("\r", "");

            try{
                JSONArray jsonarray = new JSONArray(response);
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonObject = jsonarray.getJSONObject(i);
                    String transactionId = jsonObject.getString("transactionId");
                    String senderAccountId = jsonObject.getString("from");
                    String receiverAccountId = jsonObject.getString("to");
                    double amount = jsonObject.getDouble("amount");

                    String senderId = senderAccountId.replace(Constants.BANK_ACCOUNT_TAG, "");
                    String receiverId = receiverAccountId.replace(Constants.BANK_ACCOUNT_TAG,"");

                    if(mCustomerId.equals(senderId)){
                        Log.d(LOG_TAG, "senderId : "+senderId+" -- "+receiverId+" -- "+amount);
                        Transaction transaction = new Transaction();
                        transaction.setTransactionId(transactionId);
                        transaction.setReceiverName(receiverId);
                        transaction.setAmount(amount);
                        mTransactions.add(transaction);
                    }
                    setTransactionAdapter();
                }
            }catch (Exception e){
            }
        }
    }

    private class FetchBalance extends AsyncTask<String, String, String> {

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

            Webservice webservice = new Webservice();
            response = webservice.getRequest(Webservice.BASE_URL+Webservice.ADD_BALANCE+"/"+mCustomerId);

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
                BankAccount bankAccount = gson.fromJson(response, BankAccount.class);
                Log.d(LOG_TAG , "onPostExecute : "+response+" --- "+bankAccount.getBalance());

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Constants.isAccount, true);
                editor.putFloat(Constants.BALANCE, bankAccount.getBalance());
                editor.commit();

                double currentBalance = prefs.getFloat(Constants.BALANCE, 0);
                double totalBalance = bankAccount.getBalance();

                setBalance(mBalanceTxt, totalBalance);

            }catch (Exception e){
            }
        }
    }

    private class AddBalanceTask extends AsyncTask<String, String, String> {

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

            JSONObject balanceObject = new JSONObject();
            try {
                balanceObject.put("$class", Constants.BANK_ACCOUNT_TAG);
                balanceObject.put("customer", Constants.CUSTOMER_TAG+mCustomerId);
                balanceObject.put("accountId", mCustomerId);
                balanceObject.put("balance", Double.parseDouble(mBalanceValue));

                Log.d(Constants.LOG_TAG, ""+balanceObject);

                Webservice webservice = new Webservice();
                response = webservice.postRequest(Webservice.BASE_URL+Webservice.ADD_BALANCE, balanceObject.toString());

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
                BankAccount bankAccount = gson.fromJson(response, BankAccount.class);
                Log.d(LOG_TAG , "onPostExecute : "+response+" --- "+bankAccount.getBalance());

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Constants.isAccount, true);
                editor.putFloat(Constants.BALANCE, bankAccount.getBalance());
                editor.commit();

                setBalance(mBalanceTxt, bankAccount.getBalance());

            }catch (Exception e){
            }
        }
    }

    private class UpdateBalanceTask extends AsyncTask<String, String, String> {

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

            JSONObject balanceObject = new JSONObject();
            try {
                balanceObject.put("$class", Constants.BANK_ACCOUNT);
                balanceObject.put("customer", Constants.CUSTOMER_TAG+mCustomerId);

                double currentBalance = prefs.getFloat(Constants.BALANCE, 0);
                balanceObject.put("balance", currentBalance + Double.parseDouble(mBalanceValue));

                Log.d(Constants.LOG_TAG, ""+balanceObject);

                Webservice webservice = new Webservice();
                response = webservice.putRequest(Webservice.BASE_URL+Webservice.ADD_BALANCE+"/"+mCustomerId, balanceObject.toString());

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
                BankAccount bankAccount = gson.fromJson(response, BankAccount.class);
                Log.d(LOG_TAG , "onPostExecute : "+response+" --- "+bankAccount.getBalance());

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putFloat(Constants.BALANCE, bankAccount.getBalance());
                editor.commit();

                setBalance(mBalanceTxt, bankAccount.getBalance());

            }catch (Exception e){
            }
        }
    }

    private class TransferBalanceTask extends AsyncTask<String, String, String> {

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

            JSONObject transferJsonObject = new JSONObject();
            try {
                transferJsonObject.put("$class", Constants.TRANSFER_AMOUNT);
                transferJsonObject.put("from", Constants.BANK_ACCOUNT_TAG+mCustomerId);
                transferJsonObject.put("to", Constants.BANK_ACCOUNT_TAG+mReceiverId);
                transferJsonObject.put("amount", Double.parseDouble(mTransferAmount));

                Log.d(Constants.LOG_TAG, ""+transferJsonObject);

                Webservice webservice = new Webservice();
                response = webservice.postRequest(Webservice.BASE_URL+Webservice.TRANSFER_AMOUNT, transferJsonObject.toString());

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

            double currentBalance = prefs.getFloat(Constants.BALANCE, 0);
            double totalBalance = currentBalance - Double.parseDouble(mTransferAmount);

            setBalance(mBalanceTxt, totalBalance);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat(Constants.BALANCE, (float) totalBalance);
            editor.commit();

            Log.d(LOG_TAG, "onPostExecute : "+currentBalance+" - "+mTransferAmount+" = "+totalBalance);

            new FetchTransactionTask().execute();
        }
    }

    private void setBalance(TextView balanceTxt, double balance){
        balanceTxt.setText(""+getBalance(balance));
    }
}
