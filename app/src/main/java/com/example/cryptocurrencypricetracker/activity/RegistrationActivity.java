package com.example.cryptocurrencypricetracker.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
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
import com.example.cryptocurrencypricetracker.repository.CoinRepository;
import com.example.cryptocurrencypricetracker.repository.UserAccountRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
                            mNotificationHelper.send("Sikeres regisztráció!");
                            userAccountRepository.createUserAccount(new UserAccount(username, emailAddress, phoneNumberEditText.getText().toString(), new ArrayList<>()));
                            initSignedInData();
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

    private void initSignedInData() {
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

}