package com.pbl.viewplus;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.MoreObjects;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Contract.Caption;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceClient;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;

public class WebBrowser extends AppCompatActivity {

    private ScrollView scrollView;
    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; // 웹뷰세팅
    private EditText mText; // Url 입력 받을 PlainText 선언
    private ImageButton mSearchButton;
    private ImageButton mOCRButton;
    private ImageButton mAnalyzeButton;
    private ImageButton mCopyButton;

    private String url = "http://www.naver.com"; // url담을 변수 선언
    private List<String> image_src = new ArrayList<>();
    Bitmap resultBitmap;
    String copyUrl = null;

    TTS_controller tts = new TTS_controller();// tts 객체 생성


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        // 권한
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.Internet"}, 0);

        scrollView = (ScrollView) findViewById(R.id.scrollView);
        mWebView = (WebView) findViewById(R.id.webView);
        mText = (EditText) findViewById(R.id.urlText);
        mSearchButton = (ImageButton) findViewById(R.id.searchButton);
        mAnalyzeButton = findViewById(R.id.AzureButton);
        mOCRButton = findViewById(R.id.OCRButton);
        //mClearButton = (Button) findViewById(R.id.clearButton);
        mCopyButton = findViewById(R.id.copyButton);


        // TTS 객체 초기화
        tts.initTTS(this, null);

        mWebView.setVisibility(View.VISIBLE); // 웹뷰는 불러오기위해 VISIBLE로 설정
        WebView.enableSlowWholeDocumentDraw();
        //첫 세팅 웹뷰
        openUrl();

        // 검색버튼
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                url = mText.getText().toString();

                // 검색 버튼 누르면, 키보드 숨기기
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mText.getWindowToken(),0);

                if ( url.equals("") ){
                    speakTTS(3);
                }else { // http://나 https://를 안붙였을때 http://를 추가함.
                    if( !url.startsWith("http://") && !url.startsWith("https://") ){
                        url = "http://" + url;
                    }

                    openUrl();
                    mText.setText("");
                }
            }
        });

        // 붙여넣기 버튼
        mCopyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getCopyText();
                if (copyUrl == null) {
                    speakTTS(4);
                } else{
                    mText.setText(copyUrl);
                }
            }
        });

        // 이미지 분석 버튼
        mAnalyzeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getCopyText();
                if (copyUrl == null || !copyUrl.startsWith("http")) {
                    speakTTS(1);
                }else{
                    Intent intent = new Intent(getApplicationContext(), WebResult.class);
                    intent.putExtra("ResultUrl", copyUrl);
                    startActivity(intent);
                }
            }
        });

        // 글자 분석 버튼
        mOCRButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getCopyText();
                if (copyUrl == null || !copyUrl.startsWith("http")) {
                    speakTTS(1);
                }else{
                    Intent intent = new Intent(getApplicationContext(), WebOCRResult.class);
                    intent.putExtra("ResultUrl", copyUrl);
                    startActivity(intent);
                }
            }
        });

        //롱 클릭 활성화 후 리스너
        mWebView.setLongClickable(true);
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                WebView.HitTestResult result = mWebView.getHitTestResult();

                switch(result.getType()){
                    case WebView.HitTestResult.IMAGE_TYPE: //그냥 img태그 //뉴스기사에서 사진만 있을 때
                        String url = result.getExtra();
                        copyText(url);
                        speakTTS(2);
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: //a태그의 img태그 //거의 여기 걸림
                        String url2 = result.getExtra();
                        copyText(url2);
                        speakTTS(2);
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: //a태그의 http일 때
                        String link = result.getExtra();
                        copyText(link);
                        speakTTS(2);
                        break;
                }

                return false;//true이면 동시 실행 안됨, false이면 LongClick 복사 후에 onClick 실행됨
            }
        });
    }
    void copyText(String text){
        if(text.length() != 0){
            //문자열을 클립보드에 넣는수 있는 클립데이터 형태로 포장
            ClipData clip = ClipData.newPlainText("text", text);

            //클립보드 관리자 객체를 가져옴
            ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(clip);//클립보드에 저장하는 부분
        }
    }

    void getCopyText(){
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip != null) {
            ClipData.Item item = clip.getItemAt(0);
            copyUrl = (String) item.getText();
        }
    }

    //웹뷰 띄워주는 메서드
    private void openUrl() {
        mWebView.setWebViewClient(new WebViewClient(){//페이지 로드 후 작업 정의
            //1/21
            @Override//모든 URL 변경시시
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                scrollView.scrollTo(0,0);//스크롤 맨위로
            }


            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
                super.onReceivedError(view, request, error);

                int errorCode = error.getErrorCode();
                switch (errorCode) {
                    case ERROR_AUTHENTICATION: // 서버에서 사용자 인증 실패
                        break;
                    case ERROR_BAD_URL: // 잘못된 URL
                        break;
                    case ERROR_CONNECT: // 서버로 연결 실패
                        break;
                    case ERROR_FAILED_SSL_HANDSHAKE: // SSL handshake 수행 실패
                        break;
                    case ERROR_FILE: // 일반 파일 오류
                        break;
                    case ERROR_FILE_NOT_FOUND: // 파일을 찾을 수 없습니다
                        break;
                    case ERROR_HOST_LOOKUP: // 서버 또는 프록시 호스트 이름 조회 실패
                        break;
                    case ERROR_IO: // 서버에서 읽거나 서버로 쓰기 실패
                        break;
                    case ERROR_PROXY_AUTHENTICATION: // 프록시에서 사용자 인증 실패
                        break;
                    case ERROR_REDIRECT_LOOP: // 너무 많은 리디렉션
                        break;
                    case ERROR_TIMEOUT: // 연결 시간 초과
                        break;
                    case ERROR_TOO_MANY_REQUESTS: // 페이지 로드중 너무 많은 요청 발생
                        break;
                    case ERROR_UNKNOWN: // 일반 오류
                        break;
                    case ERROR_UNSUPPORTED_AUTH_SCHEME: // 지원되지 않는 인증 체계
                        break;
                    case ERROR_UNSUPPORTED_SCHEME: // URI가 지원되지 않는 방식
                        break;
                }
                view.loadUrl("about:blank");
                Log.d("onReceivedError", errorCode+"!!!" );
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);

                view.loadUrl("about:blank");
                Log.d("onReceivedHttpError", "onReceivedHttpError" );
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);

                view.loadUrl("about:blank");
                Log.d("onReceivedSslError", "onReceivedSslError" );
            }
        }); // 클릭시 새창 안뜨게

        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        if (mWebSettings != null && url != null) {
            mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
            mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
            mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
            mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
            mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부////////
            mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
            mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부///////
            mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
            mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부
            mWebSettings.setJavaScriptEnabled(true); //웹뷰 자바스크립트 활성화
            mWebSettings.setLoadsImagesAutomatically(true);
            mWebSettings.setLightTouchEnabled(true);

            mWebView.setHorizontalScrollBarEnabled(false);//가로 스크롤바 감추기
            mWebView.setVerticalScrollBarEnabled(false);//세로 스크롤바 감추기


            mWebView.loadUrl(url); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작 naver.com
        }
    }


    //나는 뒤로가기 버튼을 누르면 나가지지 않고, 웹뷰에서 뒤로가기로 기능을 넣고싶다.
    private long backBtnTime = 0;
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;
        if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
            mWebView.goBack();
            backBtnTime = curTime;
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }


    public void onStop(){
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (tts != null){
            tts.ttsDestory();
        }
        webViewDestroy();
    }

    //연관 리소스를 clear해서 메모리 누수 줄이기
    public void webViewDestroy(){
        //캐시,히스토리 웹데이터 삭제
        mWebView.clearCache(true);
        mWebView.clearHistory();

        mWebView.removeJavascriptInterface("bridge");
        mWebView.loadUrl("about:blank");
        mWebView.destroyDrawingCache();
        mWebView.removeAllViews();
        mWebView.removeAllViewsInLayout();
        mWebView.setVisibility(View.GONE);
        mWebView = null;
    }

    public void speakTTS(int num){
        String str;
        switch (num){
            case 1: str = "분석할 이미지가 없습니다.";
                break;
            case 2: str = "사진을 복사했습니다.";
                break;
            case 3: str = "URL을 입력해주세요.";
                break;
            case 4: str = "붙여넣을 URL이 없습니다.";
                break;
            default:
                str = null;
        }
        tts.speakOut(str);
        Toast.makeText(WebBrowser.this, str, Toast.LENGTH_SHORT).show();
    }
}