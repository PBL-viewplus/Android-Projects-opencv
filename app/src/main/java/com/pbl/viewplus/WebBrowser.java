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

    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; // 웹뷰세팅
    private EditText mText; // Url 입력 받을 PlainText 선언
    private Button mSearchButton;
    private ImageView mImageView;
    private Button mNextButton;
    private Button mPreButton;
    private Button mAnalyzeButton;
    private Button mClearButton;
    private TextView mAnalyzeResult;
    private Button mCopyButton;
    private Boolean flag = false; //분석버튼 실행 변수

    private String url = "https://www.naver.com"; // url담을 변수 선언
    //private String url = "";
    private List<String> image_src = new ArrayList<>();
    private int index = 0;
    private String resultText="";
    Bitmap resultBitmap;
    String intentUrl = "";

    private final String API_KEY = "d4e5bcc8873949e88fd2a12c19a5bcc5";
    private final String API_LINK = "https://westus.api.cognitive.microsoft.com/vision/v1.0";

    VisionServiceClient visionServiceClient = new VisionServiceRestClient(API_KEY,API_LINK);
    // tts 객체 생성
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
        /*mNextButton = (Button) findViewById(R.id.nextButton);
        mPreButton = (Button) findViewById(R.id.preButton);*/
        mAnalyzeButton = (Button) findViewById(R.id.analyzeButton);
        mAnalyzeResult = (TextView) findViewById(R.id.analyzeResult);
        mClearButton = (Button) findViewById(R.id.clearButton);
        mCopyButton = (Button) findViewById(R.id.copyButton);

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
        //첫 세팅 웹뷰
        openUrl();

        // 검색버튼
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image_src.clear();
                //입력받은 url을 url에 넣음
                url = mText.getText().toString();
                //입력이 있으면 띄워줌
                flag = true;

                //http://나 https://를 안붙였을때 http://를 추가함.
                // https는 http로 연결시 자동으로 접속됨
                if (url != null) {
                    if(!url.startsWith("http://") && !url.startsWith("https://") ){
                        System.out.println("url2:" + url);
                        url = "http://" + url;
                    }
                    //웹뷰에 띄워줌
                    openUrl();
                }
            }
        });

        // 지우기 버튼
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mText.setText(null);
            }
        });

        // 붙여넣기 버튼
        mCopyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                System.out.println("!!!!!!"+clipboardManager.getPrimaryClip());

                // 클립보드에 데이터가 있고 그 데이터가 텍스트 타입인 경우
                ClipData clip = clipboardManager.getPrimaryClip();
                ClipData.Item item = clip.getItemAt(0);
                mText.setText(item.getText());
            }
        });

/*        // 이전 버튼
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
        });*/

        // 분석 버튼
        mAnalyzeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WebResult.class);
                //intent.putExtra("ResultImage", intentImage);
                //intent.putExtra("ResultText", intentText);
                intent.putExtra("ResultUrl", intentUrl);
                Log.e("짜증나3","왜안돼?");
                startActivity(intent);
                //if (flag) {
                //webAnalyze();

                //인텐트로 분석이미지와 분석결과를 던져주고 보여줌.
/*
                    //분석이미지
                    //BitmapDrawable d = (BitmapDrawable)mImageView.getDrawable();
                    //Bitmap intentImage = d.getBitmap();
                    Bitmap intentImage = resultBitmap;
                    //Log.e("짜증나1","왜안돼?");
                    //while(TextUtils.isEmpty(resultText)){
                    //sleep(1000);
                    //}
                    //분석결과
                    String intentText = resultText;
                    //Log.e("짜증나2","왜안돼?");
*/
                //Intent intent = new Intent(getApplicationContext(), WebSample.class);

                //Log.e("짜증나4","왜안돼?");
                //}
            }
        });

        //롱 클릭 활성화 후 리스너
        mWebView.setLongClickable(true);
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //Toast.makeText(WebBrowser.this, "LongClick", Toast.LENGTH_SHORT).show();

                WebView.HitTestResult result = mWebView.getHitTestResult();
                //Toast.makeText(WebBrowser.this, result.toString(), Toast.LENGTH_LONG).show();

                switch(result.getType()){
                    case WebView.HitTestResult.IMAGE_TYPE: //그냥 img태그 //뉴스기사에서 사진만 있을 때
                        String url = result.getExtra();
                        copyText(url);
                        //Toast.makeText(WebBrowser.this, "case1: " + url, Toast.LENGTH_LONG).show();
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: //a태그의 img태그 //거의 여기 걸림
                        String url2 = result.getExtra();
                        copyText(url2);
                        //Toast.makeText(WebBrowser.this, "case2: " + url2, Toast.LENGTH_LONG).show();
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: //a태그의 http일 때
                        String link = result.getExtra();
                        copyText(link);
                        //Toast.makeText(WebBrowser.this, "case3: " + link, Toast.LENGTH_LONG).show();
                        break;
                }

                //copyToClipboard(link, AppConstants.URL_TO_COPY);


                /*//안걸림
                if (result != null) {
                    if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                        String linkToCopy = result.getExtra();
                        Toast.makeText(WebBrowser.this, "linkToCopy:" + linkToCopy, Toast.LENGTH_LONG).show();
                        //copyToClipboard(linkToCopy, AppConstants.URL_TO_COPY);
                    }
                }*/

                return true;
            }
        });
    }
    void copyText(String text){
        intentUrl = text;
        if(text.length() != 0){
            //문자열을 클립보드에 넣는수 있는 클립데이터 형태로 포장
            ClipData clip = ClipData.newPlainText("text", text);

            //클립보드 관리자 객체를 가져옴
            ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(clip);//클립보드에 저장하는 부분

            //Toast.makeText(this, "Text Copied", Toast.LENGTH_SHORT).show();
        }
    }

/*    void pasteText(){
        //클립보드 관리자 객체를 가져옴
        ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

        //클립보드에 값이 없으면
        if(cm.hasPrimaryClip() == false){
            Toast.makeText(this, "clipboard empty", Toast.LENGTH_SHORT).show();
            return;
        }

        //클립보드의 값이 텍스트가 아니면?
        if(cm.getPrimaryClipDescription().hasMimeType(
                ClipDescription.MIMETYPE_TEXT_PLAIN)==false){
            Toast.makeText(this, "clip is not text", Toast.LENGTH_SHORT).show();
            return;
        }

        //클립데이터를 읽는다.
        ClipData clip = cm.getPrimaryClip();
        ClipData.Item item = clip.getItemAt(0);//첫번째 데이터를 가져옴
        TextView pastetext = (TextView)findViewById(R.id.pastetext);
        pastetext.setText(item.getText());//텍스트뷰에 세팅해줌
    }*/

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
            //text를 doc로 파싱
            Document doc = Jsoup.parse(html);

//          Log.d("result: ", "doc= " + doc);
            //img 태그만 선별
            Elements imgs = doc.select("img");
            Log.d("result: ", "doc= " + imgs);

            for (Element img : imgs) {

                if (img.attr("abs:src") != "") {
                    image_src.add(img.attr("abs:src"));
                    Log.e("src", img.attr("abs:src"));
                }

                if (img.attr("abs:data-src") != "") {
                    image_src.add(img.attr("abs:data-src"));
                    Log.e("src", img.attr("abs:data-src"));
                }
            }

            System.out.println("!!!!!!!!!!!!" + image_src); // 이미지 URL들

            new DownloadFilesTask().execute(image_src.get(0));

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
            resultBitmap = result;
            mImageView.setImageBitmap(result);
        }
    }

    protected void webAnalyze(){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();/*
        BitmapDrawable drawable = (BitmapDrawable)mImageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();*/
        //bitmap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        //.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bitmap 크기 압축
        //Bitmap imgBitmap = ((BitmapDrawable)((mImageView)).getDrawable()).getBitmap();
        Bitmap imgBitmap = resultBitmap;
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); // bitmap 크기 압축
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        AsyncTask<InputStream, String, String> visionTask = new AsyncTask<InputStream, String, String>() {
            // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
            ProgressDialog progressDialog = new ProgressDialog(WebBrowser.this); // 실시간 진행 상태 알림

            @Override // 작업시작
            protected void onPreExecute() {
                progressDialog.show();
            } // progressdialog 생성

            @Override // 진행중
            protected String doInBackground(InputStream... inputStreams) {
                try {
                    tts.speakOutString("분석중입니다");
                    publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거
                    String[] features = {"Description"};
                    String[] details = {};

                    AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0], features, details);

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
            protected void onPostExecute(String s) {

                if (TextUtils.isEmpty(s)) {
                    mAnalyzeResult.setText("인식할 수 없습니다");
                    Toast.makeText(WebBrowser.this, "API Return Empty Result", Toast.LENGTH_SHORT).show();
                    tts.speakOut(mAnalyzeResult);
                } else {
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
            protected void onProgressUpdate(String... values) {
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
            resultText = resultWord;
            mAnalyzeResult.setText(resultWord);
            tts.speakOut(mAnalyzeResult);
            //Toast.makeText(getApplicationContext(),resultWord,Toast.LENGTH_SHORT).show();
        }
    };


    //나는 뒤로가기 버튼을 누르면 나가지지 않고, 웹뷰에서 뒤로가기로 기능을 넣고싶다.
    private long backBtnTime = 0;
    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else if (0 <= gapTime && 2000 >= gapTime) {
            super.onBackPressed();
        } else {
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
    }
}