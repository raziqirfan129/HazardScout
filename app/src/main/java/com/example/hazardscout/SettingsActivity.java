package com.example.hazardscout;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.hazardscout.api.ApiConfig;
import com.example.hazardscout.data.AppPreferences;
import com.example.hazardscout.util.InsetUtils;

public class SettingsActivity extends AppCompatActivity {
    private EditText edtServerUrl;
    private RadioGroup radioTheme;
    private TextView txtConnectionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        InsetUtils.applyToolbarInset(toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        edtServerUrl = findViewById(R.id.edtServerUrl);
        radioTheme = findViewById(R.id.radioTheme);
        txtConnectionResult = findViewById(R.id.txtConnectionResult);
        Button btnSave = findViewById(R.id.btnSaveSettings);
        Button btnTest = findViewById(R.id.btnTestConnection);
        Button btnReset = findViewById(R.id.btnResetServer);

        edtServerUrl.setText(AppPreferences.getServerUrl(this));
        selectTheme(AppPreferences.getThemeMode(this));

        btnSave.setOnClickListener(view -> saveSettings());
        btnTest.setOnClickListener(view -> testConnection());
        btnReset.setOnClickListener(view -> {
            edtServerUrl.setText(AppPreferences.DEFAULT_SERVER_URL);
            txtConnectionResult.setText("");
        });
    }

    private void selectTheme(int mode) {
        int id = R.id.themeSystem;
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) id = R.id.themeLight;
        if (mode == AppCompatDelegate.MODE_NIGHT_YES) id = R.id.themeDark;
        ((RadioButton) findViewById(id)).setChecked(true);
    }

    private int selectedThemeMode() {
        int checked = radioTheme.getCheckedRadioButtonId();
        if (checked == R.id.themeLight) return AppCompatDelegate.MODE_NIGHT_NO;
        if (checked == R.id.themeDark) return AppCompatDelegate.MODE_NIGHT_YES;
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
    }

    private boolean validateUrl() {
        String url = edtServerUrl.getText().toString().trim();
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            edtServerUrl.setError(getString(R.string.error_server_url));
            edtServerUrl.requestFocus();
            return false;
        }
        return true;
    }

    private void saveSettings() {
        if (!validateUrl()) return;
        AppPreferences.setServerUrl(this, edtServerUrl.getText().toString());
        int mode = selectedThemeMode();
        AppPreferences.setThemeMode(this, mode);
        AppCompatDelegate.setDefaultNightMode(mode);
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
    }

    private void testConnection() {
        if (!validateUrl()) return;
        AppPreferences.setServerUrl(this, edtServerUrl.getText().toString());
        txtConnectionResult.setText(R.string.testing_connection);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.healthUrl(this),
                null,
                response -> {
                    boolean success = response.optBoolean("success", false);
                    txtConnectionResult.setText(success
                            ? getString(R.string.connection_success)
                            : response.optString("message", getString(R.string.connection_failed)));
                },
                error -> txtConnectionResult.setText(R.string.connection_failed)
        );
        request.setRetryPolicy(new DefaultRetryPolicy(8000, 0, 1f));
        Volley.newRequestQueue(this).add(request);
    }
}
