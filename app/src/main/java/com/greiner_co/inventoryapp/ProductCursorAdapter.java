package com.greiner_co.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.greiner_co.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Custom {@link CursorAdapter} to handle the products list
 * Created by Jens Greiner on 15.07.17.
 */

public class ProductCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        int nameColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        String productName = cursor.getString(nameColumnIndex);
        String productPrice = String.valueOf(cursor.getFloat(priceColumnIndex));
        final int quantity = cursor.getInt(quantityColumnIndex);
        String productQuantity = String.valueOf(quantity);
        final Uri uri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID)));
        Log.d(LOG_TAG, "URI: " + uri);

        nameTextView.setText(productName);
        priceTextView.setText(context.getString(R.string.price_label) + " " + productPrice);
        quantityTextView.setText(productQuantity);

        Button saleButton = (Button) view.findViewById(R.id.button_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    int newQuantity = quantity - 1;

                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                    context.getContentResolver().update(uri, values, null, null);
                    Log.d(LOG_TAG, "URI for update: " + uri);
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_product_sold), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
