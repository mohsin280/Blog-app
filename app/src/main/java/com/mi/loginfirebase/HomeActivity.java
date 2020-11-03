package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class HomeActivity extends AppCompatActivity {
    private Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private LinearLayout linear_new_post;
    private boolean isFirstPageFirstLoad = true;
    private  BlogPostModel lastVisible;

    //for adapter
    private RecyclerView recycleView_blog_list;
    private List<BlogPostModel> blog_post_list;
    private BlogRecyclerAdapter blogRecyclerAdapter;


    String current_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        //for Toolbar
        mainToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Home");

        linear_new_post=findViewById(R.id.linear_new_post);

        linear_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this,NewPostActivity.class));
                finish();
            }
        });

        //for RecyclerView Adapter
        recycleView_blog_list = findViewById(R.id.rv_blog_list);
        blog_post_list = new ArrayList<>();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_post_list);
        recycleView_blog_list.setLayoutManager(new LinearLayoutManager(HomeActivity.this,RecyclerView.VERTICAL,false));
        recycleView_blog_list.setAdapter(blogRecyclerAdapter);

        firebaseFirestore.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null)
                {
                    //Toast.makeText(HomeActivity.this,"Error = "+e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.d("fb","Error:"+e.getMessage());
                }
                else{
                    for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String blogPostId = doc.getDocument().getId();
                            BlogPostModel blogPostModel = doc.getDocument().toObject(BlogPostModel.class).withId(blogPostId);

                            blog_post_list.add(blogPostModel);
                            blogRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_btn_logout:
                logout();
                return true;
            case  R.id.action_btn_account:
                accountSetting();
            default:return false;

        }
    }

    private void accountSetting() {
        startActivity(new Intent(HomeActivity.this,AccountSetting.class));
        finish();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        sentToLogin();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
        if(current_user == null)
        {
            sentToLogin();
        }
        else{
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(!task.getResult().exists()){
                            Toast.makeText(HomeActivity.this, "please complete the account setting!! ", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(HomeActivity.this,AccountSetting.class));
                        }
                    }
                    else {
                        String error = task.getException().getMessage();
                        Toast.makeText(HomeActivity.this, "Error : " + error, Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });

        }


    }

    private void sentToLogin() {
        startActivity(new Intent(HomeActivity.this,MainActivity.class));
        finish();
    }

}
