package com.juankyapps.igenda.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.juankyapps.igenda.R;
import com.juankyapps.igenda.model.Task;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private final Activity context;
    private final ArrayList<Task> tasks;
    View.OnClickListener onClickListener;
    View.OnLongClickListener onLongClickListener;
    View.OnClickListener onCheckedClickListener;
    CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public TaskListAdapter(Activity context, ArrayList<Task> items){
        this.context = context;
        this.tasks = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_row, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener onChekedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public void setOnCheckedClickListener(View.OnClickListener onCheckedClickListener) {
        this.onCheckedClickListener = onCheckedClickListener;
    }

    @Override
    public int getItemCount() {
        return this.tasks.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task t = tasks.get(position);

        holder.titleTextView.setText(t.getTitle());
        holder.descTextView.setText(t.getDesc());
        holder.checkBoxView.setChecked(t.isCompleted());
    }

    @Override
    public void onClick(View v) {
        if (onClickListener != null) {
            onClickListener.onClick(v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (onLongClickListener != null) {
            return onLongClickListener.onLongClick(v);
        }
        return false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descTextView;
        private CheckBox checkBoxView;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.titleTextView = view.findViewById(R.id.title);
            this.descTextView = view.findViewById(R.id.desc);
            this.checkBoxView = view.findViewById(R.id.checkbox);
            view.setOnClickListener(onClickListener);
            view.setOnLongClickListener(onLongClickListener);
            checkBoxView.setOnClickListener(onCheckedClickListener);
        }

    }

}