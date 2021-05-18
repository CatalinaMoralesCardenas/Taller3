package com.example.taller3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.bumptech.glide.Glide;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView users;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseUser user;
    private DatabaseReference dbReference;
    private boolean stateUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        users = findViewById(R.id.userList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        users.setLayoutManager(linearLayoutManager);

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("users");

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        user = FirebaseAuth.getInstance().getCurrentUser();
        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {

            @Override
            public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_users, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder( UserViewHolder holder, int position, User model) {
                String key = this.getRef(position).getKey();
                state(key, holder, model);
            }
        };

        users.setAdapter(adapter);
    }

    public void state(String key, UserViewHolder holder, User model) {
        dbReference = FirebaseDatabase.getInstance().getReference("users/" + key);
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    System.out.println("-------------------------------------------------");
                    if (snapshot.child("availability").getValue().toString().equals("true")) {

                        stateUser = true;
                        if(!key.equals(user.getUid())) {
                            if(stateUser) {
                                Glide.with(UserListActivity.this).load(model.getUrlProfilePicture()).into(holder.getProfilePic());
                                holder.getName().setText(model.getName() + " " + model.getLastname());

                                holder.getLocation().setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(UserListActivity.this, RealTimePositionActivity.class);
                                        intent.putExtra("userKey", key);
                                        startActivity(intent);
                                    }
                                });
                            }
                        } else {
                            holder.setIsRecyclable(false);
                            holder.itemView.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        holder.setIsRecyclable(false);
                        holder.itemView.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}