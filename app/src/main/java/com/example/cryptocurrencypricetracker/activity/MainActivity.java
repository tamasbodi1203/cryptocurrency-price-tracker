package com.example.cryptocurrencypricetracker.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.repository.CoinRepository;
import com.example.cryptocurrencypricetracker.repository.UserAccountRepository;
import com.example.cryptocurrencypricetracker.entity.Coin;
import com.example.cryptocurrencypricetracker.entity.UserAccount;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    private SharedPreferences preferences;

    EditText emailEditText;
    EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userAccountRepository = new UserAccountRepository();
        coinRepository = new CoinRepository();
        mCoinsData = new ArrayList<>();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

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
                    initSignedInData();
                } else {
                    Log.d(LOG_TAG, "Bejelentkezés sikertelen: " + task.getException().getMessage());
                    Toast.makeText(MainActivity.this, "Bejelentkezés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });
        showProgressDialog(this, "Firebase autentikáció folyamatban...");
    }

    public void loginAsGuest(View view) {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Sikeres bejelentkezés vendégként!");
                    initGuestData();
                } else {
                    Log.d(LOG_TAG, "Sikertelen bejelentkezés vendégként: " + task.getException().getMessage());
                    Toast.makeText(MainActivity.this, "Bejelentkezés sikertelen: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });
        showProgressDialog(this, "Firebase autentikáció folyamatban...");
    }

    private void initSignedInData() {
        mCoinsData.clear();
        Task getSignedInUserAccount = userAccountRepository.getSignedInUserAccount();
        Task getCoins = coinRepository.getCoins();
        Task<Void> allTasks = Tasks.whenAll(getSignedInUserAccount, getCoins);
        allTasks.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                QuerySnapshot querySnapshot = (QuerySnapshot) getSignedInUserAccount.getResult();
                for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                    mUserAccountData = documentSnapshot.toObject(UserAccount.class);
                    mUserAccountData.setId(documentSnapshot.getId());
                    mWatchlistData = mUserAccountData.getWatchlistItems();
                }

                QuerySnapshot coinQuerySnapShot = (QuerySnapshot) getCoins.getResult();
                for (DocumentSnapshot documentSnapshot : coinQuerySnapShot.getDocuments()) {
                    Coin coin = documentSnapshot.toObject(Coin.class);
                    coin.setId(documentSnapshot.getId());
                    mCoinsData.add(coin);
                }
                if (coinQuerySnapShot.getDocuments().isEmpty()) {
                    initDataFromFile();
                    Task getCoinsRecall = coinRepository.getCoins();
                    Task<Void> recall = Tasks.whenAll(getCoinsRecall);
                    recall.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            QuerySnapshot coinQuerySnapShot = (QuerySnapshot) getCoinsRecall.getResult();
                            for (DocumentSnapshot documentSnapshot : coinQuerySnapShot.getDocuments()) {
                                Coin coin = documentSnapshot.toObject(Coin.class);
                                coin.setId(documentSnapshot.getId());
                                mCoinsData.add(coin);
                            }
                            startCoinList();
                        }
                    });
                } else {
                    startCoinList();
                }
            }
        });
    }

    private void initGuestData() {
        Task getCoins = coinRepository.getCoins();
        Task<Void> allTasks = Tasks.whenAll(getCoins);
        allTasks.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mWatchlistData = new ArrayList<>();

                QuerySnapshot coinQuerySnapShot = (QuerySnapshot) getCoins.getResult();
                for (DocumentSnapshot documentSnapshot : coinQuerySnapShot.getDocuments()) {
                    Coin coin = documentSnapshot.toObject(Coin.class);
                    coin.setId(documentSnapshot.getId());
                    mCoinsData.add(coin);
                }
                if (coinQuerySnapShot.getDocuments().isEmpty()) {
                    initDataFromFile();
                    Task getCoinsRecall = coinRepository.getCoins();
                    Task<Void> recall = Tasks.whenAll(getCoinsRecall);
                    recall.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            QuerySnapshot coinQuerySnapShot = (QuerySnapshot) getCoinsRecall.getResult();
                            for (DocumentSnapshot documentSnapshot : coinQuerySnapShot.getDocuments()) {
                                Coin coin = documentSnapshot.toObject(Coin.class);
                                coin.setId(documentSnapshot.getId());
                                mCoinsData.add(coin);
                            }
                            startCoinList();
                        }
                    });
                } else {
                    startCoinList();
                }
            }
        });
    }

    private void initDataFromFile() {
        String[] itemCoinGeckoId = getResources().getStringArray(R.array.cryptocurrency_item_coin_gecko_ids);
        String[] itemSymbol = getResources().getStringArray(R.array.cryptocurrency_item_symbols);
        String[] itemsPrice = getResources().getStringArray(R.array.cryptocurrency_item_prices);
        String[] itemsPercentageChange = getResources().getStringArray(R.array.cryptocurrency_item_percentage_changes);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.cryptocurrency_item_images);


        for (int i = 0; i < itemSymbol.length; i++) {
            Coin coin = new Coin(itemCoinGeckoId[i], itemSymbol[i], Double.parseDouble(itemsPrice[i]), Double.parseDouble(itemsPercentageChange[i]), itemsImageResource.getResourceId(i, 0));
            coinRepository.addCoin(coin);
        }

        itemsImageResource.recycle();

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