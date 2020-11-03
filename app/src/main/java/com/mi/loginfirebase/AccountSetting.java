package com.mi.loginfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSetting extends AppCompatActivity {

    Toolbar accnt_toolbar;
    private CircleImageView circleImageView;
    private EditText et_name;
    private Button btn_account;
    private boolean isChanged = false;

    private String user_id;
    private String user_name;
    private Uri imageUri = null;

    private StorageReference mStorageRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        et_name = findViewById(R.id.et_name);
        btn_account = findViewById(R.id.btn_account);
        progressBar = findViewById(R.id.progressBar);

        user_id = mFirebaseAuth.getCurrentUser().getUid();


        accnt_toolbar = findViewById(R.id.accnt_toolbar);
        setSupportActionBar(accnt_toolbar);
        getSupportActionBar().setTitle("Account Setting");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        circleImageView = findViewById(R.id.circle_imageView);

        firebaseFirestore.collection("Users").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                user_name = task.getResult().getString("name");
                                String image = task.getResult().getString("image");
                                imageUri = Uri.parse(image);

                                et_name.setText(user_name);
                                Picasso.get().load(imageUri).placeholder(R.drawable.profile_pic).into(circleImageView);

                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(AccountSetting.this, "Error : " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        btn_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_name = et_name.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(user_name) && imageUri != null) {

                    if (isChanged) {

                        user_id = mFirebaseAuth.getCurrentUser().getUid();
                        final StorageReference image_path = mStorageRef.child("profile_name").child(user_id + ".jpg");

                        image_path.putFile(imageUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {

                                                storeToFireStore(uri, user_name);
                                                progressBar.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AccountSetting.this, "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });


                    } else {
                        storeToFireStore(null, user_name);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Toast.makeText(AccountSetting.this, "Text or image is empty!!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                }

            }
        });


        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //request for storage permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(AccountSetting.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(AccountSetting.this, "Permission Denied!!", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(AccountSetting.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        cropImagePickerFunc();
                    }
                } else {
                    cropImagePickerFunc();
                }
            }
        });

    }

    private void storeToFireStore(Uri uri, String user_name) {
        //download url of image which is now in storage
        final Uri downloadUrl;
        if (uri != null)
            downloadUrl = uri;
        else
            downloadUrl = imageUri;

        Map<String, String> user_map = new HashMap<>();
        user_map.put("name", user_name);
        user_map.put("image", downloadUrl.toString());

        firebaseFirestore.collection("Users").document(user_id).set(user_map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            isChanged = true;
                            Toast.makeText(AccountSetting.this, "Account setting is updated", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(AccountSetting.this, HomeActivity.class));
                            finish();
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(AccountSetting.this, "Error : " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void cropImagePickerFunc() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(AccountSetting.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                circleImageView.setImageURI(imageUri);
                isChanged = true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}

