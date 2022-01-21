package com.pbl.viewplus;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

@RequiresApi(api = Build.VERSION_CODES.O)
public class WebOCRResult extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;
    private WebView webView;
    private ImageButton minusButton;
    private ImageButton plusButton;
    private ImageButton againButton;
    private ImageButton backButton;

    private WebSettings mWebSettings; // 웹뷰세팅
    private String dataPath = "";

    private Bitmap originBitmap;
    private Bitmap changeBitmap;
    private String result="";
    Bitmap bitmap;


    private String url = "https://www.naver.com"; // url담을 변수 선언
    private List<String> image_src = new ArrayList<>();

    private final String API_KEY = "d4e5bcc8873949e88fd2a12c19a5bcc5";
    private final String API_LINK = "https://westus.api.cognitive.microsoft.com/vision/v1.0";

    VisionServiceClient visionServiceClient = new VisionServiceRestClient(API_KEY,API_LINK);

    //암호화
    //public static String alias = "ItsAlias"; //안드로이드 키스토어 내에서 보여질 키의 별칭
    public String TAG="hello";
    //public byte[] key = AES.generateRandomBase64Token(16);

    //FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private FirebaseAuth mAuth;

    Tesseract tesseract = new Tesseract();
    // tts 객체 생성
    TTS_controller tts = new TTS_controller();
    //Regex regex = new Regex();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_result);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 가로 화면 고정

        setTitle("분석 결과");

        Intent intent = getIntent();
        url = intent.getStringExtra("ResultUrl");

        // tesseract 언어 데이터 경로
        dataPath = getFilesDir() + "/tesseract/";
        tesseract.checkFile(new File(dataPath + "tessdata/"), "kor", dataPath, getApplicationContext());
        tesseract.checkFile(new File(dataPath + "tessdata/"), "eng", dataPath, getApplicationContext());

        // tesseract 객체 초기화
        tesseract.tessInit(dataPath);

        // TTS 객체 초기화
        tts.initTTS(this, null);

        imageView = findViewById(R.id.WR_imageView);
        textView = findViewById(R.id.WR_TextView);
        webView = findViewById(R.id.WR_WebView);
        minusButton = findViewById(R.id.WR_btn_minus);
        againButton = findViewById(R.id.WR_btn_again);
        plusButton = findViewById(R.id.WR_btn_plus);
        backButton = findViewById(R.id.WR_btn_back);

        webView.setVisibility(View.VISIBLE); // 웹뷰는 불러오기위해 VISIBLE로 설정
        WebSettings settings = webView.getSettings();
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

        // 돋보기 -버튼
        minusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getTextSize() > 70) {
                    textView.setTextSize(textView.getTextSize() / Resources.getSystem().getDisplayMetrics().density - 10);
                }
            }
        });

        // TTS 다시 듣기 버튼
        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.speakOut(textView.getText().toString());
            }
        });

        // 돋보기 +버튼
        plusButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textView.getTextSize() < 320) {
                    textView.setTextSize(textView.getTextSize() / Resources.getSystem().getDisplayMetrics().density + 10);
                }
            }
        });

        // 뒤로가기 버튼
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //웹뷰 띄워주는 메서드
    private void openUrl() {
        webView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = webView.getSettings(); //세부 세팅 등록
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
            webView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
            //페이지 로드 후 작업 정의
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('body')[0].innerHTML);");
                }
            });
            webView.loadUrl(url); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작 naver.com
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
            imageView.setImageBitmap(result);
            bitmap = result;
            webAnalyze();
        }
    }


    protected void webAnalyze(){
        changeBitmap = bitmap;
        originBitmap = bitmap;
        AsyncTask<InputStream, String, String> ocrTask = new AsyncTask<InputStream, String, String>() {
            //String result;
            // AsyncTask<doInBackground() 변수 타입, onProgressUpdate() 변수 타입, onPostExecute() 변수 타입>
            ProgressDialog progressDialog = new ProgressDialog(WebOCRResult.this); // 실시간 진행 상태 알림

            @Override // 작업시작
            protected void onPreExecute() {
                progressDialog.show();
            } // progressdialog 생성

            @Override // 진행중
            protected String doInBackground(InputStream... inputStreams) {
                tts.speakOut("분석중입니다");
                publishProgress("분석중입니다..."); // 이 메서드를 호출할 때마다 UI 스레드에서 onProgressUpdate의 실행이 트리거

                progressDialog.setCanceledOnTouchOutside(false);

                result = tesseract.processImage(changeBitmap);
                return result;
            }

            @SuppressLint("StaticFieldLeak")
            @Override // 종료
            protected void onPostExecute(String s) {

                if (TextUtils.isEmpty(s)) {
                    progressDialog.dismiss();
                    textView.setText("인식할 수 없습니다");
                    tts.speakOut(textView.getText().toString());
                } else {
                    progressDialog.dismiss();

                    /*//텍스트에 마스킹할 부분이 있다면
                    if(regex.isRegex(result)){
                        //검열 묻는 팝업창
                        Intent regexDialogIntent = new Intent(getApplicationContext(), RegexDialog.class);
                        startActivityForResult(regexDialogIntent, 3);
                        //분석결과 지우기
                        textView.setText("");
                        //다음 분석을 위해 셋팅
                        regex.hasRegex= false;
                    }else{
                        //분석결과 지우기
                        textView.setText("");
                        //아니면 바로 보여줌
                        textView.setText(result);
                        tts.speakOut(textView);
                        System.out.println("hooonononono"+ result);
                    }*/
                    //분석결과 지우기
                    textView.setText("");
                    //아니면 바로 보여줌
                    textView.setText(result);
                    tts.speakOut(textView.getText().toString());

                }


            }

            @Override
            protected void onProgressUpdate(String... values) {
                progressDialog.setMessage(values[0]);
            }
        };
        ocrTask.execute();
    }

    //파파고 번역에 필요한 핸들러
    @SuppressLint("HandlerLeak")
    Handler papago_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String resultWord = bundle.getString("resultWord");
            textView.setText(resultWord);
            tts.speakOut(textView.getText().toString());
            //Toast.makeText(getApplicationContext(),resultWord,Toast.LENGTH_SHORT).show();
        }
    };


    public void onStop(){
        super.onStop();
        tts.ttsStop();
    }

    public void onDestroy() {
        super.onDestroy();
        if (changeBitmap != null) {
            changeBitmap = null;
        }

        if (tts != null) {
            tts.ttsDestory();
        }
    }

    public void onPause() {
        super.onPause();
        tts.ttsStop();
    }
}