package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText et_email_login;
    private EditText et_pass_login;
    private Button btn_login;
    private TextView tv_sign_up;
    private TextView forgotPass;
    private TextView tv_help;
    private ImageView iv_vis;
    private Boolean flag = true;
    private ProgressBar progressBar;
    private FirebaseAuth mFirebaseAuth;
    private CheckBox check;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor prefsEditor;
    private Boolean saveLogin;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        et_email_login = findViewById(R.id.et_email_login);
        et_pass_login = findViewById(R.id.et_pass_login);
        btn_login = findViewById(R.id.btn_login);
        tv_sign_up = findViewById(R.id.tv_sign_up);
        check = findViewById(R.id.check);
        iv_vis = findViewById(R.id.iv_vis);
        progressBar = findViewById(R.id.pg_l);
        forgotPass = findViewById(R.id.forgetPass);
        tv_help=findViewById(R.id.tv_help);

        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        prefsEditor = sharedPreferences.edit();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    Toast.makeText(MainActivity.this, "You are logged in!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                } else {
                    Toast.makeText(MainActivity.this, "please login!", Toast.LENGTH_LONG).show();
                }
            }
        };

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String email = et_email_login.getText().toString();
                String password = et_pass_login.getText().toString();
                if (email.isEmpty()) {
                    et_email_login.setError("please enter email id");
                    et_email_login.requestFocus();
                } else if (password.isEmpty()) {
                    et_pass_login.setError("please enter password");
                    et_pass_login.requestFocus();
                } else {
                    //for shredpref
                    if (v == btn_login)
                        msharedpref(v, email, password);
                    mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "User logged in!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Wrong email or password!", Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
        tv_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                MainActivity.this.finish();
            }
        });

        iv_vis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    et_pass_login.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    iv_vis.setImageResource(R.drawable.ic_action_vis);
                    flag = false;
                } else {
                    et_pass_login.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    iv_vis.setImageResource(R.drawable.ic_action_vis_off);
                    flag = true;
                }

            }
        });
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(v.getContext());
                input.setHint("Enter Email To Received Reset Link.");
                AlertDialog.Builder box = new AlertDialog.Builder(v.getContext());
                box.setTitle("Reset Password ?");
                box.setView(input);

                box.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = input.getText().toString().trim();
                        mFirebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Reset link sent to your email.", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Error! Reset link is not send." + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                box.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Nothing
                    }
                });
                box.create().show();
            }
        });

        tv_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Sorry! Developers are sleeping.", Toast.LENGTH_LONG).show();
            }
        });


        saveLogin = sharedPreferences.getBoolean("saveLogin", false);
        if (saveLogin) {
            et_email_login.setText(sharedPreferences.getString("username", ""));
            et_pass_login.setText(sharedPreferences.getString("password", ""));
            check.setChecked(true);
        }

    }

    private void msharedpref(View v, String email, String password) {
        if (check.isChecked()) {
            prefsEditor.putBoolean("saveLogin", true);
            prefsEditor.putString("username", email);
            prefsEditor.putString("password", password);
            prefsEditor.commit();
        } else {
            prefsEditor.clear();
            prefsEditor.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }
}
