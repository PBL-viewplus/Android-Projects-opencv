package com.pbl.viewplus;

public class hDataitem {
    //추출 결과, 벡터
    private String text;
    private String iv1;
    private String iv2;
    private String date;
    private String k;
    private String pic;
    private String piciv;


    public hDataitem(){}

//    public hDataitem(String date, String text, String vt, String k) {
//        this.date=date;
//        this.text=text;
//        this.v=vt;
//        this.k=k;
//    }

    public hDataitem(String date, String text, String iv1, String iv2, String k) {
        this.date=date;
        this.text=text;
        this.iv1=iv1;
        this.iv2=iv2;
        this.k=k;
    }

    public hDataitem(String date, String text, String iv1, String iv2, String k,  String piciv) {
        this.date=date;
        this.text=text;
        this.iv1=iv1;
        this.iv2=iv2;
        this.k=k;
        this.piciv=piciv;
    }

    public void setText(String t){
        text=t;
    }

    public void setDate(String d){date=d;}
    public void setK(String k){this.k=k;}

    public void setIv1(String iv1) {
        this.iv1 = iv1;
    }

    public void setIv2(String iv2) {
        this.iv2 = iv2;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public void setPiciv(String piciv) {
        this.piciv = piciv;
    }

    public String getText() {
        return text;
    }


    public String getDate(){
        return date;
    }
    public String getK(){
        return k;
    }

    public String getIv1() {
        return iv1;
    }

    public String getIv2() {
        return iv2;
    }

    public String getPic() {
        return pic;
    }

    public String getPiciv() {
        return piciv;
    }
}
