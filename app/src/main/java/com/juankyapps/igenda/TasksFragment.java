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
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.juankyapps.igenda.adapters.TaskListAdapter;
import com.juankyapps.igenda.model.Event;
import com.juankyapps.igenda.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TasksFragment extends Fragment {

    private ArrayList<Task> tasks = new ArrayList<>();
    private TaskListAdapter list;
    private TimeLineRecyclerView recyclerView;
    private SwipeRefreshLayout swipe;

    private boolean isGettingData = false;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        swipe = view.findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTasks();
            }
        });

        list = new TaskListAdapter((Activity) container.getContext(), tasks);
        list.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = recyclerView.getChildAdapterPosition(v);
                removeItem(position);
                return true;
            }
        });
        list.setOnCheckedClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getLayoutParams();
                int position = recyclerView.getChildAdapterPosition((View) v.getParent().getParent());
                setCompleted(position);
            }
        });
        recyclerView = view.findViewById(R.id.timeline);
        recyclerView.addItemDecoration(getSectionCallback(tasks));
        recyclerView.setAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        getTasks();

        return view;
    }

    public void addItem(final Task task) {
        this.tasks.add(task);
        Collections.sort(tasks, new TaskSorter());
        this.list.notifyDataSetChanged();
        // Real add
        db.collection("users").document(mAuth.getUid())
                .collection("tasks").add(getTaskMap(task))
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(TasksFragment.this.getContext(), "No se pudo agregar la tarea", Toast.LENGTH_SHORT).show();
                        }
                        getTasks();
                    }
                });
    }

    public void removeItem(int index) {
        Task t = this.tasks.get(index);
        this.tasks.remove(index);
        this.list.notifyDataSetChanged();
        // Real remove
        db.collection("users").document(mAuth.getUid())
                .collection("tasks").document(t.getId()).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(TasksFragment.this.getContext(), "No se pudo eliminar la tarea", Toast.LENGTH_SHORT).show();
                        }
                        getTasks();
                    }
                });
    }

    public void getTasks() {
        if (isGettingData) { return; }
        swipe.setRefreshing(true);
        isGettingData = true;
        db.collection("users").document(mAuth.getUid())
                .collection("tasks")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    tasks.clear();
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        String title = doc.getString("title");
                        String desc = doc.getString("desc");
                        boolean completed = doc.getBoolean("completed");
                        tasks.add(new Task(doc.getId(), title, desc, false, completed));
                    }
                    Collections.sort(tasks, new TaskSorter());
                    list.notifyDataSetChanged();
                    swipe.setRefreshing(false);
                    isGettingData = false;
                } else {
                    Toast.makeText(TasksFragment.this.getContext(), "No se pudieron obtener las tareas", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setCompleted(int position) {
        Task t = tasks.get(position);
        t.setCompleted(!t.isCompleted());
        Collections.sort(tasks, new TaskSorter());
        list.notifyDataSetChanged();
        db.collection("users").document(mAuth.getUid())
                .collection("tasks").document(t.getId())
                .update("completed", t.isCompleted()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(TasksFragment.this.getContext(), "Ocurrió un error", Toast.LENGTH_SHORT).show();
                }
                getTasks();
            }
        });
    }

    private Map<String, Object> getTaskMap(Task task) {
        Map<String, Object> item = new HashMap<>();
        item.put("title", task.getTitle());
        item.put("desc", task.getDesc());
        item.put("completed", task.isCompleted());
        item.put("favorite", task.isFavorite());
        return item;
    }

    public String getTitle() {
        return "Tareas";
    }

    private RecyclerSectionItemDecoration.SectionCallback getSectionCallback(final List<Task> singerList) {
        return new RecyclerSectionItemDecoration.SectionCallback() {

            @Nullable
            @Override
            public SectionInfo getSectionHeader(int position) {
                Task task = tasks.get(position);
                return new SectionInfo(task.isCompleted() ? "Completado" : "Pendiente", "", AppCompatResources.getDrawable(TasksFragment.this.getContext(), R.drawable.ic_ball_blue_24dp));
            }

            @Override
            public boolean isSection(int position) {
                if (position == 0) { return true; }
                Task task = tasks.get(position);
                return task.isCompleted() && !tasks.get(position-1).isCompleted();
            }
        };
    }

    private class TaskSorter implements Comparator<Task> {
        @Override
        public int compare(Task o1, Task o2) {
            if (o1.isCompleted() && !o2.isCompleted()) { return 1; }
            else if (!o1.isCompleted() && o2.isCompleted()) { return -1; }
            return 0;
        }
    }

}
