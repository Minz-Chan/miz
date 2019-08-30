package com.miz.utils.security;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class GoogleAuthenticator {

    private static final int TIME_OFFSET    = 1;
    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    /**
     * 创建一个密钥
     */
    public static String createSecretKey(String token) {
        byte[] bytes = DigestUtils.messageDigest(DigestAlgorithm.SHA1, token.getBytes());
        Base32 base32 = new Base32();
        String secretKey = base32.encodeToString(bytes);
        return secretKey.toLowerCase();
    }

    private static byte[] hmacSha(String crypto, byte[] keyBytes, byte[] text) {
        try {
            Mac mac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            mac.init(macKey);
            return mac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    public static String generateVerificationCode(String key, String time, int codeDigits, String crypto) {
        // 前8字节是移动因子。 兼容 RFC 4226
        while (time.length() < 16) {
            time = "0".concat(time);
        }

        byte[] msg = bytesOf(time);
        byte[] k = bytesOf(key);
        byte[] hash = hmacSha(crypto, k, msg);
        // 将选定字节放入结果int中
        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        int otp = binary % DIGITS_POWER[codeDigits];
        // 不足6位时前面补0
        StringBuilder result = new StringBuilder(Integer.toString(otp));
        while (result.length() < codeDigits) {
            result.append("0").append(result);
        }
        return result.toString();
    }

    private static byte[] bytesOf(String hex) {
        byte[] bytes = new BigInteger("10" + hex, 16).toByteArray();
        return Arrays.copyOfRange(bytes, 1, bytes.length - 1);
    }

    /**
     * 根据密钥获取验证码
     * 返回字符串是因为数值有可能以0开头
     * @param secretKey 密钥
     * @param time  第几个30秒 System.currentTimeMillis() / 1000 / 30
     */
    public static String getVerificationCode(String secretKey, long time) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey.toUpperCase());
        String hexKey = Hex.encodeHexString(bytes);
        String hexTime = Long.toHexString(time);
        return generateVerificationCode(hexKey, hexTime, 6, "HmacSHA1");
    }

    /**
     * 校验方法
     * @param secretKey 密钥
     * @param code  6位验证码
     */
    private static boolean verify(String secretKey, String code) {
        long time = System.currentTimeMillis() / 1000 / 30;
        String verificationCode = getVerificationCode(secretKey, time);
        return code.equals(verificationCode);
    }

    /**
     * 校验方法
     * @param secretKey 密钥
     * @param code  6位验证码
     * @param loose 是否宽松验证 如果开启则验证的验证码的有效期±1, 除了当次验证码可通过,上次和下次验证码也可通过
     */
    public static boolean verify(String secretKey, String code, boolean loose) {
        if(!loose) return verify(secretKey, code);
        long time = System.currentTimeMillis() / 1000 / 30;
        for (int i = -TIME_OFFSET; i <= TIME_OFFSET; i++) {
            String verificationCode = getVerificationCode(secretKey, time + i);
            if (code.equals(verificationCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成Google Authenticator二维码所需信息
     * Google Authenticator 约定的二维码信息格式 : otpauth://totp/{issuer}:{account}?secret={secret}&issuer={issuer}
     * @param secret  密钥 使用createSecretKey方法生成
     * @param account 用户账户
     * @param issuer  服务名称
     */
    private static String createQRCodeData(String secret, String account, String issuer) throws UnsupportedEncodingException {
        return "otpauth://totp/"+account+ "?secret="+secret+"&issuer="+ URLEncoder.encode(issuer, "UTF-8");
    }

    public static String createQRCodeDataByToken(String token, String account, String issuer) throws UnsupportedEncodingException {
        String secret = createSecretKey(token);
        return createQRCodeData(secret, account, issuer);
    }

}
