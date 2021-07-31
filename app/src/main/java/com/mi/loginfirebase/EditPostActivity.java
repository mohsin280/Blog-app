package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditPostActivity extends AppCompatActivity {
    private Toolbar new_edit_post_toolbar;
    private EditText et_edit_post_desc;
    private Button btn_edit_new_post;
    private String blogPostId;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        new_edit_post_toolbar = findViewById(R.id.new_edit_post_toolbar);
        setSupportActionBar(new_edit_post_toolbar);
        getSupportActionBar().setTitle("Edit Post");
        //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et_edit_post_desc = findViewById(R.id.et_edit_post_description);
        btn_edit_new_post = findViewById(R.id.btn_edit_new_post);


        Intent intent = getIntent();
        blogPostId = intent.getStringExtra("postId");

        firebaseFirestore = FirebaseFirestore.getInstance();


        btn_edit_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc = et_edit_post_desc.getText().toString();
                if (TextUtils.isEmpty(desc)) {
                    et_edit_post_desc.setError("Description field is empty");
                } else {
                    firebaseFirestore.collection("Posts").document(blogPostId).update("desc", desc)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "post is updated!!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//                                    intent.putExtra("grp_code",);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });


    }
}