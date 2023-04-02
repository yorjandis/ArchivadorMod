package com.ypg.archivador;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.ypg.archivador.utily.GetFilePath;
import com.ypg.archivador.utily.Keyboard_y;
import com.ypg.archivador.utily.UisY;
import com.ypg.archivador.utily.UtilsY;
import com.ypg.archivador.utily.modalGenerateQR;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ToolsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RESULTCODE = 1;
    Button btn_path, btn_encpFile, btn_decFile, btn_hashFile, btn_hashText, btn_checkHashFile, btn_checkHashText, btn_close;
    Button btn_enctext, btn_dectext, btn_keygenerator, btn_QRGenerator;
    ImageView keyboard, showhidepass;
    private Boolean isvisible1 = false;
            ;
    EditText edit_path,edit_path_migrar, edit_pass, edit_text;
    Spinner spin_alg;

    private static final String[] alg = new String[]{"SHA-512", "SHA-384", "SHA-256", "SHA-1", "SHA", "MD5"};

    ConstraintLayout layoutroot;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Evitar que se tome capturas de pantalla
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        Fuente: https://www.iteramos.com/pregunta/34979/como-puedo-evitar-que-android-tomar-una-captura-de-pantalla-cuando-la-aplicacion-pasa-a-segundo-plano

        setContentView(R.layout.activity_tools);

        layoutroot =  findViewById(R.id.activity_tools_root_layout);



        UtilsY.fragmentActualName = ""; //poniendo valor nulo porque no estoy dentro de un fragmento

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);//bloquear rotacion de pantalla


        btn_path                =  findViewById(R.id.tools_btn_papth);
        btn_encpFile            =  findViewById(R.id.tools_btn_enc_fichero);
        btn_decFile             =  findViewById(R.id.tools_btn_deenc_fichero);
        btn_enctext             =  findViewById(R.id.tools_btn_enc_texto);
        btn_dectext             =  findViewById(R.id.tools_btn_deenc_texto);
        btn_hashFile            =  findViewById(R.id.tools_btn_hash_fichero);
        btn_hashText            =  findViewById(R.id.tools_btn_hash_text);
        btn_checkHashFile       =  findViewById(R.id.tools_btn_checkhash_fichero);
        btn_checkHashText       =  findViewById(R.id.tools_btn_checkhash_text);
        btn_keygenerator        =  findViewById(R.id.tools_btn_keygenerator);
        btn_QRGenerator         =  findViewById(R.id.tools_btn_qrgenerator);
        btn_close               =  findViewById(R.id.tools_btn_close);

        edit_path       =  findViewById(R.id.tools_edit_papth);
        edit_pass       =  findViewById(R.id.tools_edit_pass);
        edit_text       =  findViewById(R.id.tools_edit_text);
        keyboard        = findViewById(R.id.tools_keyboard);
        showhidepass    = findViewById(R.id.tools_showhidepass);
;

        spin_alg = findViewById(R.id.tools_spin_algorit);
        spin_alg.setAdapter(new ArrayAdapter<>(getBaseContext(), R.layout.support_simple_spinner_dropdown_item, alg));


        btn_close.setOnClickListener(this);
        btn_path.setOnClickListener(this);
        btn_encpFile.setOnClickListener(this);
        btn_decFile.setOnClickListener(this);
        btn_enctext.setOnClickListener(this);
        btn_dectext.setOnClickListener(this);
        btn_hashFile.setOnClickListener(this);
        btn_hashText.setOnClickListener(this);
        btn_checkHashFile.setOnClickListener(this);
        btn_checkHashText.setOnClickListener(this);
        btn_keygenerator.setOnClickListener(this);
        btn_QRGenerator.setOnClickListener(this);
        keyboard.setOnClickListener(this);
        showhidepass.setOnClickListener(this);



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

    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id){

            case (R.id.tools_btn_close):
                super.onBackPressed();
                break;

            case (R.id.tools_btn_papth):
                //Abriendo un cuadro de dialogo para seleccionar el fichero
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, ToolsActivity.RESULTCODE);
                break;

            case (R.id.tools_keyboard):
                Keyboard_y keyboard_y = new Keyboard_y(this, false);
                keyboard_y.ShowKeyboard(edit_pass);
                break;

            case (R.id.tools_showhidepass):
                if(!isvisible1){
                    edit_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showhidepass.setImageResource(R.drawable.ic_showpass);
                    isvisible1 = true;
                }else{
                    edit_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showhidepass.setImageResource(R.drawable.ic_noshowpass);
                    isvisible1 = false;
                }

                break;


            case (R.id.tools_btn_enc_fichero): //encripta el fichero (le adiciona el prefijo:"_enc"
                edit_text.setText("");
                File file, file_enc;
                //Comprobación de campos:
                if(!edit_path.getText().toString().isEmpty() && !edit_pass.getText().toString().isEmpty()){
                    //encriptando...
                    if (UtilsY.encryptFile(edit_pass.getText().toString(), edit_path.getText().toString(), edit_path.getText().toString() + "_enc")){

                        file = new File(edit_path.getText().toString());
                        file_enc = new File(edit_path.getText().toString() + "_enc");

                        if (file_enc.exists()){
                            edit_text.setText(">>"+ R.string.datos_del_fichero_original + "\n");
                            edit_text.append("Name: "+ file.getName() + "\n");
                            try {
                                edit_text.append("Hash ("+ spin_alg.getSelectedItem().toString()+ "): "+ UtilsY.hashFile(edit_path.getText().toString(), spin_alg.getSelectedItem().toString())+ "\n");
                            } catch (IOException | NoSuchAlgorithmException e) {
                                UtilsY.msgY("Error: " + e);
                            }
                            edit_text.append(">>"+ R.string.tamano_bytes + file.length() + "\n");
                            edit_text.append(":::::::::::::::::::::::::::::::::::\n");


                            edit_text.append(">>"+getString(R.string.datos_del_fichero_encriptado)+ "\n");
                            edit_text.append(getString(R.string.nombre)+": \n");
                            try {
                                //calculando el hash del fichero encriptado
                                edit_text.append("Hash ("+ spin_alg.getSelectedItem().toString()+ "): "+ UtilsY.hashFile(edit_path.getText().toString() + "_enc", spin_alg.getSelectedItem().toString()) + "\n");
                            } catch (IOException | NoSuchAlgorithmException e) {
                                UtilsY.msgY("Error: " + e);
                            }
                            //Calculando el tamaño del fichero encriptado
                            edit_text.append(getString(R.string.tamano_bytes) + file_enc.length());
                        }else{
                            UtilsY.msgY(getString(R.string.la_operación_ha_fallado));
                        }
                    }else{
                        UtilsY.msgY(getString(R.string.la_operación_ha_fallado));
                    }

                }else{
                    UtilsY.msgY(getBaseContext().getString(R.string.debe_proveer_direccion_contraseña));
                }
                 break;

            case (R.id.tools_btn_deenc_fichero): //desencripta un fichero (se le adiciona el sujijo dec a la extension)

                if(!edit_path.getText().toString().isEmpty() && !edit_pass.getText().toString().isEmpty()){
                    File file_encc, file_dec;

                    //desencriptando
                    if (UtilsY.decryptFile(edit_pass.getText().toString(), edit_path.getText().toString(), edit_path.getText().toString()+ "_dec")) {
                        file_encc = new File(edit_path.getText().toString());
                        file_dec = new File(edit_path.getText().toString()+ "_dec");
                        //si se desencriptó realmente el fichero:
                        if (file_dec.exists()){
                            edit_text.setText(">>"+ getString(R.string.datos_del_fichero_encriptado) + "\n");
                            edit_text.append("Nombre: " + file_encc.getName() +"\n");
                            try {
                                edit_text.append("Hash ("+ spin_alg.getSelectedItem().toString()+ "): " + UtilsY.hashFile(edit_path.getText().toString(), spin_alg.getSelectedItem().toString()) + "\n");
                            } catch (IOException | NoSuchAlgorithmException e) {
                                UtilsY.msgY("Error: " + e);
                            }
                            edit_text.append(getString(R.string.tamano_bytes) + file_encc.length() + "\n");
                            edit_text.append(":::::::::::::::::::::::::::::::::::\n");
                            edit_text.append(">>"+getString(R.string.datos_del_fichero_desencriptado));
                            edit_text.append("Name: " + file_dec.getName() +"\n");
                            try {
                                edit_text.append("Hash ("+ spin_alg.getSelectedItem().toString()+ "): " + UtilsY.hashFile(edit_path.getText().toString()+ "_dec", spin_alg.getSelectedItem().toString()) + "\n");
                            } catch (IOException | NoSuchAlgorithmException e) {
                                UtilsY.msgY("Error: " + e);
                            }
                            edit_text.append(getString(R.string.tamano_bytes) + file_dec.length());
                        }else{
                            UtilsY.msgY(getString(R.string.la_operación_ha_fallado));
                        }

                    }else{
                        UtilsY.msgY(getString(R.string.la_operación_ha_fallado));
                    }
                }
                break;

            case (R.id.tools_btn_enc_texto): //Encriptar un texto
                if (!edit_pass.getText().toString().isEmpty() && !edit_text.getText().toString().isEmpty() ) {
                    try {
                        UisY uisY = new UisY(this, "",null);
                        uisY.DialogShowResultText(getResources().getString(R.string.encriptar_texto),getString(R.string.resultado),UtilsY.encriptarY(edit_text.getText().toString(),edit_pass.getText().toString(),true));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    showmsgY(getString(R.string.debe_colocar_un_texto_y_contraseña));
                }
                break;

            case (R.id.tools_btn_deenc_texto): //desencriptando un texto

                if (!edit_pass.getText().toString().isEmpty() && !edit_text.getText().toString().isEmpty() ) {

                    UisY uisY = new UisY(this,"",null);
                    try {
                        uisY.DialogShowResultText(getResources().getString(R.string.desencriptar_texto),getResources().getString(R.string.resultado),UtilsY.desencriptarY(edit_text.getText().toString(),edit_pass.getText().toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    showmsgY(getResources().getString(R.string.debe_colocar_un_texto_y_contraseña));
                }
                break;

            case (R.id.tools_btn_hash_fichero): //hash de un fichero

                if(!edit_path.getText().toString().isEmpty()){

                    try {
                        String hash =   UtilsY.hashFile(edit_path.getText().toString(), spin_alg.getSelectedItem().toString());
                        if (!hash.isEmpty()){
                            edit_text.setText("The hash "+spin_alg.getSelectedItem().toString() +" of file is: "+ hash);
                        }else {
                            UtilsY.msgY(getString(R.string.la_operación_ha_fallado));
                        }

                    } catch (IOException | NoSuchAlgorithmException e) {
                        UtilsY.msgY("Error: " + e);
                    }

                }else{
                    UtilsY.msgY(getString(R.string.debe_establecer_la_ruta_de_un_fichero));
                }
                break;

            case (R.id.tools_btn_hash_text): //hash de texto

                if(!edit_text.getText().toString().isEmpty()){
                    String hash;

                    hash =  UtilsY.hashText(edit_text.getText().toString(), spin_alg.getSelectedItem().toString());
                    edit_text.append("\n\n Hash: "+ hash);

                }else{
                    UtilsY.msgY(getString(R.string.debe_establecer_un_texto_para_calcular_su_hash));
                }
                break;

            case (R.id.tools_btn_checkhash_fichero): //Chequear hash de fichero

                if(!edit_path.getText().toString().isEmpty() && !edit_text.getText().toString().isEmpty()){
                    String hashfile;
                    try {
                        hashfile =   UtilsY.hashFile(edit_path.getText().toString(), spin_alg.getSelectedItem().toString());
                        if (hashfile.equals(edit_text.getText().toString())){
                            edit_text.append("\n\n"+ getString(R.string.el_hash_coincide));
                        }else{
                            edit_text.append("\n\n"+ getString(R.string.los_hash_NO_son_iguales));
                        }
                    } catch (IOException | NoSuchAlgorithmException e) {
                        UtilsY.msgY("Error: " + e);
                    }


                }else{
                    UtilsY.msgY(getString(R.string.debe_establecer_un_fichero_y_un_hash));
                }
                break;

            case (R.id.tools_btn_checkhash_text): //chequear hash de texto
                if (edit_path.isEnabled()){
                    if(UtilsY.hashTextCheck(edit_text.getText().toString(), edit_path.getText().toString(), spin_alg.getSelectedItem().toString())){
                        UtilsY.msgY(getString(R.string.el_hash_coincide));
                    } else{
                        UtilsY.msgY(getString(R.string.el_hash_NO_Coincide));
                    }
                    edit_path.setText("");
                    edit_path.setEnabled(false);
                }else{ //Si el edit no esta habilitado: se informa y se habilita.
                    UtilsY.msgY(getString(R.string.colocar_el_hash_en_a_comprobrar_en_el_cuadro));
                    edit_path.setText("");
                    edit_path.setEnabled(true);
                }
                break;

            case (R.id.tools_btn_keygenerator): //abre el generador de contraseñas
                UisY uisY = new UisY(this,"",null);
                uisY.DialogKeyGenerator();
                break;

            case (R.id.tools_btn_qrgenerator): //Abre el generador de códigos QR
                modalGenerateQR modalGenerator = new modalGenerateQR(this);
                modalGenerator.DialogGenerateQR("Example");
                break;

        }


    }//onclick



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULTCODE){
            if (resultCode == RESULT_OK){
                Uri uu;
                if (data != null){
                    uu = data.getData();
                    String s = GetFilePath.getPathFromUri(getApplicationContext(),uu);
                    edit_path.setText(s);
                }
            }
        }





    }

    public void showmsgY(String msg){
        Snackbar snackbar = Snackbar.make(edit_text, msg,Snackbar.LENGTH_SHORT);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
        snackbar.show();
    }



}//class