package com.example.renya.myapplication_ui;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Renya on 2016-11-09.
 */
public class ImageListActivity extends AppCompatActivity {


    TextView textTargetUri, textTargetPath1, textTargetPath2;
    ImageView imageViewfromCam;
    final static int SELECT_IMAGE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menulist1);

        Button buttonLoadImage = (Button)findViewById(R.id.loadimage);
        textTargetUri = (TextView)findViewById(R.id.targeturi);
        textTargetPath1 = (TextView)findViewById(R.id.targetpath1);
        textTargetPath2 = (TextView)findViewById(R.id.targetpath2);
        imageViewfromCam = (ImageView)findViewById(R.id.imageView);

        buttonLoadImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_IMAGE);
            }});
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            Uri targetUri = data.getData();
            textTargetUri.setText("Uri: " + targetUri.toString());
            textTargetPath1.setText("path: " + getPathFromUri_managedQuery(targetUri));
            textTargetPath2.setText("path: " + getPathFromUri_CursorLoader(targetUri));


            //이미지를 비트맵으로 가져오기
            Bitmap bitmap=null;
            if (requestCode == SELECT_IMAGE){
                Uri image = data.getData();
                try {
                    ExifInterface exif = new ExifInterface(getPathFromUri_CursorLoader(targetUri));
                    int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int exifDegree = exifOrientationToDegrees(exifOrientation);
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),image);
                    bitmap= rotate(bitmap,exifDegree);
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }


            imageViewfromCam.setImageBitmap(bitmap);
        }
    }

    //using deprecated managedQuery() method
    private String getPathFromUri_managedQuery(Uri uri){
        String [] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = managedQuery(
                uri,
                projection,
                null,   //selection
                null,   //selectionArgs
                null   //sortOrder
        );

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    //using CursorLoader() method for API level 11 or higher
    private String getPathFromUri_CursorLoader(Uri uri){

        String [] projection = {MediaStore.Images.Media.DATA};

        CursorLoader cursorLoader = new CursorLoader(
                getApplicationContext(),
                uri,
                projection,
                null,   //selection
                null,   //selectionArgs
                null   //sortOrder
        );

        Cursor cursor = cursorLoader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    public static Bitmap rotate(Bitmap bitmap, int degrees)
    {
        if(degrees != 0 && bitmap != null)
        {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try
            {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted)
                {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex)
            {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    public static int exifOrientationToDegrees(int exifOrientation)
    {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            return 90;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            return 180;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            return 270;
        }
        return 0;
    }
}