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


public class SignInActivity extends AppCompatActivity {
    EditText et_email;
    EditText et_pass;
    Button btn_signUp;
    ImageView iv_vis;
    TextView tv_login;
    private ProgressBar progressBar;
    FirebaseAuth mFirebaseAuth;
    Boolean flag = true;
    long id=0;

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
                final String email = et_email.getText().toString();
                final String password = et_pass.getText().toString();

                if (email.isEmpty()) {
                    et_email.setError("please enter email id");
                    progressBar.setVisibility(View.INVISIBLE);
                    et_email.requestFocus();
                } else if (password.isEmpty()) {
                    et_pass.setError("please enter password");
                    progressBar.setVisibility(View.INVISIBLE);
                    et_pass.requestFocus();
                }else if(password.length()<6){
                    et_pass.setError("Password must have at least 6 characters");
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else{
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.INVISIBLE);

                            if (task.isSuccessful()) {
                                Toast.makeText(SignInActivity.this, "Sign up successful", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(SignInActivity.this, AccountSetting.class));
                                finish();
                            } else {
                                String errorMsg = task.getException().getMessage();
                                Toast.makeText(SignInActivity.this, "Error :"+errorMsg, Toast.LENGTH_LONG).show();
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
