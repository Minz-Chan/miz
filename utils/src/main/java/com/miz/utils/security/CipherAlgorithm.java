package com.miz.utils.security;

public enum CipherAlgorithm {

    AES_CBC_PKCS7Padding("AES","AES/CBC/PKCS7Padding"), // 与iOS端的加密解密兼容
    DES_CBC_PKCS5Padding("DES", "DES/CBC/PKCS5Padding");

    String cipherAlgorithm, keyAlgorithm;
    CipherAlgorithm(String keyAlgorithm, String cipherAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
        this.cipherAlgorithm = cipherAlgorithm;
    }
    public String getCipherAlgorithm() {
        return cipherAlgorithm;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }
}