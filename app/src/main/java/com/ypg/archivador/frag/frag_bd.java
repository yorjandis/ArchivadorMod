package com.ypg.archivador.frag;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.ypg.archivador.MainActivity;
import com.ypg.archivador.R;
import com.ypg.archivador.utily.UtilsY;

import java.util.ArrayList;
import java.util.List;


//Fragmento que maneja la UI de presentación y gestión de las categorias/entradas de la BD
public class frag_bd extends Fragment {


    Spinner    spintCatg;   //View lista de la categoria
    ListView   listEntr;    //View Lista de entradas
    SearchView searchEntr;  //cuadro de busqueda

    FloatingActionsMenu fabmenu;
    FloatingActionButton fabAddCatg, fabAddEnt, fabDelCatg, fabRenCatg;

    ArrayAdapter<String> G_adapterListCatg;     //Adapter para la lista de categorias
    MyListAdapterEnt G_adapterListEnt;          //Adapter para la lista de entradas
    List<String> G_listaEnt;                    //Lista de elementos de entradas



    public frag_bd() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UtilsY.fragmentActualName = "bd";
        MainActivity.imgCopy.setVisibility(View.INVISIBLE);
        MainActivity.imgPaste.setVisibility(View.INVISIBLE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.frag__bd, container, false);
        return fragmentView;

    }//onCreateView


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        searchEntr   = (SearchView) view.findViewById(R.id.fbd_search_entr);
        spintCatg       = (Spinner) view.findViewById(R.id.fbd_spin_catg);
        listEntr        = (ListView) view.findViewById(R.id.fbd_list_entr);

        ConstraintLayout constraintLayout = (ConstraintLayout) view.findViewById(R.id.fbd_constrainlayout);


        fabmenu     = (FloatingActionsMenu) view.findViewById(R.id.fabmenu);
        fabAddCatg  = (FloatingActionButton) view.findViewById(R.id.fbd_fab_add_catg);
        fabAddEnt   = (FloatingActionButton) view.findViewById(R.id.fbd_fab_add_ent);
        fabDelCatg  = (FloatingActionButton) view.findViewById(R.id.fbd_fab_del_catg);
        fabRenCatg  = (FloatingActionButton) view.findViewById(R.id.fbd_fab_ren_catg);





        //Cargando las categorias de la BD, si se provee de un fichero y contraseña válidos:
        List<String> lista;
        lista = UtilsY.loadCatg();

        //Yor IMPORTANTE!!!, Si el tamaño es 0, significa que no hay Categorias que cargar (la BD no se ha cargado correctamente)
        // : se resetea todoo y vuelve al inicio
        if (lista.size() == 0){
            //Reseteando las variables globales:
            UtilsY.spinCatgItemposition = -1;
            UtilsY.G_patBD = "";
            UtilsY.pass = "";
            UtilsY.isEditModif = false;
            UtilsY.isBDOpenEnc = false;
            UtilsY.isBDOpenEncMismaPass = false;
            UtilsY.msgY(getContext().getString(R.string.la_base_datos_no_se_pudo_cargar));
            Navigation.findNavController(view).navigate(R.id.frag_home);
        }

        G_adapterListCatg = new ArrayAdapter<String>(getContext(), R.layout.spinner_items_layout, lista);
        spintCatg.setAdapter(G_adapterListCatg);


        //Implementación del SearchView
        searchEntr.setQueryHint(getContext().getString(R.string.buscar_en_titulos_de_entradas));

        searchEntr.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                // G_adapterListEnt.getFilter().filter(newText);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!newText.equals("")){
                    setFilterListEnt(newText); //Ejecutando la busqueda en los titulos de las entradas
                }else{
                    if (G_adapterListEnt != null){
                        G_adapterListEnt.clear();
                        G_listaEnt = UtilsY.loadEnt(spintCatg.getSelectedItem().toString());
                        G_adapterListEnt.addAll(G_listaEnt);
                    }

                }
                return false;
            }
        });

     


        //selecciona el ultimo item accedido de la lista de categoria (para que al volver de la ventana de values se
                // seleccione la categoria anteriormente marcada)
         if (UtilsY.spinCatgItemposition != -1){
             spintCatg.setSelection(UtilsY.spinCatgItemposition);
         }


         //Almacena la última dirección de la BD cargada:  en sharedPreferent
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config", Context.MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastBDPath", UtilsY.G_patBD);
        editor.commit();



         //Evento del constraintLayout (Solo oculta/muestra el menú flotante)
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fabmenu.isExpanded()){
                    fabmenu.collapse();
                }else{
                    fabmenu.expand();
                }
            }
        });


        //Evento ListCatg (Al hacer click en una categoria)
        spintCatg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                fabmenu.collapse();

                G_listaEnt = UtilsY.loadEnt(G_adapterListCatg.getItem(position));
                G_adapterListEnt = new MyListAdapterEnt(getContext(),R.layout.row_list_entra, G_listaEnt);
                listEntr.setAdapter(G_adapterListEnt);

                UtilsY.spinCatgItemposition = position; //Actualizando info de position en la variable global

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fabmenu.collapse();
            }
        });


        //Evento de ListEntr
        listEntr.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fabmenu.collapse();
                view.setSelected(true);
                //Pasando valores al fragmento
                Bundle bb = new Bundle();
                bb.putString("catg",spintCatg.getSelectedItem().toString());
                bb.putString("ent",listEntr.getItemAtPosition(position).toString());
                Navigation.findNavController(view).navigate(R.id.frag_values, bb);
            }
        });


        //EVENTOS DE LOS BOTONES DEL MENU FLOTANTE

        //Add Catg
        fabAddCatg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowModal_Add_Catg_Ent("cat", spintCatg, listEntr );
            }
        });


        //Renombrar una Categoria
        fabRenCatg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RenombrarCategoria(spintCatg.getSelectedItem().toString());
            }
        });

        //Add Ent
        fabAddEnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowModal_Add_Catg_Ent("ent", spintCatg,  listEntr);
            }
        });

        //Del Catg
        fabDelCatg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.eliminar_la_categoria)+ spintCatg.getSelectedItem().toString());
                builder.setMessage(R.string.quieres_eliminar_Categoria_se_eliminará_entradas_asociadas);

                builder.setPositiveButton(getContext().getString(R.string.eliminar), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                               if (UtilsY.delCateg(spintCatg.getSelectedItem().toString())) {
                                     UtilsY.msgY(getString(R.string.la_categoria_se_elimino_correctamente));
                                   //actualizar la lista de Categorias:
                                   List<String> lista;
                                   lista = UtilsY.loadCatg();
                                   G_adapterListCatg.clear();
                                   G_adapterListCatg.addAll(lista);
                                   G_adapterListCatg.notifyDataSetChanged();

                               }else{

                                   UtilsY.msgY(getString(R.string.no_se_pudo_eliminar_la_categoria) );
                               }//if
                            }
                        });
                builder.setNegativeButton(getContext().getString(R.string.cancelar), (dialog, which) -> dialog.dismiss());
                builder.setCancelable(false);
                builder.show();
            }
        });




    }//onViewCreated


    @Override
    public void onResume() {
        super.onResume();
        UtilsY.fragmentActualName = "bd";
        MainActivity.imgCopy.setVisibility(View.INVISIBLE);
        MainActivity.imgPaste.setVisibility(View.INVISIBLE);
    }

    //ventana modal para adicionar nuevas categorias o entrada  Catg
      //El parámetro view puede ser un Spiner o un  ListView
    private void ShowModal_Add_Catg_Ent(String op, Spinner spinnerCatgP, ListView listEntP) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getContext().getString(R.string.adicionar_nueva_categoria));
        if(op.equals("ent")){ builder.setTitle(getContext().getString(R.string.adicionar_nueva_entrada_en) +": "+ spinnerCatgP.getSelectedItem().toString());}

        builder.setMessage(getContext().getString(R.string.por_favor_coloque_un_nombre));
        builder.setCancelable(false); //no permite cerrar el dialogo si damos click afuera o boton atras


        //Obtiene una instancia del view a mostrar (se toma de un archivo xml layout)
        //El archivo "R.layout.input_values" es un xml layout que solo contiene un textedit
        View view = LayoutInflater.from(getContext()).inflate(R.layout.modal_add_categ_entrad, null, false);

        //Se obtiene una instancia de los objetos de la vista
        final EditText editNewCatg = (EditText) view.findViewById(R.id.maddcatg_edittext);

        if(op.equals("ent")){
            editNewCatg.setHint(getContext().getString(R.string.nueva_entrada));
        }else{
            editNewCatg.setHint(getContext().getString(R.string.nueva_categoria));
        }

        builder.setView(view); //Colocando la vista en el dialogos

        //evento de los botones

        builder.setPositiveButton(getContext().getString(R.string.adicionar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (op.equals("cat")){
                    //determinando si ya existe la categoria en la lista actual
                    int size =  spinnerCatgP.getAdapter().getCount();
                    Boolean temp = false;
                    for (int i = 0; i < size; i++){
                        //
                        if(spinnerCatgP.getAdapter().getItem(i).toString().equalsIgnoreCase(editNewCatg.getText().toString())){
                            temp = true;
                            break;
                        }
                    }

                    //Si no esta el item entonces se adiciona la nueva categoría
                    if (!temp){
                        if (UtilsY.addNewCatg(editNewCatg.getText().toString())){
                            //actualizar la lista de Categorias:
                            List<String> lista;
                            lista = UtilsY.loadCatg();
                            G_adapterListCatg.clear();
                            G_adapterListCatg.addAll(lista);
                            G_adapterListCatg.notifyDataSetChanged();
                        }else{
                            UtilsY.msgY(getString(R.string.error_no_se_ha_podido_adicionar_nueva_categoria));
                        }
                    }else{//Si existe la Categoria lo anuncia
                        UtilsY.msgY( getString(R.string.ya_existe_una_categoría_con_ese_nombre));

                    }
                    //:::::::::::::::::::::ADICIONANDO NUEVA ENTRADA ::::::::::::::::::::::::
                }else{

                    //determinando si ya existe la entrada en la lista actual
                    int size =  listEntP.getAdapter().getCount();
                    Boolean temp = false;
                    for (int i = 0; i < size; i++){
                        //
                        if(listEntP.getAdapter().getItem(i).toString().equalsIgnoreCase(editNewCatg.getText().toString())){
                            temp = true; //si encuentra una ocurrencia pone a true el flag
                            break;
                        }
                    }

                    //Si no esta el item entonces se adiciona la nueva categoría
                    if (!temp){
                        if (UtilsY.addNewEnt(spinnerCatgP.getSelectedItem().toString(), editNewCatg.getText().toString(),"")){
                            //actualizar la lista de Categorias:
                            List<String> lista;
                            lista = UtilsY.loadEnt(spinnerCatgP.getSelectedItem().toString());
                            G_adapterListEnt.clear();
                            G_adapterListEnt.addAll(lista);
                            G_adapterListEnt.notifyDataSetChanged();
                        }else{
                            UtilsY.msgY( getString(R.string.error_no_se_ha_podido_adicionar_la_nueva_entrada));
                        }
                    }else{//Si existe la Categoria lo anuncia
                        UtilsY.msgY( getString(R.string.ya_existe_una_entrada_con_ese_nombre));
                    }

                }//fin else (adicionando nueva entrada

            }
        });

        builder.setNegativeButton(getContext().getString(R.string.cancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }


    //CODIGO PARA GENERAR EL LISTVIEW PERSONALIZADO (Lista de Entradas)
    private class MyListAdapterEnt extends ArrayAdapter<String>{
        private final int layout;
        //Constructor
        public MyListAdapterEnt(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layout = resource;
        }

        @Override
        public View getView(int position,  View convertView,  ViewGroup parent) {
            ViewHolder mainViewholder = null;

            if(convertView == null){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolder viewHolder = new ViewHolder(); //Clase que contendrá los elementos de cada file del listview
                    viewHolder.imageDel = convertView.findViewById(R.id.row_listent_icodel);
                    viewHolder.imageRen = convertView.findViewById(R.id.row_listent_icorem);
                    viewHolder.textView = convertView.findViewById(R.id.row_listent_text);

                    viewHolder.textView.setText(getItem(position));

                    //Evento de la imagen Del Entrada (Elimina la entrada actual)
                    viewHolder.imageDel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                DelEntrada(viewHolder.textView.getText().toString(), spintCatg.getSelectedItem().toString());
                        }
                    });

                    //Evento de la imagen de renombrar entrada
                    viewHolder.imageRen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       RenombrarEntrada(G_adapterListEnt.getItem(position));
                    }
                     });

                    convertView.setTag(viewHolder);

            }else{
                mainViewholder = (ViewHolder) convertView.getTag();
                mainViewholder.textView.setText(getItem(position));

            }

            return convertView;
        }
    }

    //Clase utilitaria que mantendrá los elementos clickeable de cada fila
    private class ViewHolder {
        ImageView imageDel;
        ImageView imageRen;
        TextView textView;
    }



//::::::::::::::::::LISTA DE FUNCIONES ÚTILES :::::::::::::::::::::::::::::::::::::

    //Elimina una entrada (Asociada al imagen Del de cada file de entrada)
    private void DelEntrada(String entradaP, String categP){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.eliminar_Entrada)+ entradaP);
        builder.setMessage(R.string.quieres_eliminar_entrada);

        builder.setPositiveButton(getContext().getString(R.string.eliminar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (UtilsY.delEnt(categP,entradaP )) {
                    UtilsY.msgY(getString(R.string.la_entrada_elimino_correctamente));
                    //actualizar la lista de entradas.
                    List<String> lista;
                    lista = UtilsY.loadEnt(categP);
                    G_adapterListEnt.clear();
                    G_adapterListEnt.addAll(lista);
                    G_adapterListEnt.notifyDataSetChanged();

                }else{

                    UtilsY.msgY(getString(R.string.no_se_pudo_eliminar_entrada));
                }//if
            }
        });
        builder.setNegativeButton(getContext().getString(R.string.cancelar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();

    }

    //Renombra una entrada dada:
    private void RenombrarEntrada(String oldEntradaP){

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.renombrar_entrada)+ oldEntradaP);
            builder.setMessage(R.string.por_favor_coloque_nombre_nueva_entrada);
            builder.setCancelable(false); //no permite cerrar el dialogo si damos click afuera o boton atras

            View view = LayoutInflater.from(getContext()).inflate(R.layout.modal_add_categ_entrad, null, false);

            final EditText editNewEntrada = (EditText) view.findViewById(R.id.maddcatg_edittext);

            builder.setView(view); //Colocando la vista en el dialogos


            builder.setPositiveButton(R.string.renombrar, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!editNewEntrada.getText().toString().trim().equals("")){

                        String newEntrada = editNewEntrada.getText().toString().trim(); //Obteniendo el valor de la nueva entrada(sin espacios)

                        //Determinando si existe una entrada igual:

                        //determinando si ya existe la categoria en la lista actual
                        int size =  listEntr.getAdapter().getCount();
                        Boolean temp = false;
                        for (int i = 0; i < size; i++){
                            //
                            if(listEntr.getAdapter().getItem(i).toString().equalsIgnoreCase(newEntrada)){
                                temp = true;
                                break;
                            }
                        }
                        //Si no existe la entrada:
                        if (!temp){
                            if (UtilsY.renEnt(oldEntradaP, newEntrada)){ //Ejecutando la operación de renombre
                                //actualizar la lista de Categorias:
                                List<String> lista;
                                lista = UtilsY.loadEnt(spintCatg.getSelectedItem().toString());
                                G_adapterListEnt.clear();
                                G_adapterListEnt.addAll(lista);
                                G_adapterListEnt.notifyDataSetChanged();
                                fabmenu.collapse();
                            }else{
                                UtilsY.msgY(getString(R.string.no_ha_podido_renombrar_entrada));
                            }

                        }else{
                            UtilsY.msgY(getString(R.string.ya_existe_entrada_con_este_nombre));
                        }
                    }

                }
            });

            builder.setNegativeButton(getContext().getString(R.string.cancelar
            ), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

               builder.show();
        }//RenombrarEntrada


    //Renombrar una Categoria
    private void RenombrarCategoria(String oldCategoriaP){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.renombrar_una_categoria)+ oldCategoriaP);
        builder.setMessage(getString(R.string.por_favor_coloque_una_nueva_categoria));
        builder.setCancelable(false); //no permite cerrar el dialogo si damos click afuera o boton atras

        View view = LayoutInflater.from(getContext()).inflate(R.layout.modal_add_categ_entrad, null, false);

        final EditText editNewCategoria = (EditText) view.findViewById(R.id.maddcatg_edittext);

        builder.setView(view); //Colocando la vista en el dialogos


        builder.setPositiveButton(R.string.renombrar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!editNewCategoria.getText().toString().trim().equals("")){

                    String newCategoria = editNewCategoria.getText().toString().trim(); //Obteniendo el valor de la nueva entrada(sin espacios)

                    //Determinando si existe una categoría igual:

                    //determinando si ya existe la categoria en la lista actual
                    int size =  spintCatg.getAdapter().getCount();
                    Boolean temp = false;
                    for (int i = 0; i < size; i++){
                        //
                        if(spintCatg.getAdapter().getItem(i).toString().equalsIgnoreCase(newCategoria)){
                            temp = true;
                            break;
                        }
                    }
                    //Si no existe la entrada:
                    if (!temp){
                        if (UtilsY.renCatg(oldCategoriaP, newCategoria)){ //Ejecutando la operación de renombre
                            //actualizar la lista de Categorias:
                            List<String> lista;
                            lista = UtilsY.loadCatg();
                            G_adapterListCatg.clear();
                            G_adapterListCatg.addAll(lista);
                            G_adapterListCatg.notifyDataSetChanged();
                            fabmenu.collapse();
                        }else{
                            UtilsY.msgY(getString(R.string.no_ha_podido_renombrar_entrada));
                        }

                    }else{
                        UtilsY.msgY(getString(R.string.ya_existe_una_categoria_con_ese_nombre));
                    }
                }

            }
        });




        builder.setNegativeButton(getContext().getString(R.string.cancelar
        ), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();

    }




    //Devuelve una lista con los elementos filtrados en un ListView
    //esta función permite hacer las búsquedas de cualquier cadena (case insensitive) en los títulos de las entrads
    private  void setFilterListEnt(String textToSearchP){

            int i = 0;
            List<String> lista = new ArrayList<>();

            for (i = 0; i < G_adapterListEnt.getCount(); i++) {
                if (G_adapterListEnt.getItem(i).toLowerCase().contains(textToSearchP.toLowerCase())) {
                    lista.add(G_adapterListEnt.getItem(i));
                }
            }//for
            G_adapterListEnt.clear();
            G_adapterListEnt.addAll(lista);

    }


}//class