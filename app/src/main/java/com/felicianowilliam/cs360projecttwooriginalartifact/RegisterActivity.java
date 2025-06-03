//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//
//import static com.felicianowilliam.cs360projecttwo.Constants.EXTRA_USERNAME;
//
//import androidx.appcompat.app.AppCompatActivity;
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
//import com.felicianowilliam.cs360projecttwo.SessionManager;
//import com.felicianowilliam.cs360projecttwo.SigninActivity;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.android.material.textfield.TextInputLayout;
//
//public class RegisterActivity extends AppCompatActivity {
//
//    private com.felicianowilliam.cs360projecttwo.MyDatabaseHelper myDbHelper;
//    // Declare UI elements
//    private TextInputLayout tilEmail, tilPassword;
//    private TextInputEditText editTextEmail, editTextPassword;
//    private MaterialButton buttonRegister, buttonSignin;
//    private SessionManager sessionManager;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
//        sessionManager = new SessionManager(getApplicationContext());
//        myDbHelper = new MyDatabaseHelper(this);
//
//        // Initialize UI elements
//        tilEmail = findViewById(R.id.tilEmail);
//        tilPassword = findViewById(R.id.tilPassword);
//        editTextEmail = findViewById(R.id.editTextEmail);
//        editTextPassword = findViewById(R.id.editTextPassword);
//        buttonRegister = findViewById(R.id.buttonRegister);
//        buttonSignin = findViewById(R.id.buttonSignin);
//
//        // Set click listener for the Register button
//        buttonRegister.setOnClickListener(v -> {
//            // Call a method to handle registration logic
//            handleRegistration();
//        });
//
//
//        buttonSignin.setOnClickListener(v -> {
//
//            Toast.makeText(RegisterActivity.this, "Navigate to Signin Screen", Toast.LENGTH_SHORT).show();
//            // Example Navigation:
//            Intent signinIntent = new Intent(RegisterActivity.this, SigninActivity.class);
//            startActivity(signinIntent);
//            finish();
//        });
//    }
//
//    private void handleRegistration() {
//        // Clear previous errors
//        tilEmail.setError(null);
//        tilPassword.setError(null);
//
//        // Get input values
//        String email = editTextEmail.getText().toString().trim();
//        String password = editTextPassword.getText().toString().trim();
//
//        // Basic Validation
//        boolean isValid = true;
//
//        if (TextUtils.isEmpty(email)) {
//            tilEmail.setError("Email is required");
//            isValid = false;
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            tilEmail.setError("Enter a valid email address");
//            isValid = false;
//        }
//
//        if (TextUtils.isEmpty(password)) {
//            tilPassword.setError("Password is required");
//            isValid = false;
//        } else if (password.length() < 6) {
//            tilPassword.setError("Password must be at least 6 characters");
//            isValid = false;
//        }
//
//        if (!isValid) {
//            return;
//        }
//
//        // If validation passes
//        if(myDbHelper.registerUser(email,password)){
//            Toast.makeText(this, "Registration Successful", Toast.LENGTH_LONG).show();
//            sessionManager.createLoginSession(email);
//            // Navigate to another screen after successful registration
//            Intent mainIntent = new Intent(RegisterActivity.this, InventoryActivity.class);
//            mainIntent.putExtra(EXTRA_USERNAME, email);
//            //Clear the activity stack so the user can't go back to Register using the back button
//            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(mainIntent);
//            finish();
//        }else{
//            Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show();
//        }
//    }
//}
