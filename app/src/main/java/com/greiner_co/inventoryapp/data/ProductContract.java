package com.greiner_co.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for products database
 * Created by Jens Greiner on 15.07.17.
 */

public final class ProductContract {
    public static final String CONTENT_AUTHORITY = "com.greiner_co.inventoryapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "products";

    private ProductContract() {
        // intentionally left bland to prevent others from instantiating the contract class
    }

    public static class ProductEntry implements BaseColumns {
        // Content Uri to access the product data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // MIME type of the {@link #CONTENT_URI} for a list of products
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // MIME type of the {@link #CONTENT_URI} for a single product
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "products";

        // Unique ID number for the product (type: INTEGER, AUTOINCREMENT, PRIMARY KEY)
        public static final String _ID = BaseColumns._ID;

        // Name of the product (type: TEXT, NOT NULL)
        public static final String COLUMN_PRODUCT_NAME = "name";

        // Price of the single product (type: REAL, NOT NULL)
        public static final String COLUMN_PRODUCT_PRICE = "price";

        // Quantity of the product to be available (type: INTEGER, NOT NULL, DEFAULT 0)
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";

        // Supplier of the product (type: TEXT, NOT NULL)
        public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";

        // Supplier phone of the product (type: TEXT, NOT NULL)
        public static final String COLUMN_PRODUCT_SUPPLIER_PHONE = "phone";

        // Supplier email address of the product (type: TEXT, NOT NULL)
        public static final String COLUMN_PRODUCT_SUPPLIER_EMAIL = "email";

        // Image of the product (type: TEXT, NOT NULL, DEFAULT "default_image"
        // @link https://stackoverflow.com/a/6606163/1469260
        public static final String COLUMN_PRODUCT_IMAGE = "image";
    }
}
