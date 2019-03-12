package com.example.ibmhyperlegerblockchain;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class Utils {
    public static String getBalance(double balance){
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setMaximumFractionDigits(2);
        Currency currency = Currency.getInstance("EUR");
        format.setCurrency(currency);
        String amount = format.format(balance);
        return amount;
    }
}
