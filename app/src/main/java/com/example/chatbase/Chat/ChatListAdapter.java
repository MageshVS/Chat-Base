package com.example.chatbase.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbase.MessageActivity;
import com.example.chatbase.R;
import com.example.chatbase.User.UserObjects;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<com.example.chatbase.Chat.ChatListAdapter.ChatListHolder> {

    ArrayList<ChatObject> chatList;
    public ChatListAdapter(ArrayList<ChatObject> chatList){
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public com.example.chatbase.Chat.ChatListAdapter.ChatListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);

        com.example.chatbase.Chat.ChatListAdapter.ChatListHolder chatListHolder = new com.example.chatbase.Chat.ChatListAdapter.ChatListHolder(layoutView);
        return chatListHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull com.example.chatbase.Chat.ChatListAdapter.ChatListHolder holder, int position) {
        holder.title.setText(chatList.get(position).getChatId());

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MessageActivity.class);
                intent.putExtra("chatObject", chatList.get(holder.getAdapterPosition()));
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public class ChatListHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public ConstraintLayout layout;
        public ChatListHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            layout = itemView.findViewById(R.id.chatItem);
        }
    }
}
