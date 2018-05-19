package com.easyfitness.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.easyfitness.BuildConfig;
import com.easyfitness.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ccombes on 18/04/14.
 */

public class ImageUtil {

    static public void setThumb(ImageView mImageView, String pPath) {
        try {
            if (pPath == null) return;
            File f = new File(pPath);
            if(!f.exists() || f.isDirectory()) return; // TODO Si le fichier n'existe pas, pourrait verifier si le fichier source existe et creer la miniature.

            // Get the dimensions of the View
            float targetW = 128;//mImageView.getWidth();
            //float targetH = mImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pPath, bmOptions);
            float photoW = bmOptions.outWidth;
            float photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = (int)(photoW/targetW); //Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
            mImageView.setImageBitmap(bitmap);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public void saveThumb(String pPath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pPath, bmOptions);
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        float scaleFactor = photoW/photoH; //Math.min(photoW/targetW, photoH/targetH);

        Bitmap ThumbImage =null;
        //if (photoW < photoH)
        ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(pPath), 128, (int)(128/scaleFactor));
        //else
        //	ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(pPath), (int)(96/scaleFactor), 96);

        // extract path without the .jpg
        String pathOfOutputImage = "";
        pathOfOutputImage = pPath.substring(0, pPath.lastIndexOf('.')) + "_TH.jpg";

        try
        {
            FileOutputStream out = new FileOutputStream(pathOfOutputImage);
            ThumbImage.compress(Bitmap.CompressFormat.JPEG, 80, out);
        }
        catch (Exception e)
        {
            Log.e("Image", e.getMessage(), e);
        }
    }


    Fragment mF=null;
    String mFilePath = null;

    public String getFilePath() {
        return mFilePath;
    }

    public boolean CreatePhotoSourceDialog(Fragment pF) {

        mF = pF;

        String[] profilListArray = new String[2];
        profilListArray[0] = mF.getResources().getString(R.string.camera);
        profilListArray[1] = mF.getResources().getString(R.string.gallery);

        requestPermissionForWriting(pF);

        AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(mF.getActivity());
        itemActionbuilder.setTitle("").setItems(profilListArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ListView lv = ((AlertDialog)dialog).getListView();

                switch (which) {
                    // Galery
                    case 1 :
                        getGaleryPict(mF);
                        break;
                    // Camera
                    case 0:
                        dispatchTakePictureIntent(mF);
                        break;
                    default:
                }
            }
        });
        itemActionbuilder.show();

        return true;
    }

    static public void setPic(ImageView mImageView, String pPath) {
        try {
            if (pPath == null) return;
            File f = new File(pPath);
            if(!f.exists() || f.isDirectory()) return;

            // Get the dimensions of the View
            int targetW = mImageView.getWidth();
            int targetH = mImageView.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = photoW/targetW; //Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
            mImageView.setImageBitmap(bitmap);

            mImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            mImageView.setAdjustViewBounds(true);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_PICK_GALERY_PHOTO = 2;
    public static final int REQUEST_DELETE_IMAGE = 3;

    private void dispatchTakePictureIntent(Fragment pF) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mF.getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(pF);
                mFilePath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mF.getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                mF.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void getGaleryPict(Fragment pF) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        pF.startActivityForResult(photoPickerIntent, REQUEST_PICK_GALERY_PHOTO);
    }

    public void galleryAddPic(Fragment pF, String file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        pF.getActivity().sendBroadcast(mediaScanIntent);
    }


    private File createImageFile(Fragment pF) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = pF.getActivity().getExternalFilesDir(
                Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;

    private void requestPermissionForWriting(Fragment pF) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(pF.getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(pF.getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(pF.getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
}
