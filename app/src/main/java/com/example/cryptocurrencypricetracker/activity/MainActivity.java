package com.example.cryptocurrencypricetracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.entity.UserAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;
    private static final int RC_SIGN_IN = 123;

    private SharedPreferences preferences;
    private GoogleSignInClient mGoogleSignInClient;

    EditText emailEditText;
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUserAccounts = mFireStore.collection("UserAccounts");

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("920242195711-7ouf8dp3r2u4mkkje21e9p6oscl2mgiu.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(LOG_TAG, "Firebase Auth with Google: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e(LOG_TAG, "Google bejelentkezés sikertelen", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres Google bejelentkezés!");
                    startCoinList();
                } else {
                    Log.d(LOG_TAG, "Google bejelentkezés sikertelen: " + task.getException().getMessage());
                    Toast.makeText(MainActivity.this, "Bejelentkezés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
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

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres bejelentkezés!");
                    initUserAccount();
                } else {
                    Log.d(LOG_TAG, "Bejelentkezés sikertelen: " + task.getException().getMessage());
                    Toast.makeText(MainActivity.this, "Bejelentkezés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });
        showProgressDialog(this, "Firebase autentikáció folyamatban...");
    }

    public void loginWithGoogle(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    public void loginAsGuest(View view) {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres bejelentkezés vendégként!");
                    mWatchlistData = new ArrayList<>();
                    startCoinList();
                } else {
                    Log.d(LOG_TAG, "Sikertelen bejelentkezés vendégként: " + task.getException().getMessage());
                    Toast.makeText(MainActivity.this, "Bejelentkezés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });
        showProgressDialog(this, "Firebase autentikáció folyamatban...");
    }

    private void initUserAccount() {
        mProgressDialog.setMessage("Felhasználói fiók inicializálása...");
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
                                startCoinList();
                            }
                        } else {
                            Log.d(LOG_TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void startCoinList() {
        Intent intent = new Intent(this, CoinListActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
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