//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_ID;
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_NAME;
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_ITEM_QUANTITY;
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.content.Intent;
//import android.database.Cursor;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.Toast;
//
//import com.felicianowilliam.cs360projecttwo.AddItemActivity;
//import com.felicianowilliam.cs360projecttwo.Constants;
//import com.felicianowilliam.cs360projecttwo.EditItemActivity;
//import com.felicianowilliam.cs360projecttwo.InventoryAdapter;
//import com.felicianowilliam.cs360projecttwo.InventoryItem;
//import com.felicianowilliam.cs360projecttwo.MyDatabaseHelper;
//import com.felicianowilliam.cs360projecttwo.R;
//import com.felicianowilliam.cs360projecttwo.SessionManager;
//import com.felicianowilliam.cs360projecttwo.SigninActivity;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnItemActionListener {
//
//
//    private MyDatabaseHelper dbHelper;
//    private RecyclerView recyclerViewInventory;
//    private InventoryAdapter inventoryAdapter;
//    private List<InventoryItem> inventoryItemList;
//    private FloatingActionButton fabAddItem;
//    private Toolbar toolbar;
//    private String currentUsername;
//    private SessionManager sessionManager;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        sessionManager = new SessionManager(getApplicationContext());
//
//        if (!sessionManager.isLoggedIn()) {
//            Log.w("InventoryActivity", "User not logged in. Redirecting to Signin.");
//            // Redirect using the method in SessionManager or manually:
//            Intent i = new Intent(InventoryActivity.this, SigninActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(i);
//            finish();
//            return;
//        }
//
//        setContentView(R.layout.activity_inventory);
//        // Find Views
//        toolbar = findViewById(R.id.toolbar);
//        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
//        fabAddItem = findViewById(R.id.fabAddItem);
//
//        // Retrieve the username from intent extras
//        String usernameExtra = getIntent().getStringExtra(EXTRA_USERNAME);
//        if (usernameExtra == null) {
//            usernameExtra = sessionManager.getUsername();
//        }
//
//
//        currentUsername = sessionManager.getUsername();
//        usernameExtra = usernameExtra == null ? sessionManager.getUsername() : usernameExtra;
//        if (currentUsername == null) {
//            currentUsername = getIntent().getStringExtra(Constants.EXTRA_USERNAME);
//        }
//
//        if (currentUsername == null || currentUsername.isEmpty()) {
//            Log.e("InventoryActivity", "Username is NULL even after login check. Logging out.");
//            Toast.makeText(this, "Session error. Logging out.", Toast.LENGTH_LONG).show();
//            sessionManager.logoutUser(); // Force logout and redirect
//            finish();
//            return;
//        }
//
//        inventoryItemList = new ArrayList<>();
//
//        // Initialize database and fetch user-specific items
//        dbHelper = new MyDatabaseHelper(this);
//
//        //  Setup Toolbar
//        setSupportActionBar(toolbar);
//        // Set Navigation Icon (Hamburger)
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setTitle(usernameExtra.substring(0,usernameExtra.indexOf('@'))+"'s Inventory");
//        }
//        int numberOfColumns =  1;
//        //  Setup RecyclerView
//        recyclerViewInventory.setLayoutManager(new GridLayoutManager(this,numberOfColumns));
//        inventoryAdapter = new InventoryAdapter(this, inventoryItemList, this);
//        recyclerViewInventory.setAdapter(inventoryAdapter);
//
//        loadInventoryItems(usernameExtra);
//
//        //  Setup FAB Click Listener
//        fabAddItem.setOnClickListener(view -> {
//            Intent addIntent = new Intent(this, AddItemActivity.class);
//            addIntent.putExtra(EXTRA_USERNAME, currentUsername);
//            this.startActivityForResult(addIntent,1);
//
//
//        });
//
//    }
//
//
//    private void loadInventoryItems(String username) {
//
//        // Get user ID from the authentication table using the username
//        Cursor dbCursor = dbHelper.getUserByUsername(username);
//
//        int userID = -1;
//        if (dbCursor != null && dbCursor.moveToFirst()) {
//            int userIdIndex = dbCursor.getColumnIndex("_id");
//            if (userIdIndex != -1) {
//                userID = dbCursor.getInt(userIdIndex);
//            }
//            dbCursor.close();
//        }
//
//        // Handle case where user was not found
//        if (userID == -1) {
//            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
//            return; // Exit method to prevent further execution
//        }
//
//        // Fetch inventory items where user_id = userID
//        Cursor itemsCursor = dbHelper.getAllItemsByUserId(userID);
//        if (itemsCursor != null) {
//            if (itemsCursor.getCount() == 0) { // No items found
//                Toast.makeText(this, "No items found for this user", Toast.LENGTH_SHORT).show();
//                Log.d("InventoryActivity", "No items found for this user" + userID);
//
//            } else {
//                inventoryItemList.clear();
//                Log.d("InventoryActivity", "ItemsCursor Count: " + itemsCursor.getCount());
//                while (itemsCursor.moveToNext()) {
//                    int cursorID = itemsCursor.getColumnIndex("_id");
//                    int cursorItemName = itemsCursor.getColumnIndex("item_name");
//                    int cursorQuantity = itemsCursor.getColumnIndex("quantity");
//                    if(cursorID >= 0 && cursorItemName >= 0 && cursorQuantity >= 0){
//                        int itemId = itemsCursor.getInt(cursorID);
//                        String itemName = itemsCursor.getString(cursorItemName);
//                        int quantity = itemsCursor.getInt(cursorQuantity);
//
//                        inventoryItemList.add(new InventoryItem(itemId, quantity, itemName));
//                    }
//                    else{
//                        Toast.makeText(this, "Error fetching inventory!", Toast.LENGTH_SHORT).show();
//                    }
//
//                }
//            }
//            itemsCursor.close(); // Close cursor
//        } else {
//            Toast.makeText(this, "Error fetching inventory!", Toast.LENGTH_SHORT).show();
//        }
//
//        // Update RecyclerView (Even if no items, it prevents null pointer issues)
//        inventoryAdapter.notifyDataSetChanged();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 1){
//            recreate();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        //  Refresh Data When Activity Resumes
//        Log.d("InventoryActivity", "onResume called. Reloading inventory.");
//        // Check username validity again just in case, though onCreate check should suffice
//        if (currentUsername != null && !currentUsername.isEmpty()) {
//            inventoryItemList.clear();
//            loadInventoryItems(currentUsername);
//        } else {
//            Log.e("InventoryActivity", "onResume: Username is null or empty, cannot reload inventory.");
//        }
//    }
//
//    //  Handle Toolbar Menu Item Creation
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
//        return true;
//    }
//
//    //  Handle Toolbar Item Clicks
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.logout) { // Make sure ID matches your menu item for logout
//            // Call Logout from Session Manager
//            sessionManager.logoutUser();
//            finish();
//            return true;
//        }
//        // Handle other menu items if any
//        return super.onOptionsItemSelected(item);
//    }
//
//    // Implement Adapter Click Listener Methods
//    @Override
//    public void onEditClick(InventoryItem item) {
//       Intent editIntent = new Intent(this, EditItemActivity.class);
//       editIntent.putExtra(EXTRA_USERNAME, getIntent().getStringExtra(EXTRA_USERNAME));
//       editIntent.putExtra(String.valueOf(EXTRA_ITEM_ID), item.getId());
//       editIntent.putExtra(EXTRA_ITEM_NAME, item.getItemName());
//       editIntent.putExtra(String.valueOf(EXTRA_ITEM_QUANTITY), item.getQuantity());
//       this.startActivityForResult(editIntent,1);
//
//    }
//
//    @Override
//    public void onDeleteClick(InventoryItem item) {
//        if(dbHelper.deleteOneRow(String.valueOf(item.getId()))){
//            Toast.makeText(this, "Deleting: " + item.getItemName() + " (ID: " + item.getId() + ")", Toast.LENGTH_SHORT).show();
//            String currentUsername = getIntent().getStringExtra(EXTRA_USERNAME);
//            if (currentUsername != null) {
//                loadInventoryItems(currentUsername); // reload data from db
//                Log.d("InventoryActivity", "Reloaded inventory after deletion.");
//            }
//        }
//        else{
//            Toast.makeText(this, "Item not found for deletion.", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//}
