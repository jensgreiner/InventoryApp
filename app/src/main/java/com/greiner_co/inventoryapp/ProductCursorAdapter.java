package com.greiner_co.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.greiner_co.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Custom {@link CursorAdapter} to handle the products list
 * Created by Jens Greiner on 15.07.17.
 */

public class ProductCursorAdapter extends CursorAdapter {
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView amountTextView = (TextView) view.findViewById(R.id.amount);

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int amountColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_AMOUNT);

        String productName = cursor.getString(nameColumnIndex);
        String productPrice = String.valueOf(cursor.getFloat(priceColumnIndex));
        String productAmount = String.valueOf(cursor.getInt(amountColumnIndex));

        nameTextView.setText(productName);
        priceTextView.setText(productPrice);
        amountTextView.setText(productAmount);
    }
}
