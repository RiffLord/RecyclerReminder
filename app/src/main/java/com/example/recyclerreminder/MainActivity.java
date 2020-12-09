package com.example.recyclerreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "MainActivity";
    public static final String PREF_SCHEDULE = "sharedPrefChoices";
    public static final String PREF_ALARMHRS = "sharedPrefAlarmHr";
    public static final String PREF_ALARMMINS = "sharedPrefAlarmMin";

    private SharedPreferences mRecycleSchedule;

    private Map<String, Map<String, Boolean>> mScheduleMap;
    private String[] mWasteList;

    private Map<String, CardView> mCardMap;

    private int mAlarmHrs;
    private int mAlarmMins;

    //  TODO: the app should notify the user to throw out the trash the day before, ie. if there is waste to dispose of on Monday, the notification should arrive Sunday evening
    //  TODO: The alarm should open a NotificationActivity informing the user what waste is scheduled for the following morning (the alarm should fire at 21:30 by default)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWasteList = getResources().getStringArray(R.array.waste);
        mScheduleMap = new HashMap<>();

        mCardMap = new HashMap<>();
        mCardMap.put(getString(R.string.monday), (CardView) findViewById(R.id.mondayCard));
        mCardMap.put(getString(R.string.tuesday), (CardView) findViewById(R.id.tuesdayCard));
        mCardMap.put(getString(R.string.wednesday), (CardView) findViewById(R.id.wednesdayCard));
        mCardMap.put(getString(R.string.thursday), (CardView) findViewById(R.id.thursdayCard));
        mCardMap.put(getString(R.string.friday), (CardView) findViewById(R.id.fridayCard));
        mCardMap.put(getString(R.string.saturday), (CardView) findViewById(R.id.saturdayCard));

        for (CardView card : mCardMap.values()) card.setOnClickListener(this);

        loadRecycleSchedule();
        displayWasteSchedule();

        Log.d(TAG, "HOURS: " + mAlarmHrs);
        Log.d(TAG, "MINUTES: " + mAlarmMins);
    }

    @Override
    public void onClick(View v) {
        mWasteList = getResources().getStringArray(R.array.waste);
        getWasteForCurrentDay(v.getTag().toString(), mWasteList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_alarm) {
            loadAlarmSettings();
            changeReminderTime(true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayRecycleDialog(String day, String[] items, boolean[] status) {
        Map<String, Boolean> choices = mScheduleMap.get(day);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(day + " Recycling");
        builder.setMultiChoiceItems(items, status,
                (dialog, which, isChecked) -> choices.put(items[which], isChecked));

        builder.setPositiveButton("OK", ((dialog, which) -> {
            saveWasteScheduleForCurrentDay(day, choices);
            displayWasteSchedule();
            dialog.dismiss();
        }));
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void getWasteForCurrentDay(String dayTag, String[] items) {
        for (String waste : items)
            mScheduleMap.get(dayTag).putIfAbsent(waste, false);

        boolean[] wasteEnabled = new boolean[mScheduleMap.get(dayTag).size()];

        for (int i = 0; i < wasteEnabled.length; ++i)
            wasteEnabled[i] = mScheduleMap.get(dayTag).get(items[i]);

        displayRecycleDialog(dayTag, items, wasteEnabled);
    }

    private void saveWasteScheduleForCurrentDay(String day, Map<String, Boolean> schedule) {
        mRecycleSchedule = MainActivity.this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = mRecycleSchedule.edit();

        mScheduleMap.put(day, schedule);

        Gson gson = new Gson();
        String weeklySchedule = gson.toJson(mScheduleMap);
        prefEditor.putString(PREF_SCHEDULE, weeklySchedule).apply();
    }

    private void displayWasteSchedule() {
        for (CardView card : mCardMap.values()) {
            TextView wasteTextView = findViewById(card.getChildAt(1).getId());
            wasteTextView.setText("");

            for (String item : mWasteList) {
                if (mScheduleMap.containsKey(card.getTag().toString()) && mScheduleMap.get(card.getTag().toString()).containsKey(item))
                    if (mScheduleMap.get(card.getTag().toString()).get(item)) {
                        wasteTextView.append(item + '\n');
                    }
            }
        }
    }

    //  Serves to visualize correct values when opening the app.
    private void loadRecycleSchedule() {
        mRecycleSchedule = MainActivity.this.getPreferences(MODE_PRIVATE);

        if (mRecycleSchedule.getString(PREF_SCHEDULE, null) != null) {
            Gson gson = new Gson();
            String scheduleString = mRecycleSchedule.getString(PREF_SCHEDULE, null);
            Type destinationType = new TypeToken<HashMap<String, Map<String, Boolean>>>() {}.getType();
            mScheduleMap = gson.fromJson(scheduleString, destinationType);
        } else
            for (CardView card : mCardMap.values()) mScheduleMap.put(card.getTag().toString(), new HashMap<>());
    }

    private void loadAlarmSettings() {
        mRecycleSchedule = MainActivity.this.getPreferences(MODE_PRIVATE);
        mAlarmHrs = mRecycleSchedule.getInt(PREF_ALARMHRS, 21);
        mAlarmMins = mRecycleSchedule.getInt(PREF_ALARMMINS, 30);
    }

    private void saveAlarmSettings(int hrs, int mins) {
        mRecycleSchedule = MainActivity.this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = mRecycleSchedule.edit();
        prefEditor.putInt(PREF_ALARMHRS, hrs).apply();
        prefEditor.putInt(PREF_ALARMMINS, mins).apply();
    }

    private void changeReminderTime(boolean is24Hr) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                onTimeSetListener, mAlarmHrs,
                mAlarmMins, is24Hr);
        timePickerDialog.setTitle(getString(R.string.time_picker_dialog));
        timePickerDialog.show();
    }

    private void setAlarm(Calendar calendar) {
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(), 1, intent, 0
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, hourOfDay, minute) -> {
        Calendar calendar = Calendar.getInstance();
        Calendar alarmCalendar = (Calendar) calendar.clone();
        alarmCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        alarmCalendar.set(Calendar.MINUTE, minute);
        alarmCalendar.set(Calendar.SECOND, 0);
        alarmCalendar.set(Calendar.MILLISECOND, 0);

        saveAlarmSettings(hourOfDay, minute);

        if (alarmCalendar.compareTo(calendar) <= 0) {
            // Today Set time passed, count to tomorrow
            alarmCalendar.add(Calendar.DATE, 1);
        }

        setAlarm(alarmCalendar);
    };
}