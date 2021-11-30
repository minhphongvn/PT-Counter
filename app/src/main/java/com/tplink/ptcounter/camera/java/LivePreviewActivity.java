package com.tplink.ptcounter.camera.java;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.annotation.KeepName;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase;
import com.tplink.ptcounter.activity.PrefixLiveCamera;
import com.tplink.ptcounter.camera.CameraSource;
import com.tplink.ptcounter.camera.CameraSourcePreview;
import com.tplink.ptcounter.camera.GraphicOverlay;
import com.tplink.ptcounter.R;
import com.tplink.ptcounter.camera.java.posedetector.PoseDetectorProcessor;
import com.tplink.ptcounter.camera.java.posedetector.classification.PoseClassifierProcessor;
import com.tplink.ptcounter.camera.preference.PreferenceUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Live preview demo for ML Kit APIs. */
@KeepName
public final class LivePreviewActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback {

  private static final String POSE_DETECTION = "Pose Detection";

  private static final String TAG = "LivePreviewActivity";
  private static final int PERMISSION_REQUESTS = 1;

  private CameraSource cameraSource = null;
  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;
  private String selectedModel = POSE_DETECTION;
  Chronometer simpleChronometer;

  FirebaseFirestore db = FirebaseFirestore.getInstance();
  FirebaseAuth mAuth = FirebaseAuth.getInstance();

  Date date = new Date();
  SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
  SimpleDateFormat defaultformatter = new SimpleDateFormat("dd-MM-yyyy");

  //TTS
  private static TextToSpeech mTTS;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setContentView(R.layout.activity_live_preview);

    /**------------- its for the camera preview----------- */
    preview = findViewById(R.id.preview_view);
    if (preview == null) {
      Log.d(TAG, "Preview is null");
    }

    /**------------its for graphic that is inside the preview to show the imaging-------------- */
    graphicOverlay = findViewById(R.id.graphic_overlay);
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null");
    }

    /**------------for changing the camera back and front-------------*/
//    ToggleButton facingSwitch = findViewById(R.id.facing_switch);
//    facingSwitch.setOnCheckedChangeListener(this);

    /**------------Settings Button----------------------*/
//    ImageView settingsButton = findViewById(R.id.settings_button);
//    settingsButton.setOnClickListener(
//        v -> {
//          Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
//          intent.putExtra(
//              SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.LIVE_PREVIEW);
//          startActivity(intent);
//        });
    /**-----------TTS-------------**/
    mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
      @Override
      public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
          int result = mTTS.setLanguage(new Locale("vi"));

          if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
            Log.e("TTS", "Not Supported");
          }else{

          }
        }else{
          Log.e("TTS", "Init Failed");
        }
      }
    });

    addControls();

    /**-----------Get Permission-----------------**/

    if (allPermissionsGranted()) {
      createCameraSource(selectedModel);
    } else {
      getRuntimePermissions();
    }
  }

//  private static final String TIME = "00:05";

  private void addControls() {
    Button btnFinish = findViewById(R.id.finishExercise);

    simpleChronometer = (Chronometer) findViewById(R.id.counterUp); // initiate a chronometer
    simpleChronometer.start();

    simpleChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
      @Override
      public void onChronometerTick(Chronometer chronometer) {
        if (PoseClassifierProcessor.publicRep == PrefixLiveCamera.MAXREP && PrefixLiveCamera.MAXREP > 0) {
          simpleChronometer.stop();
          preview.stop();
          speak("Chúc mừng bạn đã hoàn thành bài tập. Số rep của bạn là " + PoseClassifierProcessor.publicRep);
          isTTSSpeaking();
        }
      }

    });

    btnFinish.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        simpleChronometer.stop();
        preview.stop();
        speak("Chúc mừng bạn đã hoàn thành bài tập. Số rep của bạn là " + PoseClassifierProcessor.publicRep);
        isTTSSpeaking();
      }
    });
  }

  public void isTTSSpeaking(){

    final Handler h =new Handler();

    Runnable r = new Runnable() {

      public void run() {

        if (!mTTS.isSpeaking()) {
          finish();
        }

        h.postDelayed(this, 1000);
      }
    };

    h.postDelayed(r, 1000);
  }

  public static void speak(String text) {
    mTTS.setPitch(1);
    mTTS.setSpeechRate(1.2f);
    mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
  }

  List<Map<String, Object>> plans = new ArrayList<>();
  private void saveData(){

    db.collection(mAuth.getUid()).document(formatter.format(date))
    .get()
    .addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        DocumentSnapshot document = task.getResult();
        if (document.exists()) {
          plans.addAll((List<Map<String, Object>>) document.get("summary"));

          // Create a new user with a first, middle, and last name
          Map<String, Object> plan = new HashMap<>();
          plan.put("total_time", simpleChronometer.getText());
          plan.put("type", PrefixLiveCamera.MODE);
          plan.put("rep", PoseClassifierProcessor.publicRep);

          plans.add(plan);
          Map<String, Object> summary = new HashMap<>();
          summary.put("summary", plans);
          summary.put("date", defaultformatter.format(date));

          db.collection(mAuth.getUid()).document(formatter.format(date))
            .set(summary)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(@NonNull Void aVoid) {
                Log.d(TAG, "DocumentSnapshot added with ID: ");
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
              }
            });
        } else {

          // Create a new user with a first, middle, and last name
          Map<String, Object> plan = new HashMap<>();
          plan.put("total_time", simpleChronometer.getText());
          plan.put("type", PrefixLiveCamera.MODE);
          plan.put("rep", PoseClassifierProcessor.publicRep);

          Map<String, Object> summary = new HashMap<>();
          summary.put("summary", plans);
          summary.put("date", defaultformatter.format(date));

          db.collection(mAuth.getUid()).document(formatter.format(date))
            .set(summary)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
              @Override
              public void onSuccess(@NonNull Void aVoid) {
                Log.d(TAG, "DocumentSnapshot added with ID: ");
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
              }
            });
        }
      }
    });



    // Add a new document with a generated ID
//    db.collection(mAuth.getUid())
//      .add(plan)
//      .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//        @Override
//        public void onSuccess(DocumentReference documentReference) {
//          Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//        }
//      })
//      .addOnFailureListener(new OnFailureListener() {
//        @Override
//        public void onFailure(@NonNull Exception e) {
//          Log.w(TAG, "Error adding document", e);
//        }
//      });

  }

  private void createCameraSource(String model) {
    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = new CameraSource(this, graphicOverlay);
    }

    try {
      switch (model) {
        case POSE_DETECTION:

          PoseDetectorOptionsBase poseDetectorOptions = PreferenceUtils.getPoseDetectorOptionsForLivePreview(this);
          Log.i(TAG, "Using Pose Detector with options " + poseDetectorOptions);

          boolean shouldShowInFrameLikelihood = PreferenceUtils.shouldShowPoseDetectionInFrameLikelihoodLivePreview(this);
          boolean visualizeZ = PreferenceUtils.shouldPoseDetectionVisualizeZ(this);
          boolean rescaleZ = PreferenceUtils.shouldPoseDetectionRescaleZForVisualization(this);
          boolean runClassification = PreferenceUtils.shouldPoseDetectionRunClassification(this);

          cameraSource.setMachineLearningFrameProcessor(
                  new PoseDetectorProcessor(
              this, poseDetectorOptions, shouldShowInFrameLikelihood, visualizeZ, rescaleZ,
              runClassification, /* isStreamMode = */true, cameraSource));
          break;

        default:
          Log.e(TAG, "Unknown model: " + model);
      }
    } catch (RuntimeException e) {
      Log.e(TAG, "Can not create image processor: " + model, e);
      Toast.makeText(
          getApplicationContext(),
          "Can not create image processor: " + e.getMessage(),
          Toast.LENGTH_LONG)
      .show();
    }
  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private void startCameraSource() {
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null");
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null");
        }
        preview.start(cameraSource, graphicOverlay);
      } catch (IOException e) {
        Log.e(TAG, "Unable to start camera source.", e);
        cameraSource.release();
        cameraSource = null;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    createCameraSource(selectedModel);
    startCameraSource();
  }

  /** Stops the camera. */
  @Override
  protected void onPause() {
    super.onPause();
    preview.stop();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    simpleChronometer.stop();
    preview.stop();
    saveData();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }

  private String[] getRequiredPermissions() {
    try {
      PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
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

  @Override
  public void onRequestPermissionsResult(
          int requestCode, String[] permissions, int[] grantResults) {
    Log.i(TAG, "Permission granted!");
    if (allPermissionsGranted()) {
      createCameraSource(selectedModel);
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
