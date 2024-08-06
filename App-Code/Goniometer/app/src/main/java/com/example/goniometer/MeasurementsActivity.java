package com.example.goniometer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeasurementsActivity extends AppCompatActivity {

    private static final String TAG = "MeasurementsActivity";
    private ListView listViewMeasurements;
    private DatabaseHelper dbHelper;
    private MeasurementsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurements);

        listViewMeasurements = findViewById(R.id.listViewMeasurements);
        TextView textViewPatientId = findViewById(R.id.textViewPatientId);
        dbHelper = DatabaseHelper.getInstance(this);

        long patientId = getIntent().getLongExtra("PATIENT_ID", -1);
        Log.d(TAG, "Patient ID: " + patientId);

        if (patientId != -1) {
            String patientName = dbHelper.getPatientNameById(patientId);

            if (patientName != null) {
                String patientInfo = "Name: " + patientName + "\n" + "ID: " + patientId;
                textViewPatientId.setText(patientInfo);
            } else {
                textViewPatientId.setText("Patient not found");
                Log.e(TAG, "Patient not found for ID: " + patientId);
            }

            displayMeasurements(patientId);
        } else {
            Log.e(TAG, "Invalid patient ID: " + patientId);
            textViewPatientId.setText("Patient not found");
        }
    }

    private void displayMeasurements(long patientId) {
        try {
            Log.d(TAG, "Fetching measurements for patient ID: " + patientId);
            List<Measurement> measurements = dbHelper.getMeasurementsForPatient(patientId);

            if (measurements == null) {
                measurements = new ArrayList<>();
            }

            // Group measurements by type
            Map<String, List<Measurement>> groupedMeasurements = new HashMap<>();
            for (Measurement measurement : measurements) {
                String type = measurement.getMeasurementType();
                if (!groupedMeasurements.containsKey(type)) {
                    groupedMeasurements.put(type, new ArrayList<>());
                }
                groupedMeasurements.get(type).add(measurement);
            }

            // Flatten the grouped measurements into a single list with headers
            List<Object> displayList = new ArrayList<>();
            for (Map.Entry<String, List<Measurement>> entry : groupedMeasurements.entrySet()) {
                String type = entry.getKey();
                List<Measurement> typeMeasurements = entry.getValue();

                // Add header
                displayList.add(type);

                // Add measurements for this type
                displayList.addAll(typeMeasurements);
            }

            adapter = new MeasurementsAdapter(this, displayList);
            listViewMeasurements.setAdapter(adapter);

        } catch (Exception e) {
            Log.e(TAG, "Error displaying measurements", e);
        }
    }

    private class MeasurementsAdapter extends ArrayAdapter<Object> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        public MeasurementsAdapter(@NonNull MeasurementsActivity context, List<Object> items) {
            super(context, 0, items);
        }

        @Override
        public int getItemViewType(int position) {
            Object item = getItem(position);
            return item instanceof String ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            int viewType = getItemViewType(position);

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item_measurement, parent, false);
            }

            TextView textViewMeasurementType = convertView.findViewById(R.id.textViewMeasurementType);
            TableRow tableHeaderRow = convertView.findViewById(R.id.tableHeaderRow);
            TableRow tableRowMeasurement = convertView.findViewById(R.id.tableRowMeasurement);
            TextView textViewLeftAngle = convertView.findViewById(R.id.textViewLeftAngle);
            TextView textViewRightAngle = convertView.findViewById(R.id.textViewRightAngle);
            TextView textViewTimestamp = convertView.findViewById(R.id.textViewTimestamp);

            if (viewType == TYPE_HEADER) {
                // This item is a header
                String headerText = (String) getItem(position);
                textViewMeasurementType.setText(headerText);
                textViewMeasurementType.setVisibility(View.VISIBLE);
                tableHeaderRow.setVisibility(View.VISIBLE); // Show header row for type headers
                tableRowMeasurement.setVisibility(View.GONE); // Hide data row for headers
            } else {
                // This item is a measurement
                Measurement measurement = (Measurement) getItem(position);
                textViewMeasurementType.setVisibility(View.GONE);
                tableHeaderRow.setVisibility(View.GONE); // Hide header row for measurements
                tableRowMeasurement.setVisibility(View.VISIBLE);

                // Populate table row with measurement details
                textViewLeftAngle.setText(measurement.getLeftAngle() + "°");
                textViewRightAngle.setText(measurement.getRightAngle() + "°");
                textViewTimestamp.setText(measurement.getTimestamp());
            }

            return convertView;
        }
    }
}
