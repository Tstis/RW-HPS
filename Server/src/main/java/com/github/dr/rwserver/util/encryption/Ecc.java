/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util.encryption;

/**
 * @author Dr
 */
public class Ecc {
    /*
    private final static int KEY_SIZE = 256;//bit
    private final static String SIGNATURE = "SHA256withECDSA";

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }


/*
    public static void main(String[] args) {
        try {
            KeyPair keyPair = getKeyPair();
            ECPublicKey pubKey = (ECPublicKey) keyPair.getPublic();
            ECPrivateKey priKey = (ECPrivateKey) keyPair.getPrivate();
            //System.out.println("[pubKey]:\n" + getPublicKey(keyPair));
            //System.out.println("[priKey]:\n" + getPrivateKey(keyPair));

            //测试文本
            String content = "abcdefg";

            //加密
            byte[] cipherTxt = encrypt(content.getBytes(), pubKey);
            //解密
            byte[] clearTxt = decrypt(cipherTxt, priKey);
            //打印
            System.out.println("content:" + content);
            System.out.println("cipherTxt["+cipherTxt.length+"]:" + new String(cipherTxt));
            System.out.println("clearTxt:" + new String(clearTxt));

            //签名
            byte[] sign = sign(content, priKey);
            //验签
            boolean ret = verify(content, sign, pubKey);
            //打印
            System.out.println("content:" + content);
            System.out.println("sign["+sign.length+"]:" + new String(sign));
            System.out.println("verify:" + ret);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[main]-Exception:" + e.toString());
        }
    }
 *

    //生成秘钥对
    public KeyPair getKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");//BouncyCastle
        keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    //获取公钥(Base64编码)
    public String getPublicKey(KeyPair keyPair) {
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        byte[] bytes = publicKey.getEncoded();
        return Base64.encode(bytes);
    }

    //获取私钥(Base64编码)
    public String getPrivateKey(KeyPair keyPair) {
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        byte[] bytes = privateKey.getEncoded();
        return Base64.encode(bytes);
    }

    //公钥加密
    public byte[] encrypt(byte[] content, ECPublicKey pubKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        return cipher.doFinal(content);
    }

    //私钥解密
    public byte[] decrypt(byte[] content, ECPrivateKey priKey) throws Exception {
        Cipher cipher = Cipher.getInstance("ECIES", "BC");
        cipher.init(Cipher.DECRYPT_MODE, priKey);
        return cipher.doFinal(content);
    }

    //私钥签名
    public byte[] sign(String content, ECPrivateKey priKey) throws Exception {
        //这里可以从证书中解析出签名算法名称
        //Signature signature = Signature.getInstance(getSigAlgName(pubCert));
        Signature signature = Signature.getInstance(SIGNATURE);//"SHA256withECDSA"
        signature.initSign(priKey);
        signature.update(content.getBytes());
        return signature.sign();
    }

    //公钥验签
    public boolean verify(String content, byte[] sign, ECPublicKey pubKey) throws Exception {
        //这里可以从证书中解析出签名算法名称
        //Signature signature = Signature.getInstance(getSigAlgName(priCert));
        Signature signature = Signature.getInstance(SIGNATURE);//"SHA256withECDSA"
        signature.initVerify(pubKey);
        signature.update(content.getBytes());
        return signature.verify(sign);
    }

    /**
     * 解析证书的签名算法，单独一本公钥或者私钥是无法解析的，证书的内容远不止公钥或者私钥
     * *
    private String getSigAlgName(File certFile) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
        X509Certificate x509Certificate = (X509Certificate) cf.generateCertificate(new FileInputStream(certFile));
        return x509Certificate.getSigAlgName();
    }
    */
}
