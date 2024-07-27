package com.example.goniometer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AssessmentActivity extends AppCompatActivity {

    protected Button buttonHeadRotation;
    protected Button buttonWristRotation;
    protected Button buttonLeftArmRotation;
    protected Button buttonRightArmRotation;
    protected Button buttonLeftLegRotation;
    protected Button buttonRightLegRotation;
    private long patientId;

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

        // Retrieve patient ID
        patientId = getIntent().getLongExtra("PATIENT_ID", -1);

        setupUI();
    }

    private void setupUI() {
        // Initialize Buttons
        buttonHeadRotation = findViewById(R.id.buttonHeadRotation);
        buttonLeftArmRotation = findViewById(R.id.buttonLeftArm);
        buttonRightArmRotation = findViewById(R.id.buttonRightArm);
        buttonLeftLegRotation = findViewById(R.id.buttonLeftLeg);
        buttonRightLegRotation = findViewById(R.id.buttonRightLeg);
        buttonWristRotation = findViewById(R.id.buttonWristRotation);

        // Set Click Listeners
        buttonHeadRotation.setOnClickListener(v -> goToHeadRotation());
        buttonLeftArmRotation.setOnClickListener(v -> goToLeftArmRotation());
        buttonRightArmRotation.setOnClickListener(v -> goToRightArmRotation());
        buttonLeftLegRotation.setOnClickListener(v -> goToLeftLegRotation());
        buttonRightLegRotation.setOnClickListener(v -> goToRightLegRotation());
        buttonWristRotation.setOnClickListener(v ->goToWristRotation());

    }
    private void goToWristRotation(){
        Intent intent = new Intent(this, WristRotation.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToHeadRotation() {
        Intent intent = new Intent(this, HeadRotation.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToLeftArmRotation() {
        Intent intent = new Intent(this, LeftArmRotation.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToRightArmRotation() {
        Intent intent = new Intent(this, RightArmRotation.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToLeftLegRotation() {
        Intent intent = new Intent(this, LeftLegRotation.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToRightLegRotation() {
        Intent intent = new Intent(this, RightLegRotation.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }
}
