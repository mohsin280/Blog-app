package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CommentEditActivity extends AppCompatActivity {

    private Toolbar cmt_edit_toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private EditText et_edit_cmt;
    private ImageButton btn_edit_send_cmt;
    private ImageButton btn_edit_cancel_cmt;

    private String cmtId;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_edit);

        cmt_edit_toolbar = findViewById(R.id.cmt_edit_toolbar);
        et_edit_cmt = findViewById(R.id.et_edit_cmt);
        btn_edit_send_cmt = findViewById(R.id.btn_edit_send_cmt);
        btn_edit_cancel_cmt = findViewById(R.id.btn_edit_cancel_cmt);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        cmtId = intent.getStringExtra("cmtId");
        postId = intent.getStringExtra("postId");

        btn_edit_send_cmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String cmt = et_edit_cmt.getText().toString();
                if (!TextUtils.isEmpty(cmt)) {
                    firebaseFirestore.collection("Posts/" + postId + "/Comments").document(cmtId).update("comment", cmt)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "comment is updated!!", Toast.LENGTH_LONG).show();
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