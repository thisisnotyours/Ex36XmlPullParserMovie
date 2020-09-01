package com.suek.ex36xmlpullparsermovie;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 1)
    ListView listView;
    ArrayAdapter adapter;

    ArrayList<String> items= new ArrayList<>();

    // 5)
    String apiKey= "02d845b0a8862f4cab4cf05f234ae76a";  //kobis.or.kr 에서 발급받은 api key 값


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2)
        listView= findViewById(R.id.listview);
        adapter= new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }





    public void clickBtn(View view) {
        //버튼을 클릭했을때 잘되는지 테스트해본다
        /*items.add(new String("aaa"));
        adapter.notifyDataSetChanged();*/

        // 3) 리스트에 보여줄 데이터들을 네트워크를 통해서 읽어와서 분석하기..
        //    인터넷사용하려면 반드시 permission 을 작성해야함.
        //    즉, 네트워크작업은(오래걸리는 작업) 반드시 별도의 Thread 가 해야함
        Thread t= new Thread(){
            @Override
            public void run() {

                // 6)
                Date date= new Date(); //오늘날짜를 가지고있음
                date.setTime(date.getTime() - (1000*60*60*24) );  //1일전- 하루치의 시간이 빠짐
                SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");  //날짜를 지정한 패턴(yyyyMMdd)으로 만들어줌

                String dateStr= sdf.format(date);  //검색날짜 "20200526"



                // 4) 영화진흥위원회(네트워크)의 서버에서 일일박스오피스 정보가있는 xml 문서를 읽어오기
                String address= "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml?"
                                +"key=" + apiKey
                                +"&targetDt=" + dateStr;


                //###### 번외 ##################################################################################
                // api 28버전부터 인터넷 주소가 https 가 아닌 http 로 되면 동작안됨
                // 만약 되게하고 싶다면 Manifest.xml 에 추가작업   --->  android:usesCleartextTraffic="true" 속성추가
                //###### 번외 ##################################################################################



                // 7) 완성된 네트워크 주소와 연결하여 데이터 읽어오기
                //    무지개로드(Stream)를 만들어주는 해임달(URL)객체 생성
                try {
                    URL url= new URL(address);

                    // 8) 해임달에게 무지개로드 열어달라고 하기..
                    InputStream is= url.openStream();    //바이트 스트림(바이트로 읽어드림.. 그럼 어떻게 문자열로 읽어냄?)
                    InputStreamReader isr= new InputStreamReader(is);

                    // 9) xml 문서를 isr 로 부터 받아와서 분석해주는 분석가객체를 만들어주는 공장생성
                    XmlPullParserFactory factory= XmlPullParserFactory.newInstance();   //new 대신에
                    XmlPullParser xpp= factory.newPullParser();   //분석가객체 생산!
                    xpp.setInput(isr);

                    //xpp.next();  pullparse 는 이미 문서에 커서에 들어와있어서 생략
                    int eventType= xpp.getEventType();

                    StringBuffer buffer= new StringBuffer();  //스트링보관하는 곳(문자열을 모아놓는 녀석)


                    while (eventType != XmlPullParser.END_DOCUMENT){

                        switch (eventType){
                            case XmlPullParser.START_DOCUMENT:

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "분석을 시작합니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;

                            case XmlPullParser.START_TAG:
                                String tagName= xpp.getName();

                                if(tagName.equals("dailyBoxOffice")){
                                    buffer= new StringBuffer();
                                }else if(tagName.equals("rank")){
                                    buffer.append("순위 : ");      //기존에 있는 글자에 순위: 더함
                                    xpp.next();
                                    buffer.append(xpp.getText() + "\n");

                                }else if(tagName.equals("movieNm")){
                                    buffer.append("제목 : ");
                                    xpp.next();
                                    buffer.append(xpp.getText() + "\n");

                                }else if(tagName.equals("openDt")){
                                    buffer.append("개봉일 : ");
                                    xpp.next();
                                    buffer.append(xpp.getText() + "\n");

                                }else if(tagName.equals("audiCnt")){
                                    buffer.append("일 관객수 : ");
                                    xpp.next();
                                    buffer.append(xpp.getText() + "\n");

                                }else if(tagName.equals("audiAcc")){
                                    buffer.append("누적 관객수 : ");
                                    xpp.next();
                                    buffer.append(xpp.getText() + "\n");

                                }
                                break;

                            case XmlPullParser.TEXT:
                                break;

                            case XmlPullParser.END_TAG:
                                String tagName2= xpp.getName();
                                if(tagName2.equals("dailyBoxOffice")){
                                    //지금까지 누적된 StringBuffer 를 String 으로 변환
                                    String s= buffer.toString();
                                    items.add(s);
                                    //리스트뷰 갱신. 왜? 화면이 안보이니까 (화면변화는 반드시 UI(메인화면) Thread 만)
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });

                                }
                                break;
                        }

                        eventType= xpp.next();

                    }//while..

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "분석을 마쳤습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });





                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }


            }
        };
        t.start();

    }//clickBtn


}//MainActivity
