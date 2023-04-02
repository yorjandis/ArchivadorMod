package com.ypg.archivador.frag;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ypg.archivador.BuildConfig;
import com.ypg.archivador.MainActivity;
import com.ypg.archivador.PrivacyActivity;
import com.ypg.archivador.R;
import com.ypg.archivador.utily.GetFilePath;
import com.ypg.archivador.utily.Keyboard_y;
import com.ypg.archivador.utily.UisY;
import com.ypg.archivador.utily.UtilsY;

import java.io.File;


public class frag_home extends Fragment {

    //variables de flag para controlar los img que sirven para mostrar las contraseñas
    private boolean isvisible1 = false;
    private boolean isvisible2 = false;
    //controla los resultados de los dialogos para crear y escoger ficheros de BD (nueva forma de OnActivityResult)
    private ActivityResultLauncher<Intent> launcherResult;
    private int G_ResultCode =  0; //RequestCode

    //Views Globales

    private  EditText G_editPathBD;
    private  TextView G_textNameFile;
    public static TextView txt_version;



    public frag_home() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilsY.fragmentActualName = "home";



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
                                String s = GetFilePath.getPathFromUri(getContext(),intent.getData());
                                File file = new File(s);
                                if (file.exists()){
                                    G_editPathBD.setText(s); //Accediendo al edit de la ventana modal
                                    G_textNameFile.setText(getResources().getString(R.string.ultimo_fichero_accedido) + file.getName());
                                }
                            //Crear BD
                            }else if(G_ResultCode == 13){
                                String s = GetFilePath.getPathFromUri(getContext(),intent.getData());
                                G_editPathBD.setText(s);
                            }

                            G_ResultCode = 0; //reset el flag
                        }
                    }
                });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_home, container, false);
    }


    @Override
    public void onViewCreated( View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.imgCopy.setVisibility(View.INVISIBLE);
        MainActivity.imgPaste.setVisibility(View.INVISIBLE);

        Button btn_abrirBd, btn_creaBd;
        TextView text_privacy;
        SwitchCompat switchc;

        btn_abrirBd = (Button) view.findViewById(R.id.fhome_btn_openbd);
        btn_creaBd = (Button) view.findViewById(R.id.fhome_btn_createbd);
        text_privacy = (TextView) view.findViewById(R.id.fhome_text_privacy);
        switchc = (SwitchCompat) view.findViewById(R.id.fhome_btn_switch);
        txt_version = (TextView) view.findViewById(R.id.fhome_txt_version);
        frag_home.txt_version.setTextColor(Color.BLACK);
        frag_home.txt_version.setText("v."+ BuildConfig.VERSION_NAME);



        //Verificando si existe una nueva versión y actualizando el TextView

        txt_version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilsY.isVersionDisponible){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getContext().getPackageName())));
                }
            }
        });






        //Ajusta el estado del botón switch de aceptar terminos y condiciones
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String privacy = sharedPreferences.getString("isAceptedPrivacy", "");

        if(!privacy.equals("")){ //Si se ha aceptado los terminos y condiciones: Habilitar el boton
            switchc.setChecked(true);
            UtilsY.privacyisAcepted = true;
        }else{ //si no se ha aceptado ( o se ha deshabilitado el boton switch)
            switchc.setChecked(false);
            UtilsY.privacyisAcepted = false;
        }

        //evento del botón wwitch
        switchc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(isChecked){
                    editor.putString("isAceptedPrivacy", "true");
                    UtilsY.privacyisAcepted = true;
                }else{
                    editor.putString("isAceptedPrivacy", "");
                    UtilsY.privacyisAcepted = false;
                }
                editor.commit();
            }
        });


        //abre la ventana de los términos y condiciones
        text_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PrivacyActivity.class);
                startActivity(intent);
            }
        });


        btn_creaBd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchc.isChecked()){
                    ShowModalCreateBD( v);
                }else{
                    UtilsY.msgY(getContext().getString(R.string.debe_aceptar_terminos_condiciones));
                }
            }
        });


        btn_abrirBd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchc.isChecked()) {
                    ShowModalOpenBD(v);
                }else{
                    UtilsY.msgY(getContext().getString(R.string.debe_aceptar_terminos_condiciones));
                }
            }
        });




    }

    @Override
    public void onResume() {
        super.onResume();
        UtilsY.fragmentActualName = "home";
        MainActivity.imgCopy.setVisibility(View.INVISIBLE);
        MainActivity.imgPaste.setVisibility(View.INVISIBLE);

    }

    // Modal Abrir BD
    //v es la vista contenedor del fragment actual (se utiliza en el objeto navigation)
    private void ShowModalOpenBD( View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.abrir_base_de_datos);
        builder.setMessage(R.string.por_favor_ofresca_siguientes_datos);
        builder.setCancelable(false); //no permite cerrar el dialogo si damos click afuera o boton atras

        //Obtiene una instancia del view a mostrar (se toma de un archivo xml layout)
        View view = LayoutInflater.from(getContext()).inflate(R.layout.modal_abrir_bd, null, false);

        //Se obtiene una instancia de los objetos de la vista
        G_editPathBD   = (EditText) view.findViewById(R.id.mabrirbd_edit_pathBD);
        G_textNameFile = (TextView) view.findViewById(R.id.mabrirbd_text_filename);
        final EditText editPass     = (EditText) view.findViewById(R.id.mabrirbd_edit_passbd);
        final Button btn_buscarPath = (Button) view.findViewById(R.id.mabrirbd_btn_path);
        final CheckBox check_isEncryptBD = (CheckBox) view.findViewById(R.id.mabrirbd_check_bdencrip);
        final CheckBox check_MismoPass = (CheckBox) view.findViewById(R.id.mabrirbd_check_mismopass);
        final EditText editPassEncrypt = (EditText) view.findViewById(R.id.mabrirbd_edit_passencryptbd);
        final ImageView img_showHidePass1 = (ImageView) view.findViewById(R.id.mabrirbd_img_showhide1);
        final ImageView img_showHidePass2 = (ImageView) view.findViewById(R.id.mabrirbd_img_showhide2);
        final ImageView img_keyboard1 = (ImageView) view.findViewById(R.id.mabrirbd_img_keyboard1);
        final ImageView img_keyboard2 = (ImageView) view.findViewById(R.id.mabrirbd_img_keyboard2);




        //Coloca la dirección de la ultima base de dato asociada:
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
        String path = sharedPreferences.getString("lastBDPath", "");
        //Asegurándose que la ruta corresponde a un fichero que existe:
        File file = new File(path);
        if (file.exists()){
            G_editPathBD.setText(path);//Colocando el nombre del fichero en el edit del path
            G_textNameFile.setText(getString(R.string.ultimo_fichero_accedido)+ file.getName());
            editPass.requestFocus();
        }


        builder.setView(view); //Colocando la vista en el dialogos

        //Evento de los checkbox
        check_isEncryptBD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check_isEncryptBD.isChecked()) {

                    check_MismoPass.setVisibility(View.VISIBLE);
                    check_MismoPass.setChecked(true);

                }else{
                    check_MismoPass.setVisibility(View.INVISIBLE);
                    editPassEncrypt.setVisibility(View.INVISIBLE);
                    img_showHidePass2.setVisibility(View.INVISIBLE);
                    img_keyboard2.setVisibility(View.INVISIBLE);
                    editPassEncrypt.setText("");
                }
            }
        });

        check_MismoPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check_MismoPass.isChecked()){
                    editPassEncrypt.setVisibility(View.INVISIBLE);
                    img_showHidePass2.setVisibility(View.INVISIBLE);
                    img_keyboard2.setVisibility(View.INVISIBLE);
                }else{
                    editPassEncrypt.setVisibility(View.VISIBLE);
                    img_showHidePass2.setVisibility(View.VISIBLE);
                    img_keyboard2.setVisibility(View.VISIBLE);
                }
            }
        });

        //imagen show/hide pass #1
        img_showHidePass1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isvisible1){
                    editPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    img_showHidePass1.setImageResource(R.drawable.ic_showpass);
                    isvisible1 = true;
                }else{
                    editPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    img_showHidePass1.setImageResource(R.drawable.ic_noshowpass);
                    isvisible1 = false;
                }
            }
        });

        //imagen show/hide pass #2
        img_showHidePass2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isvisible1){
                    editPassEncrypt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    img_showHidePass2.setImageResource(R.drawable.ic_showpass);
                    isvisible1 = true;
                }else{
                    editPassEncrypt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    img_showHidePass2.setImageResource(R.drawable.ic_noshowpass);
                    isvisible1 = false;
                }
            }
        });

        //imagen teclado virtual #1
        img_keyboard1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard_y keyboard_y = new Keyboard_y(getContext(), true);
                keyboard_y.ShowKeyboard(editPass);
            }
        });

        //imagen teclado virtual #2
        img_keyboard2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard_y keyboard_y = new Keyboard_y(getContext(), true);
                keyboard_y.ShowKeyboard(editPassEncrypt);
            }
        });


        //Boton Buscar la ruta de la BD
        btn_buscarPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Abre un cuadro de diálogo fileChoiser y se captura el resultado en MainActivity=>onActivityResult
                Intent intentfilepicker = new Intent(Intent.ACTION_GET_CONTENT);
                intentfilepicker.setType("*/*");
                G_ResultCode = 12;
                launcherResult.launch(intentfilepicker);
            }
        });

        //Boton abrir la BD
            builder.setPositiveButton(R.string.abrir_bd, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String pathBD = G_editPathBD.getText().toString().trim();
                    String pass = editPass.getText().toString();
                    String passEncrypt = editPassEncrypt.getText().toString();

                    //Reseteando las variables globales:
                    UtilsY.spinCatgItemposition = -1;
                    UtilsY.G_patBD = "";
                    UtilsY.pass = "";
                    UtilsY.passEncripted = "";
                    UtilsY.isEditModif = false;
                    UtilsY.isBDOpenEnc = false;
                    UtilsY.isBDOpenEncMismaPass = false;

                    //Comprobaciones de campo
                    if (!pathBD.equals("") && !pass.equals("")){

                        //Verificar si es necesario desencriptar primero la BD

                        if(check_isEncryptBD.isChecked()){//Si hay que desencriptar la BD

                            if (check_MismoPass.isChecked()){ //Si se utiliza el mismo pass
                                UtilsY.isBDOpenEnc = true; //flag que dice que la BD se tuvo que desencriptar para abrirse
                                UtilsY.isBDOpenEncMismaPass = true;//flag que dice que la BD se desencriptó con el mismo pass

                                UtilsY.decryptFile(pass,pathBD,pathBD); //desencriptando...
                                UtilsY.G_patBD = pathBD;
                                UtilsY.pass = pass;
                                Navigation.findNavController(v).navigate(R.id.frag_bd);

                            }else{ //Si NO se utiliza el mismo pass
                                //Chequeando que se haya puesto el pass de encriptación:
                                if (!passEncrypt.equals("")){
                                    UtilsY.isBDOpenEnc = true; //flag que dice que la BD se tuvo que desencriptar para abrirse
                                    UtilsY.isBDOpenEncMismaPass = false;//flag que dice que la BD NO se desencriptó con el mismo pass

                                    UtilsY.decryptFile(passEncrypt,pathBD,pathBD);//desencriptando...
                                    UtilsY.G_patBD = pathBD;
                                    UtilsY.pass = pass;
                                    UtilsY.passEncripted = passEncrypt; //almacenando la contraseña de encriptado
                                    Navigation.findNavController(v).navigate(R.id.frag_bd);
                                }else{
                                    UtilsY.msgY(getContext().getString(R.string.debe_colocarse_clave_encriptacion));
                                }
                            }

                        }else{ //Si no hay que desencriptar la BD:
                            UtilsY.G_patBD = pathBD.trim();
                            UtilsY.pass = pass;
                            Navigation.findNavController(v).navigate(R.id.frag_bd);
                        }

                    }else{
                        UtilsY.msgY(getContext().getString(R.string.debe_proveer_direccion_contraseña));
                    }
                }
            });


        //Boton Atras
       builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.cancel();
           }
       });
        builder.show();
    }



    // Modal Crear BD
    private void ShowModalCreateBD(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.crear_base_datos);
        builder.setMessage(R.string.por_favor_ofresca_siguientes_datos);
        builder.setCancelable(false); //no permite cerrar el dialogo si damos click afuera o boton atras


        //Obtiene una instancia del view a mostrar (se toma de un archivo xml layout)
        //El archivo "R.layout.input_values" es un xml layout que solo contiene un textedit
        View view = LayoutInflater.from(getContext()).inflate(R.layout.modal_crear_bd,null, false);

        //Se obtiene una instancia de los objetos de la vista
        G_editPathBD   = (EditText) view.findViewById(R.id.mabrirbd_edit_pathBD);
        final EditText editPass     = (EditText) view.findViewById(R.id.mabrirbd_edit_passbd);
        final Button btn_buscarPath = (Button) view.findViewById(R.id.mabrirbd_btn_path);
        final CheckBox check_Encrypt = (CheckBox) view.findViewById(R.id.mcrearbd_check_encripbd);
        final CheckBox check_mismoPass = (CheckBox) view.findViewById(R.id.mcrearbd_check_mismopass);
        final EditText editPassEncrypt = (EditText) view.findViewById(R.id.mcrearbd_edit_passencript);
        final ImageView img1 = (ImageView) view.findViewById(R.id.mcrearbd_img1);
        final ImageView img2 = (ImageView) view.findViewById(R.id.mcrearbd_img2);
        final ImageView img_showhide1 = (ImageView) view.findViewById(R.id.mcrearbd_img_showhide1);
        final ImageView img_showhide2 = (ImageView) view.findViewById(R.id.mcrearbd_img_showhide2);
        final ImageView img_keyboard1 = (ImageView) view.findViewById(R.id.mcrearbd_img_keyboard1);
        final ImageView img_keyboard2 = (ImageView) view.findViewById(R.id.mcrearbd_img_keyboard2);



        G_editPathBD.setEnabled(false); //No deja que se modifique el boron

        builder.setView(view); //Colocando la vista en el dialogos


        //Eventos de los CheckBox:
        check_Encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(check_Encrypt.isChecked()){
                    check_mismoPass.setVisibility(View.VISIBLE);
                    check_mismoPass.setChecked(true);

                }else{
                    check_mismoPass.setVisibility(View.INVISIBLE);
                    editPassEncrypt.setVisibility(View.INVISIBLE);
                    img2.setVisibility(View.INVISIBLE);
                    img_showhide2.setVisibility(View.INVISIBLE);
                    img_keyboard2.setVisibility(View.INVISIBLE);
                    editPassEncrypt.setText("");
                }

            }
        });

        check_mismoPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(check_mismoPass.isChecked()){
                    editPassEncrypt.setVisibility(View.INVISIBLE);
                    img2.setVisibility(View.INVISIBLE);
                    img_showhide2.setVisibility(View.INVISIBLE);
                    img_keyboard2.setVisibility(View.INVISIBLE);

                }else{
                    editPassEncrypt.setVisibility(View.VISIBLE);
                    img2.setVisibility(View.VISIBLE);
                    img_showhide2.setVisibility(View.VISIBLE);
                    img_keyboard2.setVisibility(View.VISIBLE);

                }

            }
        });

        //imagen teclado virtual #1
        img_keyboard1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard_y keyboard_y = new Keyboard_y(getContext(), true);
                keyboard_y.ShowKeyboard(editPass);
            }
        });

        //imagen teclado virtual #2
        img_keyboard2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Keyboard_y keyboard_y = new Keyboard_y(getContext(), true);
                keyboard_y.ShowKeyboard(editPassEncrypt);
            }
        });


        //Imagen pass #1 generar una contraseña
        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UisY uisY = new UisY(getContext(),"", null);
                editPass.setText( uisY.GenerateKey(uisY.getCharSet(), 10));
            }
        });
        //Imagen pass #2 generar una contraseña
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UisY uisY = new UisY(getContext(),"", null);
                editPassEncrypt.setText( uisY.GenerateKey(uisY.getCharSet(), 10));
            }
        });

        //imagen show/hide pass #1
        img_showhide1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isvisible1){
                    editPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    img_showhide1.setImageResource(R.drawable.ic_showpass);
                    isvisible1 = true;
                }else{
                    editPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    img_showhide1.setImageResource(R.drawable.ic_noshowpass);
                    isvisible1 = false;
                }
            }
        });
        //imagen show/hide pass #2
        img_showhide2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isvisible2){
                    editPassEncrypt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    img_showhide2.setImageResource(R.drawable.ic_showpass);
                    isvisible2 = true;
                }else{
                    editPassEncrypt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    img_showhide2.setImageResource(R.drawable.ic_noshowpass);
                    isvisible2 = false;
                }

            }
        });;


        //Boton Buscar: muestra un cuadro de dialogo para crear un fichero cualquiera
        btn_buscarPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                     G_ResultCode = 13;
                    launcherResult.launch(intent);


                    //getActivity().startActivityForResult(intent, MainActivity.CREATE_NEW_FILE_CODE);
            }
        });

        //Boton Crear la BD
        builder.setPositiveButton(R.string.crear_bd, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String pathBD = G_editPathBD.getText().toString().trim();
                String pass = editPass.getText().toString();
                String passEncrypt = editPassEncrypt.getText().toString();




                //Comprobaciones de campo
                if (!pathBD.equals("") && !pass.equals("")){


                    if (check_Encrypt.isChecked()){ //Si se debe encriptar la BD

                        if (check_mismoPass.isChecked()){ //Con la misma clave
                            //intentando crear la BD
                            try {
                                UtilsY.crearBDY(pathBD, pass);
                                //Encriptando la BD
                                UtilsY.encryptFile(pass,pathBD,pathBD);
                                UtilsY.msgY(getString(R.string.bdcreadaok));
                            } catch (Exception e) {
                                UtilsY.msgY( "Error: "+ e);
                            }


                        }else{//si se debe encriptar con una clave dada:
                            //Comprobando si se ha dado una clave de encriptacion

                            if(!passEncrypt.equals("")){
                                //intentando crear la BD
                                try {
                                    UtilsY.crearBDY(pathBD, pass);
                                    //Encriptando la BD
                                    UtilsY.encryptFile(passEncrypt,pathBD,pathBD);
                                    UtilsY.msgY(getString(R.string.bdcreadaok));

                                } catch (Exception e) {
                                    UtilsY.msgY("Error: "+ e);
                                }

                            }else{//si no se ha dado una clave de encriptación
                                UtilsY.msgY(getContext().getString(R.string.debe_colocarse_clave_encriptacion));
                            }

                        }

                    }else{ //si la BD no esta encriptada
                        //intentando crear la BD
                        try {
                            UtilsY.crearBDY(pathBD, pass);
                            UtilsY.msgY(getString(R.string.bdcreadaok));
                        } catch (Exception e) {
                            UtilsY.msgY( "Error: "+ e);
                        }
                    }

                }else{
                    UtilsY.msgY( getContext().getString(R.string.debe_proveer_direccion_contraseña));
                }
            }
        });


        //Boton Atras
        builder.setNegativeButton(getContext().getString(R.string.cancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();



    }





}