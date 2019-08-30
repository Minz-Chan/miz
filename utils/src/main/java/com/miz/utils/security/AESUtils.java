package com.miz.utils.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.Security;

public class AESUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] generateKey() throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(CipherAlgorithm.AES_CBC_PKCS7Padding.getKeyAlgorithm());
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();
        return key.getEncoded();
    }

    /**
     *  加密
     * @param original 明文
     * @param keyBytes 密钥
     * @param iv 向量iv，可增加加密算法的强度
     * @param cipherAlgorithm 算法
     * @return 加密后的密文
     */
    public static byte[] encrypt(byte[] original, byte[] keyBytes, IvParameterSpec iv, CipherAlgorithm cipherAlgorithm) throws GeneralSecurityException {
        //转化为密钥
        Key key = new SecretKeySpec(keyBytes,cipherAlgorithm.getKeyAlgorithm());
        Cipher cipher = Cipher.getInstance(cipherAlgorithm.getCipherAlgorithm());
        //设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, key,iv);
        return cipher.doFinal(original);
    }

    /**
     *  解密
     * @param ciphertext 密文
     * @param keyBytes 密钥
     * @param iv 向量iv
     * @param cipherAlgorithm 算法
     * @return 解密后的明文
     */
    public static byte[] decrypt(byte[] ciphertext, byte[] keyBytes, IvParameterSpec iv, CipherAlgorithm cipherAlgorithm) throws GeneralSecurityException{
        Key key = new SecretKeySpec(keyBytes,cipherAlgorithm.getKeyAlgorithm());
        Cipher cipher = Cipher.getInstance(cipherAlgorithm.getCipherAlgorithm());
        //设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, key,iv);
        return cipher.doFinal(ciphertext);
    }

}
