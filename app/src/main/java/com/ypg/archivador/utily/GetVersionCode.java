package com.ypg.archivador.utily;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.ypg.archivador.BuildConfig;
import com.ypg.archivador.R;
import com.ypg.archivador.frag.frag_home;

import org.jsoup.Jsoup;

public class GetVersionCode extends AsyncTask<Void, String, String> {

    String G_versionOnline = "";
    String currentVersion = "";
    String packageName = "";


    Context context;


    public GetVersionCode(Context context, String CurrentVersion, String PackageName) {
        this.context = context;
        currentVersion = CurrentVersion;
        packageName = PackageName;
    }


    @Override
    public String doInBackground(Void... voids)  {

        try {
            G_versionOnline = Jsoup.connect("https://play.google.com/store/apps/details?id=com.ypg.archivador" + "&hl=it")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select(".hAyfc .htlgb")
                    .get(7)
                    .ownText();
        } catch (Exception e) {
            G_versionOnline = "error";
        }
        return null;
    }

    @Override
    public void onPostExecute(String onlineVersion) {
        super.onPostExecute(String.valueOf(onlineVersion));

        // Log.d("yyy", "Current version " + currentVersion + "playstore version " + G_versionOnline);

        if (G_versionOnline.equals("error") ){return;}

        if (!currentVersion.equals(G_versionOnline)){
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