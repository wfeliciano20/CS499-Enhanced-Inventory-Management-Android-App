//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;
//
//
//import android.content.Intent;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//
//import com.felicianowilliam.cs360projecttwo.InventoryActivity;
//import com.felicianowilliam.cs360projecttwo.MyDatabaseHelper;
//import com.felicianowilliam.cs360projecttwo.R;
//import com.felicianowilliam.cs360projecttwo.SessionManager;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//public class AddItemActivity extends AppCompatActivity {
//
//        private MyDatabaseHelper dbHelper;
//
//        // UI Elements
//        private Toolbar toolbar;
//        private TextInputLayout tilItemName, tilItemQuantity;
//        private TextInputEditText editTextItemName, editTextItemQuantity;
//        private MaterialButton buttonSaveItem;
//        private String username;
//        private SessionManager sessionManager;
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            sessionManager = new SessionManager(getApplicationContext());
//            // Use the modified layout file
//            setContentView(R.layout.activity_add_item);
//
//            dbHelper = new MyDatabaseHelper(this);
//
//            //  Map values with ids
//            toolbar = findViewById(R.id.toolbarAdd);
//            tilItemName = findViewById(R.id.tilItemName);
//            tilItemQuantity = findViewById(R.id.tilItemQuantity);
//            editTextItemName = findViewById(R.id.editTextItemName);
//            editTextItemQuantity = findViewById(R.id.editTextItemQuantity);
//            buttonSaveItem = findViewById(R.id.buttonSaveItem);
//
//
//            //  Setup Toolbar
//            setSupportActionBar(toolbar);
//            if (getSupportActionBar() != null) {
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//                getSupportActionBar().setTitle("Add Inventory Item");
//                toolbar.setTitleTextColor(getResources().getColor(R.color.dark_purple_text));
//                toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.dark_purple_text));
//
//            }
//
//
//            Intent intent = getIntent();
//            username = intent.getStringExtra(EXTRA_USERNAME);
//            Log.d("AddItemActivity", "Username received: " + username);
//
//            // Check if username was received
//            if (username == null || username.isEmpty()) {
//                Log.e("AddItemActivity", "Username not passed correctly via Intent extra: " + EXTRA_USERNAME);
//                Toast.makeText(this, "Error: Cannot determine user. Unable to add item.", Toast.LENGTH_LONG).show();
//                finish();
//                return;
//            }
//
//            setTitle("Add Inventory Item");
//            buttonSaveItem.setText("Add Item");
//
//
//            //Set Save Button Listener
//            buttonSaveItem.setOnClickListener(v -> saveItem());
//        }
//
//        // Method to save (or update) the item
//        private void saveItem() {
//            //  Clear previous errors
//            tilItemName.setError(null);
//            tilItemQuantity.setError(null);
//
//            // Get Input
//            String name = editTextItemName.getText().toString().trim();
//            String quantityStr = editTextItemQuantity.getText().toString().trim();
//
//            // Validation
//            boolean isValid = true;
//            int quantity = 0;
//
//            if (TextUtils.isEmpty(name)) {
//                tilItemName.setError("Item name is required");
//                isValid = false;
//            }
//
//            if (TextUtils.isEmpty(quantityStr)) {
//                tilItemQuantity.setError("Quantity is required");
//                isValid = false;
//            } else {
//                try {
//                    quantity = Integer.parseInt(quantityStr);
//                    if (quantity < 0) {
//                        tilItemQuantity.setError("Quantity cannot be negative");
//                        isValid = false;
//                    }
//                } catch (NumberFormatException e) {
//                    tilItemQuantity.setError("Invalid number format");
//                    isValid = false;
//                }
//            }
//            // Stop if validation fails
//            if (!isValid) {
//                return;
//            }
//            Toast.makeText(this,username,Toast.LENGTH_SHORT).show();
//            Cursor dbCursor = dbHelper.getUserByUsername(username);
//            Log.d( "dbCursor", dbCursor.toString());
//            int userID = -1; // Default to -1 if not found
//            if (dbCursor != null && dbCursor.moveToFirst()) {
//                int userIdIndex = dbCursor.getColumnIndex("_id");
//                if (userIdIndex != -1) {
//                    userID = dbCursor.getInt(userIdIndex);
//                    Log.d("AddItemActivity", "User ID: " + userID);
//                }
//                dbCursor.close();
//            }
//
//            // Handle case where user was not found
//            if (userID == -1) {
//                Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
//                return; // Exit method to prevent further execution
//            }
//
//
//
//
//            if(dbHelper.insertINVENTORYItem( name, quantity,userID )){
//                Intent resultIntent = new Intent(AddItemActivity.this, InventoryActivity.class);
//                resultIntent.putExtra(EXTRA_USERNAME, username);
//                startActivity(resultIntent);
//                finish();
//
//            }else{
//                Toast.makeText(this, "Error updating item", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        // Toolbar Menu Handling
//        @Override
//        public boolean onCreateOptionsMenu(Menu menu) {
//            // Reuse the same menu as the inventory list
//            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
//            return true;
//        }
//
//        @Override
//        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//            int id = item.getItemId();
//
//            if (id == R.id.logout) {
//                sessionManager.logoutUser();
//                return true;
//            }
//
//            return super.onOptionsItemSelected(item);
//        }
//
//    }
//
//
//
