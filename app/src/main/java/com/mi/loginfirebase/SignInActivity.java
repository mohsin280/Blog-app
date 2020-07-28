package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignInActivity extends AppCompatActivity {
    EditText et_email;
    EditText et_pass;
    Button btn_signUp;
    ImageView iv_vis;
    TextView tv_login;
    private ProgressBar progressBar;
    FirebaseAuth mFirebaseAuth;
    Boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        mFirebaseAuth = FirebaseAuth.getInstance();
        et_email = findViewById(R.id.et_email);
        et_pass = findViewById(R.id.et_pass);
        btn_signUp = findViewById(R.id.btn_sign_up);
        iv_vis=findViewById(R.id.iv_vis_s);
        tv_login=findViewById(R.id.tv_login);
        progressBar=findViewById(R.id.pg);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email = et_email.getText().toString();
                String password = et_pass.getText().toString();


                if (email.isEmpty()) {
                    et_email.setError("please enter email id");
                    et_email.requestFocus();
                } else if (password.isEmpty()) {
                    et_pass.setError("please enter password");
                    et_pass.requestFocus();
                } else if (email.isEmpty() && password.isEmpty()) {
                    Toast.makeText(SignInActivity.this, "Fields are empty!", Toast.LENGTH_LONG).show();
                }else if(password.length()<6){
                    et_pass.setError("Password must have at least 6 characters");
                }
                else{
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.INVISIBLE);
                            if (!task.isSuccessful()) {
                                Toast.makeText(SignInActivity.this, "Sign up  unsuccessful, Wrong email id or password!!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SignInActivity.this, "Sign up successful", Toast.LENGTH_LONG).show();

                                startActivity(new Intent(SignInActivity.this, HomeActivity.class));
                                SignInActivity.this.finish();
                            }
                        }
                    });
                }

            }
        });

        iv_vis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flag){
                    et_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    iv_vis.setImageResource(R.drawable.ic_action_vis);
                    flag=false;

                }
                else{
                    et_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    iv_vis.setImageResource(R.drawable.ic_action_vis_off);
                    flag=true;
                }

            }
        });
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this,MainActivity.class));
                SignInActivity.this.finish();
            }
        });
    }
}
