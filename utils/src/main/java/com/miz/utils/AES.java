package com.miz.utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;

public class AES {
    
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
      
    //算法名  
    private static final String KEY_ALGORITHM = "AES";
    //加解密算法/模式/填充方式  
    //可以任意选择，为了方便后面与iOS端的加密解密，采用与其相同的模式与填充方式  
    //ECB模式只用密钥即可对数据进行加密解密，CBC模式需要添加一个参数iv  
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";
  
    //生成密钥  
    public static byte[] generateKey() throws Exception{  
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);  
        keyGenerator.init(128);  
        SecretKey key = keyGenerator.generateKey();  
        return key.getEncoded();  
    }  

    //转化成JAVA的密钥格式  
    public static Key convertToKey(byte[] keyBytes) throws Exception{
        return new SecretKeySpec(keyBytes,KEY_ALGORITHM);
    }  
      
    //加密  
    public static byte[] encrypt(byte[] data,byte[] keyBytes,IvParameterSpec iv) throws Exception {
        //转化为密钥  
        Key key = convertToKey(keyBytes);  
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);  
        //设置为加密模式  
        cipher.init(Cipher.ENCRYPT_MODE, key,iv);
        return cipher.doFinal(data);  
    }

    //解密  
    public static byte[] decrypt(byte[] encryptedData, byte[] keyBytes, IvParameterSpec iv) throws Exception{
        Key key = convertToKey(keyBytes);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);  
        //设置为解密模式  
        cipher.init(Cipher.DECRYPT_MODE, key,iv);  
        return cipher.doFinal(encryptedData);  
    } 
  
}  