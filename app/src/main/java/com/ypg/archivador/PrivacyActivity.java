package com.ypg.archivador;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ypg.archivador.utily.UtilsY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PrivacyActivity extends AppCompatActivity {

    WebView textinfo;
    Button btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        UtilsY.fragmentActualName = ""; //poniendo valor nulo porque no estoy dentro de un fragment

        textinfo = (WebView) findViewById(R.id.a_privacy_edit);
        btn_back = (Button) findViewById(R.id.a_privacy_btn_back);



        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });


        try {
            InputStreamReader archivo = new InputStreamReader(getResources().openRawResource(R.raw.privacypolicy));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String todo = "";
            while (linea != null) {
                todo = todo + linea + "\n";
                linea = br.readLine();
            }
            br.close();
            archivo.close();
            textinfo.loadData(todo, "text/html", "utf-8");
        } catch (IOException e) {
        }



    }


    @Override
    protected void onStop() {
        super.onStop();
        //Determinando si se ha apagado la pantalla y en ese caso cerrando la BD
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if(!isScreenOn){ //SE HA APAGADO LA PANTALLA
            UtilsY.closeBD();
        }
    }
}


