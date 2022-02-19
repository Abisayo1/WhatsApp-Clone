package com.sriyank.securityapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sriyank.securityapp.Adapter.ChatAdapter;
import com.sriyank.securityapp.Models.MessageModel;
import com.sriyank.securityapp.databinding.ActivityGroupChatBinding;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sp = getApplicationContext().getSharedPreferences("MyUser", Context.MODE_PRIVATE);
        String name = sp.getString("name", "Message");


        FirebaseMessaging.getInstance().subscribeToTopic("all");


        getSupportActionBar().hide();

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupChatActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ArrayList<MessageModel> messageModels = new ArrayList<>();

        final String senderId = FirebaseAuth.getInstance().getUid();
        binding.userName.setText("Group Chat");

        final ChatAdapter adapter = new ChatAdapter(messageModels, this);
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        database.getReference().child("Group Chat")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModels.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            MessageModel model = dataSnapshot.getValue(MessageModel.class);
                            messageModels.add(model);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("all");

                 String message = binding.enterMessage.getText().toString();
                 MessageModel model = new MessageModel(senderId, message);
                model.setTimestamp(new Date().getTime());

                binding.enterMessage.setText(null);
                database.getReference().child("Group Chat")
                        .push()
                        .setValue(model)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                FcmNotificationsSender notificationsSender = new FcmNotificationsSender("/topics/all",
                                        name, message,getApplicationContext(),GroupChatActivity.this);
                                notificationsSender.SendNotifications();
                                Toast.makeText(GroupChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();

                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                FirebaseMessaging.getInstance().subscribeToTopic("all");
                                            }
                                        },
                                        5000
                                );


                            }
                        });



            }
        });

    }
}