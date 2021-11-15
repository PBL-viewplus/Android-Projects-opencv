package com.pbl.viewplus;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AES {

    // 키스토어 키 존재 여부 확인
    public static boolean isExistKey(String alias) throws Exception {
        //AndroidKeyStore 프로바이더를 통해 Android KeyStore 인스턴스를 로드
        //aliases() 메서드를 호출해 키 저장소의 항목을 나열
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String nextAlias = aliases.nextElement();
            if (nextAlias.equals(alias)) {
                return true;
            }
        }
        return false;
    }

    // 키스토어 키 생성
    public static void generateKey(String alias) throws Exception {
        //AES암호화 사용
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        //KeyGenParameterSpec로 생성될 키의 속성을 정의
        final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias, //사용할 별칭
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)//키를 사용할 목적(암,복호화)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)//암,복호화할 데이터에 사용될 block mode ->GCM
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)//변환 알고리즘
                .build();
        //keyGenParameterSpec를 이용해 keyGenerator를 초기화 하고, Secret Key(비밀키)를 만듦
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    // 키스토어 키 조회
    public static SecretKey getKeyStoreKey(String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        //SecretKeyEntry는 비밀키를 얻어올때 사용
        final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null);
        return secretKeyEntry.getSecretKey();
    }

    // 키스토어 키로 AES256 암호화. string->bytes->암호화->base64 인코딩
    public static String[] encByKeyStoreKey(SecretKey secretKey, String plainText) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] enc = cipher.doFinal(plainText.getBytes());
        //초기화 벡터(첫 블록을 암호화할 때 사용되는 값) 암호화나 복호화를 마무리할때 사용
        byte[] iv = cipher.getIV();
        //base64 인코딩한걸 String 배열로 넣어줌
        String encText = Base64.encodeToString(enc, 0);
        String ivText = Base64.encodeToString(iv, 0);

        String[] result = new String[2];
        result[0] = encText;
        result[1] = ivText;
        return result;
    }

    // 키스토어 키로 AES256 복호화. base64디코딩->복호화->bytes->string
    public static String decByKeyStoreKey(SecretKey secretKey, String encText, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        //BLOCK_MODE_GCM로  GCMParameterSpec(128, 120.. 등의 인증 태그길이를 가진)애를 필요로하며,
        // 이전에 암호화할 때 사용한 IV값을 전달해야 복호화가 이루어진다
        GCMParameterSpec spec = new GCMParameterSpec(128, Base64.decode(iv, 0));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        byte[] dec = cipher.doFinal(Base64.decode(encText, 0));  // 확인 필요
        return new String(dec);
    }

}
