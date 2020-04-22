package com.example.intersect;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 9001;
    private String user_email = "";
    long start_time = -1;
    long end_time = -1;
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
    DatabaseReference teamsdatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        teamsdatabase = FirebaseDatabase.getInstance().getReference("Teams");

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        setContentView(R.layout.activity_main);
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                        startActivityForResult(signInIntent, RC_SIGN_IN);
                        break;
                    // ...
                }
            }
        });
        findViewById(R.id.start_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_time = -1;
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "Start day picker");
            }
        });
        findViewById(R.id.end_day).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                end_time = -1;
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "Start day picker");
            }
        });
        findViewById(R.id.function_executor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_events();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String email = account.getEmail();
                user_email = email;
                Toast login_sucessful =  Toast.makeText(getApplicationContext(), "Sucessful loged in as " + email, Toast.LENGTH_SHORT);
                login_sucessful.show();
            } catch (Exception e) {
                Toast login_error = Toast.makeText(getApplicationContext(), "There was a error loggin in! Please try again", Toast.LENGTH_SHORT);
                login_error.show();
            }
        }
    }

    private void get_events() {
        if(user_email.length() < 2) {
            Toast login_error = Toast.makeText(getApplicationContext(), "Please login!", Toast.LENGTH_SHORT);
            login_error.show();
            return;
        } if(start_time < 0) {
            Toast start_error = Toast.makeText(getApplicationContext(), "Choose start date", Toast.LENGTH_SHORT);
            start_error.show();
            return;
        } if(end_time < 0) {
            Toast end_error = Toast.makeText(getApplicationContext(), "Choose end date", Toast.LENGTH_SHORT);
            end_error.show();
        }
        get_slots();
    }

    private void get_slots(){
        List<CalendarEvent> ce  =  CalendarService.readCalendar(getApplicationContext(), start_time, end_time, user_email);
        if(ce.size() == 0) {
            return;
        }
        ScrollView sv = findViewById(R.id.ll);
        sv.removeAllViews();
        for(CalendarEvent current: ce) {
            TextView tv = new TextView(getApplicationContext());
            tv.setLayoutParams(lp);
            Date start = new Date(current.getBegin());
            Date end = new Date(current.getEnd());
            String message = current.getTitle() + ": " + start.toString() + " -> " + end.toString();
            String location = current.getLocation();
            if(location.length() > 1) {
                message += " @ " + location;
            }
            tv.setText(message);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.f);
            sv.addView(tv);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Date chosen_date = c.getTime();
        String message = DateFormat.getDateInstance().format(chosen_date);
        if(start_time == -1) {
            start_time = chosen_date.getTime();
            TextView tv = findViewById(R.id.start_text);
            tv.setText(message);
        } else if(end_time == -1) {
            end_time = chosen_date.getTime();
            TextView tv = findViewById(R.id.end_text);
            tv.setText(message);
        }
    }
}
