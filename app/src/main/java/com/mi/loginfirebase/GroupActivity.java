package com.mi.loginfirebase;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupActivity extends AppCompatActivity {
    private Toolbar mainToolbar;
    private EditText et_grp_code;
    private Button btn_create_grp;
    private Button btn_join_grp;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private String current_user_id;

    //for adapter
    private RecyclerView recycleView_grp_list;
    private ArrayList<String> data;
    private GrpRecyclerAdapter grpRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        et_grp_code = findViewById(R.id.et_grp_code);
        btn_create_grp = findViewById(R.id.btn_create_grp);
        btn_join_grp = findViewById(R.id.btn_join_grp);

        final GrpCode globalGrpCode = (GrpCode) getApplicationContext();

        //for toolbar
        mainToolbar = findViewById(R.id.toolbar_grp);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Group Panel");


        btn_create_grp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String RandomeCode = UUID.randomUUID().toString().substring(0, 16);

                globalGrpCode.setGrpCode(RandomeCode);

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("grp_code", RandomeCode);
                Toast.makeText(getApplicationContext(), "New group created!!", Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });

        btn_join_grp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String grpCode = et_grp_code.getText().toString();
                globalGrpCode.setGrpCode(grpCode);

                if (grpCode.isEmpty()) {
                    et_grp_code.setError("please enter group code");
                    et_grp_code.requestFocus();
                } else if (grpCode.length() < 16) {
                    et_grp_code.setError("Invalid code!!\ncode must have 16 characters");
                } else {
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.putExtra("grp_code", grpCode);
                    startActivity(intent);
                }
            }
        });

        //for RecyclerView Adapter
        recycleView_grp_list = findViewById(R.id.rv_grp_list);
        data = new ArrayList<>();

        //fetching my group data
        firebaseFirestore.collection("Posts")
                .whereEqualTo("user_id", current_user_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                data.add((String) document.getData().get("grp_code"));

                            }
                        } else {
                            Log.d("da", "Error getting documents: ", task.getException());
                        }

                        grpRecyclerAdapter = new GrpRecyclerAdapter(data);
                        recycleView_grp_list.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
                        recycleView_grp_list.setAdapter(grpRecyclerAdapter);


                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_btn_logout:
                logout();
                return true;
            case R.id.action_btn_account:
                accountSetting();
            default:
                return false;

        }
    }

    private void accountSetting() {
        startActivity(new Intent(getApplicationContext(), AccountSetting.class));
        finish();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        sentToLogin();
    }

    private void sentToLogin() {
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

}