package com.tplink.ptcounter.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tplink.ptcounter.R;
import com.tplink.ptcounter.activity.PrefixLiveCamera;
import com.tplink.ptcounter.model.Exercise;

import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private Context mContext;
    private List<Exercise> mListExcercise;

    public ExerciseAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setData(List<Exercise> exerciseList) {
        this.mListExcercise = exerciseList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.excercise_item, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {

        Exercise exercise = mListExcercise.get(position);

        if (mListExcercise == null) {
            return;
        }

        holder.imgEx.setImageResource(exercise.getResId());
        holder.exName.setText(exercise.getName());
        holder.desEx.setText(exercise.getDescription());

        //Button to Pose
        holder.exStartBtn.setEnabled(exercise.isActive());
        holder.exStartBtn.setText(exercise.isActive()?"Bắt đầu":"Sắp có");
        holder.exStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.exStartBtn.getContext(), PrefixLiveCamera.class);
                Bundle bundle = new Bundle();
                bundle.putString("modeClass", exercise.getClassName());
                intent.putExtras(bundle);
                holder.exStartBtn.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mListExcercise != null) {
            return mListExcercise.size();
        }
        return 0;
    }

    public class ExerciseViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgEx;
        private TextView exName;
        private TextView desEx;
        private Button exStartBtn;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);

            this.imgEx = itemView.findViewById(R.id.img_exercise);
            this.exName = itemView.findViewById(R.id.name_exercise);;
            this.desEx = itemView.findViewById(R.id.des_exercise);
            this.exStartBtn = itemView.findViewById(R.id.startExercise);
        }
    }

}
