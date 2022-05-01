package com.example.cryptocurrencypricetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegistrationActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegistrationActivity.class.getName();
    private static final int SECRET_KEY = 99;
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private NotificationHelper mNotificationHelper;

    EditText usernameEditText;
    EditText emailAddressEditText;
    EditText passwordEditText;
    EditText passwordConfirmEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        int secretKey = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secretKey != 99) {
            finish();
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        emailAddressEditText = findViewById(R.id.emailAddressEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordConfirmEditText = findViewById(R.id.passwordConfirmEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");

        usernameEditText.setText(username);
        passwordEditText.setText(password);

        mAuth = FirebaseAuth.getInstance();
        mNotificationHelper = new NotificationHelper(this);
    }

    public void registration(View view) {
        // TODO: validálás
        String userName = usernameEditText.getText().toString();
        if (("").equals(usernameEditText.getText().toString())) {
            Log.e(LOG_TAG, "Felhasználónév megadása kötelező.");
            Toast.makeText(RegistrationActivity.this, "AFelhasználónév megadása kötelező." , Toast.LENGTH_LONG).show();

            return;
        }
        String emailAddress = emailAddressEditText.getText().toString();
        if (("").equals(emailAddressEditText.getText().toString())) {
            Log.e(LOG_TAG, "E-mail cím megadása kötelező.");
            Toast.makeText(RegistrationActivity.this, "E-mail cím megadása kötelező." , Toast.LENGTH_LONG).show();

            return;
        }
        String password = passwordEditText.getText().toString();
        if (("").equals(passwordEditText.getText().toString()) || passwordEditText.getText().toString().length() < 6) {
            Log.e(LOG_TAG, "Legalább 6 karakter hosszú jelszó megadása kötelező.");
            Toast.makeText(RegistrationActivity.this, "Legalább 6 karakter hosszú jelszó megadása kötelező." , Toast.LENGTH_LONG).show();

            return;
        }
        String passwordConfirm = passwordConfirmEditText.getText().toString();
        if (("").equals(passwordConfirmEditText.getText().toString())) {
            Log.e(LOG_TAG, "Megerősítő jelszó megadása kötelező.");
            Toast.makeText(RegistrationActivity.this, "Megerősítő jelszó megadása kötelező." , Toast.LENGTH_LONG).show();

            return;
        }
        if (!password.equals(passwordConfirm)) {
            Log.e(LOG_TAG, "A jelszó és a megerősítő jelszó nem egyezik.");
            Toast.makeText(RegistrationActivity.this, "A jelszó és a megerősítő jelszó nem egyezik." , Toast.LENGTH_LONG).show();

            return;
        }

        mAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Felhasználó sikeresen létrehozva.");
                            mNotificationHelper.send("Sikeres regisztráció!");
                            startWatchlist();
                        } else {
                            Log.d(LOG_TAG, "Felhasználó létrehozása sikertelen: " + task.getException().getMessage());
                            Toast.makeText(RegistrationActivity.this, "Felhasználó létrehozása sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void cancel(View view) {
        finish();
    }

    private void startWatchlist() {
        Intent intent = new Intent(this, CoinListActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

}