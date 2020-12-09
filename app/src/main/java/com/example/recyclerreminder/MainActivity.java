package com.example.recyclerreminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String PREF_SCHEDULE = "sharedPrefChoices";
    private SharedPreferences mRecycleSchedule;

    private Map<String, Map<String, Boolean>> mScheduleMap;
    private String[] mWasteList;

    private Map<String, CardView> mCardMap;

    //  TODO: the app should notify the user to throw out the trash the day before, ie. if there is waste to dispose of on Monday, the notification should arrive Sunday evening

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
    }

    @Override
    public void onClick(View v) {
        mWasteList = getResources().getStringArray(R.array.waste);
        getWasteForCurrentDay(v.getTag().toString(), mWasteList);
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
}