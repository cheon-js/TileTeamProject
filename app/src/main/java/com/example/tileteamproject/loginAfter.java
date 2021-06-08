package com.example.tileteamproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class loginAfter extends AppCompatActivity{

    private TMapGpsManager tMapGps = null;
    private TMapView tMapView;
    private double lat;
    private double lon;
    private double realdistance = 0; //거리값
    private static String AppKey = "l7xx2b1c5cd91b914c2c9c80aab1109ae5d3";
    private int choiceRoute = 0; //경로 종류 선택받음
    private int check = 0;


    ArrayAdapter<POI> mAdapter;
    LinearLayout layout;

    private double currentLatitude;
    private double currentlongitude;
    private boolean locationState = true; //현위치로 이동 여부
//    private TMapPoint tMapPointStart = null;
    private TMapPoint tMapPointEnd = null;
    private TMapPoint tMapPointStart = new TMapPoint(35.147303, 129.034140);//tpqmsdlffpqms
    //private TMapPoint tMapPointEnd = new TMapPoint(35.15222715129512, 129.03291744152403);//영화의 전당
//35.152215129512, 129.03291744152403 스타팰리스
//35.14491598554919, 129.03571818298877 정보관
    private DrawerLayout mDrawerLayout;
    private Context context = this;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase,ref;
    private final ArrayList<Integer> list = new ArrayList<>();
    // 사용자 정의 함수로 블루투스 활성 상태의 변경 결과를 App으로 알려줄때 식별자로 사용됨 (0보다 커야함)
    static final int REQUEST_ENABLE_BT = 10;
    int mPariedDeviceCount = 0;
    Set<BluetoothDevice> mDevices;
    // 폰의 블루투스 모듈을 사용하기 위한 오브젝트.
    BluetoothAdapter mBluetoothAdapter;
    /**
     BluetoothDevice 로 기기의 장치정보를 알아낼 수 있는 자세한 메소드 및 상태값을 알아낼 수 있다.
     연결하고자 하는 다른 블루투스 기기의 이름, 주소, 연결 상태 등의 정보를 조회할 수 있는 클래스.
     현재 기기가 아닌 다른 블루투스 기기와의 연결 및 정보를 알아낼 때 사용.
     */
    BluetoothDevice mRemoteDevie;
    // 스마트폰과 페어링 된 디바이스간 통신 채널에 대응 하는 BluetoothSocket
    BluetoothSocket mSocket = null;
    OutputStream mOutputStream = null;
    BufferedReader mInputStream = null;
    GPSTread thread = new GPSTread();
    GoogleSignInAccount signInAccount;
    String googlename;
    String gps = null;
    double LatF;
    double laV;
    float LongF;
    float loV;


    @SuppressLint("ClickableViewAccessibility")
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE); //키보드 내리기

        layout = (LinearLayout) findViewById(R.id.tmap);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        googlename = signInAccount.getDisplayName();

        checkBluetooth();
        delete();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver, filter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new ItemSelectedListener());



        //상태바 투명 & 아이콘 회색
        setStateBar();

        //TMapAPI 활용(지도, 현재위치)
        setTMap();

        //현재위치를 받아오는 부분
        TMapGpsManager gps = new TMapGpsManager(this);
        gps.setMinTime(1000);
        gps.setMinDistance(5);
        gps.setProvider(gps.GPS_PROVIDER);
        //안드로이드 기기의 API 버전이 23이상일 때 위치권한 허용에 관한 여부를 물음
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }
        gps.OpenGps();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentlongitude = location.getLongitude();
                    tMapView.setLocationPoint(currentlongitude, currentLatitude);
                    if (locationState==true) {
                        tMapView.setCenterPoint(currentlongitude, currentLatitude); //지도 센터에 처음 한번만 뜨게함
                        locationState = false;
                    }
                }
            }
            public void onProviderDisabled(String provider) { }
            public void onProviderEnabled(String provider) { }
            public void onStatusChanged(String provider, int status, Bundle extras) { }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, mLocationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);






//        /* 도착지 text 삭제 및 경로 제거 */
//        ImageView cancelFin = (ImageView) findViewById(R.id.cancel_fin);
//        cancelFin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                tMapView.removeTMapPath();//경로 제거
//                editEnd.setText(null);//text 삭제
//                tMapPointEnd = null; //도착지 초기화
//                listView.setVisibility(View.GONE); //listview 숨기기
//            }
//        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();

                if(id == R.id.account){
                    Toast.makeText(context, title + ": 마이페이지 이동 ", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),MyPage.class);
                    startActivity(intent);
                }
                else if(id == R.id.setting){
                    Toast.makeText(context, title + ": 설정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                }
                else if(id == R.id.logout){
                    Toast.makeText(context,   "로그아웃 후 시작화면으로 이동합니다.  ", Toast.LENGTH_SHORT).show();
                    signOut();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                else if(id == R.id.butt) {
                    Toast.makeText(context, "경로 삭제", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),test.class);
                    startActivity(intent);
                }
                return true;
            }
            private void signOut() {
                FirebaseAuth.getInstance().signOut();
            }
        });

    } // -- onCreate()

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action){
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    final long[] timings = new long[] {0, 3000, 0, 3000, 0, 3000};
                    //final int[] amplitudes = new int[] {0, 50, 100, 50, 150};

                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, 0));
                    } else {
                        vibrator.vibrate(10000);
                    }
                    break;

            }
        }
    };

    BluetoothDevice getDeviceFromBondedList(String name) {
        // BluetoothDevice : 페어링 된 기기 목록을 얻어옴.
        BluetoothDevice selectedDevice = null;
        // getBondedDevices 함수가 반환하는 페어링 된 기기 목록은 Set 형식이며,
        // Set 형식에서는 n 번째 원소를 얻어오는 방법이 없으므로 주어진 이름과 비교해서 찾는다.
        for(BluetoothDevice deivce : mDevices) {
            // getName() : 단말기의 Bluetooth Adapter 이름을 반환
            if(name.equals(deivce.getName())) {
                selectedDevice = deivce;
                break;
            }
        }
        return selectedDevice;
    }

    public void connectToSelectedDevice(String selectedDeviceName) {
        // BluetoothDevice 원격 블루투스 기기를 나타냄.
        mRemoteDevie = getDeviceFromBondedList(selectedDeviceName);
        // java.util.UUID.fromString : 자바에서 중복되지 않는 Unique 키 생성.
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 소켓 생성, RFCOMM 채널을 통한 연결.
            // createRfcommSocketToServiceRecord(uuid) : 이 함수를 사용하여 원격 블루투스 장치와 통신할 수 있는 소켓을 생성함.
            // 이 메소드가 성공하면 스마트폰과 페어링 된 디바이스간 통신 채널에 대응하는 BluetoothSocket 오브젝트를 리턴함.
            mSocket = mRemoteDevie.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect(); // 소켓이 생성 되면 connect() 함수를 호출함으로써 두기기의 연결은 완료된다.

            // 데이터 송수신을 위한 스트림 얻기.
            // BluetoothSocket 오브젝트는 두개의 Stream을 제공한다.
            // 1. 데이터를 보내기 위한 OutputStrem
            // 2. 데이터를 받기 위한 InputStream
            mOutputStream = mSocket.getOutputStream();
            mInputStream = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

            // 데이터 수신 준비.
            //beginListenForData();

            thread.start();

        }catch(Exception e) { // 블루투스 연결 중 오류 발생
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            //finish();  // App 종료
        }
    }

    class GPSTread extends Thread{


        @Override
        public void run() {
            //readBufferPosition = 0;                 // 버퍼 내 수신 문자 저장 위치.
            //readBuffer = new byte[1024];            // 수신 버퍼.
            int bytes;
            byte[] buffer = new byte[1024];
            String targetStr = "GPGGA";




            while(!Thread.currentThread().isInterrupted()){
                try{
                    String read = mInputStream.readLine();
                    if(targetStr.equals(read.substring(1,6))){
                        System.out.println(read);

                        int first = read.indexOf(",");
                        int two = read.indexOf(",", first +1);
                        int three = read.indexOf(",", two +1);
                        int four = read.indexOf(",", three + 1);
                        int five = read.indexOf(",",four +1);
                        System.out.println(first);
                        System.out.println(two);
                        System.out.println(three);
                        System.out.println(four);
                        System.out.println(five);

                        String Lat = read.substring(two+1, three);
                        String Long = read.substring(four + 1, five);

                        System.out.println("Lat =" + Lat);
                        System.out.println("Long =" +Long);

                        String Lat1 = Lat.substring(0, 2);
                        String Lat2 = Lat.substring(2);

                        String Long1 = Long.substring(0, 3);
                        String Long2 = Long.substring(3);

                        LatF = Double.parseDouble(Lat1) + Double.parseDouble(Lat2)/60;

                        LongF = Float.parseFloat(Long1) + Float.parseFloat(Long2)/60;

                        String wedo = Double.toString(LatF);

                       // String wedo2 = wedo.substring(0,7);
                        String kyengdo = Float.toString(LongF);
                       // String kyeongdo2 = kyengdo.substring(0,7);
                        gps = wedo +","+ kyengdo;
                        System.out.println(gps);
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        long now = System.currentTimeMillis();
                        Date mDate = new Date(now);
                        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd") ;
                        SimpleDateFormat simpleDate1 = new SimpleDateFormat(""+System.currentTimeMillis());
                        String time = simpleDate.format(mDate);
                        String fulltime = simpleDate1.format(mDate);

                        mDatabase.child(googlename).child(time).child(fulltime).setValue(gps);
                        SystemClock.sleep(10000);
                    }


                }catch (Exception e) {
                    e.printStackTrace();
                }
               /* Message msg = new Message();
                msg.what = 0;
                msg.obj = gps;
                gpshandler.sendMessage(msg);*/
            }

        }
    }

    /*class beginListenGPSData extends Handler {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);


            switch (msg.what){
                case 0:


            }
        }
    }*/

    // 블루투스 지원하며 활성 상태인 경우.
    void selectDevice() {
        // 블루투스 디바이스는 연결해서 사용하기 전에 먼저 페어링 되어야만 한다
        // getBondedDevices() : 페어링된 장치 목록 얻어오는 함수.
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPariedDeviceCount = mDevices.size();

        if(mPariedDeviceCount == 0 ) { // 페어링된 장치가 없는 경우.
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            //finish(); // App 종료.
        }
        // 페어링된 장치가 있는 경우.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");

        // 각 디바이스는 이름과(서로 다른) 주소를 가진다. 페어링 된 디바이스들을 표시한다.
        List<String> listItems = new ArrayList<String>();
        for(BluetoothDevice device : mDevices) {
            // device.getName() : 단말기의 Bluetooth Adapter 이름을 반환.
            listItems.add(device.getName());
        }
        listItems.add("취소");  // 취소 항목 추가.


        // CharSequence : 변경 가능한 문자열.
        // toArray : List형태로 넘어온것 배열로 바꿔서 처리하기 위한 toArray() 함수.
        final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
        // toArray 함수를 이용해서 size만큼 배열이 생성 되었다.
        listItems.toArray(new CharSequence[listItems.size()]);

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                // TODO Auto-generated method stub
                if(item == mPariedDeviceCount) { // 연결할 장치를 선택하지 않고 '취소' 를 누른 경우.
                    Toast.makeText(getApplicationContext(), "연결할 장치를 선택하지 않았습니다.", Toast.LENGTH_LONG).show();
                    //finish();
                }
                else { // 연결할 장치를 선택한 경우, 선택한 장치와 연결을 시도함.
                    connectToSelectedDevice(items[item].toString());
                }
            }

        });

        builder.setCancelable(false);  // 뒤로 가기 버튼 사용 금지.
        AlertDialog alert = builder.create();
        alert.show();
    }


    void checkBluetooth() {
        /**
         * getDefaultAdapter() : 만일 폰에 블루투스 모듈이 없으면 null 을 리턴한다.
         이경우 Toast를 사용해 에러메시지를 표시하고 앱을 종료한다.
         */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null ) {  // 블루투스 미지원
            Toast.makeText(getApplicationContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            //finish();  // 앱종료
        }
        else { // 블루투스 지원
            /** isEnable() : 블루투스 모듈이 활성화 되었는지 확인.
             *               true : 지원 ,  false : 미지원
             */
            if(!mBluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
                Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // REQUEST_ENABLE_BT : 블루투스 활성 상태의 변경 결과를 App 으로 알려줄 때 식별자로 사용(0이상)
                /**
                 startActivityForResult 함수 호출후 다이얼로그가 나타남
                 "예" 를 선택하면 시스템의 블루투스 장치를 활성화 시키고
                 "아니오" 를 선택하면 비활성화 상태를 유지 한다.
                 선택 결과는 onActivityResult 콜백 함수에서 확인할 수 있다.
                 */
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else // 블루투스 지원하며 활성 상태인 경우.
                selectDevice();
        }
    }



    // onDestroy() : 어플이 종료될때 호출 되는 함수.
    //               블루투스 연결이 필요하지 않는 경우 입출력 스트림 소켓을 닫아줌.
    @Override
    protected void onDestroy() {
        try{
            thread.interrupt(); // 데이터 수신 쓰레드 종료
            mInputStream.close();
            mSocket.close();
        }catch(Exception e){}
        super.onDestroy();
    }


    // onActivityResult : 사용자의 선택결과 확인 (아니오, 예)
    // RESULT_OK: 블루투스가 활성화 상태로 변경된 경우. "예"
    // RESULT_CANCELED : 오류나 사용자의 "아니오" 선택으로 비활성 상태로 남아 있는 경우  RESULT_CANCELED

    /**
     사용자가 request를 허가(또는 거부)하면 안드로이드 앱의 onActivityResult 메소도를 호출해서 request의 허가/거부를 확인할수 있다.
     첫번째 requestCode : startActivityForResult 에서 사용했던 요청 코드. REQUEST_ENABLE_BT 값
     두번째 resultCode  : 종료된 액티비티가 setReuslt로 지정한 결과 코드. RESULT_OK, RESULT_CANCELED 값중 하나가 들어감.
     세번째 data        : 종료된 액티비티가 인테트를 첨부했을 경우, 그 인텐트가 들어있고 첨부하지 않으면 null
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // startActivityForResult 를 여러번 사용할 땐 이런 식으로 switch 문을 사용하여 어떤 요청인지 구분하여 사용함.
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) { // 블루투스 활성화 상태
                    selectDevice();
                }
                else if(resultCode == RESULT_CANCELED) { // 블루투스 비활성화 상태 (종료)
                    Toast.makeText(getApplicationContext(), "블루투수를 사용할 수 없어 프로그램을 종료합니다", Toast.LENGTH_LONG).show();
                   // finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //상태바 투명 & 아이콘 회색 설정 함수
    private void setStateBar() {
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (view != null) {
                // 23 버전 이상일 때 상태바 하얀 색상에 회색 아이콘 색상을 설정
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                getWindow().setStatusBarColor(Color.parseColor("#f2f2f2"));
            }
        } else if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            getWindow().setStatusBarColor(Color.BLACK);
        }
    }



    //TMapAPI 활용(지도, 현재위치)
    private void setTMap() {
        /* 지도 부분 */
        LinearLayout linearLayoutTmap = (LinearLayout) findViewById(R.id.tmap);
        tMapView = new TMapView(this);
        tMapView.setSKTMapApiKey(AppKey);
        linearLayoutTmap.addView(tMapView);
        /* 현위치 아이콘표시 */
        tMapView.setIconVisibility(true);
        /* 줌레벨 */
        tMapView.setZoomLevel(15);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        /*현재 위치 마커 커스텀*/
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.custom_poi_marker_end);
        //tMapView.setIcon(bitmap);
    }


    // 경로 그리는 함수
    public void drawCashPath(TMapPoint tMapPointStart, TMapPoint tMapPointEnd) {
        TMapData tmapdata = new TMapData();
        ArrayList passList = new ArrayList<>();
        //자동차 다중 경로
        tmapdata.findPathDataWithType(TMapData.TMapPathType.CAR_PATH, tMapPointStart, tMapPointEnd, null, choiceRoute, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setLineColor(Color.BLUE); // Color.rgb(85, 90, 181)
                tMapView.addTMapPath(polyLine);
                realdistance = polyLine.getDistance(); //거리
            }
        });
    }


    //주소 검색


    //마커 표시 함수
    public void addMarker(TMapPOIItem poi) {
        TMapMarkerItem item = new TMapMarkerItem();
        item.setTMapPoint(poi.getPOIPoint());
        Bitmap icon = ((BitmapDrawable) ContextCompat.getDrawable(this, R.drawable.custom_poi_marker_selected)).getBitmap();
        item.setIcon(icon);
        item.setPosition(0.5f, 1);
        item.setCalloutTitle(poi.getPOIName());
        //item.setCalloutSubTitle(poi.getPOIContent());
        item.setCanShowCallout(true);
        tMapView.addMarkerItem(poi.getPOIID(), item);
    }


    //해당 좌표로 지도 이동 함수
    private void moveMap(double lat, double lng) {
        tMapView.setCenterPoint(lng, lat);
    }

    //좌표를 주소로 변환하는 함수
    public String getCurrentAddress(double latitude, double longitude) {
        //지오코더 - GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }

  /*  //뒤로가기 - listview 지우기, 앱 종료
    public void onBackPressed() {
        //super.onBackPressed(); // 기존 뒤로 가기 버튼의 기능을 막기 위해 주석 처리 또는 삭제

        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지났으면 Toast 출력
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            listView.setVisibility(View.GONE); //주소 선택 후 listview 안보이게
            return;
        }
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지나지 않았으면 종료
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {
            finish();
        }
    }
*/
    public void delete(){
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMdd");
        String time = simpleDate.format(mDate);


        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        String delname = signInAccount.getDisplayName();
        ref = FirebaseDatabase.getInstance().getReference(delname);//delname 손건
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    String data = snapshot.getKey();//data 값이 위도 123123 경도 123123 String 형식이라 값을 못받음
                    int data2 = Integer.parseInt(data);
                    list.add(data2);
                }
                int realdata = Integer.parseInt(time);
                int Mdata = realdata-3;

                Iterator iterator = list.iterator();
                while (iterator.hasNext()){
                    int ttime = (int) iterator.next();
                    if(ttime < Mdata){
                        String deletedata = Integer.toString(ttime);
                        mDatabase.child(delname).child(deletedata).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //mDatabase.child(delname).child(time).removeValue();
    }

    class ItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener{

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            String name = signInAccount.getDisplayName();
            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cancel);
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 50, 50, true);
            switch(menuItem.getItemId())
            {
                /* 트래커 위치 확인 설정 버튼 */
                case R.id.tracker:
                    if(check%2==0){

                        ref = FirebaseDatabase.getInstance().getReference(name);
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                Object o = snapshot.getValue();
                                String dbAll = o.toString();
                                //String [] all=null;
                                //Log.e("test",""+a);
                                String [] strArray = dbAll.split("=");

                                for(int i=2;i<strArray.length;i++) {
                                    if(40< strArray[i].length() && strArray[i].length()<43) {
                                        String laValue = strArray[i].substring(0,17); //위도 파싱
                                        String loValue = strArray[i].substring(18,27); //경도 파싱
                                        //Log.e("test", "" + strArray[i]+ "/////////"+c+"//"+b);
                                        laV = Double.parseDouble(laValue);
                                        loV = Float.parseFloat(loValue);
                                        System.out.println(laV + "asdasd"+loV);
                                        tMapView.removeAllMarkerItem();
                                        //TMapPoint tpoint = tMapView.getLocationPoint();

                                        tMapView.setIconVisibility(true);

                                        ArrayList alTMapPoint = new ArrayList();
                                        for(int k=0; k<strArray.length;k++) {
                                            alTMapPoint.add(new TMapPoint(laV, loV));//현재 트래커 좌표
                                            alTMapPoint.add(new TMapPoint(laV, loV));
                                            alTMapPoint.add(new TMapPoint(laV, loV));
                                            alTMapPoint.add(new TMapPoint(laV, loV));
                                        }
                                        //alTMapPoint.add(new TMapPoint(35.144963769997695, 129.03680991327593));//종로3가
                                        //alTMapPoint.add(new TMapPoint(35.144963769997695, 129.037809913275935));//종로5가
                                        for (int j = 0; j < alTMapPoint.size(); j++) {
                                            //for(ArrayList  add : alTMapPoint ){
                                            TMapMarkerItem markerItem1 = new TMapMarkerItem();
                                            // 마커 아이콘 지정
                                            markerItem1.setIcon(resized);
                                            // 마커의 좌표 지정
                                            markerItem1.setTMapPoint((TMapPoint) alTMapPoint.get(j));

                                            //지도에 마커 추가
                                            tMapView.addMarkerItem("markerItem" + j, markerItem1);
                                        }
                                    }
                                    //String b = strArray[i].substring(10,18);

                                }

                                //
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                            }
                        });



                        check ++;
                    }else{
                        check++;
                        tMapView.removeAllMarkerItem();
                        tMapView.removeTMapPath();
                    }
                    break;
                case R.id.main:
                    if (check % 2 == 0){
                    Toast.makeText(context, " 길찾기 ", Toast.LENGTH_SHORT).show();
                    tMapView.removeAllMarkerItem();
                    tMapPointEnd = new TMapPoint(LatF, LongF);
                    drawCashPath(tMapPointStart, tMapPointEnd);

                    check ++;
                        //Log.e("test","Test"+check);
                    }else{
                        check++;
                        tMapView.removeAllMarkerItem();
                        tMapView.removeTMapPath();
                        //Log.e("test","Test"+check);
                    }
                    break;
                case R.id.location:
                    if (check % 2 == 0) {
                        Toast.makeText(context, " location ", Toast.LENGTH_SHORT).show();
                        TMapPoint tpoint = tMapView.getLocationPoint();
                        double Latitude_ = tpoint.getLatitude(); //위도
                        double Longitude_ = tpoint.getLongitude(); //경도
                        tMapView.setCenterPoint(Longitude_, Latitude_);
                        /* 현위치 회전 */
                        tMapView.setCompassMode(true);
                        /*현재 위치 마커 레이더 생성*/
                        tMapView.setSightVisible(true);
                        check++;
                    }else{
                        check++;
                        /* 현위치 회전 */
                        tMapView.setCompassMode(false);
                        /*현재 위치 마커 레이더 생성*/
                        tMapView.setSightVisible(false);
                    }
                    break;

            }
            return true;
        }
    }






}
