package com.example.intersect;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 9001;
    private String user_email = "";
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
    DatabaseReference teamsdatabase;
    ArrayList<String> current_users_teams = new ArrayList<String>();
    boolean user_team_exists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission(42, Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR);
        teamsdatabase = FirebaseDatabase.getInstance().getReference("Teams");
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
        findViewById(R.id.create_team).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_new_team();
            }
        });
        findViewById(R.id.join_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join_existing_team();
            }
        });
        if (getIntent().hasExtra("Email")) {
            user_email = getIntent().getStringExtra("Email");
        }
        find_user_teams();
    }

    private void checkPermission(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissions)
            ActivityCompat.requestPermissions(this, permissionsId, callbackId);
    }

    private void create_new_team() {
        if (user_email.length() > 1) {
            EditText team_getter = findViewById(R.id.team_name_enter);
            String team_name = team_getter.getText().toString();
            String team_id = teamsdatabase.push().getKey();
            TeamUnit te = new TeamUnit(team_id, team_name, user_email);
            teamsdatabase.child(team_id).setValue(te);
            show_selected_team(team_id);
        } else {
            Toast.makeText(getApplicationContext(), "Please login in first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void join_existing_team() {
        if (user_email.length() > 1) {
            EditText id_getter = findViewById(R.id.join_existing_team);
            String team_id = id_getter.getText().toString();
            team_exists(team_id);
            if (user_team_exists) {
                show_selected_team(team_id);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please login in first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void team_exists(final String team_id) {
        teamsdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(team_id)) {
                    user_team_exists = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Incorrect Team ID", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void show_selected_team(String team_id) {
        Intent move = new Intent(getApplicationContext(), teamdisplay.class);
        move.putExtra("Team_id", team_id);
        move.putExtra("Email", user_email);
        startActivity(move);
    }

    private void find_user_teams() {
        teamsdatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout ll = findViewById(R.id.users_teams);
                try {
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        TeamUnit te = messageSnapshot.getValue(TeamUnit.class);
                        if (te != null && te.user_activity.containsKey(user_email)
                                && !current_users_teams.contains(te.getId())) {
                            current_users_teams.add(te.getId());
                            TextView tv = new TextView(getApplicationContext());
                            tv.setLayoutParams(lp);
                            tv.setText(te.getName());
                            tv.setId(current_users_teams.size() - 1);
                            tv.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String team_id = current_users_teams.get(v.getId());
                                    show_selected_team(team_id);
                                }
                            });
                            ll.addView(tv);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
                Toast login_sucessful = Toast.makeText(getApplicationContext(), "Sucessful loged in as " + email, Toast.LENGTH_SHORT);
                email = email.replace(".", ",");
                user_email = email;
                login_sucessful.show();
                find_user_teams();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "There was a error loggin in: " + e.toString(), Toast.LENGTH_LONG).show();
                System.out.println(e.toString());
            }
        }
    }

}
