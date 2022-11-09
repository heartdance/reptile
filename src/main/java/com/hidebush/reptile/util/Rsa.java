package com.hidebush.reptile.util;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class Rsa {

    public static String sign(String data, String privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(loadPrivateKey(privateKey));
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        signature.update(bytes);
        byte[] signed = signature.sign();
        return Base64.encodeBase64String(signed);
    }

    private static RSAPrivateKey loadPrivateKey(String privateKeyStr) {
        try {
            byte[] buffer = Base64.decodeBase64(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return  (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无此算法");
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("私钥非法");
        } catch (NullPointerException e) {
            throw new RuntimeException("私钥数据为空");
        }
    }
}
