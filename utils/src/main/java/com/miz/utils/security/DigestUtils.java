package com.miz.utils.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DigestUtils {

    private static final Logger log = LoggerFactory.getLogger(DigestUtils.class);

    public static byte[] messageDigest(DigestAlgorithm algorithm, byte[] originalBytes) {
        try {
            MessageDigest messageDigest =  MessageDigest.getInstance(algorithm.name());
            return messageDigest.digest(originalBytes);
        } catch (NoSuchAlgorithmException e) {
            log.debug(e.getMessage());
            return null;
        }
    }

}
