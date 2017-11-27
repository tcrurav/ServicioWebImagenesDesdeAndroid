package com.example.tiburcio.bajarsubirimagen;
/* Enlaces a mirar relacionados:
     1.) https://www.androidlearning.com/multipart-request-using-android-volley/
     2.) https://developer.android.com/reference/android/widget/ProgressBar.html
     3.) https://developer.android.com/guide/topics/permissions/index.html
*/

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.ImageRequest;
import com.android.volley.request.SimpleMultiPartRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class SeleccionarImagen extends AppCompatActivity {
    public static final String BASE_URL = "http://192.168.1.178:8080/MiSW/webresources";

    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3;
    AppCompatActivity estaActividad;

    String filePath;

    ImageView imagen;
    TextInputEditText nombreImagen;
    Button enviar;
    Button descargar;
    ProgressBar progressBar;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_imagen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageBrowse();
            }
        });

        estaActividad = this;
        progressBar = (ProgressBar) findViewById(R.id.progressBar_cyclic);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        imagen = (ImageView) findViewById(R.id.imagen);
        nombreImagen = (TextInputEditText) findViewById(R.id.nombreImagen);
        enviar = (Button) findViewById(R.id.enviar);

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filePath != null) {
                    pedirPermiso();
                } else {
                    Toast.makeText(getApplicationContext(), "Imagen no seleccionada", Toast.LENGTH_LONG).show();
                }
            }
        });
        descargar = (Button) findViewById(R.id.descargar);
        descargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageRequest();
            }
        });
    }

    private void mostrarProgreso() {
        linearLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void ocultarProgreso() {
        linearLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void imageBrowse() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                Uri picUri = data.getData();
                filePath = getPath(picUri);
                imagen.setImageURI(picUri);
            }
        }
    }

    // Get Path of selected image
    private String getPath(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void pedirPermiso(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(estaActividad,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(estaActividad,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                /*Log.i("tiburcio", "el should");*/
            } else {

                // No explanation needed, we can request the permission.

                /*Log.i("tiburcio", "el no should");*/

                ActivityCompat.requestPermissions(estaActividad,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        /*Log.i("tiburcio", "fin pedir permiso");*/
        imageUpload(filePath);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i("tiburcio", "permitido");
                    imageUpload(filePath);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    /*Log.i("tiburcio", "denegado");*/
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void imageUpload(final String imagePath) {

        mostrarProgreso();
        SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, BASE_URL + "/imagen/subir",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        /*Log.d("Response", response);*/
                        Toast.makeText(estaActividad, "Imagen enviada", Toast.LENGTH_LONG).show();
                        /*try {
                            JSONObject jObj = new JSONObject(response);
                            String message = jObj.getString("message");

                            Toast.makeText(estaActividad, "No ha habido error", Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(estaActividad, "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }*/
                        ocultarProgreso();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                ocultarProgreso();
            }
        });

        smr.addFile("fichero", imagePath);
        MyApplication.getInstance().addToRequestQueue(smr);
    }

    private void imageRequest(){

        mostrarProgreso();
        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(BASE_URL + "/imagen/descarga/" + nombreImagen.getText().toString(),
                null,null,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        imagen.setImageBitmap(bitmap);
                        ocultarProgreso();
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(estaActividad, "Some Thing Goes Wrong", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                        ocultarProgreso();
                    }
                });
        // Access the RequestQueue through your singleton class.
        MyApplication.getInstance().addToRequestQueue(request);
    }

}

