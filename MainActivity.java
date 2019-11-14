package com.example.camera;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int GALLERY_REQUEST_CODE = 2;
    String currentPhotoPath;
    ImageView imageView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Listener Class and assignment
        BtnListener listener = new BtnListener();
        findViewById(R.id.cameraBtn).setOnClickListener(listener);
        findViewById(R.id.galleryBtn).setOnClickListener(listener);

    }

    private class BtnListener implements OnClickListener {
        @Override
        //On-click event handler
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.cameraBtn:
                    //To camera
                    dispatchTakePictureIntent();
                    break;
                case R.id.galleryBtn:
                    pickFromGallery();
                    break;
            }
        }
    }

    //Used to save image to a particular file path; called in dispatchTakePictureIntent
    private File createImageFile() throws IOException {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //This is the directory in which the file will be created. This is the default
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /*prefix*/
                ".jpg",         /*suffix*/
                storageDir      /*directory*/
        );
        //save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        try{
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        } catch (IOException ex ) {
            ex.printStackTrace();
            Toast.makeText(MainActivity.this, "Image Failed to Save", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Result code is RESULT_OK only if the user selects an Image/Captures and image
        if(resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:
                    testPath();
                    testDisplay();
                    galleryAddPic();
                    Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
                    break;
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected image
                    Uri selectedImage = data.getData();

                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    //Get the Cursor
                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    //Move to first row
                    cursor.moveToFirst();
                    //Get the column index of MediaStore.Images.Media.DATA
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //Gets the string value in the column
                    currentPhotoPath = cursor.getString(columnIndex);

                    cursor.close();
                    //Set the Image in ImageView after decoding the String
                    testDisplay();
                    testPath();
                    Toast.makeText(MainActivity.this, "Image Saved", Toast.LENGTH_SHORT).show();
                    break;
            }
    }

    private void pickFromGallery() {
        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void testPath(){
        textView = this.findViewById(R.id.textView);
        textView.setText(currentPhotoPath);
    }

    private void testDisplay(){
        Bitmap bmImg = BitmapFactory.decodeFile(currentPhotoPath);
        imageView = this.findViewById(R.id.imageView);
        imageView.setImageBitmap(bmImg);
    }

}
