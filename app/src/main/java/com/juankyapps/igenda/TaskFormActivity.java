package com.juankyapps.igenda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import xyz.sangcomz.stickytimelineview.TimeLineRecyclerView;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aminography.primecalendar.PrimeCalendar;
import com.aminography.primecalendar.civil.CivilCalendar;
import com.aminography.primedatepicker.PickType;
import com.aminography.primedatepicker.fragment.PrimeDatePickerBottomSheet;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.juankyapps.igenda.adapters.EventListAdapter;
import com.juankyapps.igenda.adapters.TaskListAdapter;
import com.juankyapps.igenda.model.Event;
import com.juankyapps.igenda.model.Task;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TaskFormActivity extends AppCompatActivity {

    TextInputEditText titleInput;
    TextInputEditText descInput;
    Button addBtn;
    Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_form);

        titleInput = findViewById(R.id.titleInput);
        descInput = findViewById(R.id.descInput);

        addBtn = findViewById(R.id.addButton);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddButtonClick(v);
            }
        });
        cancelBtn = findViewById(R.id.cancelButton);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancelButtonClick(v);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.getDecorView().setSystemUiVisibility(0);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

    }

    private void onAddButtonClick(View v) {
        String title = titleInput.getText().toString();
        String desc = descInput.getText().toString();
        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(TaskFormActivity.this, "Debes rellenar todos los campos...", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("title", title);
        intent.putExtra("desc", desc);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void onCancelButtonClick(View v) {
        finish();
    }
}
