package com.juankyapps.igenda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.FirestoreGrpc;
import com.juankyapps.igenda.adapters.TabViewPagerAdapter;
import com.juankyapps.igenda.model.Event;
import com.juankyapps.igenda.model.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_EVENT = 1;
    private static final int ADD_TASK = 2;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fab;
    private Toolbar toolbar;
    private Menu menu;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int selectedView = 0;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot doc) {
                boolean isInitialized =  doc.contains("initialized") ? doc.getBoolean("initialized") : false;
                if (!isInitialized) {
                    Toast.makeText(MainActivity.this, "Por favor, completa la configuración inicial", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, WizardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateAppbar();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager = findViewById(R.id.viewPager);
        fab = findViewById(R.id.fab);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabTextColors(
                Color.argb(178,255,255,255),
                Color.argb(255,255,255,255));
        setupViewPager(viewPager);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ev = ADD_EVENT;
                Class c = EventFormActivity.class;
                switch (selectedView) {
                    case 0:
                        c = EventFormActivity.class;
                        ev = ADD_EVENT; break;
                    case 1:
                        c = TaskFormActivity.class;
                        ev = ADD_TASK;
                        break;
                }
                Intent intent = new Intent(MainActivity.this, c);
                startActivityForResult(intent, ev);
            }
        });

        // Set title
        updateTitle();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.getDecorView().setSystemUiVisibility(0);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (this.selectedView) {
            case 0: agendaOptions(item.getItemId()); break;
            case 1: tasksOptions(item.getItemId()); break;
        }
        return true;
    }

    public void agendaOptions(int option) {
        TimeLineFragment agenda = (TimeLineFragment) getCurrentFragment();
        switch (option) {
            case R.id.action_update:
                agenda.getEvents(); break;
            case R.id.action_prev:
                agenda.setSelectedDate(addDate(agenda.getSelectedDate(), -1));
                updateTitle();
                break;
            case R.id.action_next:
                agenda.setSelectedDate(addDate(agenda.getSelectedDate(), 1));
                updateTitle();
                break;
            case R.id.action_signout:
                signout();
                break;
        }
    }

    public void tasksOptions(int option) {
        TasksFragment tasks = (TasksFragment) getCurrentFragment();
        switch (option) {
            case R.id.action_update: break;
                //tasks.getTasks(); break;
            case R.id.action_signout:
                signout();
                break;
        }
    }

    private void signout() {
        mAuth.signOut();
        Intent intent = new Intent(this, InitialActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) { return; }
        switch(requestCode) {
            case ADD_EVENT:
                addEvent(data);
                break;
            case ADD_TASK:
                addTask(data);
                break;
        }
    }

    private void addEvent(Intent data) {
        if (this.selectedView == 1) { return; }
        String title = data.getExtras().getString("title");
        String location = data.getExtras().getString("location");
        String start = data.getExtras().getString("start");
        try {
            Date st = (new SimpleDateFormat("dd/MM/yyyy HH:mm")).parse(start);
            TimeLineFragment agenda = (TimeLineFragment) getCurrentFragment();
            agenda.addItem(new Event("", title, location, "", st, new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Ocurrió un error", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addTask(Intent data) {
        if (this.selectedView == 0) { return; }
        String title = data.getExtras().getString("title");
        String desc = data.getExtras().getString("desc");
        TasksFragment tasks = (TasksFragment) getCurrentFragment();
        tasks.addItem(new Task("", title, desc, false, false));
    }

    private void setupViewPager(ViewPager viewPager) {
        TabViewPagerAdapter tabViewPagerAdapter = new TabViewPagerAdapter(getSupportFragmentManager());
        tabViewPagerAdapter.addFragment(new TimeLineFragment(), "Agenda");
        tabViewPagerAdapter.addFragment(new TasksFragment(), "Tareas");
        viewPager.setAdapter(tabViewPagerAdapter);
    }

    private void updateTitle() {
        String title = "iGenda";
        Fragment fragment = getCurrentFragment();
        this.selectedView = viewPager.getCurrentItem();
        switch (selectedView) {
            case 0:
                title = ((TimeLineFragment) fragment).getTitle(); break;
            case 1:
                title = "Tareas"; break;
            default: break;
        }
        toolbar.setTitle(title);
    }

    private void updateMenu() {
        if (menu == null) { return; }
        menu.clear();
        int menuView = R.menu.timeline_view_menu;
        this.selectedView = viewPager.getCurrentItem();
        switch (selectedView) {
            case 0:
                menuView = R.menu.timeline_view_menu;
                break;
            case 1:
                menuView = R.menu.tasks_view_menu;
                break;
            default:
                break;
        }
        getMenuInflater().inflate(menuView, menu);
    }

    private void updateAppbar() {
        updateTitle();
        updateMenu();
    }

    private Date addDate(Date d, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(Calendar.DATE, amount);
        return calendar.getTime();
    }

    private Fragment getCurrentFragment() {
        return ((TabViewPagerAdapter) viewPager.getAdapter()).getItem(viewPager.getCurrentItem());
    }

}
