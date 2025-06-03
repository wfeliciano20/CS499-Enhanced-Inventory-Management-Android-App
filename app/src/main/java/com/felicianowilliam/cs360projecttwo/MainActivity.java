package com.felicianowilliam.cs360projecttwo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * {@code MainActivity} is the entry point of the application.
 * @author William Feliciano
 */
public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    /**
     * Called when the activity is first created.
     * This is where you should do all of your normal static set up: create views, bind data to lists, etc.
     * This method also initializes the {@code SessionManager} to manage user sessions.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

        return insets;

        });

        sessionManager = new SessionManager(getApplicationContext());
    }

    /**
     * Called when the activity is no longer visible to the user.
     * This method is typically used to release resources or save application state.
     * In this case, it logs out the user when the activity is stopped.
     * It does not take any inputs.
     * It does not produce any outputs.
     */
    @Override
    protected void onStop() {

        super.onStop();

        sessionManager.logoutUser();
    }
}