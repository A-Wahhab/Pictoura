package com.example.pictoura;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class mainpage extends AppCompatActivity {

    private StorageReference storageref;
    final int permissioncode = 999;
    final int CAMERA_REQUEST = 555;
    final int GALLERY_REQUEST = 666;

    Uri imageUri;
    LinearLayout ll;
    String user;
    String imageurl;
    ArrayList<String> list;
    Bitmap bmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        if (ActivityCompat.checkSelfPermission(mainpage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainpage.this, new String[]{Manifest.permission.CAMERA}, permissioncode);
        }
        if (ActivityCompat.checkSelfPermission(mainpage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainpage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissioncode);
        }

        list = new ArrayList<>();
        FloatingActionButton cameraButton = findViewById(R.id.btn_camera);
        FloatingActionButton galleryButton = findViewById(R.id.btn_gallery);
        ll = findViewById(R.id.linearL);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            user=bundle.getString("username");
        }


        DatabaseReference dref = FirebaseDatabase
                .getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Posts");

        dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ll.removeAllViews();
                for(DataSnapshot ds :snapshot.getChildren())
                {
                    String imagepath = Objects.requireNonNull(ds.child("path").getValue()).toString();
                    String nooflike = Objects.requireNonNull(ds.child("likes").getValue()).toString();
                    String username = Objects.requireNonNull(ds.child("username").getValue()).toString();
                    String comment = Objects.requireNonNull(ds.child("comments").getValue()).toString();
                    String postid = Objects.requireNonNull(ds.child("ID").getValue()).toString();
                    Log.d("DATA COMMENT FIREBASE", comment);
                    Log.d("DATA PATH FROM FIREBASE", imagepath);

                    setimageinView(imagepath,nooflike,username,comment,postid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "database failed", Toast.LENGTH_LONG).show();
            }
        });

        ActivityResultLauncher<Intent> handleCameraActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        uploadimage();

                    }
                });
        ActivityResultLauncher<Intent> handleGalleryActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent i  = result.getData();
                        assert i != null;
                        if (i.getData() != null) {
                            imageUri = i.getData();
                            uploadimage();
                        }
                    }
                });



        cameraButton.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            handleCameraActivity.launch(intent);
        });

        galleryButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            handleGalleryActivity.launch(intent);
        });


    }

    @SuppressLint({"SetTextI18n", "RtlHardcoded"})
    void setimageinView(String imagepath, String nooflike, String username, String comment, String pid)
    {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading....");
        pd.show();
        storageref = FirebaseStorage.getInstance().getReferenceFromUrl(imagepath);
        ImageView imageView = new ImageView(mainpage.this);
        TextView textView = new TextView(mainpage.this);
        TextView tv = new TextView(mainpage.this);

        TextView ctv = new TextView(mainpage.this);
        EditText ct = new EditText(mainpage.this);
        Button cbt = new Button(mainpage.this);
        try {
            Log.d("****Comments", comment);
            JSONArray ja = new JSONArray(comment);
            final File localfile = File.createTempFile("temp", ".jpg");
            storageref.getFile(localfile).addOnCompleteListener(task -> {
                //   Toast.makeText(getApplicationContext(), "GOT THE IMAGE FROM FIREBASE", Toast.LENGTH_SHORT).show();
                bmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                imageView.setImageBitmap(bmap);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(800, 800);
                imageView.setLayoutParams(layoutParams);
                imageView.getAdjustViewBounds();
                ll.setGravity(Gravity.CENTER);
                ll.addView(imageView);
                textView.setText("\t" + username);
                textView.setText(username + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t Likes: " +nooflike);
                textView.setTextSize(20);
                textView.setTextColor(WHITE);
                textView.setGravity(Gravity.LEFT);
                textView.setWidth(layoutParams.rightMargin);
                ll.addView(textView);
                tv.setText("LIKE: ");
                tv.isClickable();
                tv.setOnClickListener(v -> updatelike(pid,nooflike));
                tv.setTextSize(10);
                tv.setTextColor(WHITE);
                ll.addView(tv);
                if (ja.length() > 0) {
                    ctv.setText("Comments: ");
                    ctv.setGravity(Gravity.CENTER);
                    ctv.setTextColor(BLACK);
                    ctv.setAllCaps(true);
                    ctv.setTextSize(12);
                    ctv.setTextColor(WHITE);
                    ll.addView(ctv);
                    for(int i=0;i<ja.length();i++)
                    {
                        TextView c = new TextView(mainpage.this);
                        try {

                            JSONObject j = ja.getJSONObject(i);
                            String content = j.getString("content");
                            String u = j.getString("username");
                            Log.d("*******content", content);
                            c.setText(u+":"+content);
                            c.setTextSize(10);
                            c.setTextColor(WHITE);
                            ll.addView(c);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                ct.setHint("Add new Comments...");
                ct.setTextSize(12);
                ct.setTextColor(WHITE);
                ll.addView(ct);
                if (!user.equals(username)) {

                    Button follow = new Button(mainpage.this);
                    follow.setText("Follow");
                    follow.setTextSize(12);
                    follow.setBackgroundColor(Color.parseColor("#27F0ECEC"));
                    follow.setHighlightColor(Color.parseColor("#008080"));
                    follow.setOnClickListener(v -> updateFollowers(follow,username));
                    ll.addView(follow);
                }
                cbt.setTextColor(WHITE);
                cbt.setText("Post Comment");
                cbt.setTextSize(12);
                cbt.setTextColor(WHITE);
                cbt.setBackgroundColor(Color.parseColor("#27F0ECEC"));
                cbt.setHighlightColor(Color.parseColor("#008080"));

                ll.addView(cbt);
                cbt.setOnClickListener(v -> postcomment(pid,ct.getText().toString(),ja));
                pd.dismiss();
            }).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "FAILED ME", Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            pd.dismiss();
            e.printStackTrace();
        }

    }

    private void updateFollowers(Button follow, String followUser){
        follow.setEnabled(false);
        DatabaseReference dref = FirebaseDatabase
                .getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users");

        // Updating Followers of followUser
        dref.child(followUser).child("followers").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else {
                Log.d("firebase", String.valueOf(Objects.requireNonNull(task.getResult()).getValue()));
                try {
                    JSONArray Followers = new JSONArray(String.valueOf(task.getResult().getValue()));
                    Followers.put(user);
                    dref.child(followUser).child("followers").setValue(Followers.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void updatelike(String pid, String nooflike) {
        DatabaseReference dref = FirebaseDatabase
                .getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Posts");
        int count = Integer.parseInt(nooflike);
        count = count+1;
        String newlike = String.valueOf(count);
        dref.child(pid).child("likes").setValue(newlike);
    }

    private void postcomment(String pid, String s, JSONArray ja) {
        DatabaseReference dref = FirebaseDatabase
                .getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Posts");

        JSONObject j = new JSONObject();
        try {
            j.put("content", s);
            j.put("username", user);
            ja.put(j);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dref.child(pid).child("comments").setValue(ja.toString());

    }

    void uploadimage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading....");
        pd.show();
        if(imageUri!=null)
        {
            storageref = FirebaseStorage.getInstance().getReference("images")
                    .child(System.currentTimeMillis()+"."+filextension(imageUri));
            UploadTask uploadtask = storageref.putFile(imageUri);

            uploadtask.continueWithTask(task -> storageref.getDownloadUrl()).addOnCompleteListener(task -> {
                Uri imagelink = task.getResult();
                assert imagelink != null;
                imageurl = imagelink.toString();

                DatabaseReference dref = FirebaseDatabase.getInstance("https://pictoura-f4ae4-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("Posts");
                String postid = dref.push().getKey();
                HashMap<String, Object> list = new HashMap<>();
                ArrayList<String> comments = new ArrayList<>();
                list.put("comments", comments.toString());
                list.put("ID", postid);
                list.put("username", user);
                list.put("likes","0");
                list.put("path", imageurl);


                assert postid != null;
                dref.child(postid).setValue(list);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getMenuInflater();
        inf.inflate(R.menu.profile_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profilepage) {
            Intent i = new Intent(mainpage.this, profilepage.class);
            i.putExtra("username", user);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
        super.onActivityResult(requestCode, resultCode, i);
        if (requestCode == CAMERA_REQUEST) {
            uploadimage();
        }
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && null != i) {
            if (i.getData() != null) {
                imageUri = i.getData();
                uploadimage();
            }
        }
    }
}
