package com.example.chatbase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.chatbase.Chat.ChatObject;
import com.example.chatbase.Message.MediaAdapter;
import com.example.chatbase.Message.MessageCustomAdapter;
import com.example.chatbase.Message.MessageListAdapter;
import com.example.chatbase.Message.MessageObject;
import com.example.chatbase.User.UserObjects;
import com.example.chatbase.Utils.SendNotification;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.chatbase.LoginActivity.PERF_NAME;
import static com.example.chatbase.Message.MessageCustomAdapter.MESSAGE_TYPE_IN;
import static com.example.chatbase.Message.MessageCustomAdapter.MESSAGE_TYPE_OUT;

public class MessageActivity extends AppCompatActivity {

    private static final int MEDIA_INTENT_CODE = 1;
    RecyclerView messageRecyclerView, mediaRecyclerView;
    RecyclerView.Adapter messageAdapter, mediaAdapter;
    RecyclerView.LayoutManager messageLayout, mediaLayout;
    ArrayList<MessageObject> messageList;
    ArrayList<String> mediaList;

    ChatObject mChatObject;
    DatabaseReference mChatMessagesDB;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mChatObject = (ChatObject) getIntent().getSerializableExtra("chatObject");
        mChatMessagesDB  = FirebaseDatabase.getInstance().getReference().child("chat").child(mChatObject.getChatId()).child("messages");

        FloatingActionButton sendBtn = findViewById(R.id.sendBtn);
        mMessage = findViewById(R.id.chatmessage);
        mMessage.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (mMessage.getRight() - mMessage.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        getMediaGalary();
                        return true;
                    }
                }
                return false;
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        getUSerName();
        initializeRecyclerView();
        initializeMediaRecyclerView();
        getChatMessages();
    }

    private void getUSerName() {
        SharedPreferences sharedPreferences = getSharedPreferences(PERF_NAME, MODE_PRIVATE);
        userName = sharedPreferences.getString("userName", "UserName");
    }

    ArrayList<String> mediaIDList = new ArrayList<>();
    int totalMediaUrls = 0;
    EditText mMessage;
    String creatorName;
    private void sendMessage() {

            String messageID = mChatMessagesDB.push().getKey();
            DatabaseReference chatDB = mChatMessagesDB.child(messageID);

            final Map<String, Object> newMessageMap = new HashMap<>();
            if (!mMessage.getText().toString().isEmpty())
                newMessageMap.put("text", mMessage.getText().toString());
            newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());
            newMessageMap.put("creatorName", userName);

            if (!mediaList.isEmpty()){
                for (String mediaUrl : mediaList){
                    String mediaId = chatDB.child("media").push().getKey();
                    mediaIDList.add(mediaId);
                    final StorageReference filePath = FirebaseStorage.getInstance().
                            getReference().child("chat").child(mChatObject.getChatId()).child(messageID).child(mediaId);

                    UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUrl));

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    newMessageMap.put("/media/"+mediaIDList.get(totalMediaUrls)+"/", uri.toString());

                                    totalMediaUrls++;
                                    if (totalMediaUrls == mediaList.size()){
                                        updateDatabaseWithNewMessage(chatDB, newMessageMap);
                                    }
                                }
                            });
                        }
                    });
                }
            }else{
                if (!mMessage.getText().toString().isEmpty()){
                    updateDatabaseWithNewMessage(chatDB, newMessageMap);
                }
            }
    }

    private void updateDatabaseWithNewMessage(DatabaseReference newMessageID, Map<String, Object> newMessageMap) {
        newMessageID.updateChildren(newMessageMap);
        mMessage.setText(null);
        mediaIDList.clear();
        mediaList.clear();
        mediaAdapter.notifyDataSetChanged();

        String message;
        if (newMessageMap.get("text") != null){
            message = newMessageMap.get("text").toString();
        }else{
            message = "send Media";
        }

        for (UserObjects mUser : mChatObject.getUserObjectsArrayList()){
            if (!mUser.getUid().equals(FirebaseAuth.getInstance().getUid())){
                new SendNotification(message, "new Message", mUser.getNotificationKey());
            }
        }
    }

    private void initializeMediaRecyclerView() {
        mediaList = new ArrayList<>();
        mediaRecyclerView = findViewById(R.id.mediaRecyclerView);
        mediaRecyclerView.setHasFixedSize(false);
        mediaRecyclerView.setNestedScrollingEnabled(false);
        mediaLayout = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        mediaRecyclerView.setLayoutManager(mediaLayout);
        mediaAdapter = new MediaAdapter(getApplicationContext(), mediaList);
        mediaRecyclerView.setAdapter(mediaAdapter);
    }

    private void getChatMessages(){
        mChatMessagesDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){
                    String text = "",
                            creator = "",
                            creatorName = "";
                    int messageNumber = 0;
                    ArrayList<String> mediaUrlList = new ArrayList<>();
                    if (snapshot.child("text").getValue() != null){
                        text = snapshot.child("text").getValue().toString();
                    }if (snapshot.child("creator").getValue() != null){
                        creator = snapshot.child("creator").getValue().toString();
                    }
                    if (snapshot.child("creatorName").getValue() != null){
                        creatorName = snapshot.child("creatorName").getValue().toString();
                    }
                    if (snapshot.child("media").getChildrenCount() > 0)
                        for (DataSnapshot dataSnapshot : snapshot.child("media").getChildren())
                            mediaUrlList.add(dataSnapshot.getValue().toString());
                    Log.i("messagenumber", "creator: "+creator);
                    Log.i("messagenumber", "uid: "+FirebaseAuth.getInstance().getUid());
                    if (creator.equals(FirebaseAuth.getInstance().getUid())){
                        messageNumber = MESSAGE_TYPE_OUT;
                    }else {
                        messageNumber = MESSAGE_TYPE_IN;
                    }
                    Log.i("messagenumber", "onChildAdded: "+messageNumber);
                    MessageObject mMessage = new MessageObject(messageNumber, snapshot.getKey(), creator, creatorName, text, mediaUrlList);
                    messageList.add(mMessage);
                    messageLayout.scrollToPosition(messageList.size() - 1);
                    messageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void initializeRecyclerView(){
        messageList = new ArrayList<>();
        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageRecyclerView.setHasFixedSize(false);
        messageRecyclerView.setNestedScrollingEnabled(false);
        messageLayout = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        messageRecyclerView.setLayoutManager(messageLayout);
        messageAdapter = new MessageCustomAdapter(this, messageList);
        messageRecyclerView.setAdapter(messageAdapter);
    }
    private void getMediaGalary() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose Image(s)"), MEDIA_INTENT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == MEDIA_INTENT_CODE){
                Log.i("media", "onActivityResult: ");
                if (data.getClipData() == null){
                    mediaList.add(data.getData().toString());
                }else{
                    for (int i = 0; i < data.getClipData().getItemCount(); i++){
                        mediaList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }
                mediaAdapter.notifyDataSetChanged();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_item){
            logOut();
            return true;
        }
        return  false;
    }
    public void logOut(){
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}