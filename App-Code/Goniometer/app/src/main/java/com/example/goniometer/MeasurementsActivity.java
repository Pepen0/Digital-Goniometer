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
            ViewHolder viewHolder;
            int viewType = getItemViewType(position);

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.list_item_measurement, parent, false);
                viewHolder.textViewMeasurementType = convertView.findViewById(R.id.textViewMeasurementType);
                viewHolder.tableHeaderRow = convertView.findViewById(R.id.tableHeaderRow);
                viewHolder.textViewLeftAngleHeader = convertView.findViewById(R.id.textViewLeftAngleHeader);
                viewHolder.textViewRightAngleHeader = convertView.findViewById(R.id.textViewRightAngleHeader);
                viewHolder.textViewTimestampHeader = convertView.findViewById(R.id.textViewTimestampHeader);
                viewHolder.tableRowMeasurement = convertView.findViewById(R.id.tableRowMeasurement);
                viewHolder.textViewLeftAngle = convertView.findViewById(R.id.textViewLeftAngle);
                viewHolder.textViewRightAngle = convertView.findViewById(R.id.textViewRightAngle);
                viewHolder.textViewTimestamp = convertView.findViewById(R.id.textViewTimestamp);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if (viewType == TYPE_HEADER) {
                // This item is a header
                String headerText = (String) getItem(position);
                viewHolder.textViewMeasurementType.setText(headerText);
                viewHolder.textViewMeasurementType.setVisibility(View.VISIBLE);
                viewHolder.tableHeaderRow.setVisibility(View.VISIBLE);
                viewHolder.tableRowMeasurement.setVisibility(View.GONE);

                // Update header text based on measurement type
                if (headerText.equals("Left Hip Abduction") || headerText.equals("Right Hip Abduction") ||
                        headerText.equals("Left Shoulder Abduction") || headerText.equals("Right Shoulder Abduction")) {
                    viewHolder.textViewLeftAngleHeader.setText("Abduction Angle");
                    viewHolder.textViewRightAngleHeader.setText("           ");
                } else {
                    viewHolder.textViewLeftAngleHeader.setText("Left Angle");
                    viewHolder.textViewRightAngleHeader.setVisibility(View.VISIBLE);
                }
            } else {
                // This item is a measurement
                Measurement measurement = (Measurement) getItem(position);
                viewHolder.textViewMeasurementType.setVisibility(View.GONE);
                viewHolder.tableHeaderRow.setVisibility(View.GONE);
                viewHolder.tableRowMeasurement.setVisibility(View.VISIBLE);

                // Populate data
                viewHolder.textViewLeftAngle.setText(measurement.getLeftAngle() + "°");
                viewHolder.textViewTimestamp.setText(measurement.getTimestamp());

                // Check if the measurement type should hide the right angle column
                String type = measurement.getMeasurementType();
                if (type.equals("Left Hip Abduction") || type.equals("Right Hip Abduction") ||
                        type.equals("Left Shoulder Abduction") || type.equals("Right Shoulder Abduction")) {
//
                    viewHolder.textViewRightAngle.setText("  ");
                    viewHolder.textViewTimestamp.setText(measurement.getTimestamp());
                } else {
                    viewHolder.textViewRightAngle.setVisibility(View.VISIBLE);
                    viewHolder.textViewRightAngle.setText(measurement.getRightAngle() + "°");
                }
            }
            return convertView;
        }

        private class ViewHolder {
            TextView textViewMeasurementType;
            TableRow tableHeaderRow;
            TextView textViewLeftAngleHeader;
            TextView textViewRightAngleHeader;
            TextView textViewTimestampHeader;
            TableRow tableRowMeasurement;
            TextView textViewLeftAngle;
            TextView textViewRightAngle;
            TextView textViewTimestamp;
        }
    }
}
