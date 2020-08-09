package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private Toolbar new_post_toolbar;
    private static final int MAX_LENGTH = 100;
    private ImageView post_imageView;
    private EditText et_post_description;
    private Button btn_new_post;
    private Uri imageUri = null;
    private ProgressBar progressBar;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        new_post_toolbar=findViewById(R.id.new_post_toolbar);
        setSupportActionBar(new_post_toolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        post_imageView = findViewById(R.id.post_image);
        et_post_description = findViewById(R.id.et_post_description);
        btn_new_post = findViewById(R.id.btn_new_post);
        progressBar = findViewById(R.id.pg_post);

        post_imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImagePickerFunc();
            }
        });

        btn_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String decs = et_post_description.getText().toString();

                if(!TextUtils.isEmpty(decs) && imageUri != null){
                    progressBar.setVisibility(View.VISIBLE);

                    String randomName = random();

                    final StorageReference filePath = storageReference.child("post images").child(randomName + ".jpg");
                    filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    storeToFirestore(uri,decs,current_user_id);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(NewPostActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(NewPostActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                }else{
                    Toast.makeText(NewPostActivity.this, "text or image field are empty!!", Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    private void storeToFirestore(Uri uri, String decs, String current_user_id) {
        Map<String,Object> postMap = new HashMap<>();
        postMap.put("image_url",uri.toString());
        postMap.put("desc",decs);
        postMap.put("user_id",current_user_id);
        postMap.put("timestamp",FieldValue.serverTimestamp());

        firebaseFirestore.collection("Posts")
                .add(postMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("onSuccess", "onSuccess: ");
                Toast.makeText(NewPostActivity.this,"Post added",Toast.LENGTH_LONG).show();
                startActivity(new Intent(NewPostActivity.this,HomeActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewPostActivity.this, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void cropImagePickerFunc() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(NewPostActivity.this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                post_imageView.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
