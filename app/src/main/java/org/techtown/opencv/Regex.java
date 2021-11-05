package org.techtown.opencv;

import org.opencv.core.Mat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
    public static boolean hasRegex=false;

    //마스킹할 패턴이 있는지 확인
    public static void isRegex(ArrayList<Matcher> matcherList){
        for(int i=0;i<matcherList.size();i++){
            if(matcherList.get(i).find()){
                hasRegex=true;
                break;
            }
        }
    }

    public static String doRegex(String input){
        String inputData=input;

        ArrayList<Pattern> maskingPattern= new ArrayList<>();
        maskingPattern.add(Pattern.compile("[a-zA-Z0-9]+@[a-zA-Z0-9]+[a-z]+")); //이메일
        maskingPattern.add(Pattern.compile("01(0|1)-(\\d{3}|\\d{4})-\\d{4}")); //휴대폰 번호
        maskingPattern.add(Pattern.compile("01(0|1) (\\d{3}|\\d{4}) \\d{4}")); //휴대폰 번호(띄어쓰기 버전)
        maskingPattern.add(Pattern.compile("010\\d{8}")); //휴대폰 번호(붙여쓴 버전)
        maskingPattern.add(Pattern.compile("0(2|[3-6][1-5])-\\d{3,4}-\\d{4}")); //전화 번호 -서울이거나 다른지역
        maskingPattern.add(Pattern.compile("\\d{6}-[1-4]\\d{6}")); //주민등록번호
        maskingPattern.add(Pattern.compile("[34569][0-9]{3}-[0-9]{4}-[0-9]{4}-[0-9]{4}")); //카드번호. 국내 앞자리 34569
        maskingPattern.add(Pattern.compile("(([가-힣]+(시|도)|[서울]|[경기]|[인천]|[강원]|[대구]|[대전]|[충청]|[광주]|[부산]|[울산]|[경상]|[전라]|[제주])( |[가-힣])+(시|군|구))( |)(|([가-힣]+( |\\d|\\d(,|\\.)|\\d |)+(읍|면|동|가|리))(^구|))( |)( |(([가-힣]|\\d(,|\\.)|(\\d+(~|-)\\d)|( |)\\d)+(로|길)))( |)((\\d{1,3}(~|-)\\d{1,3}|\\d{1,3})|)( |)(([가-힣]+(| )((\\d+동( |)\\d+호)|(\\d+호)))|)")); //주소

        //형식 일치하는지 확인
        Matcher emailMasking = maskingPattern.get(0).matcher(inputData); //이메일
        Matcher phoneNumberMasking = maskingPattern.get(1).matcher(inputData);//휴대폰 번호
        Matcher phoneBlankMasking = maskingPattern.get(2).matcher(inputData);//휴대폰 번호(띄어쓰기 버전)
        Matcher phoneNoBlankMasking = maskingPattern.get(3).matcher(inputData);//휴대폰 번호(붙여쓴 버전)
        Matcher houseNumberMasking = maskingPattern.get(4).matcher(inputData);//전화 번호 -서울이거나 다른지역
        Matcher personNumberMasking = maskingPattern.get(5).matcher(inputData);//주민등록번호
        Matcher cardMasking = maskingPattern.get(6).matcher(inputData);//카드번호. 국내 앞자리 34569
        Matcher addressMasking = maskingPattern.get(7).matcher(inputData); //주소

        ArrayList<Matcher> confirmMatcher= new ArrayList<>();
        confirmMatcher.add(emailMasking);
        confirmMatcher.add(phoneNumberMasking);
        confirmMatcher.add(phoneBlankMasking);
        confirmMatcher.add(phoneNoBlankMasking);
        confirmMatcher.add(houseNumberMasking);
        confirmMatcher.add(personNumberMasking);
        confirmMatcher.add(cardMasking);
        confirmMatcher.add(addressMasking);

        //마스킹할 패턴이 있는지 확인
        isRegex(confirmMatcher);

        while(emailMasking.find()){//이메일 ex)te*****@naver.com
            String target = emailMasking.group(0);
            String emailLeft = emailMasking.group(0).split("@")[0]; //@로 나눠 앞부분 문자2개만 표시, 나머지 *로 변환
            String emailRight = emailMasking.group(0).split("@")[1]; //naver.com같은 나머지 주소

            if(emailLeft.length() > 3) {
                char[] c = new char[emailLeft.length() -3];
                Arrays.fill(c, '*');//3자리 빼고 *로 채움
                inputData= inputData.replace(target, target.substring(0, 3) +  String.valueOf(c)+ "@" +emailRight);
            }
            System.out.println(inputData);
            //isRegex=true;
        }
        while(phoneNumberMasking.find()) {//휴대폰 번호 ex)010-****-3932
            String target = phoneNumberMasking.group(0);
            String phoneLeft = phoneNumberMasking.group(0).split("-")[0]; //-로 나눠 앞번호만 표시
            String phoneRight = phoneNumberMasking.group(0).split("-")[2]; //-로 나눠 뒷번호만 표시

            char[] c = new char[phoneNumberMasking.group(0).split("-")[1].length()];
            Arrays.fill(c, '*');//가운데 *로 채움
            inputData = inputData.replace(target, phoneLeft + "-"+ String.valueOf(c) + "-"+ phoneRight);
            System.out.println(inputData);
            //isRegex=true;
        }
        while(phoneBlankMasking.find()) {//휴대폰 번호 ex)010 **** 3932
            String target = phoneBlankMasking.group(0);
            String phoneLeft = phoneBlankMasking.group(0).split(" ")[0]; //띄어쓰기로 나눠 앞번호만 표시
            String phoneRight = phoneBlankMasking.group(0).split(" ")[2]; //띄어쓰기로 나눠 뒷번호만 표시

            char[] c = new char[phoneBlankMasking.group(0).split(" ")[1].length()];
            Arrays.fill(c, '*');//가운데 *로 채움
            inputData = inputData.replace(target, phoneLeft + " "+ String.valueOf(c) + " "+ phoneRight);
            System.out.println(inputData);
            //isRegex=true;
        }
        while(phoneNoBlankMasking.find()) {//휴대폰 번호 ex)010****3932
            String target = phoneNoBlankMasking.group(0);
            String phoneRight = phoneNoBlankMasking.group(0).substring(7,11);

            char[] c = new char[4];
            Arrays.fill(c, '*');//가운데 *로 채움
            inputData = inputData.replace(target, "010" +  String.valueOf(c) + phoneRight);
            System.out.println(inputData);
            //isRegex=true;
        }
        while(houseNumberMasking.find()) {//전화 번호 ex) 063-712-****
            String target = houseNumberMasking.group(0);
            String houseNumberLeft = houseNumberMasking.group(0).split("-")[0]; //-로 나눠 앞번호만 표시
            String houseNumberMiddle = houseNumberMasking.group(0).split("-")[1]; //-로 나눠 가운데번호만 표시

            char[] c = new char[houseNumberMasking.group(0).split("-")[2].length()];
            Arrays.fill(c, '*');//뒷부분 *로 채움
            inputData = inputData.replace(target, houseNumberLeft + "-"+ houseNumberMiddle + "-"+ String.valueOf(c));
            System.out.println(inputData);
            //isRegex=true;
        }
        while(personNumberMasking.find()) {//주민등록번호 ex)990210-2******
            String target = personNumberMasking.group(0);
            String personNumberLeft = personNumberMasking.group(0).split("-")[0]; //-로 나눠 앞번호만 표시

            char[] c = new char[6];
            Arrays.fill(c, '*');//뒷부분을 *로 채움
            inputData = inputData.replace(target, personNumberLeft + "-"+ personNumberMasking.group(0).split("-")[1].charAt(0) + String.valueOf(c));
            System.out.println(inputData);
            //isRegex=true;
        }
        while(cardMasking.find()) {//카드번호 ex)4000-****-****-9012
            String target = cardMasking.group(0);
            String cardNum1 = cardMasking.group(0).split("-")[0]; //-로 나눠 앞번호만 표시
            String cardNum4 = cardMasking.group(0).split("-")[3]; //-로 나눠 마지막번호만 표시

            char[] c = new char[4];
            Arrays.fill(c, '*');// 가운데 *로 변환
            inputData = inputData.replace(target, cardNum1 + "-"+ String.valueOf(c)+ "-"+ String.valueOf(c)+ "-"+cardNum4 );
            System.out.println(inputData);
            //isRegex=true;
        }
        while(addressMasking.find()) {//주소 ex)
            String target = addressMasking.group(0);
            System.out.println(inputData);
            inputData = inputData.replaceAll(target, "검열된 주소입니다");
            System.out.println(inputData);
            //isRegex=true;
        }

        return inputData;
    }

}
