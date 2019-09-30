package com.lhc.example.utils;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lhc
 */
public class SelectPhotoUtils {

    private final static String ALBUM_NAME = "BotsItOa";
    public final static int REQUEST_CODE_TAKE_PHOTO = 1;
    public final static int REQUEST_CODE_ALBUM_PHOTO = 2;

    private static Uri photoUri;

    public static void takePhoto(AppCompatActivity activity) {
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoUri = getMediaFileUri(activity);
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        activity.startActivityForResult(takeIntent, REQUEST_CODE_TAKE_PHOTO);
    }

    public static void openAlbum(AppCompatActivity activity) {
        Intent albumIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(albumIntent, REQUEST_CODE_ALBUM_PHOTO);
    }

    static public Uri getMediaFileUri(AppCompatActivity activity){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        if (Build.VERSION.SDK_INT >= 24) {
            String authority = activity.getPackageName()+".fileprovider";
            return FileProvider.getUriForFile(activity, authority, mediaFile);
        } else {//com.lhc.webviewjsbridge_android.fileprovider
            return Uri.fromFile(mediaFile);
        }
    }

    public static Bitmap getTaksPhoto(Intent intent, AppCompatActivity activity) {
        Bitmap bitmap = null;
        if (intent != null) {
            if (intent.hasExtra("data")) {
                bitmap = intent.getParcelableExtra("data");
            }
        } else {
            if (Build.VERSION.SDK_INT >= 24){
                try {
                    bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(photoUri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else {
                bitmap = BitmapFactory.decodeFile(photoUri.getPath());
            }
        }
        return bitmap;
    }

    public static Bitmap getAlbumPhoto(Intent intent, AppCompatActivity activity) {
        if (intent == null || activity == null) {
            return null;
        }
        Uri selectImageUri = intent.getData();
        Bitmap bitmap = null;
        if (selectImageUri != null) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = activity.getContentResolver().query(selectImageUri, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                bitmap = BitmapFactory.decodeFile(picturePath);
            }
        }
        return bitmap;
    }


    public static String bitmapToBase64(Bitmap bitmap) {
        String base64 = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                byte[] bitmapBytes = baos.toByteArray();
                base64 = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return base64;
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
