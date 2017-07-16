package com.greiner_co.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.greiner_co.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Allows the user to create a new product or edit an existing one.
 * Created by Jens Greiner on 15.07.17.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mSupplierEditText;
    private TextView mQuantityTextView;
    private int mQuantity = 0;
    private EditText mModifierEditText;
    private ImageView mImageView;
    private Uri mImageUri;

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProductHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            Log.d(LOG_TAG, "mProductHasChanged");
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_price);
        mSupplierEditText = (EditText) findViewById(R.id.edit_text_supplier);
        mQuantityTextView = (TextView) findViewById(R.id.text_view_quantity);
        mModifierEditText = (EditText) findViewById(R.id.edit_text_modifier);
        mImageView = (ImageView) findViewById(R.id.product_image);

        // Check if value was touched to offer user to save the changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: add image handler
                Toast.makeText(EditorActivity.this, "Image auswaehlen!", Toast.LENGTH_SHORT).show();
            }
        });

        // Check whether this is an edit call or an add call
        mCurrentProductUri = getIntent().getData();
        if (mCurrentProductUri == null) {
            // Called to enter a new product
            setTitle(R.string.editor_activity_title_add_product);
            mQuantityTextView.setText(String.valueOf(mQuantity));
            mImageView.setImageResource(R.drawable.default_image);
        } else {
            // Called with an existing product to edit
            setTitle(R.string.editor_activity_title_edit_product);

            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Check for new product or edit product mode
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to the database
                if (saveProduct()) {
                    // Save successful - exit activity
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private boolean saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();

        if (mCurrentProductUri == null) {
            if (TextUtils.isEmpty(nameString)) {
                Toast.makeText(this, getString(R.string.save_name_empty), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(priceString)) {
                Toast.makeText(this, getString(R.string.save_price_empty), Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(supplierString)) {
                Toast.makeText(this, getString(R.string.save_supplier_empty), Toast.LENGTH_SHORT).show();
                return false;
            }
            /*
            if (mImageUri == null) {
                Toast.makeText(this, getString(R.string.save_image_uri_empty), Toast.LENGTH_SHORT).show();
                return false;
            }
            */
        }

        float priceValue;
        try {
            priceValue = Float.parseFloat(priceString);
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, getString(R.string.save_price_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        int quantityValue;
        try {
            quantityValue = Integer.valueOf(quantityString);
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, getString(R.string.save_quantity_format_exception), Toast.LENGTH_SHORT).show();
            return false;
        }


        String imageUriValue = "default_image";
        /* TODO
        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.save_image_uri_empty), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            imageUriValue = mImageUri.toString();
        }
        */

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceValue);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityValue);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageUriValue);

        if (mCurrentProductUri == null) {
            // We have a new product
            Uri newProductUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newProductUri == null) {
                Toast.makeText(this, getString(R.string.save_error_new_product), Toast.LENGTH_LONG).show();
                return false;
            } else {
                Toast.makeText(this, getString(R.string.product_saved), Toast.LENGTH_SHORT).show();
            }
        } else {
            // We update an existing product
            int rowsUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsUpdated != 0) {
                Toast.makeText(this, getString(R.string.product_updated), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.error_updating_product), Toast.LENGTH_LONG).show();
                return false;
            }
        }

        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return;
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.error_deleting), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.product_deleted), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_IMAGE
        };

        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int nameIndex = data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceIndex = data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityIndex = data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierIndex = data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int imageIndex = data.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_IMAGE);

            mNameEditText.setText(data.getString(nameIndex));
            mPriceEditText.setText(String.valueOf(data.getFloat(priceIndex)));
            mQuantityTextView.setText(String.valueOf(data.getInt(quantityIndex)));
            mSupplierEditText.setText(data.getString(supplierIndex));
            String imageName = data.getString(imageIndex);
            if (imageName == null || imageName.isEmpty()) {
                mImageView.setImageResource(R.drawable.default_image);
            } else {
                //TODO: handle image resource
                //mImageView.setImageURI(...);
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.getText().clear();
        mPriceEditText.getText().clear();
        mQuantityTextView.setText("");
        mSupplierEditText.getText().clear();
        mImageView.setImageResource(R.drawable.default_image);
    }
}
