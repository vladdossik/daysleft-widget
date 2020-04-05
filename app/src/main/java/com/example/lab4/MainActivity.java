package com.example.lab4;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private final Calendar mCalendar = Calendar.getInstance();
    private EditText Date;
    private TextView error;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_CANCELED,resultValue);

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID){
            finish();
            return;
        }
        error = findViewById(R.id.errorOUT);
        Date = findViewById(R.id.dateIn);
        Date.setText(loadDatePref(MainActivity.this, mAppWidgetId));

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog(date);
            }
        });

        Button applyBtn = findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCalValue();

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                today.add(Calendar.DAY_OF_MONTH, 1);
                if (mCalendar.before(today)) {

                    error.setText("Событие должно быть в будущем!");
                    error.setVisibility(View.VISIBLE);
                }
                else {
                    final Context context = MainActivity.this;
                    saveDatePref(context, mAppWidgetId, Date.getText().toString());
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    CountDays.updateAppWidget(context, appWidgetManager, mAppWidgetId);
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                }
            }
        });
    }
    private void showDateDialog(DatePickerDialog.OnDateSetListener date) {
        new DatePickerDialog(MainActivity.this, date,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date.setText(sdf.format(mCalendar.getTime()));
        error.setVisibility(View.GONE);
    }
    private void setCalValue() {
        if (!Date.getText().toString().equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                mCalendar.setTime(sdf.parse(Date.getText().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
    static void saveDatePref(Context context, int appWidgetId, String text ) {
        SharedPreferences.Editor prefs = context.getSharedPreferences("com.example.lab4.CountdownW", 0).edit();
        prefs.putString("appwidget" + appWidgetId, text);
        prefs.apply();
    }

    static String loadDatePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("com.example.lab4.CountdownW", 0);
        String titleValue = prefs.getString("appwidget" + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return "";
        }
    }

}
