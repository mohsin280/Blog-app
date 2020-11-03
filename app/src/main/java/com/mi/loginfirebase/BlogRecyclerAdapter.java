package com.mi.loginfirebase;


import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    private Animation animRotate;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private List<BlogPostModel> blogPostModels;
    private Context context;


    public BlogRecyclerAdapter(List<BlogPostModel> blogPostModels) {
        this.blogPostModels = blogPostModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context = parent.getContext();

        animRotate = AnimationUtils.loadAnimation(context,R.anim.rotate);

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        BlogPostModel blogPostModel = blogPostModels.get(position);

        final String blogPostId = blogPostModels.get(position).BlogPostId;
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        holder.setDescView(blogPostModel.getDesc());

        String image_uri = blogPostModel.getImage_url();
        holder.setBlogImage(image_uri);

        long millisecond = blogPostModel.getTimestamp().getTime();
        String dateString = DateFormat.format("HH:mm dd/MM/yyyy ",new Date(millisecond)).toString();
        holder.setDate(dateString);

        String user_id = blogPostModel.getUser_id();
        holder.setNameAndProfile(user_id);

        //likes count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty())
                {
                    int count = queryDocumentSnapshots.size();
                    holder.updatesLikesCount(count);
                }
                else{
                    holder.updatesLikesCount(0);
                }
            }
        });

        //addsnap for realtime interaction
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("tag", "Listen failed.", e);
                    return;
                }
                if(documentSnapshot!=null && documentSnapshot.exists())
                {
                    holder.like_btn.setImageDrawable(context.getDrawable(R.drawable.ic_action_heart));
                }
                else{
                    holder.like_btn.setImageDrawable(context.getDrawable(R.drawable.ic_action_heart_white));
                }
            }
        });

        //likes features
        holder.like_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.like_btn.startAnimation(animRotate);
                //make collection of likes on firebase
                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            DocumentSnapshot document = task.getResult();
                            if(!document.exists())
                            {
                                Map<String , Object> likesMap =new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());
                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);
                            }
                            else{
                                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();
                            }
                        }
                        else {
                            Toast.makeText(context, "ERROR"+ task.getException(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return blogPostModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View  mview;
        private TextView desc;
        private TextView date;
        private ImageView post_image;
        private CircleImageView post_profile_image;
        private TextView post_usernName;
        private TextView tv_like_count;
        private ImageView like_btn;
        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;
            like_btn = itemView.findViewById(R.id.img_like);
        }
        private void setDescView(String descText){
            desc = mview.findViewById(R.id.tv_desc);
            desc.setText(descText);
        }
        private void setBlogImage(String downloadUri)
        {
            post_image = mview.findViewById(R.id.post_image_view);
            Picasso.get().load(downloadUri).fit().centerInside().placeholder(R.drawable.post_image_2).into(post_image);
        }
        private void setDate(String bDate)
        {
            date = mview.findViewById(R.id.tv_date);
            date.setText(bDate);
        }
        private void setNameAndProfile(String user_id){

            post_usernName = mview.findViewById(R.id.tv_user_name);
            post_profile_image = mview.findViewById(R.id.img_profile);
            firebaseFirestore.collection("Users").document(user_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        if (task.getResult().exists()) {
                            String post_user_name = task.getResult().getString("name");
                            String image = task.getResult().getString("image");

                            post_usernName.setText(post_user_name);
                            Picasso.get().load(image).placeholder(R.drawable.profile_pic).into(post_profile_image);

                        }else {
                            //exceptional handling
                            Toast.makeText(context, "ERROR"+ task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        private void updatesLikesCount(int count)
        {
            tv_like_count = mview.findViewById(R.id.tv_like_count);
            tv_like_count.setText(String.valueOf(count));
        }

    }
}
