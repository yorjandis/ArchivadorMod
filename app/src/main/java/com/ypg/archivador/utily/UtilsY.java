package com.ypg.archivador.utily;

//Clase de funciones utilitarias:

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.ypg.archivador.MainActivity;
import com.ypg.archivador.R;
import com.ypg.archivador.frag.frag_values;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class UtilsY {

    public static  Context G_context;
    public static View G_view; //test: para mostrar un mensaje en un snackbar (se establece en ActivityMain y se asigna la ToolBar)



    //Variables Globales

    //utilizadas para el algoritmo AES/GCM/NoPadding
    private static byte[] G_iv = new byte[12];                      // vector de inicialización nonce para AES/GCM
    private static final String ALG = "AES";                        //Algoritmo utilizado en la generación de la clave
    private static final String ALG_Cipher = "AES/GCM/NoPadding";   //Algoritmo de encriptamiento
    ///////////fin/////////////

    public static String    G_patBD = "";                           //almacena la ruta del archivo actual de la BD (se modifica en f_AbrirBD.java)
    public static  String   pass = "";                              //Almacena la clave actual de cifrado/descifrado dada por el usuario.
    public static String    passEncripted = "";                     //Almacena la clave de escriptación actual
    public static int       spinCatgItemposition = -1;              //Almacena el item actualmente seleccionado en la lista de Categorias
    public static String    catgName = "";                          //Almacena el nombre de la Categoria Actualmente seleccionada
    public static String    entName = "";                           //Almacena el nombre de la Categoria Actualmente seleccionada
    public static boolean   isEditModif = false;                    //Para chequear si el edittext de los values ha cambiado su contenido
    public static boolean   isBDOpenEnc = false;                    //Almacena true, si la BD se desencriptó al abrir
    public static boolean   isBDOpenEncMismaPass = false;           //si al abrir la base de datos esta se descriptó con la misma clave de cifrado
    public  static boolean  privacyisAcepted = false;               //especifica si se ha aceptado (true) los terminos y condidiones
    public static String    fragmentActualName = "";                //(bd,home,value)Almacena el nombre del fagmento actualmente cargado
    public static String    valueCopy = "";                         //variable del portapapeles interno
    public static Boolean    isVersionDisponible = false;           //se pone a true si se encuentra una versión disponible

//FUNCIONES GNERALES ::::::::::::::::::::::::::::::::::::::



//FUNCIONES fichero INI::::::::::::::::

    //función que crea la base de datos en la raíz de la memmoria. No la carga en la GUI
    public  static boolean crearBDY(String pathBDP, String passP) throws Exception {
        //Crear el fichero en la memoria

        //Lista de Categorias por defecto a adicionar (Ya encriptadas)
        List<String> CatgDefault = new ArrayList<>();
        CatgDefault.add("[" + encriptarY("NOTAS", passP, false)        + "]");
        CatgDefault.add("[" + encriptarY("IDENTIDAD", passP, false)    + "]");
        CatgDefault.add("[" + encriptarY("BANCOS", passP, false)       + "]");
        CatgDefault.add("[" + encriptarY("CONTRASEÑAS", passP, false)  + "]");
        CatgDefault.add("[" + encriptarY("FINANZAS", passP, false)     + "]");
        CatgDefault.add("[" + encriptarY("WEBS", passP, false)         + "]");

        //Creando y Chequeando que el nuevo fichero a crear
        try {
            File file = new File(pathBDP);

                try {
                    OutputStreamWriter write;
                    write = new OutputStreamWriter(new FileOutputStream(file));

                    for (int i = 0; i < CatgDefault.size(); i++){
                        write.write(CatgDefault.get(i) + "\n" );
                    }

                    write.close(); //escribe los valores y cierra el fichero
                       return true;
                }catch (Exception e){
                    return  false;
                }

        } catch (Exception e){
         return  false;
        }

    }


    //Devuelve la lista de categorias de la BD
    public  static List<String> loadCatg(){
        //File inifile = new File(Environment.getExternalStorageDirectory() + "/yorjj.txt");

        List<String> lista = new ArrayList<>();

        if (G_patBD.equals("") || !new File(G_patBD).exists()){ return lista;} //No carga la BD

        try {
            JIniFile ini = new JIniFile(UtilsY.G_patBD);
            ArrayList<String> sections =  ini.ReadSections(); //Obteniendo todas las secciones (Categorias)

            if (sections.size() > 0) { //si existen secciones que cargar
                //recorriendo las secciones para descifrarlas
                for (int i = 0; i < sections.size(); i++){

                    lista.add(desencriptarY(sections.get(i), pass));
                }
                
                return lista;

            }else{

                msgY(G_context.getResources().getString(R.string.el_fichero_de_bd_esta_vacio));
            }

        }catch (Exception e){

            msgY( "Error: "+ e);
        }

        return  lista;
    }


    //Devuelve las entradas para una Categoria Dada
    public static List<String> loadEnt(String Catg){

        List<String>  lista = new ArrayList<>();

        try{
            JIniFile ini = new JIniFile(G_patBD);
            ArrayList<String> list = ini.ReadSection(encriptarY(Catg,pass, false)); //Obteniendo todas las keys de una sección

            if (list.size() > 0) { //si existen entradas que cargar
                //recorriendo las secciones para descifrarlas

                for (int i = 0; i < list.size(); i++){
                   lista.add(desencriptarY(list.get(i), pass));
                }

                return lista;
            }

        }catch (Exception e){
            msgY("Error: " + e);
        }

        return lista;
    }


    //Devuelve el contenido de una entrada OK
    public static String loadEntvalues(String Catg, String Ent, String ValueDefault){
        String resul = "";

        try{
            JIniFile ini = new JIniFile(G_patBD);
            resul = desencriptarY(ini.ReadString(encriptarY(Catg,pass, false), encriptarY(Ent,pass, false), ValueDefault),pass);
        }catch (Exception e){
            msgY("Error: " + e);
        }
        //Reemplazando los caracteres especiales de espacio por saltos de linea
        resul =  resul.replaceAll("%/", "\n");
        return resul;
    }

    //Adicionar nueva Categoria a la BD OK
    public static Boolean addNewCatg(String catg){

        boolean resul = false;
        try{
            JIniFile ini = new JIniFile(UtilsY.G_patBD);
            ini.WriteString(encriptarY(catg,pass, false),encriptarY("EntExample",pass, false), encriptarY("ValorExample",pass, true));
            ini.UpdateFile();
            resul = true;
        }catch(Exception e){
            msgY("Error: "+ e);
        }

        return  resul;
    } //AddNewCatg


    //Eliminar una Categoria de la BD OK
    public static Boolean delCateg(String Catg){
        boolean resul = false;
        if (G_patBD.equals("") || !new File(G_patBD).exists()){ return resul;} //No carga la BD
        try {
            JIniFile ini = new JIniFile(G_patBD);
            ini.EraseSection(encriptarY(Catg,pass, false));
            //error con ini.UpdateFile()
            resul = ini.UpdateFile();
        }catch (Exception e){
            msgY("Error: " + e);
        }
        return resul;
    }

    //Elimina una entrada (key) de una categoría dada
    public static Boolean delEnt(String Catg, String Ent){
        boolean resul = false;

        try{
            JIniFile ini = new JIniFile(G_patBD);
            ini.DeleteKey(encriptarY(Catg,pass, false),encriptarY(Ent,pass, false));
            resul = ini.UpdateFile();
        }catch (Exception e){
            msgY("Error: "+ e);
        }
        return resul;
    }

    //Adiciona una nueva entrada en una categoría dada OK
    public static Boolean addNewEnt(String Catg, String NewEnt, String NewValueDefault){
        boolean resul = false;

        try{
            JIniFile ini = new JIniFile(G_patBD);
            ini.WriteString(encriptarY(Catg,pass, false), encriptarY(NewEnt, pass, false),encriptarY(NewValueDefault,pass, true));
            resul = ini.UpdateFile();
        }catch (Exception e){
            msgY("Error: " + e);
        }

        return resul;
    }

    //Modifica el value de una entrada
    public static Boolean modEnt(String Catg, String Ent, String ValueDefault){
        boolean resul = false;
        try{
            JIniFile ini = new JIniFile(G_patBD);
            ini.WriteString(encriptarY(Catg,pass, false), encriptarY(Ent, pass, false),encriptarY(ValueDefault,pass, true));
            resul = ini.UpdateFile();
        }catch (Exception e){
            msgY("Error: " + e );
        }

        return resul;
    }


    // FUNCIONES DE CIFRADO Y DESCIFRADO :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    //Genera una Secrekey A partir de una clave dada por el usuario
    private static SecretKeySpec generarkeyY(String pass) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256"); //genera un hash de 256
        byte[] key = pass.getBytes(StandardCharsets.UTF_8); //pasamos la clave a arreglo de bytes
        key = sha.digest(key); //generamos el hash
        return new SecretKeySpec(key, ALG);
    }




   //Cifrar un texto
    public static String Old_encriptarY(String txtToEncode, String pass) throws Exception {
        SecretKeySpec secretKeySpec = generarkeyY(pass);
        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] datosencriptados = cipher.doFinal(txtToEncode.getBytes());
        String datos =  android.util.Base64.encodeToString(datosencriptados, android.util.Base64.NO_PADDING + android.util.Base64.URL_SAFE);
        //String datos = Base64.encodeToString(datosencriptados, Base64.URL_SAFE + Base64.NO_PADDING);
        datos = datos.replace("\n", ""); //quitando los saltos de linea
        return datos;
    }

   //Descifrar un texto
    public static String Old_desencriptarY(String txtToDecode, String pass) throws Exception {
        SecretKeySpec secretKeySpec = generarkeyY(pass);
        Cipher cipher = Cipher.getInstance(ALG);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] datosdesencriptados = android.util.Base64.decode(txtToDecode, android.util.Base64.URL_SAFE);
        //byte[] datosdesencriptados = Base64.decode(txten,Base64.URL_SAFE + Base64.NO_PADDING);
        byte[] datos = cipher.doFinal(datosdesencriptados);
        String resul = new String(datos);
        resul = resul.replace("\n", "");//quitando los saltos de linea
        return resul;
    }

    /////////////////Nuevas Funciones de encriptación para el algoritmo AES/GCM/NoPadding //////////////////////////////////////////////////////////////////////

/*        //Generadorde la clave de 128 bits a partir de la contraseña dada por el usuario (Yor esta función es muy relantece la app mucho)
        public static SecretKey generateSecretKey(String password, byte [] iv) throws NoSuchAlgorithmException, InvalidKeySpecException {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), iv, 65536, 128); //  AES-128
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        }*/


        //cifrar una cadena de texto
        public static String encriptarY(String txtToDecode, String pass, Boolean isValue) throws Exception {

            byte[] result = new byte[0]; //Resultado final (arreglo de bytes)
            String resultFinal = "";

            ////////////////Yor: estas dos  lineas tienen el objetivo de crear un vector de inicializacion  IV distinto en cada caso
            ////////////////Pero tiene el invonveniente de generar una cadena cifrada distinta cada vez que se cifra
            /////////////////// Yor de momento estas lineas solo estarán disponibles para cifrar el contenido de las values
            //Prepare the nonce
            if(isValue == true){
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(G_iv); //Noonce should be 12 bytes

            }else { //para las categoria y las entradas utiliza un IV fijo, para permitir la lectura posterior del ini

                G_iv = "r8ei#JTFS5*G".getBytes(StandardCharsets.UTF_8);  //Utilizando un vector de inicialización fijo (crea más vulnerabilidad pero hace que funcione)

            }



            //Prepare your key/password
            SecretKey secretKey =  generarkeyY(pass); // generateSecretKey(pass, G_iv);


            Cipher cipher = Cipher.getInstance(ALG_Cipher);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, G_iv);

            //Encryption mode on!
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            //Encrypt the data
            byte [] encryptedData = cipher.doFinal(txtToDecode.getBytes(StandardCharsets.UTF_8));

            //Concatenate everything and return the final data
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + G_iv.length + encryptedData.length);
            byteBuffer.putInt(G_iv.length);
            byteBuffer.put(G_iv);
            byteBuffer.put(encryptedData);

            result = byteBuffer.array();
            resultFinal = Base64.encodeToString(result, Base64.NO_PADDING + Base64.URL_SAFE);
            resultFinal = resultFinal.replace("\n", "");//quitando los saltos de linea

            //Convertir a cadena de texto plano codificada
            return resultFinal;
        }





        //descifrar una cadena de texto
        public static String desencriptarY( String txtToDecode, String pass)
                throws Exception{

            //Pasando el texto de entrada a bytes para que pueda ser leído correctamente
            byte[] txtADec = Base64.decode(txtToDecode, Base64.NO_PADDING + Base64.URL_SAFE);

            byte[] result; //Resultado final (arreglo de bytes)
            String resultFinal = "";

            //Wrap the data into a byte buffer to ease the reading process
            ByteBuffer byteBuffer = ByteBuffer.wrap(txtADec);

            int noonceSize = byteBuffer.getInt();

            //Make sure that the file was encrypted properly
            if(noonceSize < 12 || noonceSize >= 16) {
                msgY("Error. Es posible que contenido no este cifrado");
                //Log.d("yyy", "Nonce size is incorrect. Make sure that the incoming data is an AES encrypted file.");
            }
            byte[] iv = new byte[noonceSize];
            byteBuffer.get(iv);

            //Prepare your key/password
            SecretKey secretKey =  generarkeyY(pass); //generateSecretKey(pass, iv);

            //get the rest of encrypted data
            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            Cipher cipher = Cipher.getInstance(ALG_Cipher);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

            //Encryption mode on!
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            //Encrypt the data
            result = cipher.doFinal(cipherBytes);
            resultFinal = new String(result,StandardCharsets.UTF_8);
           // resultFinal = resultFinal.replace("\n", "");//quitando los saltos de linea

            return resultFinal;

        }





    //////////////////////////////////////// FIN  //////////////////////////////////////////////



    //encriptar un fichero
    public static Boolean encryptFile(String key, String inputFilea, String outputFilea) {
        try {
            SecretKeySpec secretKeySpec = generarkeyY(key);
            File inputFile = new File(inputFilea); //fichero de entrada
            File outputFile = new File(outputFilea); //fichero de salida (encriptado)
            doCrypto(Cipher.ENCRYPT_MODE, secretKeySpec, inputFile, outputFile);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    //desencriptar un fichero
    public static Boolean decryptFile(String key, String inputFilea, String outputFilea) {
        try{
            SecretKeySpec secretKeySpec = generarkeyY(key);
            File inputFile = new File(inputFilea); //fichero de entrada (encriptado)
            File outputFile = new File(outputFilea); //fichero de salida
            doCrypto(Cipher.DECRYPT_MODE, secretKeySpec, inputFile, outputFile);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    //calcula y devuelbe  el hash de un fichero.
    public static String hashFile(String filepath, String alg) throws IOException, NoSuchAlgorithmException {
        // Set your algorithm
        // "MD2","MD5","SHA","SHA-1","SHA-256","SHA-384","SHA-512"

        File file = new File(filepath);

        if (file.exists()){
            MessageDigest md = MessageDigest.getInstance(alg); //el parametro alg contiene el algoritmo a utilizar
            FileInputStream fis = new FileInputStream(file);
            byte[] dataBytes = new byte[1024];

            int nread;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();

            StringBuilder sb = new StringBuilder();
            for (byte mdbyte : mdbytes) {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        }
        return "";

    }

    //Comprueba el hash de un fichero
    public static Boolean hashFileCheck(String filePath, String hashAlg, String hash) {

        try {
            String s =  hashFile(filePath,hashAlg);
            return s.contentEquals(hash); //el hash coincide
        } catch (Exception e) {
            System.out.print("Error: "+ e);
        }
        return false;
    }

    //Calcula el hash de un texto
    public static String hashText(String text, String alghash){
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance(alghash);
            hash = md.digest(text.getBytes());

        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                sb.append(0);
                sb.append(hex.charAt(hex.length() - 1));
            } else {
                sb.append(hex.substring(hex.length() - 2));
            }
        }
        return sb.toString();
    }

    //Comprueba el hash de un texto
    public static Boolean hashTextCheck(String text, String hash, String alg){
        String temp = hashText(text,alg);
        return temp.equals(hash);
    }




    //encripta/Desencripta ficheros
    private static void doCrypto(int cipherMode, SecretKeySpec key, File inputFile, File outputFile) {
        try {
            //Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(cipherMode, key);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

        } catch (Exception e) {
            msgY("Error encrypting/decrypting file: "+ e);

        }

    }


    // FIN => FUNCIONES DE CIFRADO Y DESCIFRADO ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::



    //muestra un mensaje (para propositos de debug)
    public  static void msgY(String message){

        Snackbar snackbar = Snackbar.make(G_view, message,Snackbar.LENGTH_SHORT);
        snackbar.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE);
        snackbar.show();

}




    //Renombra una entrada (Devuelve true si realizo bien la operación)
    public static boolean renEnt(String oldEntP, String newEntP) {
      try {
          // input the file content to the StringBuffer "input"
          BufferedReader file = new BufferedReader(new FileReader(G_patBD));
          StringBuffer inputBuffer = new StringBuffer(); //almacena el contenido del fichero
          String line;
          String entradaEnc = encriptarY(oldEntP, pass, false);
          String newEntEnc = encriptarY(newEntP, pass, false);

          while ((line = file.readLine()) != null) {
              inputBuffer.append(line);
              inputBuffer.append('\n');
          }
          file.close();
          String inputStr = inputBuffer.toString(); //Pasando el contenido a  cadena
          //Reemplazando el contenido
          inputStr =  inputStr.replace(entradaEnc+"=",newEntEnc+"=" );


          // Reescribiendo el contenido de nuevo
          FileOutputStream fileOut = new FileOutputStream(G_patBD);
          fileOut.write(inputStr.getBytes());
          fileOut.close();
          return  true;

      } catch (Exception e) {
          return false;
      }
  }


    //Renombrar Categoria (Devuelve true si realizó bien la operación)

    public static boolean renCatg(String oldCatg, String newCatg){
        try {
            // input the file content to the StringBuffer "input"
            BufferedReader file = new BufferedReader(new FileReader(G_patBD));
            StringBuffer inputBuffer = new StringBuffer(); //almacena el contenido del fichero
            String line;
            String entradaEnc = encriptarY(oldCatg, pass, false); //codificado la categoria antigua
            String newEntEnc = encriptarY(newCatg, pass, false); //Codificando la nueva Categoria

            while ((line = file.readLine()) != null) {
                inputBuffer.append(line);
                inputBuffer.append('\n');
            }
            file.close();
            String inputStr = inputBuffer.toString(); //Pasando el contenido a  cadena
            //Reemplazando el contenido
            inputStr =  inputStr.replace("[" + entradaEnc + "]","[" + newEntEnc + "]" );


            // Reescribiendo el contenido de nuevo
            FileOutputStream fileOut = new FileOutputStream(G_patBD);
            fileOut.write(inputStr.getBytes());
            fileOut.close();
            return  true;

        } catch (Exception e) {
            return false;
        }

    }


    //Cierra la BD y sale a la pantalla de inicio
    public static  void closeBD(){
        //Clear portapapeles:
        ClipboardManager clipService = (ClipboardManager) G_context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("", "");
        clipService.setPrimaryClip(clipData);

        //Actualizando los valores que se hayan modificado en el frag values
        //Chequeando que estamos en el fragmento fragValues



        if(UtilsY.fragmentActualName.equals("value")){
            //determinando primero si se ha modificdo el editvalue
            if(UtilsY.isEditModif) { //se ha modificado
                //Salvando la información
                frag_values.saveValues(UtilsY.catgName, UtilsY.entName);
            }
        }

        //encriptando la BD (Solo si se abrio con una contraseña de encriptacion)
        if(UtilsY.isBDOpenEnc){
            if (UtilsY.isBDOpenEncMismaPass){ //se ha utilizado el mismo pass para encriptación
                UtilsY.encryptFile(UtilsY.pass, UtilsY.G_patBD, UtilsY.G_patBD);
            }else{//se ha utilizado otra clave
                UtilsY.encryptFile(UtilsY.passEncripted, UtilsY.G_patBD, UtilsY.G_patBD);
            }
        }
        //Resetea las variables globales:
        UtilsY.spinCatgItemposition = -1;
        UtilsY.G_patBD = "";
        UtilsY.pass = "";
        UtilsY.passEncripted = "";
        UtilsY.isEditModif = false;
        UtilsY.isBDOpenEnc = false;
        UtilsY.isBDOpenEncMismaPass = false;

        //vuelvo a la pantalla de inicio (y limpio la pila de actividades)
        Intent intent = new Intent(G_context,  MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        G_context.startActivity(intent);

    }



}//class
