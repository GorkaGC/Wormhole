package com.example.wormhole.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wormhole.Adapter.MessageAdapter;
import com.example.wormhole.Model.Messages;
import com.example.wormhole.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String ReciverImage, ReciverUID, ReciverName,SenderUID;
    CircleImageView profileImage;
    TextView reciverName;

    //FIREBASE VARIABLES
    FirebaseDatabase database;
    FirebaseAuth firebaseAuth;

    public static String sImage;
    public static String rImage;

    CardView sendBtn;
    EditText edtMessage;

    String senderRoom,reciverRoom;

    RecyclerView messageAdapter;
    ArrayList<Messages> messagesArrayList;

    MessageAdapter mAdapter;

    MediaPlayer msgSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        //DATOS TRAIDOS DE LA HOME ACTIVITY QUE SON EL NOMBRE DE QUIEN PULSAMOS JUNTO A SU FOTO Y SU UID
        ReciverName = getIntent().getStringExtra("name");
        ReciverImage = getIntent().getStringExtra("ReciverImage");
        ReciverUID = getIntent().getStringExtra("uid");


        messagesArrayList = new ArrayList<>();

        profileImage = findViewById(R.id.profile_image);
        reciverName = findViewById(R.id.reciverName);

        sendBtn = findViewById(R.id.sendBtn);
        edtMessage = findViewById(R.id.edtMessage);
        msgSent = MediaPlayer.create(ChatActivity.this,R.raw.msg);

        Picasso.get().load(ReciverImage).into(profileImage);
        reciverName.setText(""+ReciverName);



        messageAdapter =findViewById(R.id.messageAdapter);

        //CREACION DE LINEAR LAYOUT DONDE SE VAN A IR PONIENDO LOS MENSAJES
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.findLastCompletelyVisibleItemPosition();
        linearLayoutManager.setStackFromEnd(true);
        messageAdapter.setLayoutManager(linearLayoutManager);
        mAdapter = new MessageAdapter(ChatActivity.this, messagesArrayList);
        messageAdapter.setAdapter(mAdapter);

        //ELEMENTOS DEL QUE ENVIA Y RECIBE
        SenderUID = firebaseAuth.getUid();
        senderRoom = SenderUID+ReciverUID;
        reciverRoom = ReciverUID + SenderUID;

        DatabaseReference reference = database.getReference().child("user").child(firebaseAuth.getUid());
        DatabaseReference chatReference = database.getReference().child("chats").child(senderRoom).child("messages");



        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messagesArrayList.clear();
                //UN FOR EACH QUE RECORRE EL ARRAY LIST Y VA AÑADIENDO LOS MENSAJES DEL MISMO
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Messages messages = dataSnapshot.getValue(Messages.class);
                    messagesArrayList.add(messages);

                }
                    mAdapter.notifyDataSetChanged();
                    //int nMensajes = mAdapter.getItemCount();
                    //mAdapter.notifyItemInserted(nMensajes);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        //COGER LOS DATOS DE LAS IMAGENES CON EL OBJETO REFERENCE QUE LLAMA A LA BASE DE DATOS Y COGE LO QUE NECESITE DE USER QUE EN ESTE CASO ES EL UID
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //PONEMOS LA IMAGEN TANO DEL QUE ENVIA COMO EL RECIVIDOR
                sImage = snapshot.child("imageuri").getValue().toString();
                rImage = ReciverImage;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msgSent.start();
                String message = edtMessage.getText().toString();
                if(message.isEmpty()){
                    Toast.makeText(ChatActivity.this, R.string.enter_msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                edtMessage.setText("");
                Date date = new Date();
                Messages messages = new Messages(message,SenderUID, date.getTime());

                database = FirebaseDatabase.getInstance();
                database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .push()
                .setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        database.getReference().child("chats")
                                .child(reciverRoom)
                                .child("messages")
                                .push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
                    }
                });
            }
        });


    }
}