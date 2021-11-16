package com.pbl.viewplus;

public class hDataitem {
    //추출 결과, 벡터
    private String text;
    private String v;

    public hDataitem(){}

    public hDataitem(String text, String vt) {
        this.text=text;
        this.v=vt;
    }

    public void setText(String t){
        text=t;
    }
    public void setV(String vt){
        v=vt;
    }

    public String getText() {
        return text;
    }

    public String getV() {
        return v;
    }


}
