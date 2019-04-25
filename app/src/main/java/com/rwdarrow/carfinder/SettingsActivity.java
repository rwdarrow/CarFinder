package com.rwdarrow.carfinder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private Button mSettingsSaveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sp = this.getSharedPreferences("carName", MODE_PRIVATE);
        editor = sp.edit();

        final EditText mVehicleMakeInput = findViewById(R.id.vehicleMakeInput);
        final EditText mVehicleModelInput = findViewById(R.id.vehicleModelInput);

        if (sp.getBoolean("IS_SAVED", false)) {
            mVehicleMakeInput.setText(sp.getString("MAKE", "Make"));
            mVehicleModelInput.setText(sp.getString("MODEL", "Model"));
        }

        mSettingsSaveBtn = findViewById(R.id.settingsSaveBtn);
        mSettingsSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String make = mVehicleMakeInput.getText().toString();
                String model = mVehicleModelInput.getText().toString();

                Toast.makeText(SettingsActivity.this, "Car name changed to " +
                        make + " " + model, Toast.LENGTH_SHORT).show();

                saveCarName(make, model);
            }
        });
    }

    private void saveCarName(String make, String model) {
        editor.putString("MAKE", make);
        editor.putString("MODEL", model);
        editor.putBoolean("IS_SAVED", true);
        editor.apply();
    }

    public static Intent makeIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    public static String getCarName(Context context) {
        SharedPreferences sp = context.getSharedPreferences("carName", MODE_PRIVATE);
        return sp.getString("MAKE", "My") + " " + sp.getString("MODEL", "vehicle");
    }
}
