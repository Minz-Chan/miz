package com.miz.utils.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {

    /**
     * 加密算法RSA
     */
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 这个值关系到块加密的大小，可以更改，但是不要太大，否则效率会低
     */
    private static final int KEY_SIZE = 1024;

    /**
     * 生成密钥对
     */
    public static KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM,
                new BouncyCastleProvider());
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        return keyPairGen.generateKeyPair();
    }

    /**
     * 保存密钥对
     * @param kp KeyPair
     * @param keyFilePath key输出文件目录
     */
    public static void saveKeyPair(KeyPair kp, String keyFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(keyFilePath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        // 生成密钥
        oos.writeObject(kp);
        oos.close();
        fos.close();
    }

    /**
     *  从(调用 saveKeyPair 保存的)文件获取密钥对
     * @param keyFilePath key文件目录
     * @return KeyPair
     */
    public static KeyPair keyPairOf(String keyFilePath) throws GeneralSecurityException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(keyFilePath);
        ObjectInputStream oos = new ObjectInputStream(fis);
        KeyPair kp = (KeyPair) oos.readObject();
        oos.close();
        fis.close();
        return kp;
    }

    /**
     *  RSA签名
     * @param original 原数据
     * @param privateKey 密钥
     * @param signatureAlgorithm 签名算法
     * @return 签名后的字符串
     */
    public static String sign(byte[] original, String privateKey, SignatureAlgorithm signatureAlgorithm) throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Signature signature = Signature.getInstance(signatureAlgorithm.name());
        signature.initSign(keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes)));
        signature.update(original);
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    /**
     *  验证RSA签名
     * @param original 原数据
     * @param publicKey 公钥
     * @param sign 签名
     * @param signatureAlgorithm 算法
     * @return 是否严重通过
     */
    public static boolean verify(byte[] original, String publicKey, String sign, SignatureAlgorithm signatureAlgorithm) throws GeneralSecurityException {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(signatureAlgorithm.name());
        signature.initVerify(publicK);
        signature.update(original);
        return signature.verify(Base64.getDecoder().decode(sign));
    }

    /**
     *  加密
     * @param pk 加密的公钥
     * @param original 明文
     * @return 加密后的密文
     */
    public static byte[] encrypt(PublicKey pk, byte[] original) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM, new BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, pk);
        int blockSize = cipher.getBlockSize();
        int outputSize = cipher.getOutputSize(original.length);
        int leavedSize = original.length % blockSize;
        int blocksSize = leavedSize != 0 ? original.length / blockSize + 1 : original.length / blockSize;
        byte[] ciphertext = new byte[outputSize * blocksSize];
        int i = 0;
        while (original.length - i * blockSize > 0) {
            if (original.length - i * blockSize > blockSize) cipher.doFinal(original, i * blockSize, blockSize, ciphertext, i * outputSize);
            else cipher.doFinal(original, i * blockSize, original.length - i * blockSize, ciphertext, i * outputSize);
            i++;
        }
        return ciphertext;
    }

    /**
     *  解密
     * @param pk 解密的密钥
     * @param ciphertext 已经加密的数据
     * @return 解密后的明文
     */
    public static byte[] decrypt(PrivateKey pk, byte[] ciphertext) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM, new BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, pk);
        int blockSize = cipher.getBlockSize();
        ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
        int j = 0;
        while (ciphertext.length - j * blockSize > 0) {
            bout.write(cipher.doFinal(ciphertext, j * blockSize, blockSize));
            j++;
        }
        return bout.toByteArray();
    }

}
