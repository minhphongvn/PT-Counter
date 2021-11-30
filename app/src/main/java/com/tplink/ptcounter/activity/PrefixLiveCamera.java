package com.tplink.ptcounter.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tplink.ptcounter.BuildConfig;
import com.tplink.ptcounter.R;
import com.tplink.ptcounter.camera.java.LivePreviewActivity;

import java.util.ArrayList;
import java.util.List;

public class PrefixLiveCamera extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "ChooserActivity";
    private static final int PERMISSION_REQUESTS = 1;
    public static String MODE = "";
    public static int MAXREP = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
            StrictMode.setVmPolicy(
                    new StrictMode.VmPolicy.Builder()
                            .detectLeakedSqlLiteObjects()
                            .detectLeakedClosableObjects()
                            .penaltyLog()
                            .build());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefix_live_camera);

        Bundle bundle = getIntent().getExtras();
        MODE = bundle.getString("modeClass");
        Log.d("BUON", "onCreate: " + MODE);

        EditText editRep = findViewById(R.id.inputRep);
        Button btnStart = findViewById(R.id.startFromPrefix);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editRep.getText().toString().matches("")) {
                    MAXREP = 0;
                    startActivity(new Intent(PrefixLiveCamera.this, LivePreviewActivity.class));
                    finish();
                    return;
                }
                if (Integer.parseInt(editRep.getText().toString()) > 0)
                {
                    MAXREP = Integer.parseInt(editRep.getText().toString());
                    startActivity(new Intent(PrefixLiveCamera.this, LivePreviewActivity.class));
                    finish();
                    return;
                }
            }
        });

        Log.d(TAG, "onCreate");

//        startActivity(new Intent(this, LivePreviewActivity.class));

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void back(View view) {
        finish();
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
}
