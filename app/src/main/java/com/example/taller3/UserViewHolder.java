package com.example.taller3;

import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView profilePic;
    private TextView name;
    private FloatingActionButton location;
    private CardView priCard;

    public UserViewHolder( View itemView ) {
        super(itemView);

        profilePic = itemView.findViewById(R.id.profileCard);
        name = itemView.findViewById(R.id.nameCard);
        location = itemView.findViewById(R.id.location);
        priCard = itemView.findViewById(R.id.priLayout);
    }

    public CircleImageView getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(CircleImageView profilePic) {
        this.profilePic = profilePic;
    }

    public TextView getName() {
        return name;
    }

    public void setName(TextView name) {
        this.name = name;
    }

    public FloatingActionButton getLocation() {
        return location;
    }

    public void setLocation(FloatingActionButton location) {
        this.location = location;
    }

    public CardView getPriCard() {
        return priCard;
    }

    public void setPriCard(CardView priCard) {
        this.priCard = priCard;
    }
}
