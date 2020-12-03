package com.example.chatbase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.AuthProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText nameBox, phoneBox, codeBox;
    private Button submit;
    private ProgressBar loginprogressBar;
    ProgressDialog progressDialog;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    String verificationId;
    public static final String PERF_NAME = "USERNAME";

    public static final String TAG = "magesh";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        FirebaseApp.initializeApp(this);

        userIsLoggedIn();

        phoneBox = findViewById(R.id.phoneNumber);
        codeBox = findViewById(R.id.otpNumber);
        nameBox = findViewById(R.id.name_nick);
        submit = findViewById(R.id.submit);
        //loginprogressBar = findViewById(R.id.login_progress);
        progressDialog = new ProgressDialog(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: button clicked");

                if (verificationId != null){
                    verifyPhoneNumberWithCode();
                }else{
                    startPhoneNumberVerification();
                }

                    
            }
        });

        Log.i(TAG, "onCreate: after click");
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.i(TAG, "onVerificationCompleted: ");
                signInWithPhoneCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.i(TAG, "onVerificationFailed: "+e);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId = s;
                Log.i(TAG, "onCodeSent: "+s);
                phoneBox.setVisibility(View.GONE);
                codeBox.setVisibility(View.VISIBLE);
                submit.setText("verify");
            }
        };

    }

    private void verifyPhoneNumberWithCode(){
        Log.i(TAG, "verifyPhoneNumberWithCode: started");
        //startPhoneNumberVerification();
        progressDialog.create();
        progressDialog.show();
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codeBox.getText().toString());
            signInWithPhoneCredentials(credential);
        }catch (Exception e){
            Log.i(TAG, "verifyPhoneNumberWithCode: "+e);
        }

    }
    private void signInWithPhoneCredentials(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null){
                        final DatabaseReference  userDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
                        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()){
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name",  user.getPhoneNumber());
                                    userDB.updateChildren(userMap);
                                }
                                progressDialog.dismiss();
                                storeUserName();
                                userIsLoggedIn();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

            }
        });
    }

    private void storeUserName() {
        SharedPreferences.Editor editor = getSharedPreferences(PERF_NAME, MODE_PRIVATE).edit();
        editor.putString("userName", nameBox.getText().toString());
        editor.apply();
    }

    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
        }
    }

    private void startPhoneNumberVerification() {
        Log.i(TAG, "startPhoneNumberVerification: else block");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

// Configure faking the auto-retrieval with the whitelisted numbers.
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneBox.getText().toString(), verificationId);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber( phoneBox.getText().toString())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallBacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
        //PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneBox.getText().toString(), 60, TimeUnit.SECONDS, this,  mCallBacks);

        Log.i(TAG, "startPhoneNumberVerification: finished");
    }
}