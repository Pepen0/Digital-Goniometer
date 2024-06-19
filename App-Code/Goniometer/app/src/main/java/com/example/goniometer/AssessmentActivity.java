package com.example.goniometer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AssessmentActivity extends AppCompatActivity {

    protected EditText editTextAssessName;
    protected TextView textViewSelectDesired;
    protected Button buttonHeadRotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assesment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();

    }

    private void setupUI() {

        buttonHeadRotation = findViewById(R.id.buttonHeadRotation);
        buttonHeadRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHeadRotation();
            }

        });
    }
        private void goToHeadRotation() {
            Intent intent = new Intent(this, HeadRotation.class);
            startActivity(intent);
        }
}