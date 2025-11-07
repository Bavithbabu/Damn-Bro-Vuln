package com.example.vulnlab;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NativeActivity extends AppCompatActivity {

    static {
        System.loadLibrary("vuln");
    }

    private native String processInput(String input);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native);

        EditText etInput = findViewById(R.id.etInput);
        TextView tvResult = findViewById(R.id.tvResult);
        Button btnCallNative = findViewById(R.id.btnCallNative);

        tvResult.setMovementMethod(new ScrollingMovementMethod());
        etInput.setText("HelloNative");

        btnCallNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String res = processInput(etInput.getText().toString());
                    tvResult.setText("Result: " + res);
                } catch (Throwable t) {
                    tvResult.setText("Error calling native: " + t.getMessage());
                }
            }
        });
    }
}
