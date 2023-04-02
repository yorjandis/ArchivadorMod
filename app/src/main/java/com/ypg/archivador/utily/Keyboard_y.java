package com.ypg.archivador.utily;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ypg.archivador.R;


public class Keyboard_y  implements View.OnClickListener {

    EditText edit;

    private boolean isMayus = false;
    private boolean isSimb = false;
    private boolean ismodeHide = false;
    private String textmodehide = ""; // permite almacenar las entradas de texto en esta variable sin que se vea reflejado en el edit

    private Context G_Context;

 private   ImageButton btn_1, btn_2,btn_3,btn_4,btn_5,btn_6,btn_7,btn_8,btn_9,btn_10,
            btn_11,btn_12,btn_13,btn_14,btn_15,btn_16,btn_17,btn_18,btn_19,btn_20,
            btn_21,btn_22, btn_23, btn_24, btn_25, btn_26, btn_27;
 private ImageButton btn_modehide;

    private  String[] abc; //Almacena las letras del abecedario




    
    int index; //Almacena la posicon actual del cursor eb el edit
    
    public Keyboard_y(Context g_Context, boolean PismodeHide) {
        G_Context = g_Context;
        ismodeHide = PismodeHide;
        abc = G_Context.getResources().getStringArray(R.array.abecedario);

    }



    //Muestra  el teclado virtual.
    // adicionalmente se puede pasar como parámetro un editText que tomará el valor puesto en el teclado al darle al boton copiar
    public void ShowKeyboard(EditText editText_P){
        AlertDialog.Builder builder = new AlertDialog.Builder(G_Context);
        builder.setTitle("Archivador - Teclado virtual");
        builder.setCancelable(false);




        Button btn_copy, btn_QR, btn_espacio, btn_home;
        ImageButton btn_Mayus, btn_simb, btn_back, btn_enter;

        View view = LayoutInflater.from(G_Context).inflate(R.layout.keyboard_y, null, false);

        edit        =      view.findViewById(R.id.modal_keyboard_edit);
        btn_Mayus   =      view.findViewById(R.id.modal_keyboard_btn_mayus);
        btn_simb    =      view.findViewById(R.id.modal_keyboard_btn_simb);
        btn_copy    =      view.findViewById(R.id.modal_keyboard_btn_copy);
        btn_QR      =      view.findViewById(R.id.modal_keyboard_btn_qr);
        btn_back    =      view.findViewById(R.id.modal_keyboard_back);
        btn_espacio =      view.findViewById(R.id.modal_keyboard_btn_espacio);
        btn_home    =      view.findViewById(R.id.modal_keyboard_btn_home);
        btn_enter    =     view.findViewById(R.id.modal_keyboard_btn_enter);
        btn_modehide =     view.findViewById(R.id.modal_keyboard_btn_modehide);

        // Instanciando botones
        btn_1 =   view.findViewById(R.id.modal_keyboard_b1); btn_2 =   view.findViewById(R.id.modal_keyboard_b2); btn_3 =   view.findViewById(R.id.modal_keyboard_b3);
        btn_4 =   view.findViewById(R.id.modal_keyboard_b4); btn_5 =   view.findViewById(R.id.modal_keyboard_b5); btn_6 =   view.findViewById(R.id.modal_keyboard_b6);
        btn_7 =   view.findViewById(R.id.modal_keyboard_b7); btn_8 =   view.findViewById(R.id.modal_keyboard_b8); btn_9 =   view.findViewById(R.id.modal_keyboard_b9);
        btn_10 =  view.findViewById(R.id.modal_keyboard_b10); btn_11 =  view.findViewById(R.id.modal_keyboard_b11); btn_12 =  view.findViewById(R.id.modal_keyboard_b12);
        btn_13 =  view.findViewById(R.id.modal_keyboard_b13); btn_14 =  view.findViewById(R.id.modal_keyboard_b14); btn_15 =  view.findViewById(R.id.modal_keyboard_b15);
        btn_16 =  view.findViewById(R.id.modal_keyboard_b16); btn_17 =  view.findViewById(R.id.modal_keyboard_b17); btn_18 =  view.findViewById(R.id.modal_keyboard_b18);
        btn_19 =  view.findViewById(R.id.modal_keyboard_b19); btn_20 =  view.findViewById(R.id.modal_keyboard_b20); btn_21 =  view.findViewById(R.id.modal_keyboard_b21);
        btn_22 =  view.findViewById(R.id.modal_keyboard_b22); btn_23 =  view.findViewById(R.id.modal_keyboard_b23); btn_24 =  view.findViewById(R.id.modal_keyboard_b24);
        btn_25 =  view.findViewById(R.id.modal_keyboard_b25); btn_26 =  view.findViewById(R.id.modal_keyboard_b26); btn_27 =  view.findViewById(R.id.modal_keyboard_b27);

        //SetOnClick
        btn_1.setOnClickListener(this);     btn_2.setOnClickListener(this);     btn_3.setOnClickListener(this);     btn_4.setOnClickListener(this);
        btn_5.setOnClickListener(this);     btn_6.setOnClickListener(this);     btn_7.setOnClickListener(this);     btn_8.setOnClickListener(this);
        btn_9.setOnClickListener(this);     btn_10.setOnClickListener(this);    btn_11.setOnClickListener(this);     btn_12.setOnClickListener(this);
        btn_13.setOnClickListener(this);    btn_14.setOnClickListener(this);    btn_15.setOnClickListener(this);    btn_16.setOnClickListener(this);
        btn_17.setOnClickListener(this);    btn_18.setOnClickListener(this);    btn_19.setOnClickListener(this);    btn_20.setOnClickListener(this);
        btn_21.setOnClickListener(this);    btn_22.setOnClickListener(this);    btn_23.setOnClickListener(this);    btn_24.setOnClickListener(this);
        btn_25.setOnClickListener(this);    btn_26.setOnClickListener(this);    btn_27.setOnClickListener(this);


        AsigngLetter(false); //Asignamiento inicial del texto en cada boton

        //Activando/desactivando modo oculto:
        manageModoHide();



        builder.setView(view);
        AlertDialog alertDialog = builder.create();


        btn_Mayus.setOnClickListener((View v) -> {
            if(isMayus){
                isMayus = false;
                btn_Mayus.setImageResource(R.drawable.ic_up);
            }else{
                isMayus = true;
                btn_Mayus.setImageResource(R.drawable.ic_up_active);
            }
        });

        btn_modehide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ismodeHide){
                    ismodeHide = false;
                    btn_modehide.setImageResource(R.drawable.ic_showpass);
                }else{
                    ismodeHide = true;
                    btn_modehide.setImageResource(R.drawable.ic_noshowpass);
                }
            }
        });




        btn_simb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSimb){
                    if(isMayus){
                        AsigngLetter(true);
                        isMayus = true;
                    }else{
                        AsigngLetter(false);
                        isMayus = false;
                    }
                    isSimb = false;
                }else{
                    AsingSimbAndNumber();
                    isSimb = true;
                }

            }
        });

        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //copiando el valor al control edit pasado como parámetro
                    if (editText_P != null){

                        if (ismodeHide){ //Modo oculto Activado
                            editText_P.setText(textmodehide);
                        }else{ //Modo oculto inactivo
                            editText_P.setText(edit.getText().toString());
                        }

                        alertDialog.dismiss();
                    }else{//si no se ha pasado un edit text copia el valor al portapales interno
                        UtilsY.valueCopy = edit.getText().toString();
                        UtilsY.msgY("Copiado al portapapeles interno");
                        //Toast.makeText(G_Context, "Copiado al portapapeles interno", Toast.LENGTH_LONG).show();
                    }

                    textmodehide = ""; //Resetea la variable del modohide

            }
        });

        btn_QR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modalGenerateQR modalGenerateQR = new modalGenerateQR(G_Context);
                modalGenerateQR.DialogGenerateQR(edit.getText().toString());
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ismodeHide){
                    textmodehide = textmodehide.replaceFirst(".$","");; // elimina el último caracter contenido en la variable
                }else {
                    index = edit.getSelectionStart();
                    if (index == 0){return;} //evita que se cierra la ventana por error
                    Editable editable = edit.getText();
                    editable.delete(index-1, index);
                }


            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        btn_espacio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = edit.getSelectionStart();
                Editable editable = edit.getText();
                editable.insert(index, " ");
            }
        });
        btn_enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                index = edit.getSelectionStart();
                Editable editable = edit.getText();
                editable.insert(index, "\n");
            }
        });

        //Onclick de cada botón

        alertDialog.show();

    }//Dialog ShowKeyboard


    //Func: Asigna el texto de cada boton
    private void AsigngLetter(boolean mayus_P){
            btn_1.setImageResource(R.drawable.q);
            btn_2.setImageResource(R.drawable.w);
            btn_3.setImageResource(R.drawable.e);
            btn_4.setImageResource(R.drawable.r);
            btn_5.setImageResource(R.drawable.t);
            btn_6.setImageResource(R.drawable.y);
            btn_7.setImageResource(R.drawable.u);
            btn_8.setImageResource(R.drawable.i);
            btn_9.setImageResource(R.drawable.o);
            btn_10.setImageResource(R.drawable.p);
            btn_11.setImageResource(R.drawable.a);
            btn_12.setImageResource(R.drawable.s);
            btn_13.setImageResource(R.drawable.d);
            btn_14.setImageResource(R.drawable.f);
            btn_15.setImageResource(R.drawable.g);
            btn_16.setImageResource(R.drawable.h);
            btn_17.setImageResource(R.drawable.j);
            btn_18.setImageResource(R.drawable.k);
            btn_19.setImageResource(R.drawable.l);
            btn_20.setImageResource(R.drawable.n);
            btn_21.setImageResource(R.drawable.z);
            btn_22.setImageResource(R.drawable.x);
            btn_23.setImageResource(R.drawable.c);
            btn_24.setImageResource(R.drawable.v);
            btn_25.setImageResource(R.drawable.b);
            btn_26.setImageResource(R.drawable.n);
            btn_27.setImageResource(R.drawable.m);
    }





    //Func: Asigna caracteres y numeros
    private void AsingSimbAndNumber(){

    }


    @Override
    public void onClick(View view) {
        index = edit.getSelectionStart();
        Editable editable = edit.getText();

        if (isMayus){ //es mayuscula

            if (ismodeHide){ //Si esta activo el modo oculto
                textmodehide = textmodehide + abc[Integer.parseInt(view.getTag().toString())].toUpperCase();
            }else{ //Modo oculto inactivo
                editable.insert(index, abc[Integer.parseInt(view.getTag().toString())].toUpperCase());
            }

        }else { //Es minúsculas

            if (ismodeHide){ //Si esta activo el modo oculto
                textmodehide = textmodehide + abc[Integer.parseInt(view.getTag().toString())];
            }else{ //modo oculto inactivo
                editable.insert(index, abc[Integer.parseInt(view.getTag().toString())]);
            }

        }

    }

    private void manageModoHide(){
        if(ismodeHide){
            btn_modehide.setImageResource(R.drawable.ic_noshowpass);
        }else{
            btn_modehide.setImageResource(R.drawable.ic_showpass);
        }
    }


}
