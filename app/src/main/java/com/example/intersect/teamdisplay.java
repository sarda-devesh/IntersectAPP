package com.example.intersect;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class teamdisplay extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    TeamUnit current = null;
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    DatabaseReference teamsdatabase;
    String user_email = "";
    long start_day = 0;
    long end_day = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teamdisplay);
        intialize_buttons();
        teamsdatabase = FirebaseDatabase.getInstance().getReference("Teams");
        if (getIntent().hasExtra("Email")) {
            user_email = getIntent().getStringExtra("Email");
        }
        if (getIntent().hasExtra("Team_id")) {
            String team_id = getIntent().getStringExtra("Team_id");
            read_user_team(team_id);
        }
    }

    private void intialize_buttons() {
        findViewById(R.id.start_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_day = -1;
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "Start day picker");
            }
        });
        findViewById(R.id.end_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end_day = -1;
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "Start day picker");
            }
        });
        findViewById(R.id.start_chooser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getApplicationContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        TextView tv = findViewById(R.id.start_time_choosen);
                        String message = selectedHour + ":" + selectedMinute;
                        tv.setText(message);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Start Time");
                mTimePicker.show();
            }
        });
        findViewById(R.id.end_chooser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getApplicationContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        TextView tv = findViewById(R.id.end_time_choosen);
                        String message = selectedHour + ":" + selectedMinute;
                        tv.setText(message);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select End Time");
                mTimePicker.show();
            }
        });
    }

    private void write_values(ArrayList<CalendarEvent> events) {
        if (events.size() > 0 && current.user_activity.containsKey(user_email)) {
            current.user_activity.get(user_email).clear();
            for (CalendarEvent temp_event : events) {
                current.user_activity.get(user_email).add(temp_event);
            }
            current.figure_user_times();
        } else {
            Toast.makeText(getApplicationContext(), "User is removed from hashmap: " + user_email + "," + events.size(), Toast.LENGTH_SHORT).show();
            System.out.println(current.user_activity.keySet().toString());
        }
    }

    private void read_user_team(final String team_id) {
        teamsdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    current = dataSnapshot.child(team_id).getValue(TeamUnit.class);
                    System.out.println("Read TeamUnit with id " + team_id + " " + current);
                    if (!current.user_activity.containsKey(user_email)) {
                        current.add_user(user_email);
                    }
                    ArrayList<CalendarEvent> car = current.user_activity.get(user_email);
                    boolean just_pl = car.size() == 1 && car.get(0).getBegin() == 0;
                    boolean need_update = current.getStart_time() > 10 && current.getEnd_time() > 10 && current.getMeeting_time() > 10 && just_pl;
                    if (need_update) {
                        ArrayList<CalendarEvent> events = CalendarService.readCalendar(getApplicationContext(), current.getStart_time(),
                                current.getEnd_time(), user_email);
                        write_values(events);
                    }
                    after_reading();
                } catch (Exception e) {
                    System.out.println("Error: " + e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void after_reading() {
        start_day = current.getStart_time();
        end_day = current.getEnd_time();
        update_values();
        Button b = findViewById(R.id.update);
        if (!current.getAdmin().equals(user_email)) {
            b.setVisibility(View.GONE);
        }
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update_team_settings();
            }
        });
        Button backer = findViewById(R.id.return_button);
        backer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return_to_main();
            }
        });
    }

    private void update_team_settings() {
        long team_start_day = start_day;
        long team_end_day = end_day;
        EditText et_hour = findViewById(R.id.number_hours);
        EditText et_minute = findViewById(R.id.number_minutes);
        TextView start_message = findViewById(R.id.start_time_choosen);
        String[] start_broken = start_message.getText().toString().split(":");
        TextView end_message = findViewById(R.id.end_time_choosen);
        String[] end_broken = start_message.getText().toString().split(":");
        if (start_broken.length != 2 || end_broken.length != 2) {
            Toast.makeText(getApplicationContext(), "Please make sure that all fields are filled in", Toast.LENGTH_SHORT).show();
            return;
        }
        team_start_day += TimeUnit.HOURS.toMillis(Integer.parseInt(start_broken[0])) + TimeUnit.MINUTES.toMillis(Integer.parseInt(start_broken[1]));
        team_end_day += TimeUnit.HOURS.toMillis(Integer.parseInt(end_broken[0])) + TimeUnit.MINUTES.toMillis(Integer.parseInt(end_broken[1]));
        int length_hour = Integer.parseInt(et_hour.getHint().toString());
        String et_hour_message = et_hour.getText().toString();
        if (et_hour_message.length() != 0) {
            length_hour = Integer.parseInt(et_hour_message);
        }
        int length_minutes = Integer.parseInt(et_minute.getHint().toString());
        String length_minute_message = et_minute.getText().toString();
        if (length_minute_message.length() != 0) {
            length_minutes = Integer.parseInt(length_minute_message);
        }

        long total_length = TimeUnit.HOURS.toMillis(length_hour) + TimeUnit.MINUTES.toMillis(length_minutes);
        boolean valid = team_start_day > 0 && team_end_day > 0 && total_length > 0;
        boolean updated = team_start_day != current.getStart_time() || team_end_day != current.getEnd_time() || total_length != current.getMeeting_time();
        if (valid && updated) {
            current.update_values(team_start_day, team_end_day, total_length);
        }
        ArrayList<CalendarEvent> arr = CalendarService.readCalendar(getApplicationContext(), team_start_day, team_end_day, user_email);
        write_values(arr);
        update_values();
    }

    private void update_values() {
        //Update Title
        TextView title = findViewById(R.id.Title);
        title.setText(current.getName());
        //Update ID
        TextView id_display = findViewById(R.id.join_code);
        String message = "Join Code is " + current.getId();
        id_display.setText(message);
        //Update start time
        long start_time = current.getStart_time();
        if (start_time > 10) {
            String start_date = DateFormat.getDateInstance().format(new Date(start_time));
            TextView start_display = findViewById(R.id.start_text);
            start_display.setText(start_date);
        }
        //Update end time
        long end_time = current.getEnd_time();
        if (end_time > 10) {
            String end_date = DateFormat.getDateInstance().format(new Date(end_time));
            TextView end_display = findViewById(R.id.end_text);
            end_display.setText(end_date);
        }
        //Update meeting time
        long meeting_time = current.getMeeting_time();
        EditText hours = findViewById(R.id.number_hours);
        EditText min = findViewById(R.id.number_minutes);
        if (meeting_time > 10) {
            long hrs = TimeUnit.MILLISECONDS.toHours(meeting_time);
            meeting_time = meeting_time % TimeUnit.HOURS.toMillis(1);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(meeting_time);
            hours.setHint(String.valueOf(hrs));
            min.setHint(String.valueOf(minutes));
        } else {
            hours.setHint("0");
            min.setHint("0");
        }
        //Add Meeting Suggestions
        long meeting_start = current.getMeeting_start();
        if (meeting_start > 10) {
            long meeting_end = meeting_start + current.getMeeting_time();
            SimpleDateFormat converter = new SimpleDateFormat("MMM dd,hh a");
            String display_message = converter.format(meeting_start) + " -> " +
                    converter.format(meeting_end);
            LinearLayout sv = findViewById(R.id.sugggestions);
            sv.removeAllViews();
            TextView display = new TextView(getApplicationContext());
            display.setLayoutParams(lp);
            display.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
            display.setText(display_message);
            sv.addView(display);
        } else if (meeting_start == -1590L && user_email.equals(current.getAdmin())) {
            Toast.makeText(getApplicationContext(), "Can't figure out suggestions in the range", Toast.LENGTH_SHORT).show();
        }
        //Add team members
        LinearLayout ll = findViewById(R.id.team_members);
        ll.removeAllViews();
        Set<String> members = current.user_activity.keySet();
        for (String current_user : members) {
            current_user = current_user.replace(",", ".");
            TextView tv = new TextView(getApplicationContext());
            tv.setLayoutParams(lp);
            tv.setText(current_user);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            ll.addView(tv);
        }
    }

    private void return_to_main() {
        teamsdatabase.child(current.getId()).setValue(current);
        Intent launcher = new Intent(getApplicationContext(), MainActivity.class);
        launcher.putExtra("Email", user_email);
        startActivity(launcher);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Date chosen_date = c.getTime();
        long time = chosen_date.getTime();
        String message = DateFormat.getDateInstance().format(chosen_date);
        if (start_day == -1) {
            start_day = time;
            TextView tv = findViewById(R.id.start_text);
            tv.setText(message);
        } else if (end_day == -1) {
            end_day = time;
            TextView tv = findViewById(R.id.end_text);
            tv.setText(message);
        }
    }
}
