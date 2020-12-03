package com.example.chatbase.User;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListHolder> {

    ArrayList<UserObjects> userList;
    public UserListAdapter(ArrayList<UserObjects> userList){
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserListAdapter.UserListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        UserListHolder userListHolder = new UserListHolder(layoutView);
        return userListHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final UserListAdapter.UserListHolder holder,final int position) {
        holder.user_name.setText(userList.get(position).getName());
        holder.user_phone_number.setText(userList.get(position).getPhone());

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userList.get(holder.getAdapterPosition()).setSelected(isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserListHolder extends RecyclerView.ViewHolder{
        public TextView user_name, user_phone_number;
        public CardView layout;
        CheckBox checkBox;
        public UserListHolder(@NonNull View itemView) {
            super(itemView);
            user_name = itemView.findViewById(R.id.username);
            user_phone_number = itemView.findViewById(R.id.userPhoneNumber);
            layout = itemView.findViewById(R.id.item_layout);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}
