package com.pbl.viewplus;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private WebView mWebView;
    private WebSettings mWebSettings;
    private EditText mText;
    private Button mSearchButton;
    private ImageView mImageView;
    private Button mNextButton;
    private Button mPreButton;
    private Button mAnalyzeButton;
    private TextView mAnalyzeResult;
    private Boolean flag = false; // 분석버튼 실행 관련 변수

    //    private String url = "https://www.naver.com"; // url담을 변수 선언
    private String url = "";
    private List<String> image_src = new ArrayList<>();
    private int index = 0;

    private final String API_KEY = "d4e5bcc8873949e88fd2a12c19a5bcc5";
    private final String API_LINK = "https://westus.api.cognitive.microsoft.com/vision/v1.0";

    VisionServiceClient visionServiceClient = new VisionServiceRestClient(API_KEY,API_LINK);
    TTS_controller tts = new TTS_controller();


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        // 권한
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.Internet"}, 0);

        mWebView = (WebView) findViewById(R.id.webView);
        mText = (EditText) findViewById(R.id.urlText);
        mSearchButton = (Button) findViewById(R.id.searchButton);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mNextButton = (Button) findViewById(R.id.nextButton);
        mPreButton = (Button) findViewById(R.id.preButton);
        mAnalyzeButton = (Button) findViewById(R.id.analyzeButton);
        mAnalyzeResult = (TextView) findViewById(R.id.analyzeResult);

        // TTS 객체 초기화
        tts.initTTS(this, 0);

        mWebView.setVisibility(View.VISIBLE); // 웹뷰는 불러오기위해 VISIBLE로 설정
        WebSettings settings = mWebView.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(false);
        settings.setJavaScriptEnabled(true);
        settings.setSupportMultipleWindows(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setLightTouchEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        WebView.enableSlowWholeDocumentDraw();

        // 검색버튼
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image_src.clear();
                url = mText.getText().toString();
                if (url != null) {
                    openUrl();
                }
            }
        });

        // 이전 버튼
        mPreButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(index > 0){
                    index--;
                    new DownloadFilesTask().execute(image_src.get(index));
                }
            }
        });

        // 다음 버튼
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tts.ttsStop();
                if(index < image_src.size()-1){
                    index++;
                    new DownloadFilesTask().execute(image_src.get(index));
                }
            }
        });

        // 분석 버튼
        mAnalyzeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (flag) {
                    webAnalyze();
                }
            }
        });
    }

    //웹뷰 띄워주는 메서드
    private void openUrl() {
        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
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
            //https://stackoverflow.com/questions/31238312/androidhow-to-convert-html-code-into-image-and-send-this-image-using-shareinten
            mWebSettings.setLoadsImagesAutomatically(true);
            mWebSettings.setLightTouchEnabled(true);
            //자바스크립트인터페이스 연결(자바함수 접근)
            mWebView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
            //페이지 로드 후 작업 정의
            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('body')[0].innerHTML);");
                }
            });
            mWebView.loadUrl(url); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작 naver.com
        }
    }

    //자바스크립트인터페이스로 스크립트코드 실행 후, 반환
    public class MyJavascriptInterface {
        @JavascriptInterface
        public void getHtml(String html) {
            //위 자바스크립트가 호출되면 여기로 html이 반환됨
            //html확인 방법이지만 아래 로그에러를 동반할 수 있음. //textView.setText(html);
            Document doc = Jsoup.parse(html); // html을 doc로 파싱
            Elements imgs = doc.select("img"); //img 태그만 선별

            for(Element img : imgs) {
                if(img.attr("abs:src") != "" ){
                    image_src.add(img.attr("abs:src"));
                    Log.e("src",img.attr("abs:src"));
                }
                if(img.attr("abs:data-src") != ""){
                    image_src.add(img.attr("abs:data-src"));
                    Log.e("src",img.attr("abs:data-src"));
                }
            }

            if (image_src.isEmpty()){ // 해당 url에 이미지가 없을 경우
                flag = false; // 분석 버튼 불가
                mAnalyzeResult.setText("이미지 검색결과가 없습니다.");
                tts.speakOutString("이미지 검색결과가 없습니다.");
                Bitmap tempimg = BitmapFactory.decodeResource(getResources(),R.drawable.sample);
                mImageView.setImageBitmap(tempimg);
            } else{
                flag = true; // 분석 버튼 가능
                new DownloadFilesTask().execute(image_src.get(0));
            }
        }
    }

    private class DownloadFilesTask extends AsyncTask<String,Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bmp = null;
            try {
                String img_url = strings[0]; //url of the image
                URL url = new URL(img_url);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // doInBackground 에서 받아온 total 값 사용 장소
            mImageView.setImageBitmap(result);
        }
    }

    protected void webAnalyze(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Bitmap imgBitmap = ((BitmapDrawable)((mImageView)).getDrawable()).getBitmap();
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bitmap 크기 압축
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
            // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
            ProgressDialog progressDialog = new ProgressDialog(WebBrowser.this); // 실시간 진행 상태 알림

            @Override // 작업시작
            protected void onPreExecute() {
                progressDialog.show();
            } // progressdialog 생성

            @Override // 진행중
            protected String doInBackground(InputStream... inputStreams) {
                try
                {
                    tts.speakOutString("분석중입니다");
                    publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거
                    String[] features = {"Description"};
                    String[] details = {};

                    AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0],features,details);

                    String jsonResult = new Gson().toJson(result);
                    return jsonResult;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (VisionServiceException e) {
                    e.printStackTrace();
                }
                return "";
            }

            @SuppressLint("StaticFieldLeak")
            @Override // 종료
            protected void onPostExecute(String s){

                if(TextUtils.isEmpty(s)){
                    mAnalyzeResult.setText("인식할 수 없습니다");
                    Toast.makeText(WebBrowser.this,"API Return Empty Result",Toast.LENGTH_SHORT).show();
                    tts.speakOut(mAnalyzeResult);
                }
                else {
                    progressDialog.dismiss();
                    AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                    StringBuilder result_Text = new StringBuilder();
                    for (Caption caption : result.description.captions)
                        result_Text.append(caption.text);

                    //파파고 번역
                    new Thread() {
                        @Override
                        public void run() {
                            String word = result_Text.toString();
                            Papago_translate papago = new Papago_translate();
                            String resultWord = papago.getTranslation(word, "en", "ko");

                            Bundle papagoBundle = new Bundle();
                            papagoBundle.putString("resultWord", resultWord);

                            Message msg = papago_handler.obtainMessage();
                            msg.setData(papagoBundle);
                            papago_handler.sendMessage(msg);
                        }
                    }.start();
                }
            }

            @Override
            protected void onProgressUpdate(String... values){
                progressDialog.setMessage(values[0]);
            }
        };
        visionTask.execute(inputStream);
    }

    //파파고 번역에 필요한 핸들러
    @SuppressLint("HandlerLeak")
    Handler papago_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String resultWord = bundle.getString("resultWord");
            mAnalyzeResult.setText(resultWord);
            tts.speakOut(mAnalyzeResult);
            //Toast.makeText(getApplicationContext(),resultWord,Toast.LENGTH_SHORT).show();
        }
    };

    public void onStop(){
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (tts != null){
            tts.ttsDestory();
        }
    }
}