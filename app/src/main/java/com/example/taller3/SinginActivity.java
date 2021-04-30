package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class SinginActivity extends AppCompatActivity {

    private CircleImageView urlProfilePicture;
    private EditText name;
    private EditText lastname;
    private EditText email;
    private EditText password;
    private EditText cc;
    private Button signinButton;

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private String pickerPath;
    private double longitude;
    private double latitude;
    private LocationManager locationManager;
    public static final String TAG = "FB_APP";

    private static final int STORAGE_REQUEST = 33;
    private static final int IMAGE_PICKER_REQUEST = 44;
    private static final int REQUEST_IMAGE_CAPTURE = 55;
    private static final int CAMERA_PERMISSION = 66;
    private static final int LOCATION_CODE = 11;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singin);

        urlProfilePicture = findViewById(R.id.urlProfilePicture);
        name = findViewById(R.id.name);
        lastname = findViewById(R.id.lastname);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        cc = findViewById(R.id.cc);
        signinButton = findViewById(R.id.signinButton);

        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        longitude = 4.76697;
        latitude = -73.9665033;

        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        urlProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(SinginActivity.this);
                dialog.setTitle("Foto de Perfil");

                String[] items = {"Galeria", "Cámara"};
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                PermissionsManager.requestPermission(SinginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, "", STORAGE_REQUEST);
                                break;
                            case 1:
                                PermissionsManager.requestPermission(SinginActivity.this, Manifest.permission.CAMERA, "", CAMERA_PERMISSION);
                                break;
                        }
                    }
                });

                AlertDialog dialog1 = dialog.create();
                dialog1.show();
            }
        });

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String rName = name.getText().toString();
                final String rLastname = lastname.getText().toString();
                final String rEmail = email.getText().toString();
                final String rPassword = password.getText().toString();
                final String rCc = cc.getText().toString();

                mAuth.createUserWithEmailAndPassword(rEmail,rPassword)
                        .addOnCompleteListener(SinginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser fUser = mAuth.getCurrentUser();
                                    updateUI(fUser);
                                    User regis = new User(rName, rLastname, rEmail, rPassword, pickerPath, Long.parseLong(rCc), latitude, longitude);

                                    String folder = "users";
                                    FirebaseDatabase.getInstance().getReference(folder)
                                            .child(fUser.getUid())
                                            .setValue(regis).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Toast.makeText(SinginActivity.this, "Su Usuario ha sido Creado",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                // If sign in fails, display a message to the user.
                                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                                Toast.makeText(SinginActivity.this, "Su Registro Fallo",
                                                        Toast.LENGTH_SHORT).show();
                                                updateUI(null);
                                            }
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    private void takePicture() {
        Intent takepicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if( takepicture.resolveActivity(getPackageManager()) != null ) {
            startActivityForResult(takepicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void askForImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, IMAGE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_PICKER_REQUEST && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            storageReference = storage.getReference("profilePics");
            final StorageReference imageReference = storageReference.child(imageUri.getLastPathSegment());
            pickerPath = imageUri.getLastPathSegment();
            imageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    urlProfilePicture.setImageBitmap(selectedImage);
                }
            });

        }else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Context c = this;
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(c.getContentResolver(),bitmap, "Title", null);

            Uri photoURI = Uri.parse(path);
            storageReference = storage.getReference("profilePics");
            pickerPath = System.currentTimeMillis() + ".jpeg";
            final StorageReference imageReference = storageReference.child(pickerPath);
            imageReference.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    urlProfilePicture.setImageBitmap(bitmap);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case STORAGE_REQUEST:
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    askForImage();
                }
                return;
            case CAMERA_PERMISSION:
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                }
                return;
        }

    }

    private void updateUI (FirebaseUser user){
        if(user != null){
            startActivity(new Intent(this, HomeScreenMapsActivity.class));

        }else{
            email.setText("");
            password.setText("");
        }
    }
}