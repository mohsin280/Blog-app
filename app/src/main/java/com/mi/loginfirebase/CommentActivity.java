package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentActivity extends AppCompatActivity {
    private Toolbar cmt_toolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView_cmt;
    private EditText et_cmt;
    private ImageButton btn_send_cmt;
    private ImageButton btn_cancel_cmt;

    private String userId;
    private String postId;

    private List<CommentModel> cmtModelList;
    private CommentRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        cmt_toolbar = findViewById(R.id.cmt_toolbar);
        recyclerView_cmt = findViewById(R.id.rv_cmt);
        et_cmt = findViewById(R.id.et_cmt);
        btn_send_cmt = findViewById(R.id.btn_send_cmt);
        btn_cancel_cmt = findViewById(R.id.btn_cancel_cmt);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        cmtModelList = new ArrayList<>();
        adapter = new CommentRecyclerAdapter(cmtModelList);
        recyclerView_cmt.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        recyclerView_cmt.setAdapter(adapter);

        firebaseFirestore.collection("Posts/" + postId + "/Comments").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "Error = " + e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            CommentModel model = new CommentModel();
                            model.setCmtId(doc.getDocument().getId());
                            model.setComment((String) doc.getDocument().get("comment"));
                            model.setTimestamp((Date) doc.getDocument().get("timestamp"));
                            model.setUserId((String) doc.getDocument().get("user_id"));
                            model.setPostId((String) doc.getDocument().get("post_id"));
                            cmtModelList.add(model);
                            System.out.println(model.toString());
                            adapter.notifyDataSetChanged();

                        }
                    }

                }
            }
        });

        btn_send_cmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmt = et_cmt.getText().toString();
                if (!TextUtils.isEmpty(cmt)) {
                    storeToFirestore(cmt, userId);
                } else {
                    Toast.makeText(getApplicationContext(), "comment field is empty!! ", Toast.LENGTH_LONG).show();

                }

            }
        });

        btn_cancel_cmt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_cmt.getText().clear();
            }
        });

    }

    private void storeToFirestore(String cmt, String userId) {
        Map<String, Object> cmtMap = new HashMap<>();
        cmtMap.put("comment", cmt);
        cmtMap.put("timestamp", FieldValue.serverTimestamp());
        cmtMap.put("user_id", userId);
        cmtMap.put("post_id", postId);
        System.out.println("mAP" + cmtMap);
        firebaseFirestore.collection("Posts/" + postId + "/Comments")
                .add(cmtMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "comment successfully added", Toast.LENGTH_LONG).show();
                        finish();
                        startActivity(getIntent());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error :" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}