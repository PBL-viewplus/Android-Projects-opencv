package com.pbl.viewplus;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    // 우리 키 랜덤 생성 함수
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] generateRandomBase64Token(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        //return java.util.Base64.getEncoder().withoutPadding().encodeToString(token); //base64 encoding
        return token;
    }

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

    // 사용자 지정 키로 AES256 암호화
    public static String[] encByKey(String key, String value) throws Exception {
        return encByKey(key.getBytes(), value);
    }

    // 사용자 지정 키로 AES256 암호화
    public static String[] encByKey(byte[] key, String value) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES"); // AES를 사용해서 만든 key를 secretKeySpec 변수 선언
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // 평문 암호화 해야되니까 cipher 지정
        //랜덤 벡터 초기화 추가 -gcm은 12바이트 권장
        //암호알고리즘 바꿔서 16으로 설정
        byte[] iv = new byte[16]; // 암호문에 사용될 벡터 저장할 iv 초기화
        new SecureRandom().nextBytes(iv); // keystore키를 사용하지 않고 우리 키를 사용해서 암호화 진행하여 직접 벡터 생성
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv)); //secretKeySpec와 벡터를 사용하여 암호화 모드로 cipher 초기화
        byte[] randomKey = cipher.doFinal(value.getBytes()); // cipher 사용하여 value를 AES 암호화

        //base64 인코딩한걸 String 배열로 넣어줌
        String[] result = new String[2];
        result[0] = Base64.encodeToString(randomKey, 0);
        result[1] = Base64.encodeToString(iv, 0);
        return result;
    }

    // 사용자 지정 키로 AES256 복호화
    public static String decByKey(String key, String plainText, String iv) throws Exception {
        return decByKey(key.getBytes(), plainText, iv);
    }

    // 사용자 지정 키로 AES256 복호화
    public static String decByKey(byte[] key, String encText, String iv) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES"); // AES를 사용해서 만든 key를 secretKeySpec 변수 저장
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // 평문 복호화 해야되니까 cipher 지정
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(Base64.decode(iv.getBytes(), 0))); //secretKeySpec와 벡터를 사용하여 복호화 모드로 cipher 초기화
        byte[] secureKey = cipher.doFinal(Base64.decode(encText, 0)); // cipher 사용하여 encText를 AES 복호화
        return new String(secureKey);
    }

}
