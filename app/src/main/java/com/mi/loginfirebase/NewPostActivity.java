package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private Toolbar new_post_toolbar;
    private ImageView post_imageView;
    private EditText et_post_description;
    private Button btn_new_post;
    private Uri imageUri = null;
    private ProgressBar progressBar;
    private String grpCode;

    private Bitmap compressedImageFile;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private String current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        final GrpCode globalGrpCode = (GrpCode) getApplicationContext();
        grpCode = globalGrpCode.getGrpCode();

        new_post_toolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(new_post_toolbar);
        getSupportActionBar().setTitle("Add New Post");
        //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getting group code
//        Intent intent = getIntent();
//        grpCode = intent.getStringExtra("grp_code");

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

                if (!TextUtils.isEmpty(decs) && imageUri != null) {
                    progressBar.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    final StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {

                                    // for thumb photo
                                    File actualImageFile = new File(imageUri.getPath());
                                    try {
                                        compressedImageFile = new Compressor(NewPostActivity.this)
                                                .setMaxHeight(100)
                                                .setMaxWidth(100)
                                                .setQuality(2)
                                                .compressToBitmap(actualImageFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                    byte[] thumbsData = out.toByteArray();

                                    UploadTask uploadTask = storageReference.child("post_images/thumbs")
                                            .child(randomName + ".jpg").putBytes(thumbsData);
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(final Uri uriThumbs) {

                                                    storeToFirestore(uri, uriThumbs, decs, current_user_id);

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

                } else {
                    Toast.makeText(NewPostActivity.this, "text or image field are empty!!", Toast.LENGTH_LONG).show();
                }

            }
        });


    }

    private void storeToFirestore(Uri uri, Uri uriThumb, String decs, String current_user_id) {
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("image_url", uri.toString());
        postMap.put("thumb_url", uriThumb.toString());
        postMap.put("desc", decs);
        postMap.put("user_id", current_user_id);
        postMap.put("timestamp", FieldValue.serverTimestamp());
        postMap.put("grp_code", grpCode);

        firebaseFirestore.collection("Posts")
                .add(postMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("onSuccess", "onSuccess: ");
                Toast.makeText(NewPostActivity.this, "Post added", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("grp_code", grpCode);
                startActivity(intent);
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
}
