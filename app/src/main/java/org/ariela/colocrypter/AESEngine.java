package org.ariela.colocrypter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ariela on 12/29/17.
 */

public class AESEngine {

    public class AESReturn {
        int retCode;
        String lastError;
        String data;
    }

    public static final int AES_OK              = 1;
    public static final int AES_KEY_GEN_ERROR   = 3;
    public static final int AES_KEY_ENGINE_INIT = 2;
    public static final int AES_ENCRYPT_ERROR   = 4;
    public static final int AES_WRITE_ERROR     = 5;
    public static final int AES_READ_ERROR      = 6;
    public static final int AES_DECRYPT_ERROR   = 7;
    public static final int AES_CANNOT_MAKE_DIR = 8;

    // Aux.
    private final static String ENCODING = "UTF-8";
    private final static String ALGORITHM = "AES/CBC/PKCS5Padding";
    private final static int NCHARS = 16;

    // Private variables
    private byte[] rawdata;
    private byte[] keyBytes;

    // AES Variables
    private static Cipher cipher;
    private static SecretKeySpec secretKeySpec;
    private static IvParameterSpec ivParameterSpec;

    public AESEngine (){

    }

    public AESReturn getEmptyReturn(){
        AESReturn r = new AESReturn();
        r.data = "";
        r.lastError = "";
        r.retCode = AES_OK;
        return r;
    }

    public AESReturn initEngine(String passwd){

        AESReturn r = getEmptyReturn();

        // Initializing variables.
        rawdata = null;

        // Transforming the passwd to 16 bytes.
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            InputStream in = new ByteArrayInputStream(Charset.forName(ENCODING).encode(passwd).array());
            byte[] buffer = new byte[NCHARS];
            int byteCount;
            while ((byteCount = in.read(buffer)) > 0) {
                digester.update(buffer, 0, byteCount);
            }
            keyBytes = digester.digest();
        }
        catch(Exception e){
            r.lastError = e.toString();
            r.retCode = AES_KEY_GEN_ERROR;
            return r;
        }

        // Initializing the crypto engine
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        }
        catch(Exception e){
            r.lastError = e.toString();
            r.retCode = AES_KEY_ENGINE_INIT;
            return r;
        }
        secretKeySpec = new SecretKeySpec(keyBytes, "AES");
        ivParameterSpec = new IvParameterSpec(keyBytes);

        return r;

    }


    //====================== AES Encryption ======================
    public AESReturn encrypt(String plaintext,String dataFile){

        AESReturn r = getEmptyReturn();

        try{
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            rawdata = cipher.doFinal(plaintext.getBytes("UTF8"));
        }
        catch(Exception e){
            r.lastError = e.toString();
            r.retCode = AES_ENCRYPT_ERROR;
            return r;
        }


        // Writing the file with encrypted data.
        try{
            DataOutputStream fos = new DataOutputStream(new FileOutputStream(dataFile));

            // This way the first four bytes represent the length of the written data.
            fos.writeInt((int)rawdata.length);
            fos.write(rawdata,0,rawdata.length);
            fos.close();

        }
        catch(Exception e){
            r.lastError = e.toString();
            r.retCode =  AES_WRITE_ERROR;
            return r;
        }
        return r;
    }

    //====================== AES Decryption ======================
    public AESReturn decrypt(String dataFile){

        AESReturn r = getEmptyReturn();

        // Opening the file for reading
        byte[] eBytes = null;
        try{
            DataInputStream fis = new DataInputStream(new FileInputStream(dataFile));

            // Reading the size of the written files
            int size = fis.readInt();

            // Reading the rest of the file
            eBytes = new byte[size];
            fis.readFully(eBytes);
            fis.close();

        }
        catch(Exception e){
            r.retCode = AES_READ_ERROR;
            r.lastError = e.toString();
            return r;
        }


        // The data is read and now we attempt to decrypted.
        try{
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            rawdata = cipher.doFinal(eBytes);
            r.data = new String(rawdata,ENCODING);
        }
        catch(Exception e){
            r.retCode = AES_DECRYPT_ERROR;
            r.lastError = e.toString();
            return  r;
        }

        return r;
    }

}
