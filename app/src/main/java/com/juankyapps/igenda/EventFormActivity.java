package com.juankyapps.igenda;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EventFormActivity extends AppCompatActivity {

    TextInputEditText titleInput;
    TextInputEditText locationInput;
    TextInputEditText dateInput;
    TextInputEditText timeInput;
    Button addBtn;
    Button cancelBtn;
    PrimeDatePickerBottomSheet startDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_form);

        titleInput = findViewById(R.id.titleInput);
        locationInput = findViewById(R.id.locationInput);
        dateInput = findViewById(R.id.dateInput);
        timeInput = findViewById(R.id.timeInput);

        startDatePicker = PrimeDatePickerBottomSheet.newInstance(
                new CivilCalendar(), // for example: new PersianCalendar()
                PickType.SINGLE // for example: PickType.SINGLE
        );
        startDatePicker.setOnDateSetListener(new PrimeDatePickerBottomSheet.OnDayPickedListener() {
            @Override
            public void onSingleDayPicked(@NotNull PrimeCalendar singleDay) {
                dateInput.setText((new SimpleDateFormat("dd/MM/yyyy").format(singleDay.getTime())));
            }
            @Override
            public void onRangeDaysPicked(@NotNull PrimeCalendar startDay, @NotNull PrimeCalendar endDay) {
                // TODO
            }
        });

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

        dateInput.setKeyListener(null);
        dateInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    startDatePicker.show(getSupportFragmentManager(), "START_DATE");
                } else {
                    startDatePicker.dismiss();
                }
            }
        });

        timeInput.setKeyListener(null);
        timeInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Calendar c = Calendar.getInstance();
                    int hora = c.get(Calendar.HOUR_OF_DAY);
                    int minuto = c.get(Calendar.MINUTE);
                    TimePickerDialog timePicker = new TimePickerDialog(EventFormActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String hourF =  (hourOfDay < 10)? String.valueOf("0" + hourOfDay) : String.valueOf(hourOfDay);
                            String minuteF = (minute < 10)? String.valueOf("0" + minute):String.valueOf(minute);
                            timeInput.setText(hourF + ":" + minuteF);
                        }
                    }, hora, minuto, true);
                    timePicker.show();
                }
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
        String location = locationInput.getText().toString();
        String date = dateInput.getText().toString();
        String time = timeInput.getText().toString();
        String start = date+" "+time;
        if (title.isEmpty() || location.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(EventFormActivity.this, "Debes rellenar todos los campos...", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("title", title);
        intent.putExtra("location", location);
        intent.putExtra("start", start);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void onCancelButtonClick(View v) {
        finish();
    }

}
