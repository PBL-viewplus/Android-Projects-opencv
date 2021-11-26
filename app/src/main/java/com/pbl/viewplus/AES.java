package com.pbl.viewplus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayOutputStream;
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
//        Log.d("hello" ," 결과333 : " + token[0]);//암호문
//        Log.d("hello" ," 결과333 : " + token[1]);//벡터
//        Log.d("hello" ," 결과333 : " + token.length);//벡터
        return token;
        //return Base64.encodeToString(token,0);
        //return java.util.Base64.getEncoder().withoutPadding().encodeToString(token); //base64 encoding
    }

    // 키스토어 키 존재 여부 확인
    public static boolean isExistKey(String alias) throws Exception {
        //provider= AndroidKeyStore. Android KeyStore 인스턴스를 로드
        //aliases() 메서드를 호출해 키 저장소의 항목을 나열
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        //aliases() 호출해 키 저장소의 항목 나열
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
        //KeyGenerator: 지정된 알고리즘에 대한 비밀 키를 생성 하는 개체를 반환
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        //KeyGenParameterSpec로 생성될 키의 속성을 정의
        final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias, //사용할 별칭
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)//키를 사용할 목적(암,복호화)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)//암,복호화할 데이터에 사용될 block mode ->GCM
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)//변환 알고리즘
                //.setKeyValidityForOriginationEnd() //키 암호해독에 유효하지 않은 시간 설정(날짜)
                .build();
        //keyGenParameterSpec를 이용해 keyGenerator를 초기화 하고, Secret Key(비밀키)를 만듦
        keyGenerator.init(keyGenParameterSpec);
        keyGenerator.generateKey();
    }

    // 키스토어 키 조회
    public static SecretKey getKeyStoreKey(String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        //SecretKeyEntry는 비밀키를 얻어올때 사용. getEntry()로 키 가져옴
        final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null);

        Log.d("ello" ,"33333 : " + Base64.decode(secretKeyEntry.getSecretKey().toString(), 0).length); //바이트: 41. 스트링: 59 왜..?
//        byte[]dfd=secretKeyEntry.getSecretKey().getEncoded();
//        Log.d("ello" ,"33333 : " + dfd); //바이트: 41. 스트링: 59 왜..? -> 키의 이름이라서? 내용물을 본게 아니니까.
        return secretKeyEntry.getSecretKey();
    }

    // 키스토어 키로 AES256 암호화. string->bytes->암호화->base64 인코딩
    public static String[] encByKeyStoreKey(SecretKey secretKey, byte[] plainText) throws Exception {
        //이거 왜한거지?
//        SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKey.getAlgorithm(), "AndroidKeyStore");
//        KeyInfo keyInfo;
//        try {
//            keyInfo = (KeyInfo) factory.getKeySpec(secretKey, KeyInfo.class);
//            System.out.println("helloeeeeeee"+keyInfo.isInsideSecureHardware()); //false
//        } catch (InvalidKeySpecException e) {
//            // Not an Android KeyStore key.
//        }

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); //알고리즘/모드/패딩
        //키로 cipher 초기화
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        //byte[] bplanText= Base64.decode(plainText,0);
        //Log.d("hello" ," 결과 : " + bplanText.length);//벡터

        //plainText는 암호화되지 않은 AAD. GCM의 경우 암호문이 처리되기 전에 doFinal()로 제공되어야됨
        //doFinal(): 단일 부분 작업에서 데이터를 암호화 또는 해독하거나 여러 부분으로 구성된 작업을 완료
        byte[] enc = cipher.doFinal(plainText);
        //초기화 벡터(첫 블록을 암호화할 때 사용되는 값): 암호화나 복호화를 마무리할때 사용
        //새 버퍼의 초기화 벡터(IV)를 반환
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
        //GCM 모드를 사용하는 데 필요한 매개변수 세트를 지정
        //GCM은 초기화벡터(iv), 인증 태그 T의 길이(비트)(tLen) 필요
        GCMParameterSpec spec = new GCMParameterSpec(128, Base64.decode(iv, 0)); //16바이트 인증태그 사용
        //키와 AlgorithmParameterSpec로 ciper 초기화
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
        byte[] dec = cipher.doFinal(Base64.decode(encText, 0));  // 확인 필요
        return Base64.encodeToString(dec,0);
    }

    //사용자 지정 키로 AES256 암호화
    public static String[] encByKey(String key, String value) throws Exception {
        return encByKey(Base64.decode(key,0), value);
    }

    // 사용자 지정 키로 AES256 암호화
    public static String[] encByKey(byte[] key, String value) throws Exception {
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); //알고리즘/모드/패딩
//        //키로 cipher 초기화
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//        //plainText는 암호화되지 않은 AAD. GCM의 경우 암호문이 처리되기 전에 doFinal()로 제공되어야됨
//        //doFinal(): 단일 부분 작업에서 데이터를 암호화 또는 해독하거나 여러 부분으로 구성된 작업을 완료
//        byte[] enc = cipher.doFinal(plainText.getBytes());
//        //초기화 벡터(첫 블록을 암호화할 때 사용되는 값): 암호화나 복호화를 마무리할때 사용
//        //새 버퍼의 초기화 벡터(IV)를 반환
//        byte[] iv = cipher.getIV();

        //사용자가 지정한 문자열로 키를 만듦
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        //랜덤 벡터 초기화 추가 -gcm은 12바이트 권장
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        System.out.println("helloeeeeeee2224444"+iv.length);

        //System.out.println("helloeeeeeee"+iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] randomKey = cipher.doFinal(value.getBytes());//암호화

        //base64 인코딩한걸 String 배열로 넣어줌
        String[] result = new String[2];
        result[0] = Base64.encodeToString(randomKey, 0);
        result[1] = Base64.encodeToString(iv, 0);
        Log.d("hello" ," 결과 : " + result[0]);//암호문
        Log.d("hello" ," 결과 : " + result[1]);//벡터

//        System.out.println("helloeeeeeee222"+result[1]);
//        System.out.println("helloeeeeeee222"+Base64.decode(result[1].getBytes(),0).length);
        return result;
    }


    // 사용자 지정 키로 AES256 복호화
    public static String decByKey(String key, String plainText, String iv) throws Exception {
        return decByKey(Base64.decode(key,0), plainText, iv);
    }

    // 사용자 지정 키로 AES256 복호화
    public static String decByKey(byte[] key, String encText, String iv) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(Base64.decode(iv, 0)));
        byte[] secureKey = cipher.doFinal(Base64.decode(encText, 0));
        return new String(secureKey);
    }


    // String형을 BitMap으로 변환시켜주는 함수
    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    // Bitmap을 String형으로 변환
    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
        byte[] bytes = baos.toByteArray();
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
        return temp;
    }


}
