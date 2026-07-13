package com.example.hazardscout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hazardscout.api.ApiConfig;
import com.example.hazardscout.model.Hazard;
import com.example.hazardscout.util.MarkerStyle;
import com.example.hazardscout.util.NetworkUtils;
import com.example.hazardscout.util.InsetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final double DEFAULT_LAT = 3.1390;
    private static final double DEFAULT_LNG = 101.6869;
    private static final String REQUEST_TAG = "hazard-list";

    private MapView map;
    private TextView txtStatus;
    private ProgressBar progressBar;
    private Button btnRetry;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    private Marker currentLocationMarker;
    private final List<Marker> hazardMarkers = new ArrayList<>();

    private final LocationListener singleUpdateListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            showCurrentLocation(location, true);
        }

        @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override public void onProviderEnabled(@NonNull String provider) { }
        @Override public void onProviderDisabled(@NonNull String provider) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);

        InsetUtils.applyToolbarInset(toolbar);

        setSupportActionBar(toolbar);

        txtStatus = findViewById(R.id.txtStatus);
        progressBar = findViewById(R.id.progressBar);
        btnRetry = findViewById(R.id.btnRetry);
        Button btnReport = findViewById(R.id.btnReport);
        Button btnMyLocation = findViewById(R.id.btnMyLocation);
        map = findViewById(R.id.map);

        requestQueue = Volley.newRequestQueue(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setBuiltInZoomControls(false);
        map.getController().setZoom(15.5);
        map.getController().setCenter(new GeoPoint(DEFAULT_LAT, DEFAULT_LNG));

        btnReport.setOnClickListener(view -> startActivity(new Intent(this, ReportActivity.class)));
        btnMyLocation.setOnClickListener(view -> requestLocationPermissionThenLoad());
        btnRetry.setOnClickListener(view -> loadHazardsFromServer());

        requestLocationPermissionThenLoad();
        loadHazardsFromServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
        loadHazardsFromServer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) requestQueue.cancelAll(REQUEST_TAG);
        if (locationManager != null && hasLocationPermission()) {
            try { locationManager.removeUpdates(singleUpdateListener); } catch (SecurityException ignored) { }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            loadHazardsFromServer();
            return true;
        }
        if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (id == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestLocationPermissionThenLoad() {
        if (hasLocationPermission()) {
            loadCurrentLocation();
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.location_permission_title)
                    .setMessage(R.string.location_permission_message)
                    .setPositiveButton(R.string.continue_text, (dialog, which) -> requestLocationPermission())
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST
        );
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void loadCurrentLocation() {
        if (!hasLocationPermission()) return;

        Location lastKnown = getBestLastKnownLocation();
        if (lastKnown != null) {
            showCurrentLocation(lastKnown, true);
        } else {
            setStatus(getString(R.string.status_waiting_gps), false, false);
        }

        try {
            if (locationManager == null) return;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, singleUpdateListener, null);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, singleUpdateListener, null);
            } else {
                Toast.makeText(this, R.string.location_services_disabled, Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException ignored) {
            setStatus(getString(R.string.location_permission_message), false, false);
        }
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

    private void showCurrentLocation(Location location, boolean moveCamera) {
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (currentLocationMarker == null) {
            currentLocationMarker = new Marker(map);
            currentLocationMarker.setTitle(getString(R.string.you_are_here));
            currentLocationMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_current_location));
            currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            map.getOverlays().add(currentLocationMarker);
        }
        currentLocationMarker.setPosition(point);
        currentLocationMarker.setSnippet(String.format(Locale.US,
                "GPS: %.6f, %.6f\nAccuracy: %.0f m",
                location.getLatitude(), location.getLongitude(), location.getAccuracy()));
        if (moveCamera) map.getController().animateTo(point);
        map.invalidate();
    }

    private void loadHazardsFromServer() {
        if (!NetworkUtils.isOnline(this)) {
            setStatus(getString(R.string.error_no_internet), false, true);
            return;
        }

        setStatus(getString(R.string.status_loading_hazards), true, false);
        requestQueue.cancelAll(REQUEST_TAG);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.getHazardsUrl(this),
                null,
                response -> {
                    if (!response.optBoolean("success", false)) {
                        setStatus(response.optString("message", getString(R.string.error_server_data)), false, true);
                        return;
                    }
                    JSONArray array = response.optJSONArray("data");
                    if (array == null) {
                        setStatus(getString(R.string.error_invalid_server_data), false, true);
                        return;
                    }
                    try {
                        int rendered = renderHazards(array);
                        setStatus(getResources().getQuantityString(R.plurals.hazard_count, rendered, rendered), false, false);
                    } catch (JSONException e) {
                        setStatus(getString(R.string.error_invalid_server_data), false, true);
                    }
                },
                error -> setStatus(getString(R.string.error_server_connection), false, true)
        );
        request.setTag(REQUEST_TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.5f));
        requestQueue.add(request);
    }

    private int renderHazards(JSONArray array) throws JSONException {
        for (Marker marker : hazardMarkers) map.getOverlays().remove(marker);
        hazardMarkers.clear();
        int rendered = 0;

        for (int i = 0; i < array.length(); i++) {
            Hazard hazard = Hazard.fromJson(array.getJSONObject(i));
            if (!hazard.hasValidCoordinates()) continue;

            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(hazard.getLatitude(), hazard.getLongitude()));
            marker.setTitle(hazard.getCategory() + " - " + hazard.getLocationText());
            marker.setSnippet(
                    "Reported by: " + hazard.getUserName()
                            + "\nDate/Time: " + hazard.getReportedAt()
                            + "\nDescription: " + hazard.getDescription()
                            + String.format(Locale.US, "\nGPS: %.6f, %.6f", hazard.getLatitude(), hazard.getLongitude())
            );
            marker.setIcon(MarkerStyle.iconFor(this, hazard.getCategory()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            hazardMarkers.add(marker);
            map.getOverlays().add(marker);
            rendered++;
        }
        map.invalidate();
        return rendered;
    }

    private void setStatus(String message, boolean loading, boolean retryVisible) {
        txtStatus.setText(message);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRetry.setVisibility(retryVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && hasLocationPermission()) {
                loadCurrentLocation();
            } else {
                Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }
}
