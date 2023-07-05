package com.example.androidbluecon1;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import java.text.ParseException;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import android.text.Editable;
import android.text.TextWatcher;

import android.support.annotation.Nullable;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


import android.preference.PreferenceManager;


import org.json.JSONArray;
import org.json.JSONException;



public class MainActivity extends AppCompatActivity {
    // activity_main 과 기능들을 선언
    private static final int REQUEST_ENABLE_BT = 1;
    private String previousPacket = "";
    // 알람 객체 생성
    private AlarmManager alarmManager;
    private GregorianCalendar mCalender;

    private NotificationManager notificationManager;
    private PermissionSupport permission;
    private static final String SETTINGS_PLAYER_JSON = "settings_item_json";


    NotificationCompat.Builder builder;


    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    TextView textInfo, textStatus, inputField, textView2, textView3;
    ListView listViewPairedDevice;
    private ListView logCatcher;
    LinearLayout inputPane, search_on, button1, button2, first_connect;
    ImageView imageView;

    TextView btnInstuctions;
    static final int REQ_CODE_SPEECH_INPUT = 100;
    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    ArrayAdapter arrayAdapter;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    // 검색관련 layout
    private List<String> searchlist;          // 데이터를 넣은 리스트변수
    private ListView searchlistView;          // 검색을 보여줄 리스트변수
    private EditText searcheditSearch;        // 검색어를 입력할 Input 창
    private SearchAdapter searchadapter;      // 리스트뷰에 연결할 아답터
    private ArrayList<String> searcharraylist;

    ArrayList<String> array_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        array_list = getStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON);

        // 알람 기능 선언
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mCalender = new GregorianCalendar();
        Log.v("HelloAlarmActivity", mCalender.getTime().toString());


        // activity_main에서 선언된 기능들을 선언
        textInfo = (TextView) findViewById(R.id.status);
        textStatus = (TextView) findViewById(R.id.info);
        logCatcher = (ListView) findViewById(R.id.logcatcher);
        listViewPairedDevice = (ListView) findViewById(R.id.pairedlist);
        inputPane = (LinearLayout) findViewById(R.id.inputpane);
        button1 = (LinearLayout) findViewById(R.id.button1);
        button2 = (LinearLayout) findViewById(R.id.button2);
        first_connect = (LinearLayout) findViewById(R.id.first_connect);
        search_on = (LinearLayout) findViewById(R.id.search_on);
        inputField = (TextView) findViewById(R.id.input);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        btnInstuctions = (TextView) findViewById(R.id.readbtn);
        arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, array_list);
        logCatcher.setAdapter(arrayAdapter);

        SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        Set<String> itemsSet = prefs.getStringSet("items", null);

        // 검색 관련 layout 선언
        searcheditSearch = (EditText) findViewById(R.id.searcheditSearch);
        searchlistView = (ListView) findViewById(R.id.searchlistView);

        // 검색리스트를 생성한다.
        searchlist = new ArrayList<String>();

        // 리스트의 모든 데이터를 arraylist에 복사한다.// list 복사본을 만든다.
        searcharraylist = new ArrayList<>();
        searcharraylist.addAll(array_list);

        // 리스트에 연동될 아답터를 생성한다.
        searchadapter = new SearchAdapter(searchlist, this);

        // 리스트뷰에 아답터를 연결한다.
        searchlistView.setAdapter(searchadapter);

        //permissionCheck();

        if (itemsSet != null) {
            arrayAdapter.addAll(itemsSet);
        }

        // 시스템에서 블루투스를 지원하지 않을때 경고문 출력
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this,
                    "이 시스템에서는 블루투스를 지원하지 않습니다.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 라즈베리파이에 맞는 UUID를 세팅
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        // 블루투스를 지원하지 않는 기기에게 사용할수 없음을 알림
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent;

        if (bluetoothAdapter.isEnabled()) {
            // 블루투스 관련 실행 진행
        } else {
            // 블루투스 활성화 하도록
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);
        }
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "이 기기에서는 블루투스를 지원하지 않습니다.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 권한 설정이 되어있는지 질의
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }

        // 초기 화면 구성
        String stInfo = "아래에서 스마트가방과 페어링을 진행해주세요";
        //+ "\n" +                bluetoothAdapter.getAddress();
        textInfo.setText(stInfo);
        textStatus.setText("스마트물품관리시스템에 오신걸 환영합니다!");

        // "전체 삭제" 버튼의 리스트 전체 삭제 기능 구현
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("전체삭제하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        array_list.clear();
                        array_list.addAll(array_list);
                        arrayAdapter.notifyDataSetChanged();
                        logCatcher.invalidateViews();
                        logCatcher.refreshDrawableState();
                        Toast.makeText(MainActivity.this, "삭제되었습니다.", Toast.LENGTH_LONG).show();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // 검색 기능 구현
        findViewById(R.id.Search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPane.setVisibility(View.GONE);
                search_on.setVisibility(View.VISIBLE);
                searcharraylist.clear();
                searcharraylist.addAll(array_list);
                searchlist.clear();
                searchlist.addAll(searcharraylist);
                searchadapter.notifyDataSetChanged();
                searcheditSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        // input창에 문자를 입력할때마다 호출된다.
                        // search 메소드를 호출한다.
                        String text = searcheditSearch.getText().toString();
                        search(text);
                    }
                });
            }
        });

        // logcatcher 아이템 클릭시 삭제하는 alertdialog 구현
        logCatcher.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("항목을 삭제하시겠습니까?");

                builder.setPositiveButton("예", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                            array_list.remove(position);
                            logCatcher.setItemChecked(-1, true);
                            arrayAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


        findViewById(R.id.instructionbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPane.setVisibility(View.GONE);
                search_on.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView2.setVisibility(View.GONE);
                textView3.setVisibility(View.GONE);
                first_connect.setVisibility(View.GONE);
                button1.setVisibility(View.GONE);
                button2.setVisibility(View.GONE);
                Intent intent = new Intent(getBaseContext(), instructions.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.imageAlarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPane.setVisibility(View.GONE);
                search_on.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView2.setVisibility(View.GONE);
                textView3.setVisibility(View.GONE);
                first_connect.setVisibility(View.GONE);
                button1.setVisibility(View.GONE);
                button2.setVisibility(View.GONE);
                Intent intent = new Intent(getBaseContext(), Alert_activity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.imageBluetooth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPane.setVisibility(View.GONE);
                search_on.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView2.setVisibility(View.GONE);
                textView3.setVisibility(View.GONE);
                first_connect.setVisibility(View.VISIBLE);
                button1.setVisibility(View.GONE);
                button2.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.imagelist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputPane.setVisibility(View.VISIBLE);
                search_on.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                textView2.setVisibility(View.GONE);
                textView3.setVisibility(View.GONE);
                first_connect.setVisibility(View.GONE);
                button1.setVisibility(View.GONE);
                button2.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        inputPane.setVisibility(View.GONE);
        search_on.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);
        textView3.setVisibility(View.VISIBLE);
        first_connect.setVisibility(View.GONE);
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
    }

    // 리스트 영구저장
    private void setStringArrayPref(Context context, String key, ArrayList<String> values) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray a = new JSONArray();

        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }

        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }

        editor.apply();
    }

    private ArrayList getStringArrayPref(Context context, String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(key, null);
        ArrayList urls = new ArrayList();

        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);

                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    @Override
    protected void onPause() {
        super.onPause();

        setStringArrayPref(getApplicationContext(), SETTINGS_PLAYER_JSON, array_list);
    }

    // 검색을 수행하는 메소드
    public void search(String charText) {

        // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
        searchlist.clear();
        // 문자 입력이 없을때는 모든 데이터를 보여준다.
        if (charText.length() == 0) {
            searchlist.addAll(searcharraylist);
        }
        // 문자 입력을 할때..
        else
        {
            // 리스트의 모든 데이터를 검색한다.
            for(int i = 0;i < searcharraylist.size(); i++)
            {
                // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                if (searcharraylist.get(i).toLowerCase().contains(charText))
                {
                    // 검색된 데이터를 리스트에 추가한다.
                    searchlist.add(searcharraylist.get(i));
                }
            }
        }
        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
        searchadapter.notifyDataSetChanged();
    }

    // "수동 등록" 버튼 기능 구현
    public void OnClickHandler1(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("수동 등록");

        final EditText input = new EditText(this);
        String timeString = getTimeString();
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // "추가" 버튼을 눌렀을 때 기능
        builder.setPositiveButton("추가", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                String text = input.getText().toString().trim(); // 입력한 텍스트 가져오기
                if (!text.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "입력한 값: " + text, Toast.LENGTH_SHORT).show();
                    arrayAdapter.add("물건 이름:" + text + "\n인식 시간: " + timeString);
                    vibrator.vibrate(200);
                }
            }
        });

        // 취소버튼을 눌렀을때 기능
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                Toast.makeText(getApplicationContext(), "취소되었습니다!", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // 현재시간 출력 함수
    private String getTimeString() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(calendar.getTime());
    }


    // 블루투스를 시작
    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                return;
            }
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();
    }

    private void permissionCheck() {

        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(this, this);

        // 권한 체크 후 리턴이 false로 들어오면
        if (!permission.checkPermission()){
            //권한 요청
            Toast.makeText(getApplicationContext(), "권한 설정이 되지 않았어요. \n 앱이 시작될 때 권한을 꼭 확인해주세요!", Toast.LENGTH_SHORT).show();
            permission.requestPermission();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
        if (!permission.permissionResult(requestCode, permissions, grantResults)) {
            // 다시 permission 요청
            permission.requestPermission();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // 기초 세팅 (페어링된 디바이스 리스트를 가져온 후, 버튼을 클릭시 해당 기기와 연결을 시작)
    private void setup() {

        // 권한 설정 -> 앞으로 이 부분은 전부 권한 설정으로 간주
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        // 블루투스 페어링된 기기를 전부 가져옴
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

            // 블루투스 페어링된 기기를 리스트에 추가
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
            }

            // 리스트 어댑터와 연결 , 리스트의 항목을 눌렀을때 동작 구현
            pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, pairedDeviceArrayList);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);
            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    BluetoothDevice device =
                            (BluetoothDevice) parent.getItemAtPosition(position);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        return;
                    }
                    Toast.makeText(MainActivity.this,
                            "다음 기기에 접속중입니다. : " + device.getName() + "\n"
                                    + "기기 주소: " + device.getAddress(),
                            Toast.LENGTH_LONG).show();

                    textStatus.setText("접속을 시작합니다...");
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();
                }
            });
        }
    }

    // Activity가 끝날때 실행되는 함수
    @Override
    protected void onDestroy() {
        SharedPreferences.Editor editor = getSharedPreferences("myPrefs", MODE_PRIVATE).edit();
        editor.putStringSet("items", new HashSet<String>((Collection<? extends String>) arrayAdapter));
        editor.apply();
        super.onDestroy();

        if (myThreadConnectBTdevice != null) {
            myThreadConnectBTdevice.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (myThreadConnected != null) {
                        inputField.setText(result.get(0));
                        arrayAdapter.add(System.getProperty("line.separator"));
                        arrayAdapter.add("--> " + inputField.getText().toString());
                        byte[] bytesToSend = inputField.getText().toString().getBytes();
                        myThreadConnected.write(bytesToSend);
                    }
                }
                break;
            }

            case REQUEST_ENABLE_BT: {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    setup();
                } else {
                    Toast.makeText(this,
                            "블루투스를 사용할 수 없습니다.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }
        }

    }


    // 블루투스 연결 코드
    private void startThreadConnected(BluetoothSocket socket) {

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    /*
    ThreadConnectBTdevice:
    백그라운드에서 스레드가 연결되었을때 되는 행동을 구현
    */
    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        // 블루투스 디바이스와 스레드 연결을 시도
        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    return;
                }
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 지속적으로 백그라운드 동작을 설정
        @Override
        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            //mBluetoothAdapter.cancelDiscovery();


            boolean success = false;
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    return;
                }
                bluetoothSocket.connect();
                // Log.e("","Connected");
            } catch (IOException e) {
                Log.e("", e.getMessage());
                try {
                    //Log.e("","trying fallback...");

                    bluetoothSocket = (BluetoothSocket) bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(bluetoothDevice, 1);
                    bluetoothSocket.connect();
                    success = true;
                    //Log.e("","Connected");
                } catch (Exception e2) {
                    Log.e("", "블루투스 연결을 생성할 수 없습니다!");
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }


            if (success) {
                //연결에 성공했을때...
                final String msgconnected = "Connected to: " + bluetoothDevice.getName();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textStatus.setText(msgconnected);
                        listViewPairedDevice.setVisibility(View.GONE);
                        first_connect.setVisibility(View.GONE);
                        inputPane.setVisibility(View.VISIBLE);
                        btnInstuctions.setVisibility(View.GONE);
                        textInfo.setVisibility(View.GONE);
                        textStatus.setVisibility(View.GONE);
                    }
                });

                startThreadConnected(bluetoothSocket);
            } else {
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "연결을 끝냈습니다!",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
    // thread 연결 후 통신하는 함수
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {

            byte[] buffer = new byte[1024];
            int bytes;
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    String strReceived = new String(buffer, 0, bytes);
                    String timeString = getTimeString();

                    if (!strReceived.equals(previousPacket)) {
                        previousPacket = strReceived;
                        final String msgReceived = strReceived;
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                arrayAdapter.add("물건 이름:" + msgReceived + "\n인식 시간: " + timeString);
                                Toast.makeText(getApplicationContext(), "입력한 값: " + msgReceived, Toast.LENGTH_SHORT).show();
                                vibrator.vibrate(200);
                            }
                        });
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "오류: 연결이 끊겼습니다.\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            textStatus.setText(msgConnectionLost);
                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // 연결 취소
        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
