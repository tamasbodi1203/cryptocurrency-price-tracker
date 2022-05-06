package com.example.pocketsentinel.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pocketsentinel.NotificationHelper;
import com.example.pocketsentinel.R;
import com.example.pocketsentinel.repository.UserAccountRepository;

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

        usernameTextView.setText(viewModel.getUserAccountData().getUsername());
        emailTextView.setText(viewModel.getUserAccountData().getEmailAddress());
        phoneNumberEditText.setText(viewModel.getUserAccountData().getPhoneNumber());

        mNotificationHelper = new NotificationHelper(this);
    }

    public void cancel(View view) {
        finish();
    }

    public void updateAccount(View view) {
        String phoneNumber = phoneNumberEditText.getText().toString();
        viewModel.getUserAccountData().setPhoneNumber(phoneNumber);
        viewModel.updateUserAccount().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Toast.makeText(UserAccountActivity.this, "Sikeres adatmódosítás!" , Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(UserAccountActivity.this, "Hiba történt az adatok módosítása során: " + task.getException(), Toast.LENGTH_LONG).show();
            }
        });
        finish();

    }

    public void deleteAccount(View view) {
        UserAccountRepository.getInstance().deleteUserAccount().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate(500);
                }
                mNotificationHelper.send("Fiók sikeresen törölve!");
            } else {
                Toast.makeText(UserAccountActivity.this, "Hiba történt a fiók törlése során: " + task.getException(), Toast.LENGTH_LONG).show();
            }
        });
        finishAffinity();
        startActivity(new Intent(this, MainActivity.class));
    }

}