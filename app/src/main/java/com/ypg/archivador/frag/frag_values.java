package com.ypg.archivador.frag;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.ypg.archivador.MainActivity;
import com.ypg.archivador.R;
import com.ypg.archivador.utily.UtilsY;


public class frag_values extends Fragment {



   private static EditText editValues;


    public frag_values() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UtilsY.fragmentActualName = "value";
        MainActivity.imgCopy.setVisibility(View.VISIBLE);
        MainActivity.imgPaste.setVisibility(View.VISIBLE);
        MainActivity.imgCopy.setImageResource(R.drawable.ic_bartools_copy);
        String cat, ent;
        UtilsY.catgName =  getArguments().getString("catg"); // Almacenando el nombre de la categoria actual
        UtilsY.entName =  getArguments().getString("ent");   //Almacenando el nombre de la entrada actual
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.frag_values, container, false);
    }


    @Override
    public void onResume() {
        super.onResume();
        UtilsY.fragmentActualName = "value";
        MainActivity.imgCopy.setVisibility(View.VISIBLE);
        MainActivity.imgPaste.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textTitle = (TextView) view.findViewById(R.id.fvalues_text_title);
         editValues = (EditText) view.findViewById(R.id.fvalues_editvalues);
        Button btn_back = (Button)  view.findViewById(R.id.fvalues_btn_back);




    //Mostrando el valor de una entrada en el edit de values;
        textTitle.setText(UtilsY.catgName + "\n" + UtilsY.entName);
        editValues.setText(UtilsY.loadEntvalues(UtilsY.catgName, UtilsY.entName,""));


        //Reseteando la variable global
        UtilsY.isEditModif = false; //Indica que no se ha modificado el editvalue


//Determinando si se ha cambiado el contenido del EditText
        editValues.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                    UtilsY.isEditModif = true; //Actualizando la variable
            }
        });



    //Evento del botón Back (Tambien salva la info)
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               //determinando primero si se ha modificdo el editvalue
               if(UtilsY.isEditModif) { //se ha modificado
                   //Salvando la información (el nombre de la Catg y Ent se pasan al fragmento en un bundle)

                   saveValues(UtilsY.catgName, UtilsY.entName); //salvando...
                   UtilsY.modEnt(UtilsY.catgName,UtilsY.entName, editValues.getText().toString().replaceAll("[\r\n]", "%/"));
                   UtilsY.isEditModif = false;
               }

                 Navigation.findNavController(view).navigate(R.id.action_frag_values_to_frag_bd);


            }
        });



    }//onViewCreated


    @Override
    public void onPause() {
        super.onPause();

        //Determinando si se ha apagado la pantalla (y en caso afirmativo salva el contenido)
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();
        if(!isScreenOn) { //SE HA PAPAGADO LA PANTALLA
            String cat, ent;
            assert getArguments() != null;
            cat =  getArguments().getString("catg");
            ent =  getArguments().getString("ent");
            saveValues(cat, ent);
        }


    }

    // Función que salva el contenido de editValues solo si este ha sido modificado
    public static   void  saveValues(String catgP, String entP){
      //determinando primero si se ha modificdo el editvalue
      if(UtilsY.isEditModif) { //se ha modificado
          //Salvando la información
          UtilsY.modEnt(catgP,entP, editValues.getText().toString().replaceAll("[\r\n]", "%/"));
          UtilsY.isEditModif = false;
      }
  }


  //Copia el contenido a la variable interna
    public  static void copyValue(){
        //solo funciona dentro del fragment value
        if(UtilsY.fragmentActualName.equals("value")){
            int startSelection =editValues.getSelectionStart();
            int endSelection =editValues.getSelectionEnd();

            if(startSelection != endSelection ){
                UtilsY.valueCopy = editValues.getText().toString().substring(startSelection, endSelection) ;
                MainActivity.imgCopy.setImageResource(R.drawable.ic_toolsbar_copyactivo);
            }
        }



    }

    //Pega el valor en el EditText
    public static void pasteValue(){
        //solo funciona dentro del fragment value
        if(UtilsY.fragmentActualName.equals("value")){
            int startSelection =editValues.getSelectionStart();
            int endSelection =editValues.getSelectionEnd();

            Editable edit = editValues.getText();
            edit.replace(startSelection,endSelection, UtilsY.valueCopy);
            MainActivity.imgCopy.setImageResource(R.drawable.ic_bartools_copy);
        }

    }


}//Class