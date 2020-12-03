 package com.example.chatbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.chatbase.User.UserListAdapter;
import com.example.chatbase.User.UserObjects;
import com.example.chatbase.Utils.CountryToPhonePrefix;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class FindUserContactActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;

    ArrayList<UserObjects> arrayList, contactList;
    String iso = "IN";
    public static final String TAG = "magesh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user_contact);

        arrayList = new ArrayList<>();
        contactList = new ArrayList<>();

        FloatingActionButton createChatRoom = findViewById(R.id.create_chat_room);
        createChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChat();
                moveToParentActivity();
            }
        });
        initializeRecyclerView();
        getContacts();
    }

    private void moveToParentActivity() {
        Intent intent = new Intent(FindUserContactActivity.this, MainPageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void createChat(){
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");
        DatabaseReference chatInfoDb = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("info");
        HashMap<String, Object> newChatMap = new HashMap<>();
        newChatMap.put("id", key);
        newChatMap.put("users/" + FirebaseAuth.getInstance().getUid(), true);

        boolean validChat = false;
        for (UserObjects mUser : arrayList){
            if (mUser.isSelected()){
                validChat = true;
                newChatMap.put("users/" + mUser.getUid(), true);
                userDB.child(mUser.getUid()).child("chat").child(key).setValue(true);
            }
        }
        if (validChat){
            chatInfoDb.updateChildren(newChatMap);
            userDB.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
        }
    }

    private void getContacts() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);
        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            phone = phone.replace(" ","");
            phone = phone.replace("-","");
            phone = phone.replace("{","");
            phone = phone.replace("}","");

            String ISOprefix = getCountryISO();
            if (! String.valueOf(phone.charAt(0)).equals("+")){
                phone = ISOprefix + phone;
            }
            UserObjects contacts = new UserObjects("", name, phone);
            contactList.add(contacts);
            getUSerDetails(contacts);

        }
        cursor.close();
    }

    private void getUSerDetails(UserObjects contacts) {
        Log.i("magesh", "getUSerDetails: "+contacts.getPhone());
        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user");
        Query query = userDB.orderByChild("phone").equalTo(contacts.getPhone());
        Log.i("magesh", "getUSerDetails:  MATCHES");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = "", phone = "";
                    for (DataSnapshot childSnapShot: snapshot.getChildren()) {
                        if (childSnapShot.child("phone").getValue() != null){
                            Log.i(TAG, "onDataChange: "+childSnapShot.child("phone").getValue());
                            phone = childSnapShot.child("phone").getValue().toString();
                        }
                        if (childSnapShot.child("name").getValue() != null){
                            Log.i(TAG, "onDataChange: "+childSnapShot.child("name").getValue());
                            name = childSnapShot.child("name").getValue().toString();
                        }

                        UserObjects userObjects = new UserObjects(childSnapShot.getKey(), name, phone);
                        if (name.equals(phone)){
                            for (UserObjects iterator : contactList){
                                if (iterator.getPhone().equals(userObjects.getPhone())){
                                    userObjects.setName(iterator.getName());
                                }
                            }
                        }
                        arrayList.add(userObjects);
                        adapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getCountryISO(){

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        //Log.i("magesh", "getCountryISO: "+telephonyManager);
        if (Locale.getDefault().getCountry() != null){
            if (Locale.getDefault().getCountry().equals("")){
                iso = Locale.getDefault().getCountry();
                Log.i("magesh", "getCountryISO imside : "+iso);
            }
        }
        return CountryToPhonePrefix.getPhone(iso);
    }
    private void initializeRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new UserListAdapter(arrayList);
        recyclerView.setAdapter(adapter);
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

    @Override
    public void onBackPressed() {
        moveToParentActivity();
    }
}