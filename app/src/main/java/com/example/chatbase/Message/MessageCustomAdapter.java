package com.example.chatbase.Message;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbase.R;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

public class MessageCustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<MessageObject> messageList;
    public static final int MESSAGE_TYPE_IN = 1;
    public static final int MESSAGE_TYPE_OUT = 2;

    public MessageCustomAdapter(Context context, ArrayList<MessageObject> messageList) {
        this.context = context;
        this.messageList = messageList;
    }



    private class MessageInViewHolder extends RecyclerView.ViewHolder{
        TextView message, sender;
        LinearLayout message_layout;
        ImageButton viewMedia;
        public MessageInViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chatMessage);
            sender = itemView.findViewById(R.id.sender);
            message_layout = itemView.findViewById(R.id.messageItem);
            viewMedia = itemView.findViewById(R.id.viewMediaBtn);
        }
        void bind(int position){
            MessageObject messageObject = messageList.get(position);
            message.setText(messageObject.getMessage());
            sender.setText(messageObject.getCreatorName());

            if (messageList.get(position).getMediaUrlList().isEmpty())
                viewMedia.setVisibility(View.GONE);
            viewMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("viewmedia", "onClick: clicked");
                    new ImageViewer.Builder(v.getContext(), messageList.get(position).getMediaUrlList())
                            .setStartPosition(0)
                            .show();
                }
            });
        }
    }
    private class MessageOutViewHolder extends RecyclerView.ViewHolder{
        TextView message, sender;
        LinearLayout message_layout;
        ImageButton viewMedia;
        public MessageOutViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.chatMessage);
            sender = itemView.findViewById(R.id.sender);
            message_layout = itemView.findViewById(R.id.messageItem);
            viewMedia = itemView.findViewById(R.id.viewMediaBtn);
        }
        void bind(int position){
            MessageObject messageObject = messageList.get(position);
            message.setText(messageObject.getMessage());
            sender.setText(messageObject.getCreatorName());
            if (messageList.get(position).getMediaUrlList().isEmpty())
                viewMedia.setVisibility(View.GONE);
            viewMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("viewmedia", "onClick: clicked");
                    new ImageViewer.Builder(v.getContext(), messageList.get(position).getMediaUrlList())
                            .setStartPosition(0)
                            .show();
                }
            });
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_TYPE_IN){
            return new MessageInViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_in, parent, false));
        }
        return new MessageOutViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_out, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (messageList.get(position).messageNumber == MESSAGE_TYPE_IN){
            ((MessageInViewHolder)holder).bind(position);
        }else {
            ((MessageOutViewHolder)holder).bind(position);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).messageNumber;
    }
}
