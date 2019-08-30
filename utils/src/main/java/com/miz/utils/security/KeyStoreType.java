package com.miz.utils.security;

/**
 * 密钥库类型
 * 秘钥库概念：
 *  所有的公钥和私钥同证书都会被存储在密钥库中
 *  因为证书需要被签名
 *  签名必须使用非对称加密算法+HASH算法
 *  所以一般是MD5WithRSA或者SHA1WithRSA (表格采集自网络)
 */
public enum KeyStoreType {

    /**
     * jks 后缀名 .jks/.ks
     * 【Java Keystore】密钥库的Java实现版本，provider为SUN
     * 特点 : 密钥库和私钥用不同的密码进行保护
     */
    jks,
    /**
     * PKCS12 后缀名  .jce
     * 【JCE Keystore】密钥库的JCE实现版本，provider为SUN JCE
     * 特点 : 相对于JKS安全级别更高，保护Keystore私钥时采用TripleDES
     */
    PKCS12,
    /**
     * JCEKS 后缀名  .p12/.pfx
     * 【PKCS #12】个人信息交换语法标准
     * 特点 : 1、包含私钥、公钥及其证书;
     *       2、密钥库和私钥用相同密码进行保护
     */
    JCEKS,
    /**
     * BKS 后缀名  .bks
     * 【Bouncycastle Keystore】密钥库的BC实现版本，provider为BC
     * 特点 : 基于JCE实现
     */
    BKS,
    /**
     * UBER 后缀名 .ubr
     * 【Bouncycastle UBER Keystore】密钥库的BC更安全实现版本，provider为BC
     */
    UBER

}
