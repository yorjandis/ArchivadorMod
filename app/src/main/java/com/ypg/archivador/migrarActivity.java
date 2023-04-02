package com.ypg.archivador;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.ypg.archivador.utily.GetFilePath;
import com.ypg.archivador.utily.JIniFile;
import com.ypg.archivador.utily.UtilsY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class migrarActivity extends AppCompatActivity implements View.OnClickListener {


    Button btn_buscar, btn_migrar, btn_atras;
    EditText edit_path, edit_pass;
    private ActivityResultLauncher<Intent> launcherResult;
    private int G_ResultCode = 0;
    private  String G_patBD;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //para evitar tomar capturas de pantallas:
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_migrar);

        btn_buscar = findViewById(R.id.act_migrar_btn_buscar);
        btn_migrar = findViewById(R.id.act_migrar_btn_migrar);
        btn_atras = findViewById(R.id.act_migrar_btn_atras);
        edit_path = findViewById(R.id.act_migrar_edit_path);
        edit_pass = findViewById(R.id.act_migrar_edit_pass);

        btn_buscar.setOnClickListener(this);
        btn_migrar.setOnClickListener(this);
        btn_atras.setOnClickListener(this);
        edit_path.setOnClickListener(this);
        edit_pass.setOnClickListener(this);


        //OnActivityResul (Nueva variante Actualziada)
        launcherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            //Abrir BD
                            if(G_ResultCode == 12){
                                String s = GetFilePath.getPathFromUri(migrarActivity.this,intent.getData());
                                File file = new File(s);
                                if (file.exists()){
                                    edit_path.setText(s); //Accediendo al edit de la ventana modal
                                    G_patBD = s;
                                }
                            G_ResultCode = 0; //reset el flag
                        }
                    }
                    }
                });
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
    public void onClick(View view) {
           int id = view.getId();

           switch (id){
               case (R.id.act_migrar_btn_atras):
                   super.onBackPressed();
                   break;
               case (R.id.act_migrar_btn_buscar):
                   buscarFichero();
                   break;
               case (R.id.act_migrar_btn_migrar):
                   if (!UtilsY.privacyisAcepted){
                       msgY(getString(R.string.debe_aceptar_terminos_condiciones));
                       return;
                   }

                   try {
                       migrar();
                   } catch (IOException e) {
                       msgY(getString(R.string.error_al_migrar_bd));
                   }
                   break;
           }
    }


    //Permite migrar la BD
    private void migrar() throws IOException {

        pass = edit_pass.getText().toString();


        //verificando los campos:
        if(edit_path.getText().toString().trim().isEmpty() || pass.isEmpty() ){
            msgY(getString(R.string.debe_colocar_un_texto_y_contraseña));
            return;
        }

        String Categoria, Entrada, Valor;

        //cargando el fichero ini de la BD anterior
        JIniFile ini;
        ini = new JIniFile(G_patBD);


        //creando el nuevo fichero de BD
        File file_new = new File(G_patBD+".ok");
        OutputStreamWriter write;
        write = new OutputStreamWriter(new FileOutputStream(file_new));


        List<String> listaCatg;
        List<String> listaEntradas;

        //Obteniendo la lista de categorias (ya desencriptadas)
        listaCatg = loadCatg();

        //chequeando si la lista de categoria esta vacia
        if (listaCatg.size() == 0){
            msgY(getString(R.string.error_al_migrar_bd));
            return;
        }

        //ciclo que recorre la lista de Categorias
        for (int i = 0; i < listaCatg.size(); i++){

            Categoria = listaCatg.get(i);

            //Escribiendo la nueva categoría (Seccion)
            try {
                write.write("["+UtilsY.encriptarY(Categoria,pass, false )+"]\n"); //Escribí la categoria

                //obteniendo todas las entradas para una categoria
                listaEntradas = ini.ReadSection(UtilsY.Old_encriptarY(Categoria,pass)); //Obteniendo todas las keys de una sección

                //ciclo que recorre todas las entradas de una categoria
                for (int ii = 0; ii < listaEntradas.size(); ii++){

                        Entrada = UtilsY.Old_desencriptarY(listaEntradas.get(ii), pass); //Obteniendo una entrada (descifrada)
                        Valor   = UtilsY.Old_desencriptarY(ini.ReadString(UtilsY.Old_encriptarY(Categoria,pass), UtilsY.Old_encriptarY(Entrada,pass), ""),pass);

                        //escribiendo el par Entrada=Valor
                    write.write(UtilsY.encriptarY(Entrada,pass, false)+"="+UtilsY.encriptarY(Valor,pass, true)+"\n");
                }//for

            }catch (Exception e){
                msgY(getString(R.string.error_al_migrar_bd));
            }

        }//for

        write.close();

        msgY(getString(R.string.fichero_bd_creado_ok));
    }




    //Abre un cuadro de busqueda y permite seleccionar el fichero de BD a migrar
    private void buscarFichero() {
        //Abre un cuadro de diálogo fileChoiser y se captura el resultado en MainActivity=>onActivityResult
        Intent intentfilepicker = new Intent(Intent.ACTION_GET_CONTENT);
        intentfilepicker.setType("*/*");
        G_ResultCode = 12;
        launcherResult.launch(intentfilepicker);
    }



    //Devuelve la lista de categorias de la BD
    public  List<String> loadCatg(){

        List<String> lista = new ArrayList<>();

        if (G_patBD.equals("") || !new File(G_patBD).exists()){ return lista;} //No carga la BD

        try {
            JIniFile ini = new JIniFile(G_patBD);
            ArrayList<String> sections =  ini.ReadSections(); //Obteniendo todas las secciones (Categorias)

            if (sections.size() > 0) { //si existen secciones que cargar
                //recorriendo las secciones para descifrarlas
                for (int i = 0; i < sections.size(); i++){
                    lista.add(UtilsY.Old_desencriptarY(sections.get(i), pass));
                }
                return lista;

            }else{

                UtilsY.msgY(getBaseContext().getResources().getString(R.string.el_fichero_de_bd_esta_vacio));
            }

        }catch (Exception e){

            msgY( "Error: "+ e);
        }

        return  lista;
    }


    //muestra un mensaje (para propositos de debug)
    public  void msgY(String message){

        Snackbar snackbar = Snackbar.make(btn_migrar, message,Snackbar.LENGTH_SHORT);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
        snackbar.show();

    }

}
