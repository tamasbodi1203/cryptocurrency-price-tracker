package com.example.cryptocurrencypricetracker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cryptocurrencypricetracker.NotificationHelper;
import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.entity.Coin;
import com.example.cryptocurrencypricetracker.entity.UserAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RegistrationActivity extends BaseActivity {

    private static final String LOG_TAG = RegistrationActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    private SharedPreferences preferences;
    private NotificationHelper mNotificationHelper;

    EditText usernameEditText;
    EditText emailEditText;
    EditText passwordEditText;
    EditText passwordConfirmEditText;
    EditText phoneNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        int secretKey = getIntent().getIntExtra("SECRET_KEY", 0);
        if (secretKey != 99) {
            finish();
        }

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailAddressEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordConfirmEditText = findViewById(R.id.passwordConfirmEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String username = preferences.getString("email", "");
        String password = preferences.getString("password", "");
        emailEditText.setText(username);
        passwordEditText.setText(password);

        mNotificationHelper = new NotificationHelper(this);
    }

    public void registration(View view) {
        String username = usernameEditText.getText().toString();
        if (("").equals(usernameEditText.getText().toString())) {
            Log.e(LOG_TAG, "Felhasználónév megadása kötelező!");
            Toast.makeText(RegistrationActivity.this, "Felhasználónév megadása kötelező!" , Toast.LENGTH_LONG).show();

            return;
        }
        String emailAddress = emailEditText.getText().toString();
        if (("").equals(emailEditText.getText().toString())) {
            Log.e(LOG_TAG, "E-mail cím megadása kötelező!");
            Toast.makeText(RegistrationActivity.this, "E-mail cím megadása kötelező!" , Toast.LENGTH_LONG).show();

            return;
        }
        String password = passwordEditText.getText().toString();
        if (("").equals(passwordEditText.getText().toString()) || passwordEditText.getText().toString().length() < 6) {
            Log.e(LOG_TAG, "Legalább 6 karakter hosszú jelszó megadása kötelező!");
            Toast.makeText(RegistrationActivity.this, "Legalább 6 karakter hosszú jelszó megadása kötelező!" , Toast.LENGTH_LONG).show();

            return;
        }
        String passwordConfirm = passwordConfirmEditText.getText().toString();
        if (("").equals(passwordConfirmEditText.getText().toString())) {
            Log.e(LOG_TAG, "Megerősítő jelszó megadása kötelező!");
            Toast.makeText(RegistrationActivity.this, "Megerősítő jelszó megadása kötelező!" , Toast.LENGTH_LONG).show();

            return;
        }
        if (!password.equals(passwordConfirm)) {
            Log.e(LOG_TAG, "A jelszó és a megerősítő jelszó nem egyezik!");
            Toast.makeText(RegistrationActivity.this, "A jelszó és a megerősítő jelszó nem egyezik!" , Toast.LENGTH_LONG).show();

            return;
        }

        mAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Felhasználó sikeresen létrehozva.");
                            mNotificationHelper.send("Sikeres regisztráció! Bejelentkezett felhasználóként a kedvencek listád megmarad kijelentkezés után is, valamint tudod módosítani a telefonszámod.");
                            mUserAccounts.add(new UserAccount(username, emailAddress, phoneNumberEditText.getText().toString(), new ArrayList<>()));
                            initUserAccount();
                        } else {
                            Log.d(LOG_TAG, "Felhasználó létrehozása sikertelen: " + task.getException().getMessage());
                            Toast.makeText(RegistrationActivity.this, "Felhasználó létrehozása sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
        showProgressDialog(this, "Firebase autentikáció folyamatban...");
    }

    public void cancel(View view) {
        finish();
    }

    private void startCoinList() {
        Intent intent = new Intent(this, CoinListActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    private void initUserAccount() {
        mUserAccounts.whereEqualTo("emailAddress", mAuth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(LOG_TAG, document.getId() + " => " + document.getData());
                                userAccount = document.toObject(UserAccount.class);
                                userAccount.setId(document.getId());
                                mWatchlistData = userAccount.getWatchlistItems();
                                System.out.println("valami");
                                startCoinList();
                            }
                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}