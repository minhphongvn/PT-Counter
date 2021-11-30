package com.tplink.ptcounter.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tplink.ptcounter.R;
import com.tplink.ptcounter.activity.PrefixLiveCamera;
import com.tplink.ptcounter.adapter.HistoryAdapter;
import com.tplink.ptcounter.camera.java.posedetector.classification.PoseClassifierProcessor;
import com.tplink.ptcounter.model.History;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static android.content.ContentValues.TAG;

public class HistoryFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    ListView listViewHistory;
    List<History> historyArrayList = new ArrayList<>();
    HistoryAdapter historyAdapter;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        listViewHistory = view.findViewById(R.id.listHistory);

        db.collection(mAuth.getUid())
        .get()
        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.d(TAG, document.getId() + " => **** " + document.getData().get("summary"));
                        History history = new History();
                        history.setDate(document.getData().get("date").toString());
                        List<Map<String, Object>> plans = (List<Map<String, Object>>) document.getData().get("summary");
                        if (plans != null) {
                            Integer squat = plans.stream().mapToInt(i ->  {
                                if (i.get("type").equals("squats_down")) {
                                    return Integer.parseInt(i.get("rep").toString());
                                }
                                return 0;
                            }).sum();
                            Integer pushup = plans.stream().mapToInt(i ->  {
                                if (i.get("type").equals("pushups_down")) {
                                    return Integer.parseInt(i.get("rep").toString());
                                }
                                return 0;
                            }).sum();

                            List<String> times = plans.stream().map(i -> i.get("total_time").toString()).collect(Collectors.toList());
                            Log.d(TAG, "onCompleteeeeeee: " + times);
                            String totalTime = totalTime(times);
                            Log.d(TAG, "channnnnnnnn: " + totalTime);
                            history.setContent("Tổng thời gian tập trong ngày: " + totalTime + " giây\n" + (squat!=0?"\nSquat: "  + squat + " lần\n":"") + (pushup!=0?"Chống đẩy: " + pushup + " lần":""));
                        }
                        historyArrayList.add(history);
                    }
                    historyAdapter = new HistoryAdapter(getActivity(), R.layout.history_item, historyArrayList);
                    listViewHistory.setAdapter(historyAdapter);

                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });


        return view;
    }

    public static String totalTime(List<String> times) {
        int total = 0;
        for (String time : times) {
            String[] splits = time.split(":");
            total+=(Integer.parseInt(splits[0])*60 + Integer.parseInt(splits[1]));
        }
        return ((total/60 < 10)?"0"+(total/60):total/60) + ":" + ((total%60 < 10)?"0"+(total%60):total%60);
    }
}