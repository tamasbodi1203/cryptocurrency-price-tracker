package com.example.cryptocurrencypricetracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cryptocurrencypricetracker.NotificationHelper;
import com.example.cryptocurrencypricetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class UserAccountActivity extends BaseActivity {

    private static final String LOG_TAG = UserAccountActivity.class.getName();

    private TextView usernameTextView;
    private TextView emailTextView;
    private EditText phoneNumberEditText;
    private NotificationHelper mNotificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isSignedInUser()) {
            Log.d(LOG_TAG, "Regisztrált felhasználó!");
        } else {
            Log.e(LOG_TAG, "Nem regisztrált felhasználó!");
            Toast.makeText(UserAccountActivity.this, "Ez a funkció csak regisztrált felhasználók számára érhető el!" , Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_user_account);
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailAddressTextView);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);

        usernameTextView.setText(mUserAccountData.getUsername());
        emailTextView.setText(mUserAccountData.getEmailAddress());
        phoneNumberEditText.setText(mUserAccountData.getPhoneNumber());

        mNotificationHelper = new NotificationHelper(this);
    }

    public void cancel(View view) {
        finish();
    }

    public void updateAccount(View view) {
        String phoneNumber = phoneNumberEditText.getText().toString();
        mUserAccountData.setPhoneNumber(phoneNumber);
        userAccountRepository.updateUserAccount(mUserAccountData);
        finish();
    }

    public void deleteAccount(View view) {
        userAccountRepository.deleteUserAccount(mUserAccountData);
        mNotificationHelper.send("Fiók sikeresen törölve!");
        finishAffinity();
        startActivity(new Intent(this, MainActivity.class));
    }

}