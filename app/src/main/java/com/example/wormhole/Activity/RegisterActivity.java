package com.example.wormhole.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wormhole.R;
import com.example.wormhole.Model.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    TextView txt_signIn,btn_SignUp;
    CircleImageView profile_img;
    EditText reg_name,reg_email,reg_pass,reg_cPass;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri imageUri;
    String imageURI;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    ProgressDialog progressDialog;

    MediaPlayer registered;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait. . .");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        txt_signIn = findViewById(R.id.txt_sigIn);
        profile_img = findViewById(R.id.profile_image);
        reg_name = findViewById(R.id.reg_name);
        reg_email = findViewById(R.id.reg_email);
        reg_pass = findViewById(R.id.reg_pass);
        reg_cPass = findViewById(R.id.reg_cPass);
        btn_SignUp = findViewById(R.id.btn_SignUp);


        registered = MediaPlayer.create(RegisterActivity.this,R.raw.registered);



        btn_SignUp.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            progressDialog.show();
            String name = reg_name.getText().toString();
            String email = reg_email.getText().toString();
            String pass = reg_pass.getText().toString();
            String cPass = reg_cPass.getText().toString();
            String status= "Hey There I'm using WormHole";

            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(pass) || TextUtils.isEmpty(cPass)){
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, R.string.valid_data, Toast.LENGTH_SHORT).show();
            }else if(!email.matches(emailPattern)){
                reg_email.setError("Please enter valid email");
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, R.string.invalid_mail, Toast.LENGTH_SHORT).show();
            }else if(!pass.equals(cPass) ){
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, R.string.nmatch_pass, Toast.LENGTH_SHORT).show();
            }else if(pass.length()<6){
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, R.string.invalid_pass, Toast.LENGTH_SHORT).show();
            }else{
                auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            registered.start();
                            progressDialog.dismiss();

                            DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
                            StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

                            if(imageUri!=null){
                                storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if(task.isSuccessful()){

                                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                   imageURI =  uri.toString();
                                                   Users users = new Users(auth.getUid(),name,email,imageURI,status);
                                                   reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           if(task.isSuccessful()){
                                                               startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
                                                           }else{
                                                               Toast.makeText(RegisterActivity.this,R.string.err_user , Toast.LENGTH_SHORT).show();
                                                           }
                                                       }
                                                   });
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            //SI EL USUARIO DECIDE NO PONER NADA SUYO, SE PONDRÁN ESTOS PARAMETROS POR DEFECTO
                            else{
                                String status= "Hey There I'm using WormHole";
                                imageURI =  "https://firebasestorage.googleapis.com/v0/b/wormhole-fae6c.appspot.com/o/profile.png?alt=media&token=8baf98b1-7a34-4e11-ba14-46107ed7f391";
                                Users users = new Users(auth.getUid(),name,email,imageURI,status);
                                reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
                                        }else{
                                            Toast.makeText(RegisterActivity.this,R.string.err_user , Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, R.string.wrong, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
    });


    profile_img.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            //OBSOLETO PERO FUNCIONAL
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        }
    });


    txt_signIn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
        }
    });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10){
            if(data!=null){
                imageUri = data.getData();
                profile_img.setImageURI(imageUri);
            }
        }
    }
}