package com.example.goniometer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AssessmentActivity extends BaseActivity {

    protected Button buttonHeadRotation;
    protected Button RightElbow;
    protected Button LeftElbow;
    protected Button buttonLeftArmRotation;
    protected Button buttonRightArmRotation;
    protected Button buttonLeftHipAbduction;
    protected Button buttonRightHipAbduction;
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
        setupToolbar();
    }

    @Override
    public void onPatientDeleted(int position) {

    }

    private void setupUI() {
        // Initialize Buttons
        buttonHeadRotation = findViewById(R.id.buttonHeadRotation);
        buttonLeftArmRotation = findViewById(R.id.buttonLeftArm);
        buttonRightArmRotation = findViewById(R.id.buttonRightArm);
        buttonLeftHipAbduction = findViewById(R.id.buttonLeftHipAbduction);
        buttonRightHipAbduction = findViewById(R.id.buttonRightHipAbduction);
        RightElbow= findViewById(R.id.RightElbow);
        LeftElbow= findViewById(R.id.LeftElbow);

        // Set Click Listeners
        buttonHeadRotation.setOnClickListener(v -> goToHeadRotation());
        buttonLeftArmRotation.setOnClickListener(v -> goToLeftShoulderAbduction());
        buttonRightArmRotation.setOnClickListener(v -> goToRightShoulderAbduction());
        buttonLeftHipAbduction.setOnClickListener(v -> goToLeftHipAbduction());
        buttonRightHipAbduction.setOnClickListener(v -> goToRightHipAbduction());
        RightElbow.setOnClickListener(v -> goToRightElbow());
        LeftElbow.setOnClickListener(v -> goToLeftElbow());

    }
    private void goToRightElbow(){
        Intent intent = new Intent(this, RightElbow.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }
    private void goToLeftElbow(){
        Intent intent = new Intent(this, LeftElbow.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToHeadRotation() {
        Intent intent = new Intent(this, HeadRotation.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToLeftShoulderAbduction() {
        Intent intent = new Intent(this, LeftShoulderAbduction.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToRightShoulderAbduction() {
        Intent intent = new Intent(this, RightShoulderAbduction.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToLeftHipAbduction() {
        Intent intent = new Intent(this, LeftHipAbduction.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }

    private void goToRightHipAbduction() {
        Intent intent = new Intent(this, RightHipAbduction.class);
        intent.putExtra("PATIENT_ID", patientId); // Pass patient ID
        startActivity(intent);
    }
}
