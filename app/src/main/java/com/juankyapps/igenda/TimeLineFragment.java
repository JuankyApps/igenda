package com.juankyapps.igenda;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import xyz.sangcomz.stickytimelineview.RecyclerSectionItemDecoration;
import xyz.sangcomz.stickytimelineview.TimeLineRecyclerView;
import xyz.sangcomz.stickytimelineview.model.SectionInfo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.juankyapps.igenda.adapters.EventListAdapter;
import com.juankyapps.igenda.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TimeLineFragment extends Fragment {

    private ArrayList<Event> events = new ArrayList<>();
    private EventListAdapter list;
    private TimeLineRecyclerView recyclerView;
    private SwipeRefreshLayout swipe;

    private Date selectedDate = new Date();
    private boolean isGettingData = false;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.fragment_timeline_view, container, false);

        swipe = view.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getEvents();
            }
        });

        list = new EventListAdapter((Activity) container.getContext(), events);
        list.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                removeItem(position);
                return true;
            }
        });
        recyclerView = view.findViewById(R.id.timeline);
        recyclerView.addItemDecoration(getSectionCallback(events));
        recyclerView.setAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        getEvents();

        return view;
    }

    public void addItem(final Event event) {
        this.events.add(event);
        Collections.sort(events, new Comparator<Event>() {
            @Override
            public int compare(Event o1, Event o2) {
                return o1.getStart().compareTo(o2.getStart());
            }
        });
        this.list.notifyDataSetChanged();
        // Real add
        Map<String, Object> item = new HashMap<>();
        item.put("title", event.getTitle());
        item.put("location", event.getLocation());
        item.put("start", new Timestamp(event.getStart()));
        db.collection("users").document(mAuth.getUid())
                .collection("events").add(item)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(TimeLineFragment.this.getContext(), "No se pudo agregar el evento", Toast.LENGTH_SHORT).show();
                }
                getEvents();
            }
        });
    }

    public void removeItem(int index) {
        Event e = this.events.get(index);
        this.events.remove(index);
        this.list.notifyDataSetChanged();
        // Real remove
        db.collection("users").document(mAuth.getUid())
                .collection("events").document(e.getId()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(TimeLineFragment.this.getContext(), "No se pudo eliminar el evento", Toast.LENGTH_SHORT).show();
                }
                getEvents();
            }
        });
    }

    public void getEvents() {
        if (isGettingData) { return; }
        swipe.setRefreshing(true);
        isGettingData = true;
        db.collection("users").document(mAuth.getUid())
                .collection("events")
                .whereGreaterThan("start", getTodayStart(selectedDate))
                .whereLessThan("start", getTomorrowEnd(selectedDate))
                .orderBy("start", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    events.clear();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        String title = doc.getString("title");
                        String location = doc.getString("location");
                        Date start = doc.getDate("start");
                        events.add(new Event(doc.getId(), title, location, "", start, new Date()));
                    }
                    list.notifyDataSetChanged();
                    swipe.setRefreshing(false);
                    isGettingData = false;
                } else {
                    Toast.makeText(TimeLineFragment.this.getContext(), "No se pudieron obtener los eventos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
        events.clear();
        list.notifyDataSetChanged();
        getEvents();
    }

    public String getTitle() {
        Date d = this.selectedDate;
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("d");
        String dayNum = dayNumFormat.format(d);
        String month = capitalize(monthFormat.format(d));
        return dayNum + " de " + month;
    }

    private RecyclerSectionItemDecoration.SectionCallback getSectionCallback(final List<Event> singerList) {
        return new RecyclerSectionItemDecoration.SectionCallback() {

            @Nullable
            @Override
            public SectionInfo getSectionHeader(int position) {
                Event event = events.get(position);
                String date = formatDate(event.getStart());
                Date today = new Date();
                if (isOnTheSameDay(today, selectedDate)) {
                    if (isOnTheSameDay(selectedDate, event.getStart())) { date = "Hoy"; }
                    else if(isTomorrow(selectedDate, event.getStart())) { date = "Mañana"; }
                } else if(isTomorrow(selectedDate, event.getStart())) {
                    date += " (Día siguiente)";
                }
                return new SectionInfo(date, "", AppCompatResources.getDrawable(TimeLineFragment.this.getContext(), R.drawable.ic_ball_blue_24dp));
            }

            @Override
            public boolean isSection(int position) {
                if (position == 0) { return false; }
                Event event = events.get(position);
                return !isOnTheSameDay(event.getStart(), events.get(position-1).getStart());
            }
        };
    }

    private String formatDate(Date d) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
        SimpleDateFormat dayNumFormat = new SimpleDateFormat("d");
        String day = capitalize(dayFormat.format(d));
        String dayNum = dayNumFormat.format(d);
        String month = capitalize(monthFormat.format(d));
        return day + " " + dayNum + " de " + month;
    }

    private boolean isOnTheSameDay(Date a, Date b) {
        SimpleDateFormat f = new SimpleDateFormat("d EEEE YYYY");
        return f.format(a).equals(f.format(b));
    }

    private boolean isTomorrow(Date today, Date tomorrow) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(tomorrow);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH)+1 == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private String getDateOnly(Date d) {
        return new SimpleDateFormat("dd-MMM-yyyy").format(d);
    }

    private Date getTodayStart(Date d) {
        SimpleDateFormat dateTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        String date = getDateOnly(d) + " 00:00:00";
        try {
            return dateTime.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Date getTomorrowEnd(Date d) {
        SimpleDateFormat dateTime = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        int day = Integer.parseInt(getDateOnly(d).substring(0, 2))+1;
        String date = String.format("%02d", day)+getDateOnly(d).substring(2) + " 23:59:59";
        try {
            return dateTime.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String capitalize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private Date addDate(Date d, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DATE, amount);
        return calendar.getTime();
    }

}
