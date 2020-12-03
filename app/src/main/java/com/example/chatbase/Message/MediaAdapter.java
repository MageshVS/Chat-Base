package com.example.chatbase.Message;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatbase.R;

import java.util.ArrayList;

import static com.example.chatbase.FindUserContactActivity.TAG;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder>{
    Context context;
    ArrayList<String> mediaUriList;

    public MediaAdapter(Context context, ArrayList<String> mediaUriList) {
        this.context = context;
        this.mediaUriList = mediaUriList;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, null, false);
        MediaViewHolder mediaViewHolder = new MediaViewHolder(viewLayout);
        return mediaViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Log.i("media", "onBindViewHolder: ");
        Glide.with(context).load(Uri.parse(mediaUriList.get(position))).into(holder.mediaImage);
    }

    @Override
    public int getItemCount() {
        return mediaUriList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder{
        ImageView mediaImage;
        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            mediaImage = itemView.findViewById(R.id.media_image);
        }
    }
}
