package com.example.goniometer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    protected Button buttonGuestButton;
    protected Button buttonPatientButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    setupUI();

    }

    private void setupUI() {
        buttonGuestButton = findViewById(R.id.buttonGuestButton);
        buttonGuestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAssessmentActivity();
            }
        });


        buttonPatientButton = findViewById(R.id.buttonPatientButton);
        buttonPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToPatientActivity();
            }
        });

    };

    private void goToPatientActivity() {
        Intent intent = new Intent(this, PatientActivity.class);
        startActivity(intent);
    }


    private void goToAssessmentActivity() {
        Intent intent = new Intent(this, AssessmentActivity.class);
        startActivity(intent);
    }
}