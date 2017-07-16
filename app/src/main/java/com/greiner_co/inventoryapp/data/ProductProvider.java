package com.greiner_co.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.greiner_co.inventoryapp.data.ProductContract.ProductEntry;

/**
 * @link ContentProvider} for Inventory App.
 * Created by Jens Greiner on 15.07.17.
 */

public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private ProductDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query URI " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues) {
        // Validate given contentValues
        validateContentValues(contentValues);

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(ProductEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return ContentUris.withAppendedId(uri, id);
    }

    private void validateContentValues(ContentValues contentValues) {
        // Check for valid name
        nameIsValid(contentValues);
        // Check for valid price
        priceIsValid(contentValues);
        // Check for valid amount
        quantityIsValid(contentValues);
        // Check for valid supplier
        supplierIsValid(contentValues);
        // Check for valid supplier
        supplierPhoneIsValid(contentValues);
        // Check for valid supplier
        supplierEmailIsValid(contentValues);
        // Check for valid image name
        imageIsValid(contentValues);
    }

    private void supplierEmailIsValid(ContentValues contentValues) {
        String supplierEmail = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        if (supplierEmail == null || supplierEmail.isEmpty()) {
            throw new IllegalArgumentException("Product requires a supplier email address");
        }
    }

    private void supplierPhoneIsValid(ContentValues contentValues) {
        String supplierPhone = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if (supplierPhone == null || supplierPhone.isEmpty()) {
            throw new IllegalArgumentException("Product requires a supplier phone number");
        }
    }

    private void imageIsValid(ContentValues contentValues) {
        String image = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Product requires an image");
        }
    }

    private void supplierIsValid(ContentValues contentValues) {
        String supplier = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier == null || supplier.isEmpty()) {
            throw new IllegalArgumentException("Product requires a supplier");
        }
    }

    private void quantityIsValid(ContentValues contentValues) {
        Integer amount = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("Product requires a non-negative amount in stock");
        }
    }

    private void priceIsValid(ContentValues contentValues) {
        Float price = contentValues.getAsFloat(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price.isNaN() || price < 0) {
            throw new IllegalArgumentException("Product requires a non-negative price");
        }
    }

    private void nameIsValid(ContentValues contentValues) {
        String name = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Product requires a name");
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                int numberOfRows = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                if (numberOfRows > 0) {
                    if (getContext() != null) {
                        getContext().getContentResolver().notifyChange(uri, null);
                    }
                }
                return numberOfRows;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        // If there are no values to update, bail out early
        if (contentValues == null || contentValues.size() == 0) {
            return 0;
        }

        /* Check for each possible value if it is contained and valid */
        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            nameIsValid(contentValues);
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            priceIsValid(contentValues);
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            quantityIsValid(contentValues);
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER)) {
            supplierIsValid(contentValues);
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE)) {
            supplierPhoneIsValid(contentValues);
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL)) {
            supplierEmailIsValid(contentValues);
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_IMAGE)) {
            imageIsValid(contentValues);
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int numberOfRows;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                numberOfRows = database.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numberOfRows = database.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Update not supported for " + uri);
        }

        if (numberOfRows > 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return numberOfRows;
    }
}
