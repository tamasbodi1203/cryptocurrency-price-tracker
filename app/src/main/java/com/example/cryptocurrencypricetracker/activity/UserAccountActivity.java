package com.example.cryptocurrencypricetracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.cryptocurrencypricetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class UserAccountActivity extends BaseActivity {

    private static final String LOG_TAG = UserAccountActivity.class.getName();

    private TextView usernameTextView;
    private TextView emailTextView;
    private EditText phoneNumberEditText;

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

        usernameTextView.setText(userAccount.getUsername());
        emailTextView.setText(userAccount.getEmailAddress());
        phoneNumberEditText.setText(userAccount.getPhoneNumber());
    }

    public void cancel(View view) {
        finish();
    }

    public void updateAccount(View view) {
        String phoneNumber = phoneNumberEditText.getText().toString();
        mUserAccounts.document(userAccount._getId()).update("phoneNumber", phoneNumber);
        userAccount.setPhoneNumber(phoneNumber);
        Log.d(LOG_TAG, "User account details updated.");
        finish();
    }

    public void deleteAccount(View view) {
        mUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mUserAccounts.document(userAccount._getId()).delete();
                            Log.d(LOG_TAG, "User account deleted.");
                            mNotificationHelper.send("Fiók sikeresen törölve!");
                        }
                    }
                });
        finishAffinity();
        startActivity(new Intent(this, MainActivity.class));
    }

}