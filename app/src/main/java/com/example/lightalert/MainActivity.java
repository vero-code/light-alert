package com.example.lightalert;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lightalert.adapters.ViewPagerAdapter;
import com.example.lightalert.data.ScheduleDataLoader;
import com.example.lightalert.data.WebScheduleLoader;
import com.example.lightalert.fragments.DayFragment;
import com.example.lightalert.data.Schedule;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView currentDateTextView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Schedule schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentDateTextView = findViewById(R.id.currentDate);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        currentDateTextView.setText(currentDate);

        ScheduleDataLoader dataLoader = new ScheduleDataLoader(this);
        JSONObject jsonSchedule = dataLoader.loadScheduleData();
        schedule = new Schedule(jsonSchedule);

        setupViewPager();

        WebScheduleLoader.loadSchedule(new WebScheduleLoader.ScheduleCallback() {
            @Override
            public void onLoaded(String todayData, String tomorrowData) {
                runOnUiThread(() -> {
                    if (todayData == null) return;
                    String[] daysArray = getResources().getStringArray(R.array.days_of_week);

                    Calendar calendar = Calendar.getInstance();

                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int todayIndex = (dayOfWeek + 5) % 7;
                    int tomorrowIndex = (todayIndex + 1) % 7;

                    String todayName = daysArray[todayIndex];
                    String tomorrowName = daysArray[tomorrowIndex];

                    schedule.updateDaySchedule(todayName, todayData);

                    if (tomorrowData != null) {
                        schedule.updateDaySchedule(tomorrowName, tomorrowData);
                    }

                    viewPager.setAdapter(null);
                    setupViewPager();

                    viewPager.setCurrentItem(todayIndex, false);

                    Toast.makeText(MainActivity.this, "Schedule updated!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("LightAlert", "Error: " + error);
                });
            }
        });
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        String[] days = getResources().getStringArray(R.array.days_of_week);

        for (String day : days) {
            DayFragment fragment = DayFragment.newInstance(day, schedule);
            adapter.addFragment(fragment, day);
        }

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int index = (dayOfWeek + 5) % 7;
        tabLayout.getTabAt(index).select();
    }
}
