package com.mi.loginfirebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Comment;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.CommentHolder> {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private List<CommentModel> commentModels;

    public CommentRecyclerAdapter(List<CommentModel> commentModels) {
        this.commentModels = commentModels;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_card, parent, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new CommentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentHolder holder, final int position) {
        CommentModel model = commentModels.get(position);

        final String cmtId = model.getCmtId();
        final String postId = model.getPostId();
        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        holder.setComment(model.getComment());

        try {
            long millisecond = model.getTimestamp().getTime();
            String dateString = DateFormat.format("HH:mm dd/MM/yyyy ", new Date(millisecond)).toString();
            holder.setDate(dateString);
        } catch (Exception e) {
//            Log.d("tError",e.getMessage());
        }

        String user_id = model.getUserId();
        holder.setNameAndProfile(user_id);

        if (model.getUserId().equals(currentUserId)) {
            holder.btnOption.setVisibility(View.VISIBLE);
        } else {
            holder.btnOption.setVisibility(View.GONE);
        }

        holder.btnOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("inside menu");
                PopupMenu popupMenu = new PopupMenu(holder.context, holder.btnOption);
                popupMenu.inflate(R.menu.post_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_edit: {
                                System.out.println("inside edit");
                                Intent intent = new Intent(holder.context, CommentEditActivity.class);

                                intent.putExtra("cmtId", cmtId);
                                intent.putExtra("postId", postId);
                                holder.context.startActivity(intent);
                                return true;
                            }
                            case R.id.item_delete: {
                                System.out.println("inside delete");
                                deletePost(cmtId, postId, position, holder.context);
                                return true;
                            }
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });


    }

    private void deletePost(final String cmtId, final String postId, final int i, final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Delete Comment?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                firebaseFirestore.collection("Posts/" + postId + "/Comments").document(cmtId)
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context, "comment successfully deleted!", Toast.LENGTH_LONG).show();
                                commentModels.remove(i);
                                notifyItemRemoved(i);
                                notifyDataSetChanged();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Error :" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return commentModels.size();
    }

    class CommentHolder extends RecyclerView.ViewHolder {

        private final Context context;
        private CircleImageView img_profile_cmt;
        private TextView username, date, comment;
        private ImageButton btnOption;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            img_profile_cmt = itemView.findViewById(R.id.img_profile_cmt);
            username = itemView.findViewById(R.id.tv_username_cmt);
            date = itemView.findViewById(R.id.tv_date_cmt);
            comment = itemView.findViewById(R.id.tv_user_comment);
            btnOption = itemView.findViewById(R.id.btn_cmt_menu);
        }
        private void setComment(String cmt) {
            comment.setText(cmt);
        }

        private void setDate(String bdate) {
            date.setText(bdate);
        }

        private void setNameAndProfile(String user_id) {

            firebaseFirestore.collection("Users").document(user_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {
                                    String post_user_name = task.getResult().getString("name");
                                    String image = task.getResult().getString("image");
                                    username.setText(post_user_name);
                                    Picasso.get().load(image).placeholder(R.drawable.profile_pic).into(img_profile_cmt);
                                } else {
                                    //exceptional handling
                                    Toast.makeText(context, "ERROR" + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

}
