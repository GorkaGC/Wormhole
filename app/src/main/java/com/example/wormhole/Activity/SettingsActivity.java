package com.example.wormhole.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wormhole.Model.Users;
import com.example.wormhole.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    //VARIABLES
    CircleImageView setting_image;
    EditText setting_name, setting_status;


    //FIREBASE VARIABLES
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;


    //MAS VARIABLES
    ImageView save;
    Uri selectImageURI;
    String email;
    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        //MENSAJE QUE APARECERÁ EN LOS PROCESOS DE CARGA
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait. . .");
        progressDialog.setCancelable(false);

        //CONEXION CON LOS DATOS DE LA BASE DE DATOS
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();



        setting_image = findViewById(R.id.setting_image);
        setting_name = findViewById(R.id.setting_name);
        setting_status = findViewById(R.id.setting_status);

        save = findViewById(R.id.save);

        //LLAMADA A LOS DATOS DE LA FIREBASE DATABASE
        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //COGER LOS DATOS DEL USUARIO CON EL QUE HAS INICIADO SESION PARA QUE TE SALGA TU FOTO Y TU ESTADO  ETC
                email = snapshot.child("email").getValue().toString();
                String name = snapshot.child("name").getValue().toString();
                String status = snapshot.child("status").getValue().toString();
                String image = snapshot.child("imageuri").getValue().toString();

                //PONER LOS DATOS EN LAS CAJAS DE TEXTO CORRESPONDIENTES AL USUARIO
                setting_name.setText(name);
                setting_status.setText(status);
                Picasso.get().load(image).into(setting_image);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //MISMO METODOD QUE SE USA EN EL REGISTRO PARA PONER LA FOTO
        setting_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });



        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                String name = setting_name.getText().toString();
                String status = setting_status.getText().toString();

                if (selectImageURI != null) {
                    //SI NO ESTA NULA LA SELECCION DE FOTO METO UNA TAREA DE SI ESTA COMPLETA LA ACCION
                    storageReference.putFile(selectImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //SI SE COMPLETA LE PASO UN ELEMENTO URI EL CUAL ES UNA URL ES LA FOTO QUE HEMOS SELECCIONAD
                                    String finalImageUri = uri.toString();
                                    //USAMOS EL OBJETO DE LA CLASE USER PARA QUE NOS GUARDE LOS DATOS EN SUS RESPECTIVOS CONTENEDORES
                                    Users users = new Users(auth.getUid(), name, email, finalImageUri, status);

                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //ESTO ES UNA VEZ SE HAYA COMPLETADO LA TAREA NOS ENSEÑA UN TOAST Y NOS MANDA A LA ACTIVIDAD HOME
                                                progressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, R.string.data_success, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, R.string.wrong, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                } else {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String finalImageUri = uri.toString();
                            Users users = new Users(auth.getUid(), name, email, finalImageUri, status);

                            reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, R.string.data_success, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, R.string.wrong, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            if (data != null) {
                selectImageURI = data.getData();
                setting_image.setImageURI(selectImageURI);
            }
        }
    }
}