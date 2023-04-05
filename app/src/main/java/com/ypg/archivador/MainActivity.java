package com.ypg.archivador;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentContainerView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ypg.archivador.frag.frag_values;
import com.ypg.archivador.utily.GetVersionCode;
import com.ypg.archivador.utily.Keyboard_y;
import com.ypg.archivador.utily.UtilsY;
import com.ypg.archivador.utily.modalGenerateQR;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    public static final int CREATE_NEW_FILE_CODE = 1;


    public static final int PERMISSION_REQUEST_CODE = 112; //Para los permisos:

    public  FragmentContainerView fragmenthost; //instancia del contenedor principal de fragment:
    public static ImageView imgCopy, imgPaste; //botones de la tools bar:
    public  static Boolean G_isinternet = false;

    private String G_QR_RRESULTCODE = "";

    public static MainActivity mainActivityThis; //Referencia a MainActivity


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //para evitar tomar capturas de pantallas:
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);


        mainActivityThis = this;

        //setup tools bar:
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_toolbar_ico);

        //Variables globales necesarias:
        UtilsY.G_context = getBaseContext();    //pasando el contexto de Activity para mostrar mensajes:
        UtilsY.G_view = toolbar;                //pasando una instancia de esta View para UtilsY para mostrar msg en snackbar:
        fragmenthost = (FragmentContainerView) findViewById(R.id.fragment_host);



        ImageView imgCloseBD    = (ImageView) findViewById(R.id.main_img_closebd);              // img en la tooll bar
        imgCopy                 = (ImageView) findViewById(R.id.main_img_copy);                 // img en la tooll bar
        imgPaste                = (ImageView) findViewById(R.id.main_img_paste);                // img en la tooll bar



        //recabando los permisos necesarios
           //pide solo el permiso si no se ha pedido y Aceptado anteriormente
        SharedPreferences sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
        String aa = sharedPreferences.getString("isPermisoAprobado","");
        if (aa.equals("") || aa.equals("no")){
            PedirPermisos();
        }


        //chequeando si existe una nueva versión publicada
        check_version();


       File file = new File(getFilesDir(), "bd");



        //evento del image close de la toolBar
        imgCloseBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            UtilsY.closeBD();
            }
        });

        //evento del image copy de la toolBar
        imgCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag_values.copyValue();
            }
        });

        //evento del image paste de la toolBar
        imgPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag_values.pasteValue();
            }
        });




    }//OnCreate


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Para permisos de almacenaiento:::::::::::::::::::
        if (requestCode == 2296) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) { // okokokokokokok yor
                    //Almacenando un flag que indica que se ha otorgado el permiso
                    SharedPreferences sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("isPermisoAprobado", "yes");
                    editor.apply();
                    //reiniciando la activity
                    reiniciarActivity(MainActivity.this);

                } else {
                    UtilsY.msgY(getResources().getString(R.string.debe_permitir_acceso_al_almacenamiento));
                }
            }
        }


        //Recibiendo la lectura del codigo QR:
if (G_QR_RRESULTCODE == "5522"){
    IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    UtilsY.valueCopy = intentResult.getContents();
    UtilsY.msgY(getString(R.string.qr_copiadoalportapapeles));
    G_QR_RRESULTCODE = "";
}
    }//OnActivity result



    protected void onStop() {
        super.onStop();

        //Determinando si se ha apagado la pantalla y en ese caso cerrando la BD
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if(!isScreenOn){ //SE HA APAGADO LA PANTALLA
            UtilsY.closeBD();
        }
    }

    //setup menu main
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //listener para elementos del menu main
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){

            case (R.id.menu_tools):{

                if (UtilsY.privacyisAcepted) {
                    startActivity(new Intent(MainActivity.this, ToolsActivity.class));
                }else{
                    UtilsY.msgY(getString(R.string.debe_aceptar_terminos_condiciones));
                }
                break;
            }

            case  ( R.id.menu_privacy):{
                startActivity(new Intent(MainActivity.this, PrivacyActivity.class));
                break;
            }

            case (R.id.menu_web):{
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://projectsypg.mozello.es/productos/archivador/")));
                break;
            }

            case (R.id.menu_resena):{
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName())));
                break;
            }

            case (R.id.menu_qr):{
                launch_QRRead();
                break;
            }

            case (R.id.menu_generateqr):{
                modalGenerateQR modalGenerateQR = new modalGenerateQR(MainActivity.this);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                modalGenerateQR.DialogGenerateQR(UtilsY.valueCopy);
                break;
            }

            case (R.id.menu_keyboard):{
                Keyboard_y keyboardd = new Keyboard_y(MainActivity.this, false);
                keyboardd.ShowKeyboard(null);
                break;
            }

            case (R.id.menu_migrarbd):{
                if (UtilsY.privacyisAcepted){
                    startActivity(new Intent(this, migrarActivity.class));
                }else{
                UtilsY.msgY(getString(R.string.debe_aceptar_terminos_condiciones));
                }

                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    // FUNCIONES INTERNAS ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::


    //Dialog para recabar los permisos necesarios:
    private void PedirPermisos() {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setMessage(R.string.la_app_necesita_permisos);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.acepto, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                            startActivityForResult(intent, 2296);
                        } catch (Exception e) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivityForResult(intent, 2296);

                        }
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                }
            });
            builder.setNegativeButton(R.string.salir, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            builder.show();

    }

    //reinicia una Activity
    private static void reiniciarActivity(Activity actividad){
        Intent intent=new Intent();
        intent.setClass(actividad, actividad.getClass());
        //llamamos a la actividad
        actividad.startActivity(intent);
        //finalizamos la actividad actual
        actividad.finish();
    }



    //Inicializar el lector de QR:
    private void launch_QRRead(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
        intentIntegrator.setPrompt(getResources().getString(R.string.app_name)+ "-->" + "Leyendo QR");
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setBeepEnabled(false);
        G_QR_RRESULTCODE = "5522"; //para OnactivityResult
        intentIntegrator.initiateScan();
    }




    //Chequea si existe una nueva versión y de existir, muestra un cuadro de diálogo
    private void check_version(){
        //Correr el hilo
        //WebsiteDataWorker websiteDataWorker = new WebsiteDataWorker(getApplicationContext());
        //websiteDataWorker.execute("https://projectsypg.mozello.es/productos/archivador/");


        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        G_isinternet = activeNetworkInfo != null && activeNetworkInfo.isConnected(); //estableciendo el valor a la variable global

        // Comprobando primero si existe conección a internet
        if (G_isinternet){
            // WebsiteDataWorker websiteDataWorker = new WebsiteDataWorker(getApplicationContext());
            // websiteDataWorker.execute("https://projectsypg.mozello.es/productos/neville/");

            GetVersionCode getVersionCode = new GetVersionCode(getApplicationContext(),BuildConfig.VERSION_NAME, MainActivity.this.getPackageName());
            getVersionCode.execute();
        }

    }




}//class

