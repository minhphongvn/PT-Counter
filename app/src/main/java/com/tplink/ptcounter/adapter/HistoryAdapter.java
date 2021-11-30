package com.tplink.ptcounter.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tplink.ptcounter.R;
import com.tplink.ptcounter.model.History;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HistoryAdapter extends ArrayAdapter {

    private Activity context;
    private int resource;
    private List<History> objects = new ArrayList<>();

    public HistoryAdapter(@NonNull Activity context, int resource, @NonNull List objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects.addAll(objects);
        Collections.reverse(this.objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = this.context.getLayoutInflater();
        View view = inflater.inflate(this.resource, null);
        TextView date = view.findViewById(R.id.ex_date);
        date.setText("Ngày: " + this.objects.get(position).getDate());
        date.setBackgroundColor(view.getResources().getColor(R.color.bgBottomNavigation));
        if (this.objects.get(position).getDate().equals(new SimpleDateFormat("dd-MM-yyyy").format(new Date()))){
            date.setText("Hôm nay: " + this.objects.get(position).getDate());
            date.setBackgroundColor(view.getResources().getColor(R.color.light_green_700));
        }
        TextView content = view.findViewById(R.id.ex_content);
        content.setText(this.objects.get(position).getContent());
        return view;
    }

}
