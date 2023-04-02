package com.ypg.archivador.utily;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ypg.archivador.R;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class modalGenerateQR {


    private Context G_context;




    public modalGenerateQR(Context context) {
        G_context = context;
    }






    //Cuadro de dialogo para generar un código QR

    public void DialogGenerateQR(String text_P){

        AlertDialog.Builder builder = new AlertDialog.Builder(G_context);
        builder.setTitle("Generar QR");
        builder.setCancelable(false);

        View view = LayoutInflater.from(G_context).inflate(R.layout.modal_generator_qr, null, false);

        ImageView img = (ImageView) view.findViewById(R.id.modal_qr_img);
        EditText edit = (EditText) view.findViewById(R.id.modal_qr_edit);
        ImageView btn_Atras = (ImageView) view.findViewById(R.id.modal_qr_img_back);
        Button btn_Teclado = (Button) view.findViewById(R.id.modal_qr_btn_teclado);
        Button btn_GenerarQR = (Button) view.findViewById(R.id.modal_qr_btn_gnerarqr);
        Button btn_pegar = (Button) view.findViewById(R.id.modal_qr_btn_pegar);



        builder.setView(view);

        //Generando el qr del texto pasado como parámetro:
        if(!text_P.isEmpty()){
            Bitmap bitmap = generarQR(text_P);
            if(bitmap != null){
                img.setImageBitmap(bitmap);
                edit.setText(text_P);
            }
        }

        AlertDialog alertDialog = builder.create();



        btn_Atras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btn_GenerarQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = generarQR(edit.getText().toString());
                if(bitmap != null){
                    img.setImageBitmap(bitmap);
                }

            }
        });

        btn_Teclado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard_y keyboard_yy = new Keyboard_y(G_context, false);
                keyboard_yy.ShowKeyboard(edit);
            }
        });

        btn_pegar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit.setText(UtilsY.valueCopy);
                Bitmap bitmap = generarQR(edit.getText().toString());
                if(bitmap != null){
                    img.setImageBitmap(bitmap);
                }
            }
        });



        alertDialog.show();
    }


    //Genera un bitmap de QR del texto pasado como parametro
    public Bitmap generarQR(String text_P){
        QRGEncoder qrgEncoder = new QRGEncoder(text_P, null, QRGContents.Type.TEXT, 300);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            return  bitmap;
        } catch (Exception e) {
            return  null;
        }

    }


}
