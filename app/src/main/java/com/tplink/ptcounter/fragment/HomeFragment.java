package com.tplink.ptcounter.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tplink.ptcounter.R;
import com.tplink.ptcounter.adapter.ExerciseAdapter;
import com.tplink.ptcounter.model.Exercise;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView mRecycleView;
    private ExerciseAdapter mExerciseAdapter;

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mRecycleView = view.findViewById(R.id.recycle_exercise);
        mExerciseAdapter = new ExerciseAdapter(getActivity());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        mRecycleView.setLayoutManager(linearLayoutManager);

        mExerciseAdapter.setData(getListData());
        mRecycleView.setAdapter(mExerciseAdapter);

        return view;
    }

    private List<Exercise> getListData() {

        List<Exercise> list = new ArrayList<>();
        list.add(new Exercise(R.drawable.pushup, "Chống đẩy", "Tăng cường sự dẻo dai và chắc khỏe của xương khớp", "pushups_down", true));
        list.add(new Exercise(R.drawable.squat, "Squat","Giúp lớp mỡ tích tụ bị đốt cháy, làm những bộ phận này trở nên thon gọn và săn chắc.", "squats_down", true));
        list.add(new Exercise(R.drawable.plank, "Plank", "Tăng cường trao đổi chất. Giảm đau lưng và chấn thương.", "", false));
        list.add(new Exercise(R.drawable.lunge, "Lunge", "Giảm mỡ thừa tại vùng bắp đùi và giúp đùi trở nên thon gọn, săn chắc.", "", false));
        list.add(new Exercise(R.drawable.gapbung, "Gập bụng", "Tăng cường sự dẻo dai và sức chịu đựng cho nhóm cơ ở vùng bụng", "", false));

        return list;
    }

}