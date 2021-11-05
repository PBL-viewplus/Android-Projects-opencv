package org.techtown.opencv;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WebBrowser extends AppCompatActivity {

    private WebView mWebView; // 웹뷰 선언
    private WebSettings mWebSettings; // 웹뷰세팅
    private EditText mText; // Url 입력 받을 PlainText 선언
    private Button mSearchButton;
    private ImageView mImageView;
    private Button mNextButton;
    private Button mPreButton;

    private String url = "https://www.naver.com"; // url담을 변수 선언
    private List<String> image_src = new ArrayList<>();
    private List <String> image_data_src = new ArrayList<>();
    private int index = 0;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);

        //권한
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.Internet"}, 0);

        mWebView = (WebView) findViewById(R.id.webView);
        mText = (EditText) findViewById(R.id.urlText);
        mSearchButton = (Button) findViewById(R.id.searchButton);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mNextButton = (Button) findViewById(R.id.nextButton);
        mPreButton = (Button) findViewById(R.id.preButton);

        //웹뷰는 불러오기위해 VISIBLE로 설정
        mWebView.setVisibility(View.VISIBLE);

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
                //입력받은 url을 url에 넣음
//                url = mText.getText().toString();
                //입력이 있으면 띄워줌
                if (url != null) {
                    //웹뷰에 띄워줌
                    openUrl();
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(index < image_src.size()){
                    index++;
                    new DownloadFilesTask().execute(image_src.get(index));
                }
            }
        });

        mPreButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(index > 0){
                    index--;
                    new DownloadFilesTask().execute(image_src.get(index));
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
            //text를 doc로 파싱
            Document doc = Jsoup.parse(html);
//            Log.d("result: ", "doc= " + doc);
            //img 태그만 선별
            Elements imgs = doc.select("img");
            Log.d("result: ", "doc= " + imgs);

            for(Element img : imgs) {
                image_src.add(img.attr("abs:src"));
            }
            for(Element img : imgs) {
                image_data_src.add(img.attr("abs:data-src"));
            }

            System.out.println("!!!!!!!!!!!!"+image_src); // 이미지 URL들
            System.out.println("!!!!!!!!!!!!"+image_data_src); // 이미지 URL들

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
            mImageView.setImageBitmap(result);
        }
    }
}