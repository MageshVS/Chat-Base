package com.example.chatbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.chatbase.Chat.ChatListAdapter;
import com.example.chatbase.Chat.ChatObject;
import com.example.chatbase.User.UserObjects;
import com.example.chatbase.Utils.SendNotification;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;

public class MainPageActivity extends AppCompatActivity {

    FloatingActionButton addContact;
    RecyclerView chatRecyclerView;
    RecyclerView.LayoutManager chatLayoutManager;
    RecyclerView.Adapter chatAdapter;

    ArrayList<ChatObject> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        //OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG);
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").
                        child(FirebaseAuth.getInstance().getUid()).child("notificationKey").setValue(userId);

            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
        //new SendNotification("Hello Magesh", "Chat Base", null);
        Fresco.initialize(this);

        addContact = findViewById(R.id.addContact);

        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, FindUserContactActivity.class);
                startActivity(intent);
                finish();
            }
        });
        getPermission();
        initializeRecyclerView();
        getUserChatList();
    }

    public void getUserChatList(){
        DatabaseReference reference = FirebaseDatabase.getInstance().
                getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot childSnapshot : snapshot.getChildren()){
                        ChatObject chatObject = new ChatObject(childSnapshot.getKey());
                        boolean exists = false;
                        for (ChatObject iterator : chatList){
                            if (iterator.getChatId().equals(chatObject.getChatId()))
                                exists = true;
                        }
                        if (exists)
                            continue;
                        chatList.add(chatObject);
                        getChatData(chatObject.getChatId());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getChatData(String chatId) {
        DatabaseReference chatDB = FirebaseDatabase.getInstance().
                getReference().child("chat").child(chatId).child("info");
        chatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String chatId = "";
                    if (snapshot.child("id").getValue() != null){
                        chatId = snapshot.child("id").getValue().toString();
                    }
                    for (DataSnapshot usersnapshot : snapshot.child("users").getChildren() ) {
                        for (ChatObject mChat : chatList){
                            if (mChat.getChatId().equals(chatId)){
                                UserObjects mUser = new UserObjects(usersnapshot.getKey());
                                mChat.addUserToArrayList(mUser);
                                getUserData(mUser);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserData(UserObjects mUser) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("user").child(mUser.getUid());
        mUserDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserObjects mUser = new UserObjects(snapshot.getKey());

                if (snapshot.child("notificationKey").getValue() != null){
                    mUser.setNotificationKey(snapshot.child("notificationKey").getValue().toString());
                }

                for (ChatObject mChat : chatList){
                    for (UserObjects mUserId: mChat.getUserObjectsArrayList()){
                        if (mUserId.getUid().equals(mUser.getUid())){
                            mUserId.setNotificationKey(mUser.getNotificationKey());
                        }
                    }
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeRecyclerView(){
        chatList = new ArrayList<>();
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setNestedScrollingEnabled(false);
        chatRecyclerView.setHasFixedSize(false);
        chatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        chatRecyclerView.setLayoutManager(chatLayoutManager);
        chatAdapter = new ChatListAdapter(chatList);
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void getPermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
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