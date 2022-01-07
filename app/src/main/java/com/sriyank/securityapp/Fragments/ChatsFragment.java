package com.sriyank.securityapp.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sriyank.securityapp.Adapter.UsersAdapter;
import com.sriyank.securityapp.Models.Users;
import com.sriyank.securityapp.R;
import com.sriyank.securityapp.databinding.FragmentChatsBinding;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {


    public ChatsFragment() {
        // Required empty public constructor
    }

    FragmentChatsBinding binding;
    ArrayList<Users> list = new ArrayList<Users>();
    FirebaseDatabase database;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentChatsBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        UsersAdapter adapter = new UsersAdapter(list, getContext());
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        binding.shimmerLayout.startShimmer();

        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    users.setUserId(dataSnapshot.getKey());
                    if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                        list.add(users);
                    }

                    binding.shimmerLayout.stopShimmer();
                    binding.shimmerLayout.setVisibility(View.GONE);
                    binding.chatRecyclerView.setVisibility(View.VISIBLE);



                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }
}