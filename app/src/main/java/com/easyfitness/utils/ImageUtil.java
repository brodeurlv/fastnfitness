package com.easyfitness.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.easyfitness.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtil {

    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_PICK_GALERY_PHOTO = 2;
    public static final int REQUEST_DELETE_IMAGE = 3;
    private String mFilePath = null;
    private ImageView imgView = null;
    private Fragment fragment = null;
    private ImageUtil.OnDeleteImageListener mDeleteImageListener;
    private OnPictureTakenListener mPicTakenListener;

    private static final String THUMB_PATH = "/thumb/";
    private ActivityResultLauncher<CropImageContractOptions> cropImage;

    public ImageUtil(Fragment fragment) {
        this.fragment = fragment;
        cropImage = fragment.registerForActivityResult(new CropImageContract(), (result -> {
            if (result.isSuccessful()) {
                if (mPicTakenListener != null) {
                    mPicTakenListener.onPictureTaken(result.getUriFilePath(fragment.getContext(), true));
                }
            } else {
                Log.e(getClass().getName(), "Failed to get an image", result.getError());
            }
        }));
    }

    public ImageUtil(Fragment fragment, ImageView view) {
        this(fragment);
        imgView = view;
    }

    static public String saveThumb(String pPath) {
        if (pPath == null || pPath.isEmpty()) return null;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pPath, bmOptions);
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        float scaleFactor = 1;
        if (photoH != 0) {
            scaleFactor = photoW / photoH; //Math.min(photoW/targetW, photoH/targetH);
        }

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int) scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
        Bitmap orientedBitmap = ExifUtil.rotateBitmap(pPath, bitmap);
        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(orientedBitmap, 128, (int) (128 / scaleFactor));

        // extract path without the .jpg
        String nameOfOutputImage = pPath.substring(pPath.lastIndexOf('/') + 1, pPath.lastIndexOf('.'));
        String pathOfOutputFolder = pPath.substring(0, pPath.lastIndexOf('/'));
        File pathThumbFolder = new File(pathOfOutputFolder + THUMB_PATH);
        if (!pathThumbFolder.exists()) {
            pathThumbFolder.mkdirs();
        }
        String pathOfThumbImage = pathOfOutputFolder + THUMB_PATH + nameOfOutputImage + "_TH.jpg";

        try {
            FileOutputStream out = new FileOutputStream(pathOfThumbImage);
            thumbImage.compress(Bitmap.CompressFormat.JPEG, 80, out);
        } catch (Exception e) {
            Log.e("Image", e.getMessage(), e);
        }

        return pathOfThumbImage;
    }

    static public boolean setPic(ImageView mImageView, String pPath) {
        try {
            if (pPath == null) return false;
            File f = new File(pPath);
            if (!f.exists() || f.isDirectory()) return false;

            // Get the dimensions of the View
            int targetW = mImageView.getWidth();
            if (targetW == 0) targetW = mImageView.getMeasuredWidth();
            int targetH = mImageView.getHeight();
            if (targetH == 0) targetH = mImageView.getMeasuredHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(pPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = 1;
            if (targetW != 0) {
                scaleFactor = photoW / targetW; //Math.min(photoW/targetW, photoH/targetH);
            }

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
            Bitmap orientedBitmap = ExifUtil.rotateBitmap(pPath, bitmap);
            mImageView.setImageBitmap(orientedBitmap);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void copyFileToStream(File internalFile, OutputStream zipFile) {
        try (InputStream inputStream = new FileInputStream(internalFile)) {
            copyStream(inputStream, zipFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ImageView getView() {
        return imgView;
    }

    public void setView(ImageView view) {
        imgView = view;
    }

    /**
     * Return path to the thumb of a picture if it is not already a thumb.
     * If necessary, it creates the thumb file.
     *
     * @param pPath Path to the full size picture
     * @return path to the thumb file
     */
    public static String getThumbPath(String pPath) {
        try {
            if (pPath == null || pPath.isEmpty()) return null;
            File originalFile = new File(pPath);
            if (!originalFile.exists()) return null;

            // extract path without the .jpg
            String nameOfOutputImage = pPath.substring(pPath.lastIndexOf('/') + 1, pPath.lastIndexOf('.'));
            String pathOfOutputFolder = pPath.substring(0, pPath.lastIndexOf('/'));

            // If it is already a thumb do nothing
            if (nameOfOutputImage.endsWith("_TH")) {
                return pPath;
                // else check if it already exists
            } else {
                // extract path without the .jpg
                String pathOfThumbImage = pathOfOutputFolder + THUMB_PATH + nameOfOutputImage + "_TH.jpg";
                File f = new File(pathOfThumbImage);
                if (!f.exists())
                    return saveThumb(pPath); // create thumb file
                else {
                    return pathOfThumbImage;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setOnDeleteImageListener(ImageUtil.OnDeleteImageListener listener) {
        mDeleteImageListener = listener;
    }

    public void setOnPicTakenListener(OnPictureTakenListener mPicTakenListner) {
        this.mPicTakenListener = mPicTakenListner;
    }

    public void createPhotoSourceDialog() {
        String[] optionListArray = new String[3];
        optionListArray[0] = fragment.getResources().getString(R.string.camera);
        optionListArray[1] = fragment.getResources().getString(R.string.gallery);
        optionListArray[2] = fragment.getResources().getString(R.string.remove_image);

        requestPermissionForWriting(fragment);

        AlertDialog.Builder itemActionBuilder = new AlertDialog.Builder(fragment.getActivity());
        itemActionBuilder.setTitle("").setItems(optionListArray, (dialog, which) -> {
            ListView lv = ((AlertDialog) dialog).getListView();

            switch (which) {
                // Camera
                case 0:
                    CropImageOptions cameraOptions = new CropImageOptions();
                    cameraOptions.imageSourceIncludeGallery = false;
                    cameraOptions.imageSourceIncludeCamera = true;
                    CropImageContractOptions camOptions = new CropImageContractOptions(null, cameraOptions);
                    cropImage.launch(camOptions);
                    break;
                // Galery
                case 1:
                    CropImageOptions galeryOptions = new CropImageOptions();
                    galeryOptions.imageSourceIncludeGallery = true;
                    galeryOptions.imageSourceIncludeCamera = false;
                    CropImageContractOptions galOptions = new CropImageContractOptions(null, galeryOptions);
                    cropImage.launch(galOptions);
                    break;
                case 2: // Delete picture
                    if (mDeleteImageListener != null)
                        mDeleteImageListener.onDeleteImage(ImageUtil.this);
                    break;
                // Camera
                default:
            }
        });
        itemActionBuilder.show();
    }

    private File createImageFile(Fragment fragment) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = fragment.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private Uri createMediaStoreImage(Fragment fragment) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        ContentResolver resolver = fragment.getContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FastNFitness/Profile");

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        Uri file = resolver.insert(collection, contentValues);
        return file;
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

    private void requestPermissionForWriting(Fragment pF) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(pF.getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(pF.getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
            ActivityCompat.requestPermissions(pF.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    static public File moveFile(File file, File dir) throws IOException {
        return copyFile(file, dir, "", true);
    }

    static public File moveFile(File file, File dir, String newFileName) throws IOException {
        return copyFile(file, dir, newFileName, true);
    }

    static public File copyFile(File file, File dir) throws IOException {
        return copyFile(file, dir, "", false);
    }

    static public File copyFile(File file, File dir, String newFileName) throws IOException {
        return copyFile(file, dir, newFileName, false);
    }

    static public File copyFile(File file, File dir, String newFileName, boolean moveFile) throws IOException {
        File newFile = null;
        if (newFileName.isEmpty())
            newFile = new File(dir, file.getName());
        else
            newFile = new File(dir, newFileName);

        try (FileChannel outputChannel = new FileOutputStream(newFile).getChannel(); FileChannel inputChannel = new FileInputStream(file).getChannel()) {
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            if (moveFile) file.delete();
        }

        return newFile;
    }

    static public File copyFileFromStream(InputStream inputStream, File destFolder, String newFileName) {
        File newFile = null;

        try {
            // create directory if it doesn't exists
            destFolder.mkdirs();

            newFile = new File(destFolder, newFileName);

            try (OutputStream outputStream = new FileOutputStream(newFile)) {
                copyStream(inputStream, outputStream);
            }

        } catch (Exception e) {
            Log.e("TAG", "Exception occurred " + e.getMessage());
        }
        return newFile;
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
    }

    static public File copyFileFromUri(Context context, Uri fileUri, File destFolder, String newFileName) {
        FileInputStream inputStream = null;
        File newFile = null;

        try {
            ContentResolver content = context.getContentResolver();
            inputStream = (FileInputStream) content.openInputStream(fileUri);

            newFile = copyFileFromStream(inputStream, destFolder, newFileName);
        } catch (Exception e) {
            Log.e("TAG", "Exception occurred " + e.getMessage());
        } finally {

        }
        return newFile;
    }

    public interface OnDeleteImageListener {
        void onDeleteImage(ImageUtil imgUtil);
    }

    public interface OnPictureTakenListener {
        void onPictureTaken(String uriFilePath);
    }
}
