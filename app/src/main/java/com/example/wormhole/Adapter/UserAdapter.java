package com.example.wormhole.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wormhole.Activity.ChatActivity;
import com.example.wormhole.Activity.HomeActivity;
import com.example.wormhole.Model.Users;
import com.example.wormhole.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


//EL USER ADAPTER ES PARA QUE NOS SALGA EN EL HOME ACTIVITY MEDIANTE EL RECYCLER VIEW TODOS LOS USUARIOS QUE ESTEN REGISTRADOS EN LA APP

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.Viewholder> {
    Context homeActivity;
    ArrayList<Users> usersArrayList;


    public UserAdapter(HomeActivity homeActivity, ArrayList<Users> usersArrayList) {
        this.homeActivity = homeActivity;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //AQUI VINCULAMOS EL VIEWHOLDER MEIDANTE UN LAYOUT INFLATER AL ARRAY DEL RECYCLER VIEW
        View view  = LayoutInflater.from(homeActivity).inflate(R.layout.item_user_row,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
            Users users = usersArrayList.get(position);

            //ESTA LINA ES PARA QUE NO NOS SALGA EL CHAT DE NOSOTROS MISMOS
            if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(users.getUid())){
                holder.itemView.setVisibility(View.GONE);
            }


            //LOS HOLDER ES PARA QUE TE SALGA EL TEXTO DE LO QUE LLAMAS QUE EN ESTE CASO SON EL USUARIO STATUS Y EL NOMBRE
            holder.user_name.setText(users.name);
            holder.user_status.setText(users.status);
            Picasso.get().load(users.imageuri).into(holder.user_profile);


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //CON ESTO SE TRAE LOS DATOS DEL CHAT ACTIVITY
                    Intent intent = new Intent(homeActivity, ChatActivity.class);
                    intent.putExtra("name", users.getName());
                    intent.putExtra("ReciverImage",users.getImageuri());
                    intent.putExtra("uid",users.getUid());
                    homeActivity.startActivity(intent);

                }
            });

    }



    //ESTO ES PARA QUE EL ITEM COUNT DEL RECYCLER SEA IGUAL DE GRANDE QUE EL ARRAY DE USUARIOS
    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    //CREAR UNA CLASE VIEWHOLDER CON LOS ELEMENTOS DE ALMACENAJE QUE VA A TENER
    class Viewholder extends RecyclerView.ViewHolder {
        CircleImageView user_profile;
        TextView user_name;
        TextView user_status;


        public Viewholder(@NonNull View itemView) {
            super(itemView);
            user_profile = itemView.findViewById(R.id.user_image);
            user_name = itemView.findViewById(R.id.user_name);
            user_status = itemView.findViewById(R.id.user_status);
        }
    }
}
