package com.imgprocesadondk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImgProcesadoNDK extends AppCompatActivity {
    private String tag = "ImgProcesadoNDK";
    private Bitmap bitmapOriginal = null;
    private Bitmap bitmapGrises = null;
    private Bitmap bitmapSepia = null;
    private Bitmap bitmapMarco_1 = null;
    private ImageView ivDisplay = null;
    private String mCurrentPhotoPath;
    private static int TAKE_PICTURE = 1;
    private static int SELECT_PICTURE = 2;


    static {
        System.loadLibrary("imgprocesadondk");
    }

    public native void convertirSobel(Bitmap bitmapIn, Bitmap bitmapOut);

    public native void convertirSepia(Bitmap bitmapIn, Bitmap bitmapOut);

    public native void creaMarco(Bitmap bitmapIn, Bitmap bitmapOut);

    public native void creaMarcoCallBack(Bitmap bitmapIn, Bitmap bitmapOut);

    public static boolean hayPixel(int x, int y) {
        return x % 10 == y % 10;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        setColor(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void setColor(Boolean imgOrigen) {
        if (imgOrigen) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmapOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.sampleimage, options);
            if (bitmapOriginal != null) ivDisplay.setImageBitmap(bitmapOriginal);
        }
        ivDisplay.setImageBitmap(bitmapOriginal);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_reset:
                setColor(true);
                break;

            case R.id.gray:
                bitmapGrises = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
                convertirSobel(bitmapOriginal, bitmapGrises);
                ivDisplay.setImageBitmap(bitmapGrises);
                break;
            case R.id.sepia:
                bitmapSepia = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
                convertirSepia(bitmapOriginal, bitmapSepia);
                ivDisplay.setImageBitmap(bitmapSepia);
                break;
            case R.id.marco_1:
                bitmapMarco_1 = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
                creaMarco(bitmapOriginal, bitmapMarco_1);
                ivDisplay.setImageBitmap(bitmapMarco_1);
                break;
            case R.id.marco_2:
                bitmapMarco_1 = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
                creaMarcoCallBack(bitmapOriginal, bitmapMarco_1);
                ivDisplay.setImageBitmap(bitmapMarco_1);
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    public void onTakePhoto(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        int code = TAKE_PICTURE;
        try {
            File fic = createImageFile();
            Uri output = Uri.fromFile(fic);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, output);
            startActivityForResult(takePictureIntent, code);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "No se ha podido capturar la fotografía de forma correcta " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
            if (data == null) {
                escalaImagen();
            }
        } else if (requestCode == SELECT_PICTURE) {
            Uri selectedImage = data.getData();
            InputStream is;
            try {
                is = getContentResolver().openInputStream(selectedImage);
                BufferedInputStream bis = new BufferedInputStream(is);
                Bitmap bitmap = BitmapFactory.decodeStream(bis);
                bitmapOriginal = bitmap;
                setColor(false);
            } catch (FileNotFoundException e) {
            }
        }
    }

    private void escalaImagen() {
        int targetW = ivDisplay.getWidth();
        int targetH = ivDisplay.getHeight();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(mCurrentPhotoPath), targetW, targetH, true);
        bitmapOriginal = resizedBitmap;
        ivDisplay.setImageBitmap(bitmapOriginal);
    }
}


