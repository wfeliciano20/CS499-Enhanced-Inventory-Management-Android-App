//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//import androidx.appcompat.app.AppCompatActivity;
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Patterns;
//import android.widget.Toast;
//
//import com.felicianowilliam.cs360projecttwo.InventoryActivity;
//import com.felicianowilliam.cs360projecttwo.MyDatabaseHelper;
//import com.felicianowilliam.cs360projecttwo.R;
//import com.felicianowilliam.cs360projecttwo.RegisterActivity;
//import com.felicianowilliam.cs360projecttwo.SessionManager;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//public class SigninActivity extends AppCompatActivity {
//    // DB helper
//    private com.felicianowilliam.cs360projecttwo.MyDatabaseHelper myDbHelper;
//    // Declare UI elements
//    private TextInputLayout tilEmailSignin, tilPasswordSignin;
//    private TextInputEditText editTextEmailSignin, editTextPasswordSignin;
//    private MaterialButton buttonSigninAction, buttonRegisterLink;
//    private com.felicianowilliam.cs360projecttwo.SessionManager sessionManager;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_signin); // Link the layout file
//
//        sessionManager = new SessionManager(getApplicationContext());
//        // Initialize UI elements
//        tilEmailSignin = findViewById(R.id.tilEmailSignin);
//        tilPasswordSignin = findViewById(R.id.tilPasswordSignin);
//        editTextEmailSignin = findViewById(R.id.editTextEmailSignin);
//        editTextPasswordSignin = findViewById(R.id.editTextPasswordSignin);
//        buttonSigninAction = findViewById(R.id.buttonSigninAction);
//        buttonRegisterLink = findViewById(R.id.buttonRegisterLink);
//        myDbHelper =  new MyDatabaseHelper(this);
//        // Set click listener for the Sign In button
//        buttonSigninAction.setOnClickListener(v -> {
//            // Call a method to handle signin logic
//            handleSignin();
//        });
//
//        // Set click listener for the Register button/link
//        buttonRegisterLink.setOnClickListener(v -> {
//            // Navigate to your RegisterActivity
//            Intent registerIntent = new Intent(SigninActivity.this, RegisterActivity.class);
//            startActivity(registerIntent);
//            finish();
//        });
//    }
//
//    private void handleSignin() {
//        // Clear previous errors
//        tilEmailSignin.setError(null);
//        tilPasswordSignin.setError(null);
//
//        // Get values
//        String email = editTextEmailSignin.getText().toString().trim();
//        String password = editTextPasswordSignin.getText().toString().trim();
//
//        // Basic Validation
//        boolean isValid = true;
//
//        if (TextUtils.isEmpty(email)) {
//            tilEmailSignin.setError("Email is required");
//            isValid = false;
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            tilEmailSignin.setError("Enter a valid email address");
//            isValid = false;
//        }
//
//        if (TextUtils.isEmpty(password)) {
//            tilPasswordSignin.setError("Password is required");
//            isValid = false;
//        }
//
//
//        if (!isValid) {
//            return; // Stop processing if validation fails
//        }
//
//
//        // check if login is successful
//        if(myDbHelper.login(email,password)){
//            Toast.makeText(this, "Sign In Successful", Toast.LENGTH_LONG).show();
//            sessionManager.createLoginSession(email);
//             //Navigate to the main inventory screen after successful sign-in
//            Intent inventoryIntent = new Intent(SigninActivity.this, InventoryActivity.class);
//            inventoryIntent.putExtra(EXTRA_USERNAME, email);
//            //Clear the activity stack so the user can't go back to Signin using the back button
//            inventoryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(inventoryIntent);
//            finish();
//        }else{
//            Toast.makeText(this, "Sign In Failed", Toast.LENGTH_LONG).show();
//        }
//    }
//}
