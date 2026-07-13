package com.example.hazardscout;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.hazardscout.api.ApiConfig;
import com.example.hazardscout.data.AppPreferences;
import com.example.hazardscout.util.NetworkUtils;
import com.example.hazardscout.util.InsetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 2001;

    private EditText edtName;
    private EditText edtLocation;
    private EditText edtDescription;
    private Spinner spinnerCategory;
    private TextView txtCoordinates;
    private Button btnSubmit;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    private Double selectedLat;
    private Double selectedLng;

    private final LocationListener singleUpdateListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            setSelectedLocation(location);
        }
        @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override public void onProviderEnabled(@NonNull String provider) { }
        @Override public void onProviderDisabled(@NonNull String provider) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Toolbar toolbar = findViewById(R.id.toolbarReport);
        InsetUtils.applyToolbarInset(toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        edtName = findViewById(R.id.edtName);
        edtLocation = findViewById(R.id.edtLocation);
        edtDescription = findViewById(R.id.edtDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        txtCoordinates = findViewById(R.id.txtCoordinates);
        Button btnUseGps = findViewById(R.id.btnUseGps);
        btnSubmit = findViewById(R.id.btnSubmit);

        requestQueue = Volley.newRequestQueue(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.hazard_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        edtName.setText(AppPreferences.getReporterName(this));
        btnUseGps.setOnClickListener(view -> captureCurrentGps());
        btnSubmit.setOnClickListener(view -> submitHazard());
        captureCurrentGps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && hasLocationPermission()) {
            try { locationManager.removeUpdates(singleUpdateListener); } catch (SecurityException ignored) { }
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void captureCurrentGps() {
        if (!hasLocationPermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_permission_title)
                        .setMessage(R.string.location_report_permission_message)
                        .setPositiveButton(R.string.continue_text, (dialog, which) -> requestLocationPermission())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            } else {
                requestLocationPermission();
            }
            return;
        }

        Location lastKnown = getBestLastKnownLocation();
        if (lastKnown != null) {
            setSelectedLocation(lastKnown);
        } else {
            txtCoordinates.setText(R.string.gps_waiting);
        }

        try {
            if (locationManager == null) return;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, singleUpdateListener, null);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, singleUpdateListener, null);
            } else {
                txtCoordinates.setText(R.string.location_services_disabled);
            }
        } catch (SecurityException ignored) {
            txtCoordinates.setText(R.string.location_permission_denied);
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST
        );
    }

    private Location getBestLastKnownLocation() {
        if (!hasLocationPermission() || locationManager == null) return null;
        Location best = null;
        try {
            List<String> providers = locationManager.getProviders(true);
            for (String provider : providers) {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null && (best == null || location.getAccuracy() < best.getAccuracy())) {
                    best = location;
                }
            }
        } catch (SecurityException ignored) {
            return null;
        }
        return best;
    }

    private void setSelectedLocation(Location location) {
        selectedLat = location.getLatitude();
        selectedLng = location.getLongitude();
        txtCoordinates.setText(String.format(Locale.US,
                "GPS: %.6f, %.6f (accuracy %.0f m)", selectedLat, selectedLng, location.getAccuracy()));
    }

    private void submitHazard() {
        String name = edtName.getText().toString().trim();
        String locationText = edtLocation.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String category = String.valueOf(spinnerCategory.getSelectedItem());

        edtName.setError(null);
        edtLocation.setError(null);
        edtDescription.setError(null);

        if (name.length() < 2) {
            edtName.setError(getString(R.string.error_name));
            edtName.requestFocus();
            return;
        }
        if (locationText.length() < 3) {
            edtLocation.setError(getString(R.string.error_location));
            edtLocation.requestFocus();
            return;
        }
        if (description.length() < 8) {
            edtDescription.setError(getString(R.string.error_description));
            edtDescription.requestFocus();
            return;
        }
        if (selectedLat == null || selectedLng == null) {
            Toast.makeText(this, R.string.error_gps_required, Toast.LENGTH_LONG).show();
            return;
        }
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_LONG).show();
            return;
        }

        AppPreferences.setReporterName(this, name);
        setSubmitting(true);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                ApiConfig.addHazardUrl(this),
                response -> {
                    setSubmitting(false);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.optBoolean("success", false)) {
                            Toast.makeText(this, R.string.report_success, Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, json.optString("message", getString(R.string.report_failed)), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, R.string.error_invalid_server_data, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    setSubmitting(false);
                    Toast.makeText(this, R.string.error_server_connection, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_name", name);
                params.put("user_agent", WebSettings.getDefaultUserAgent(ReportActivity.this));
                params.put("device_info", getDeviceInfo());
                params.put("app_version", BuildConfig.VERSION_NAME);
                params.put("location_text", locationText);
                params.put("latitude", String.valueOf(selectedLat));
                params.put("longitude", String.valueOf(selectedLng));
                params.put("hazard_category", category);
                params.put("hazard_description", description);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(12000, 1, 1.5f));
        requestQueue.add(request);
    }

    private String getDeviceInfo() {
        return Build.MANUFACTURER + " " + Build.MODEL
                + " | Android " + Build.VERSION.RELEASE
                + " | SDK " + Build.VERSION.SDK_INT;
    }

    private void setSubmitting(boolean submitting) {
        btnSubmit.setEnabled(!submitting);
        btnSubmit.setText(submitting ? R.string.submitting : R.string.submit_report);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && hasLocationPermission()) {
                captureCurrentGps();
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }
}
