package com.example.pictoura;

import static android.graphics.Color.WHITE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;


public class profilepage extends AppCompatActivity {
    final int permissioncode = 999;
    final int CAMERA_REQUEST = 555;
    final int GALLERY_REQUEST = 666;

    Uri imageUri;
    String user;
    private StorageReference storageref;
    Bitmap bmap;
    String followers;
    Button camupload;
    String imageurl;
    String proimageurl;
    Button galupload;
    ImageView profpic;
    TextView cf;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);

        if (ActivityCompat.checkSelfPermission(profilepage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(profilepage.this, new String[]{Manifest.permission.CAMERA}, permissioncode);
        }
        if (ActivityCompat.checkSelfPermission(profilepage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(profilepage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissioncode);
        }

        camupload = findViewById(R.id.picbtn);
        galupload = findViewById(R.id.picbtn2);

        profpic = findViewById(R.id.profpic);

        ll=findViewById(R.id.view_followers);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            user=bundle.getString("username");
        }
        TextView tv = findViewById(R.id.tv_username);
        tv.setText(user);
        tv.setTextSize(34);
        tv.setTextColor(WHITE);
        DatabaseReference dref = FirebaseDatabase
                .getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/").
                getReference("Posts");


        Query checker = FirebaseDatabase
                .getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/").
                getReference().child("Users").orderByChild("username").equalTo(user);
        checker.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    followers = Objects.requireNonNull(snapshot.child(user).child("followers").getValue()).toString();
                    proimageurl = Objects.requireNonNull(snapshot.child(user).child("profilepath").getValue()).toString();
                    if(!proimageurl.equals(""))
                    {
                        setprofilepicture();
                    }
                    Log.d("*******FOLLOWER INFO",followers);
                    
                    try {
                        JSONArray Followers = new JSONArray(followers);
                        for (int i =0 ;i< Followers.length();i++) {
                            TextView Afollower = new TextView(profilepage.this);
                            Afollower.setText("\t\t"+Followers.getString(i));
                            Afollower.setTextColor(WHITE);
                            ll.addView(Afollower);
                        }
                        cf = findViewById(R.id.tv_follower);
                        cf.setText(Followers.length() + "");
                        cf.setTextColor(WHITE);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "User Invalid", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "database failed", Toast.LENGTH_LONG).show();
            }
        });
        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds :snapshot.getChildren())
                {
                    String imagepath;
                    String usercheck = ds.child("username").getValue(String.class);
                    assert usercheck != null;
                    if(usercheck.equals(user))
                    {
                        imagepath = Objects.requireNonNull(ds.child("path").getValue()).toString();
                        selectimages(imagepath);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "database failed", Toast.LENGTH_LONG).show();
            }
        });
        camupload.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, CAMERA_REQUEST);
        });
        galupload.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select My Picture"), GALLERY_REQUEST);
        });
    }

    private void setprofilepicture() {
        storageref = FirebaseStorage.getInstance().getReferenceFromUrl(proimageurl);
        try {
            final File localfile = File.createTempFile("temp", ".jpg");
            storageref.getFile(localfile).addOnCompleteListener(task -> {
                bmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                profpic.setImageBitmap(bmap);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
                profpic.setLayoutParams(layoutParams);
                profpic.getAdjustViewBounds();
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "FAILED ME", Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i)
    {
        super.onActivityResult(requestCode, resultCode, i);
        if(requestCode == CAMERA_REQUEST)
        {
            try {
                uploadprofilepicture();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && null != i)
        {
            if(i.getData() !=null)
            {
                imageUri = i.getData();
                try {
                      uploadprofilepicture();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private void uploadprofilepicture()
    {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading....");
        pd.show();
        if(imageUri!=null)
        {
            storageref = FirebaseStorage.getInstance().getReference("images").
                    child(System.currentTimeMillis()+"."+filextension(imageUri));
            UploadTask uploadtask = storageref.putFile(imageUri);
            uploadtask.continueWithTask((Continuation) task -> storageref.getDownloadUrl()).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                Uri imagelink = task.getResult();
                assert imagelink != null;
                imageurl = imagelink.toString();

                DatabaseReference dref = FirebaseDatabase
                        .getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("Users");
                dref.child(user).child("profilepath").setValue(imageurl);

                Log.d("********DOWNLOAD LINK", imageurl);
                Toast.makeText(getApplicationContext(), "uploading done", Toast.LENGTH_LONG).show();
                pd.dismiss();
            }).addOnFailureListener(e -> {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "uploading failed", Toast.LENGTH_LONG).show();
            });
        }
    }
    private String filextension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }
    private void selectimages(String imagepath) {

        storageref = FirebaseStorage.getInstance().getReferenceFromUrl(imagepath);
        ImageView imageView = new ImageView(this);
        try {
            final File localfile = File.createTempFile("temp", ".jpg");
            storageref.getFile(localfile).addOnCompleteListener(task -> {
                bmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                imageView.setImageBitmap(bmap);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(300, 200);
                imageView.setLayoutParams(layoutParams);
                imageView.getAdjustViewBounds();
                ll.addView(imageView);
                ll.setDividerPadding(10);

            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "FAILED ME", Toast.LENGTH_LONG).show());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}