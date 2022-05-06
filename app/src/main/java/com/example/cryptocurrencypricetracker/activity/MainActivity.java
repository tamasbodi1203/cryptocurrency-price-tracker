package com.example.cryptocurrencypricetracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.repository.CoinRepository;
import com.example.cryptocurrencypricetracker.repository.UserAccountRepository;
import com.example.cryptocurrencypricetracker.view.ViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    private SharedPreferences preferences;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        viewModel = new ViewModelProvider(this).get(ViewModel.class);

    }

    public void login(View view) {

        String email = emailEditText.getText().toString();
        if (("").equals(emailEditText.getText().toString())) {
            Log.e(LOG_TAG, "E-mail cím megadása kötelező!");
            Toast.makeText(MainActivity.this, "E-mail cím megadása kötelező!" , Toast.LENGTH_LONG).show();

            return;
        }
        String password = passwordEditText.getText().toString();
        if (("").equals(passwordEditText.getText().toString())) {
            Log.e(LOG_TAG, "Jelszó megadása kötelező!");
            Toast.makeText(MainActivity.this, "Jelszó megadása kötelező!" , Toast.LENGTH_LONG).show();

            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(LOG_TAG, "Sikeres bejelentkezés!");
                        initSignedInUser();
                    } else {
                        Log.d(LOG_TAG, "Bejelentkezés sikertelen: " + task.getException().getMessage());
                        Toast.makeText(MainActivity.this, "Bejelentkezés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        hideProgressDialog();
                    }
                });
        showProgressDialog(this, "Firebase autentikáció folyamatban...");
    }

    public void loginAsGuest(View view) {
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, "Sikeres bejelentkezés vendégként!");
                initGuestUser();
            } else {
                Log.d(LOG_TAG, "Sikertelen bejelentkezés vendégként: " + task.getException().getMessage());
                Toast.makeText(MainActivity.this, "Bejelentkezés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                hideProgressDialog();
            }
        });
        showProgressDialog(this, "Firebase autentikáció folyamatban...");
    }

    private void initSignedInUser() {
        Task<Void> initTasks = Tasks.whenAll(CoinRepository.getInstance().initCoins(), UserAccountRepository.getInstance().initUserAccount());
        initTasks.addOnCompleteListener(task -> {
            if (initTasks.isSuccessful()) {
                startCoinList();
            } else {
                Log.d(LOG_TAG, "Bejelentkezés sikertelen: " + initTasks.getException().getMessage());
            }
        });
    }

    private void initGuestUser() {
        CoinRepository.getInstance().initCoins().addOnSuccessListener(queryDocumentSnapshots -> {
            startCoinList();
        });
    }

    private void startCoinList() {
        Intent intent = new Intent(this, CoinListActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
        finish();
    }

    public void registration(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);

        startActivity(intent);

    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", emailEditText.getText().toString());
        editor.putString("password", passwordEditText.getText().toString());
        editor.apply();
    }

}