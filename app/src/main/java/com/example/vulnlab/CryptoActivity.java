package com.example.vulnlab;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CryptoActivity extends AppCompatActivity {
    private static final String TAG = "CryptoActivity";
    private static final String PREFS = "lab_prefs";
    private static final String KEY_CIPHER_TEXT = "cipher_text"; // Stored as Base64

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        EditText etSecret = findViewById(R.id.etSecret);
        Button btnEncryptSave = findViewById(R.id.btnEncryptSave);
        Button btnLoadDecrypt = findViewById(R.id.btnLoadDecrypt);
        TextView tvCipher = findViewById(R.id.tvCipher);
        TextView tvPlain = findViewById(R.id.tvPlain);
        tvCipher.setMovementMethod(new ScrollingMovementMethod());

        // Default dataset
        etSecret.setText("TopSecret123");

        btnEncryptSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etSecret.getText().toString();
                try {
                    String b64 = CryptoUtils.encryptToBase64(input);
                    SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
                    sp.edit().putString(KEY_CIPHER_TEXT, b64).apply();
                    tvCipher.setText("Saved ciphertext (Base64):\n" + b64);
                    tvPlain.setText("");
                } catch (Exception e) {
                    Log.e(TAG, "Encrypt error", e);
                    tvCipher.setText("Error: " + e.getMessage());
                }
            }
        });

        btnLoadDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
                String b64 = sp.getString(KEY_CIPHER_TEXT, null);
                if (b64 == null) {
                    tvCipher.setText("No ciphertext stored yet.");
                    tvPlain.setText("");
                    return;
                }
                tvCipher.setText("Loaded ciphertext (Base64):\n" + b64);
                try {
                    String pt = CryptoUtils.decryptFromBase64(b64);
                    tvPlain.setText("Decrypted plaintext:\n" + pt);
                } catch (Exception e) {
                    Log.e(TAG, "Decrypt error", e);
                    tvPlain.setText("Error: " + e.getMessage());
                }
            }
        });
    }
}
