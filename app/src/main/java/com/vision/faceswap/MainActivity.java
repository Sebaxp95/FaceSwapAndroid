package com.vision.faceswap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ImageButton imButton1;
    private ImageButton imButton2;
    private String mCurrentPhotoPath;
    private File fileSrc;
    private File fileDst;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);

        imButton1 = findViewById(R.id.imageButton);
        imButton2 = findViewById(R.id.imageButton2);

        imButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(1);
            }
        });
        imButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(2);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(mCurrentPhotoPath == null) {
            mCurrentPhotoPath = Objects.requireNonNull(data.getData()).toString().replace("content://com.miui.gallery.open/raw", "file:");
        }
        Bitmap mImageBitmap;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
            if (mImageBitmap != null) {


                ExifInterface ei = new ExifInterface(mCurrentPhotoPath.replace("file:", ""));
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        mImageBitmap = rotateImage(mImageBitmap, 90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:
                        mImageBitmap = rotateImage(mImageBitmap, 180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:
                        mImageBitmap = rotateImage(mImageBitmap, 270);
                        break;
                }

                //mImageBitmap = rotateImage(mImageBitmap, 270);
                int imageHeight = mImageBitmap.getHeight();
                int imageWidth = mImageBitmap.getWidth();
                int scaledHeight = imButton1.getHeight() - 30;
                int scale = imageHeight / scaledHeight;
                int scaledWidth = imageWidth / scale;

                mImageBitmap = Bitmap.createScaledBitmap(mImageBitmap, scaledWidth, scaledHeight, true);
                mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);

                if (requestCode == 1) {
                    imButton1.setImageBitmap(mImageBitmap);

                    FileOutputStream fileOutputStream = new FileOutputStream(fileSrc);
                    fileOutputStream.write(byteArrayOutputStream.toByteArray());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    System.out.println(fileSrc.length());

                } else if (requestCode == 2) {
                    imButton2.setImageBitmap(mImageBitmap);
                    FileOutputStream fileOutputStream = new FileOutputStream(fileDst);
                    fileOutputStream.write(byteArrayOutputStream.toByteArray());
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    System.out.println(fileDst.length());

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (fileSrc != null && fileDst != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        File f = new FileSender().send(fileSrc, fileDst, getApplicationContext());
                        showResult(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void showResult(File imgFile){
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        ImageView myImage = (ImageView) findViewById(R.id.result);
        myImage.setImageBitmap(myBitmap);
    }


    // Select image from camera and gallery
    private void selectImage(final int request) {
        try {
            StrictMode.VmPolicy.Builder policyBuilder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(policyBuilder.build());
            final CharSequence[] options = {"Take Photo",/* "Choose From Gallery",*/ "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Option");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals("Take Photo")) {
                        dialog.dismiss();
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = createImageFile(request);
                            } catch (IOException ex) {
                                System.out.println(ex.getMessage());
                            }
                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                startActivityForResult(cameraIntent, request);
                            }
                        }
                   /* } else if (options[item].equals("Choose From Gallery")) {
                        dialog.dismiss();
                        mCurrentPhotoPath = null;
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, request);*/
                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private File createImageFile(int num) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        if (num == 1) {
            fileSrc = image;
        }
        if (num == 2) {
            fileDst = image;
        }
        return image;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
