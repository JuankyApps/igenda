package com.juankyapps.igenda.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.juankyapps.igenda.R;
import com.juankyapps.igenda.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private final Activity context;
    private final ArrayList<Event> events;
    View.OnClickListener onClickListener;
    View.OnLongClickListener onLongClickListener;

    public EventListAdapter(Activity context, ArrayList<Event> items){
        this.context=context;
        this.events = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.timeline_row, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    @Override
    public int getItemCount() {
        return this.events.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event e = events.get(position);

        holder.timeTextView.setText(new SimpleDateFormat("HH:mm").format(e.getStart()));
        holder.titleDescTextView.setText(e.getTitle() + " - " + e.getLocation());
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
        private TextView timeTextView;
        private TextView titleDescTextView;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.timeTextView = view.findViewById(R.id.time);
            this.titleDescTextView = view.findViewById(R.id.titledesc);
            view.setOnClickListener(onClickListener);
            view.setOnLongClickListener(onLongClickListener);
        }
    }

}