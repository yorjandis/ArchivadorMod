package com.ypg.archivador.utily;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.ypg.archivador.BuildConfig;
import com.ypg.archivador.R;
import com.ypg.archivador.frag.frag_home;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//esta clase no se esta utilizando

public class WebsiteDataWorker extends AsyncTask<String, Void, Void> {


    Context context;

    String G_versionOnline = "";


    public WebsiteDataWorker(Context context) {
        this.context = context;
    }

    @Override
    public Void doInBackground(String... params)   {
        URL url = null;
        String[] temp = new String[2];


        try {
            url = new URL(params[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection response = null;
        try {
            response = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream in = null;
        try {
            in = response.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = null;
        while(true){
            try {
                if (!((line = reader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (line.contains("YYY")) {
                temp = line.split("YYY");
                G_versionOnline = (temp[1]);
                break;
            }

        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
//Se consigue extraer el número de versión

        String versionApp = BuildConfig.VERSION_NAME; //1.2.10   obteniendo la version actual de la app

        if (!G_versionOnline.isEmpty()){
            //chequeando si son iguales, que quiere decir que se ha actualizado la versión
            if (!versionApp.contains(G_versionOnline)){ //si no son iguales se anuncia una nueva versión
                UtilsY.isVersionDisponible = true;
                frag_home.txt_version.setTextColor(Color.BLUE);
                frag_home.txt_version.setText("v."+ BuildConfig.VERSION_NAME + " ("+context.getString(R.string.actualizar)+")"); //colocando el nombre de la versión de la app
                UtilsY.msgY(context.getString(R.string.existe_nueva_version));
            }else
            {
                frag_home.txt_version.setTextColor(Color.BLACK);
                frag_home.txt_version.setText("v."+ BuildConfig.VERSION_NAME);
                UtilsY.isVersionDisponible = false;
            }
        }
    }
}
