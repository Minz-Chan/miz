package com.miz.utils.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class DESUtils {

    /**
     *  加密
     * @param original 明文
     * @param keyBytes 密钥
     * @param iv 向量iv，可增加加密算法的强度
     * @param cipherAlgorithm 算法
     * @return 加密后的密文
     */
    public static byte[] encrypt(byte[] original, byte[] keyBytes, IvParameterSpec iv, CipherAlgorithm cipherAlgorithm) throws GeneralSecurityException {
        DESKeySpec keySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(cipherAlgorithm.getKeyAlgorithm());
        Cipher cipher = Cipher.getInstance(cipherAlgorithm.getCipherAlgorithm());
        if (iv == null) iv = new IvParameterSpec(Arrays.copyOfRange(keyBytes, 0 , 8));
        SecretKey secretKey = keyFactory.generateSecret(keySpec);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
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
    public static byte[] decrypt(byte[] ciphertext, byte[] keyBytes, IvParameterSpec iv, CipherAlgorithm cipherAlgorithm) throws GeneralSecurityException {
        DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(cipherAlgorithm.getKeyAlgorithm());
        Cipher cipher = Cipher.getInstance(cipherAlgorithm.getCipherAlgorithm());
        if (iv == null) iv = new IvParameterSpec(Arrays.copyOfRange(keyBytes, 0 , 8));
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        return cipher.doFinal(ciphertext);
    }

}
