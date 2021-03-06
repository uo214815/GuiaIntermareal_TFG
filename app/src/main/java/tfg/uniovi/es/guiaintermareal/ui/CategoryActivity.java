package tfg.uniovi.es.guiaintermareal.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rd.PageIndicatorView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import tfg.uniovi.es.guiaintermareal.R;
import tfg.uniovi.es.guiaintermareal.adapter.CarouselAdapter;

import static tfg.uniovi.es.guiaintermareal.MainActivity.mCategoryTitle;

public class CategoryActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static String TOOLBAR_ACTION_TYPE = "";

    private ProgressDialog mProgressDialog;
    public StorageReference mStorage;
    Uri picUri;

    ViewPager viewPager;
    CarouselAdapter mCarouselAdapter;

    String nombre, description, imageUrl, taxonomy, ecology, habitat, size;
    ArrayList<String> references;
    private FABToolbarLayout morph;
    TextView vTitle, vDescription, vEcology, vTaxonomy, vHabitat, vSize, vReferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("title"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ArrayList<String> carousel = getIntent().getStringArrayListExtra("carousel");

        mStorage = FirebaseStorage.getInstance().getReference();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        mCarouselAdapter = new CarouselAdapter(CategoryActivity.this, carousel);
        viewPager.setAdapter(mCarouselAdapter);

        PageIndicatorView pageIndicatorView = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(viewPager);

        mProgressDialog = new ProgressDialog(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        morph = (FABToolbarLayout) findViewById(R.id.fabtoolbar);

        View gallery, camera, exit;
        gallery = findViewById(R.id.gallery);
        camera = findViewById(R.id.camera);
        //map = findViewById(R.id.map);
        exit = findViewById(R.id.exit);

        fab.setOnClickListener(this);
        gallery.setOnClickListener(this);
        camera.setOnClickListener(this);
        //map.setOnClickListener(this);
        exit.setOnClickListener(this);

        nombre = getIntent().getStringExtra("title");
        description = getIntent().getStringExtra("description");
        ecology = getIntent().getStringExtra("ecology");
        imageUrl = getIntent().getStringExtra("image");
        taxonomy = getIntent().getStringExtra("taxonomy");
        habitat = getIntent().getStringExtra("habitat");
        size = getIntent().getStringExtra("size");
        references = getIntent().getStringArrayListExtra("references");

        vTitle = (TextView) findViewById(R.id.vTitle);
        vDescription = (TextView) findViewById(R.id.vDescription);
        vEcology = (TextView) findViewById(R.id.vEcology);
        vTaxonomy = (TextView) findViewById(R.id.vTaxonomy);
        vHabitat = (TextView) findViewById(R.id.vHabitat);
        vSize = (TextView) findViewById(R.id.vSize);
        vReferences = (TextView) findViewById(R.id.vReferences);

        vTitle.setText(nombre);
        vDescription.setText(description);
        vEcology.setText(ecology);
        vTaxonomy.setText(taxonomy);
        vHabitat.setText(habitat);
        vSize.setText(size);

        StringBuilder linksList = new StringBuilder();
        for(int i=0; i<references.size();i++){
            linksList.append(references.get(i));
            linksList.append("\n");
        }
        vReferences.setText(linksList);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
            onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v){
        Intent intent;
        switch (v.getId()) {
            case R.id.fab:
                if((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
                        morph.show();
                }else{
                    Toast.makeText(getApplicationContext(), "Es necesario conceder los permisos para usar el botón!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.camera:
                intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                TOOLBAR_ACTION_TYPE = "camera";
                File file=getOutputMediaFile(1);
                picUri = Uri.fromFile(file); // create
                intent.putExtra(MediaStore.EXTRA_OUTPUT,picUri); // set the image file
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                break;

            case R.id.gallery:
                intent = new Intent(Intent.ACTION_PICK);
                TOOLBAR_ACTION_TYPE = "gallery";
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                break;

            /*case R.id.map:
                intent = new Intent(CategoryActivity.this, MapsActivity.class);

                intent.putExtra("ref", mCategoryRef);
                intent.putExtra("title", getIntent().getStringExtra("title"));
                startActivity(intent);
                break;*/

            case R.id.exit:
                break;
        }
        morph.hide();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (networkConnected(getApplicationContext())) {
            if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                mProgressDialog.setMessage("Subiendo archivo...");
                mProgressDialog.show();
                Uri uri;
                if(TOOLBAR_ACTION_TYPE == "gallery") {
                    //La imagen se obtiene de la galeria
                    uri = data.getData();
                    StorageReference filepath = mStorage.child("A confirmar").child(mCategoryTitle).child(nombre).child(uri.getLastPathSegment());
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CategoryActivity.this, "Imagen subida con exito!", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CategoryActivity.this, "Ha habido un fallo al subir la imagen!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{
                    //La imagen se obtiene de la camara
                    uri = picUri;
                    StorageReference filepath = mStorage.child("A confirmar").child(mCategoryTitle).child(nombre).child(uri.getLastPathSegment());
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(CategoryActivity.this, "Imagen subida con exito!", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CategoryActivity.this, "Ha habido un fallo al subir la imagen!!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }else{
            mProgressDialog.dismiss();
            Toast.makeText(CategoryActivity.this, "Es necesario tener conexion a Internet para subir la foto!!", Toast.LENGTH_LONG).show();
        }

    }

    public static boolean networkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwi = cm.getActiveNetworkInfo();
        return nwi != null && nwi.isConnectedOrConnecting();
    }

    /** Create a File for saving an image */
    private  File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Intermareal");

        /**Create the storage directory if it does not exist*/
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        /**Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".png");
        } else {
            return null;
        }

        return mediaFile;
    }

}
