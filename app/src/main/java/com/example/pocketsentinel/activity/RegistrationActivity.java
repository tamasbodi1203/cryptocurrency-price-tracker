package com.example.pocketsentinel.activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pocketsentinel.NotificationHelper;
import com.example.pocketsentinel.R;
import com.example.pocketsentinel.entity.UserAccount;
import com.example.pocketsentinel.repository.CoinRepository;
import com.example.pocketsentinel.repository.UserAccountRepository;
import com.example.pocketsentinel.view.ViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class RegistrationActivity extends BaseActivity {

    private static final String LOG_TAG = RegistrationActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    private SharedPreferences preferences;
    private NotificationHelper mNotificationHelper;
    private ViewModel viewModel;

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordConfirmEditText;
    private EditText phoneNumberEditText;

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
        viewModel = new ViewModelProvider(this).get(ViewModel.class);
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

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(LOG_TAG, "Felhasználó sikeresen létrehozva.");
                            mNotificationHelper.send("Sikeres regisztráció!");
                            viewModel.createUserAccount(new UserAccount(username, emailAddress, phoneNumberEditText.getText().toString(), new ArrayList<>()));
                            initSignedInUser();
                        } else {
                            Log.d(LOG_TAG, "Felhasználó létrehozása sikertelen: " + task.getException().getMessage());
                            Toast.makeText(RegistrationActivity.this, "Felhasználó létrehozása sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            hideProgressDialog();
                        }
                    }
                });
        showProgressDialog(this, "Firebase autentikáció folyamatban...");
    }

    public void cancel(View view) {
        finish();
    }

    private void initSignedInUser() {
        Task<Void> initTasks = Tasks.whenAll(UserAccountRepository.getInstance().initUserAccount(), CoinRepository.getInstance().initCoins());
        initTasks.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (initTasks.isSuccessful()) {
                    startCoinList();
                    Toast.makeText(RegistrationActivity.this, "Bejelentkezve.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(LOG_TAG, "Bejelentkezés sikertelen: " + initTasks.getException().getMessage());
                }
            }
        });
    }

    private void startCoinList() {
        Intent intent = new Intent(this, CoinListActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
        finish();
    }

}