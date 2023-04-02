package com.ypg.archivador.utily;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.ypg.archivador.R;

import java.util.Random;


//esta clase se encarga de mostrar cuadros de diálogos
public class UisY{

    //Variables globales para propositos generales
    private String   G_data1;
    private  Context G_context;
    private EditText G_editText; //Instancia de un componente EditText (para devolver texto al padre)

    private final String setCharMayu = "ABCDEFGHIJKLMNOPQRSTUWXYZ";
    private final String setCharMinu = "abcdefghijklmnopqrstuwxyz";
    private final String setCharNume = "012345678950701932567";
    private final String setCharSimb = "!&/$#.;-*=?_@¿";



    //constructores
    //constructor vacio:
    public UisY() {

    }

    public UisY(Context context, String param1, EditText editText) {
        G_context = context;
        G_data1 = param1;
        G_editText = editText;
    }




    //Cuadro de dialogo  para mostrar un resultado de cadena
    public void DialogShowResultText(String title, String msg, String datoAMostrar){

        AlertDialog.Builder builder = new AlertDialog.Builder(G_context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);

        //Inflando un layout y obteniendo la view correspondiente

        View view = LayoutInflater.from(G_context).inflate(R.layout.modal_tools_show_text, null, false);

        //Obteniendo las instancias de los componentes de la view
        EditText edit = (EditText) view.findViewById(R.id.modal_tools_edit);

        builder.setView(view);

        edit.setText(datoAMostrar); //estableciendo el dato del texto encriptado

        builder.setPositiveButton(R.string.copiar, (dialog, which) -> {

            ClipboardManager clipboard = (ClipboardManager) G_context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", edit.getText());
            clipboard.setPrimaryClip(clip);
        });

        builder.setNegativeButton(R.string.atras, (dialog, which) -> dialog.cancel());

        builder.show();

    }



    //Cuadro de dialogo para generar una contraseaña
    public void DialogKeyGenerator(){

        AlertDialog.Builder builder = new AlertDialog.Builder(G_context);

        builder.setTitle(R.string.generador_contraseña);
        builder.setCancelable(false);

        //inflando la view

    View view = LayoutInflater.from(G_context).inflate(R.layout.modal_tools_keygenerator, null, false);


    TextView text_key       =  view.findViewById(R.id.modal_tools_keygenerator_textkey);
    ProgressBar progressBar =  view.findViewById(R.id.modal_tools_keygenerator_progbar);
    EditText editText       =  view.findViewById(R.id.modal_tools_keygenerator_edittext);
    SeekBar seekBar         =  view.findViewById(R.id.modal_tools_keygenerator_seekbar);
    CheckBox chec_Mayus     =  view.findViewById(R.id.modal_tools_keygenerator_chech_mayu);
    CheckBox chec_Minus     =  view.findViewById(R.id.modal_tools_keygenerator_chech_minu);
    CheckBox chec_Nume      =  view.findViewById(R.id.modal_tools_keygenerator_chech_num);
    CheckBox chec_Simb      =  view.findViewById(R.id.modal_tools_keygenerator_chech_simb);
    Button btn_copy         =  view.findViewById(R.id.modal_tools_keygenerator_btn_copiar);
    Button btn_copy_Inter   =  view.findViewById(R.id.modal_tools_keygenerator_btn_copy_inter);
    Button btn_generate     =  view.findViewById(R.id.modal_tools_keygenerator_btn_generate);
    ImageButton btn_back    =  view.findViewById(R.id.modal_tools_keygenerator_btn_back);

    seekBar.setProgress(10);
    editText.setText("10");
    progressBar.setProgress(10);
    progressBar.getProgressDrawable().setColorFilter(
                Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);

    text_key.setText(GenerateKey(setCharMayu+setCharMinu+setCharNume+setCharSimb,10));

    //Evento de la barra de progreso
    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            editText.setText(String.valueOf(progress));

            String charsetY = "";
            if (chec_Mayus.isChecked()){ charsetY = charsetY.concat(setCharMayu); }
            if (chec_Minus.isChecked()){ charsetY = charsetY.concat(setCharMinu); }
            if (chec_Nume.isChecked()){ charsetY = charsetY.concat(setCharNume); }
            if (chec_Simb.isChecked()){ charsetY = charsetY.concat(setCharSimb); }

            text_key.setText(GenerateKey(charsetY,Integer.parseInt(editText.getText().toString())));
            progressBar.setProgress(seekBar.getProgress(),false);

            //configurando el color de la barra de progreso:
            if(seekBar.getProgress() > 9){
                progressBar.getProgressDrawable().setColorFilter(
                        Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
            }else{
                progressBar.getProgressDrawable().setColorFilter(
                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            }

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    });

    //evento del edittext
editText.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        seekBar.setProgress(Integer.parseInt(editText.getText().toString()));
    }
});


    builder.setView(view);
    AlertDialog alertDialog = builder.create();

    //copiar al portapapeles interno
    btn_copy.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            ClipboardManager clipboard = (ClipboardManager) G_context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(text_key.getText().toString(),text_key.getText().toString());
            clipboard.setPrimaryClip(clip);

            alertDialog.dismiss();
        }
    });
    //copia el pasword portapales interno
    btn_copy_Inter.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            UtilsY.valueCopy = text_key.getText().toString();
            Toast.makeText(G_context, G_context.getString(R.string.qr_copiadoalportapapeles), Toast.LENGTH_SHORT).show();
            alertDialog.dismiss();
        }
    });

    btn_generate.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String charsetY = "";
            if (chec_Mayus.isChecked()){ charsetY = charsetY.concat(setCharMayu); }
            if (chec_Minus.isChecked()){ charsetY = charsetY.concat(setCharMinu); }
            if (chec_Nume.isChecked()){ charsetY = charsetY.concat(setCharNume); }
            if (chec_Simb.isChecked()){ charsetY = charsetY.concat(setCharSimb); }

          text_key.setText(GenerateKey(charsetY,Integer.parseInt(editText.getText().toString())));


            progressBar.setProgress(seekBar.getProgress(),false);

            //configurando el color de la barra de progreso:
            if(seekBar.getProgress() > 9){
                progressBar.getProgressDrawable().setColorFilter(
                        Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
            }else{
                progressBar.getProgressDrawable().setColorFilter(
                        Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            }


        }
    });


    btn_back.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            alertDialog.dismiss();
        }
    });

    alertDialog.show();


}



//Función que genera cadenas  a partir de un conjunto de caracteres aleatorios
    public  String  GenerateKey(String SetChar_P, int leng_P){
        String result = "";
        Random random = new Random();

        for(int i = 1; i <= leng_P;i++){
            int randomInt = random.nextInt(SetChar_P.length());
            char randomChar = SetChar_P.charAt(randomInt);
            result =  result.concat(String.valueOf(randomChar));

        }

        return result;
    }



 //Devuelve la suma de todos los conjuntos de caracteres para las password
 public  String getCharSet(){
        return  setCharMayu + setCharNume + setCharMinu + setCharSimb;
 }






}//class
