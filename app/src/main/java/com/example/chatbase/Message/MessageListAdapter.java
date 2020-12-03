package com.example.chatbase.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.MessageViewHolder> {

    ArrayList<MessageObject> messageList;

    public MessageListAdapter(ArrayList<MessageObject> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = null;
        if (!FirebaseAuth.getInstance().getUid().equals("dMVJv7W8GNcN7O2vjkXQ5u8gBJp2")){
            layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_out, null, false);
        }else{
            layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_in, null, false);
        }
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);
        MessageViewHolder messageViewHolder = new MessageViewHolder(layoutView);
        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        holder.message.setText(messageList.get(position).getMessage());
        holder.sender.setText(messageList.get(position).getCreatorName());

        if (messageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty())
            holder.viewMedia.setVisibility(View.GONE);
        holder.viewMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("viewmedia", "onClick: clicked");
                new ImageViewer.Builder(v.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                        .setStartPosition(0)
                        .show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView message, sender;
        LinearLayout message_layout;
        Button viewMedia;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chatMessage);
            sender = itemView.findViewById(R.id.sender);
            message_layout = itemView.findViewById(R.id.messageItem);
            viewMedia = itemView.findViewById(R.id.viewMediaBtn);
        }
    }
}
