package com.example.ibmhyperlegerblockchain.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ibmhyperlegerblockchain.R;
import com.example.ibmhyperlegerblockchain.model.Transaction;

import java.util.ArrayList;

import static com.example.ibmhyperlegerblockchain.Utils.getBalance;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private ArrayList<Transaction> mTransactions;

    public TransactionAdapter(ArrayList<Transaction> transactionsList) {
        this.mTransactions = transactionsList;
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout transactionLayout;
        public TextView transactionIdTxt, receiverNameTxt, amountTxt;

        public TransactionViewHolder(View view) {
            super(view);
            transactionLayout = (RelativeLayout) view.findViewById(R.id.transcations_layout);
            transactionIdTxt = (TextView) view.findViewById(R.id.transcation_txt);
            receiverNameTxt = (TextView) view.findViewById(R.id.receiver_txt);
            amountTxt = (TextView) view.findViewById(R.id.amount_txt);
        }
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_transaction_layout, parent, false);

        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {

        if(position%2 == 0){
            holder.transactionLayout.setBackgroundColor(Color.parseColor("#f2f2f2"));
        }else{
            holder.transactionLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        holder.transactionIdTxt.setText(mTransactions.get(position).getTransactionId());
        holder.receiverNameTxt.setText(mTransactions.get(position).getReceiverName());
        holder.amountTxt.setText(getBalance(mTransactions.get(position).getAmount()));
    }

    @Override
    public int getItemCount() {
        return mTransactions.size();
    }
}
