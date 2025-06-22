package com.example.blindclient;

import static android.content.ContentValues.TAG;

import static com.example.tools.Constants.Speech_key;
import static com.example.tools.Constants.serverURL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.blindclient.entity.Guardian;
import com.example.blindclient.entity.Location;
import com.example.blindclient.entity.OnlineStatus;
import com.example.blindclient.entity.ReturnMessage;
import com.example.blindclient.re.Operation;
import com.example.blindclient.re.Re;
import com.example.relativeclient.Relative_homeActivity;
import com.example.soniceyes.R;
import com.example.tools.NetworkHandler;
import com.example.users.User;
import com.example.volunteerclient.GPSUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.regex.Pattern;

public class Blind_homeActivity extends AppCompatActivity {

    Button buttonVoice;

    private Operation status = Operation.None; // 记录当前活动

    private SharedPreferences mSharedPreferences; // 语音识别缓存
    private SpeechRecognizer mIat; // 语音识别
    private SpeechSynthesizer mTts; // 语音合成

    private Navigation navi;
//    private AMapLocationClient mLocationClient; // 定位
    private Location currentLoc; // 当前位置
    private GPSUtils gpsUtils = null; // 定位
    private GPSUtils.Locid curLoc; // 当前位置

    private boolean voiceIsFirst = true; // 用于标记语音是否可以直接播放
    private Queue<String> voiceBuffer = new LinkedList<String>(); // 用于缓存未能成功播出的语音

//    private android.hardware.Camera camera = null;
//    private SurfaceView sfv_preview;

//    private volatile boolean finishPicture = false; // 标记结束拍照

//    private File outputImage = null;

    // 新拍照方式尝试
    private Intent serviceIntent;

    private BroadcastReceiver uploadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("UPLOAD_COMPLETED".equals(intent.getAction())) {
                boolean success = intent.getBooleanExtra("success", false);
                if(success){
                    mTts.startSpeaking("前方有障碍", mSynthesizerListener);
                }
                else{
                    mTts.startSpeaking("网络故障", mSynthesizerListener);
                }

                // 上传照片
//                Integer id = User.getUserId();
//                String ret = NetworkHandler.UploadImage(NetworkHandler.GetOkHttpClient(), serverURL + "/route/upload", outputImage, id.toString());
//                if (ret.equals("Danger")) {
//                    mTts.startSpeaking("前方有障碍", mSynthesizerListener);
//                } else if (ret.equals("Safe")) {
//                } else {
//                    mTts.startSpeaking("网络故障", mSynthesizerListener);
//                }
            }
        }
    };

    private SurfaceHolder.Callback cpHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    // 开始预览
//    private void startPreview(){
//        camera = Camera.open();
//        try {
//            camera.setPreviewDisplay(sfv_preview.getHolder());
//            camera.setDisplayOrientation(90);   // 让相机旋转90度
//            camera.startPreview();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    // 停止预览
//    private void stopPreview() {
//        camera.stopPreview();
//        camera.release();
//        camera = null;
//    }

    // 定位结果监听
//    AMapLocationListener mAMapLocationListener = new AMapLocationListener(){
//        @Override
//        public void onLocationChanged(AMapLocation aMapLocation) {
//            if(aMapLocation.getErrorCode() == 0){
//                currentLoc.setLoc(aMapLocation.getLatitude(), aMapLocation.getLongitude());
//                new Thread(() -> NetworkHandler.post(serverURL + "location", JSON.toJSONString(currentLoc))).start();
//            }
//            else{
//                // 定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
//                Log.e("AmapError","location Error, ErrCode:"
//                        + aMapLocation.getErrorCode() + ", errInfo:"
//                        + aMapLocation.getErrorInfo());
//            }
//        }
//    };

    private InitListener mInitListener = code -> {
        Log.i(TAG, "InitListener init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            Toast.makeText(Blind_homeActivity.this, "初始化错误:" + code, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Blind_homeActivity.this, "初始化成功", Toast.LENGTH_LONG).show();
        }
    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            Log.i(TAG, "持续说话");
        }

        @Override
        public void onBeginOfSpeech() {
            Log.i(TAG, "开始说话");
        }

        @Override
        public void onEndOfSpeech() {
            Log.i(TAG, "结束说话");
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            switch(Re.match(recognizerResult.getResultString())){
                case Navigation:
                    String placeStr = Re.searchPlace(recognizerResult.getResultString());
//                    mTts.startSpeaking("检测到需要导航到" + placeStr, mSynthesizerListener);
                    playVoice("检测到需要导航到" + placeStr);
                    Blind_homeActivity.this.serviceIntent = new Intent(Blind_homeActivity.this, BackgroundCameraService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent);
                    } else {
                        startService(serviceIntent);
                    }
                    status = Operation.Navigation;
                    navi = new Navigation(placeStr, Blind_homeActivity.this, getApplicationContext());
//                    photoThread.start();

                    break;
                case Volunteer:
//                    mTts.startSpeaking("检测到需要呼叫志愿者", mSynthesizerListener);
                    playVoice("检测到需要呼叫志愿者");
                    new Thread(() -> {
                        NetworkHandler.post(serverURL + "/help", JSON.toJSONString(currentLoc));
//                        mTts.startSpeaking("求助请求已发送", mSynthesizerListener);
                        playVoice("求助请求已发送");
                    }).start();
                    break;
                case Guardian:
//                    mTts.startSpeaking("检测到需要呼叫监护人", mSynthesizerListener);
                    playVoice("检测到需要呼叫监护人");
                    new Thread(() -> {
                        String retStr = NetworkHandler.get(serverURL + "/relation/getginfo?blind_id=" + User.getUserId());
                        ReturnMessage<Guardian> retMsg = JSON.parseObject(retStr, new TypeReference<ReturnMessage<Guardian>>(){});
                        if(retMsg.isSuccess()){
                            Guardian guardian = retMsg.getData();
//                            mTts.startSpeaking("正在尝试呼叫亲属" + guardian.getName(), mSynthesizerListener);
                            playVoice("正在尝试呼叫亲属" + guardian.getName());
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            Uri data = Uri.parse("tel:" + guardian.getUserName());
                            intent.setData(data);
                            startActivity(intent);
                        }
                        else{
//                            mTts.startSpeaking("未绑定亲属或网络连接不稳定，请重试", mSynthesizerListener);
                            playVoice("未绑定亲属或网络连接不稳定，请重试");
                        }
                    }).start();
                    break;
                case QuitNavi:
                    playVoice("检测到需要停止导航");
                    if(status == Operation.Navigation){
                        navi.stopNavi();
                        Intent serviceIntent = new Intent(Blind_homeActivity.this, BackgroundCameraService.class);
                        stopService(serviceIntent);
//                        photoThread.interrupt();
                        playVoice("停止导航成功");
                        finish();
                        //刷新当前活动
                        Intent intent = new Intent(Blind_homeActivity.this, Blind_homeActivity.class);
                        startActivity(intent);
                    }
                    else{
                        playVoice("当前没有在导航");
                    }
                    break;
                case Quit:
                    doDestroy();
                    break;
                case Error:
                default:
//                    mTts.startSpeaking("匹配错误，请尝试重新说", mSynthesizerListener);
                    playVoice("匹配错误，请尝试重新说");
                    break;
            }
        }

        @Override
        public void onError(SpeechError speechError) {
            Log.d(TAG, "onError " + speechError.getPlainDescription(true));
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    // 拍照线程
//    private Thread photoThread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            while (!Thread.currentThread().isInterrupted()) {
//                startPreview();
//                camera.takePicture(null, null, (data, camera) -> {
//                    new Thread(() -> {
//                        try {
//                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                            outputImage = new File(getExternalCacheDir(), "output_image.jpg");
//                            // 文件输出流
//                            FileOutputStream fileOutputStream = new FileOutputStream(outputImage);
//                            // 压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
//                            float byteCount = (float) bitmap.getAllocationByteCount();
//                            int quality = (int) (1048000000.0 / byteCount);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
//                            // 写入，这里会卡顿，因为图片较大
//                            fileOutputStream.flush();
//                            // 记得要关闭写入流
//                            fileOutputStream.close();
//
//                            // 上传照片
//                            Integer id = User.getUserId();
//                            String ret = NetworkHandler.UploadImage(NetworkHandler.GetOkHttpClient(), serverURL + "/route/upload", outputImage, id.toString());
//                            if (ret.equals("Danger")) {
//                                mTts.startSpeaking("前方有障碍", mSynthesizerListener);
//                            } else if (ret.equals("Safe")) {
//                            } else {
//                                mTts.startSpeaking("网络故障", mSynthesizerListener);
//                            }
//                        } catch (IOException e) {
//                            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
//                        }
//                        finishPicture = true;
//                    }).start();
//                });
//                while (!(finishPicture || Thread.currentThread().isInterrupted())) ;
//                stopPreview();
//                finishPicture = false;
//            }
//        }
//    });

    // 播放语音
    public void playVoice(String msg){
        if(voiceIsFirst){
            voiceIsFirst = false;
            mTts.startSpeaking(msg, mSynthesizerListener);
        }
        else{
            voiceBuffer.offer(msg);
        }
    }

    private SynthesizerListener mSynthesizerListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            Log.i(TAG, "开始播放");
        }

        @Override
        public void onBufferProgress(int percent, int i1, int i2, String s) {
            Log.i(TAG, "合成进度：" + percent + "%");
        }

        @Override
        public void onSpeakPaused() {
            Log.i(TAG, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.i(TAG, "继续播放");
        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            if (speechError == null) {
                if (!voiceBuffer.isEmpty()) {
                    mTts.startSpeaking(voiceBuffer.poll(), mSynthesizerListener);
                }
                else{
                    voiceIsFirst = true;
                }
            } else {
                Log.i(TAG, speechError.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.blind_home);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        // 初始化界面
        initView();

        // 语音识别注册
        SpeechUtility.createUtility(Blind_homeActivity.this, SpeechConstant.APPID +"="+Speech_key);

        // 设置并更新登录状态
        new Thread(() -> {
            String postJSON = JSON.toJSONString((new OnlineStatus(User.getUserId())).setOnline());
            NetworkHandler.post(serverURL + "/online", postJSON);
        }).start();

        // 初始化 currentLoc
        currentLoc = new Location(User.getUserId());

        // 初始化识别对象
        mIat = SpeechRecognizer.createRecognizer(Blind_homeActivity.this, mInitListener);
        mSharedPreferences = getSharedPreferences("com.iflytek.setting",
                Activity.MODE_PRIVATE);

        // 初始化语音合成对象
        mTts = SpeechSynthesizer.createSynthesizer(Blind_homeActivity.this, mInitListener);

        // 设置语音识别参数
        setParam();

        IntentFilter filter = new IntentFilter("UPLOAD_COMPLETED");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 需要额外权限
            registerReceiver(uploadReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12
            registerReceiver(uploadReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // 旧版本
            registerReceiver(uploadReceiver, filter);
        }

        // 高德隐私合规校验
        AMapLocationClient.updatePrivacyShow(Blind_homeActivity.this, true, true);
        AMapLocationClient.updatePrivacyAgree(Blind_homeActivity.this, true);
        AMapLocationClient.setApiKey("f39b1bb9d2f77d0e7470f05aef320f2d");

//        // 定位
//        AMapLocationClientOption locationOption = new AMapLocationClientOption();
//        try {
//            mLocationClient = new AMapLocationClient(getApplicationContext());
//        } catch (Exception e) {
//            Log.e("Navigation", Objects.requireNonNull(e.getLocalizedMessage()));
//        }
//        // 定位模式
//        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        // 定位设置
//        mLocationClient.setLocationOption(locationOption);
//        // 设置监听
//        mLocationClient.setLocationListener(mAMapLocationListener);
//        // 开始定位
//        mLocationClient.startLocation();

        // GPS定位
        gpsUtils = GPSUtils.getInstance();
        new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()){
                curLoc = gpsUtils.getLocation(Blind_homeActivity.this);
                currentLoc.setLoc(curLoc.lat, curLoc.lon);
                String ret = NetworkHandler.post(serverURL + "/location", JSON.toJSONString(currentLoc));
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ignored) {}
            }
        }).start();

        new Thread(() -> {
            Log.i(TAG, "访问URL为：" + serverURL + "/relation/getginfo?blind_id=" + User.getUserId());
            String retStr = NetworkHandler.get(serverURL + "/relation/getginfo?blind_id=" + User.getUserId());
            if(Pattern.matches("(.*)IOException(.*)", retStr)){
                Log.e(TAG, "连不上服务器！！！！！！");
                return;
            }
            ReturnMessage<Guardian> retMsg = JSON.parseObject(retStr, new TypeReference<ReturnMessage<Guardian>>(){});
            if(retMsg.isSuccess()){
                Guardian guardian = retMsg.getData();
                User.setBindging(true);
                User.setRelationId(guardian.getId());
                User.setBlindPhone(guardian.getUserName());
            }
            else{
                User.setBindging(false);
            }
        }).start();
    }

    public void setParam() {

        // 语音识别

        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        String mEngineType = SpeechConstant.TYPE_CLOUD;
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        String resultType = "plain";
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        String language = "zh_cn";
        String lag = mSharedPreferences.getString("iat_language_preference",
                "mandarin");
        // 设置语言
        Log.e(TAG, "language = " + language);
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, lag);
        Log.e(TAG, "last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));

        // 设置音频保存路径，保存音频格式支持pcm、wav.
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                getExternalFilesDir("msc").getAbsolutePath() + "/iat.wav");

        // 语音合成

        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //支持实时音频返回，仅在synthesizeToUri条件下支持
            mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1");
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
        }
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
        // 设置音频保存路径，保存音频格式支持pcm、wav
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, getExternalFilesDir(null) + "/msc/tts.pcm");
    }

    // Register all buttons & set listeners
    @SuppressLint("ClickableViewAccessibility")
    protected void initView() {
        View view = View.inflate( this, (R.layout.blind_home), null);
//        sfv_preview = findViewById(R.id.sfv_preview);
        buttonVoice = findViewById(R.id.buttonVoice);

        buttonVoice.setBackgroundColor(Color.WHITE);
        buttonVoice.setTextColor(Color.BLACK);

//        sfv_preview.getHolder().addCallback(cpHolderCallback);

        buttonVoice.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    buttonVoice.setBackgroundColor(Color.RED);
                    buttonVoice.setTextColor(Color.WHITE);
                    mIat.startListening(mRecognizerListener);
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    buttonVoice.setBackgroundColor(Color.WHITE);
                    buttonVoice.setTextColor(Color.BLACK);
                    mIat.stopListening();
                }
                return false;
            }
        });
    }

    //定义一个标志表示资源是否释放
    private boolean mIsDestroyed = false;

    private void doDestroy()  {
        if (mIsDestroyed) {
            return;
        }
        new Thread(() -> {
            NetworkHandler.post(serverURL + "/online", JSON.toJSONString(new OnlineStatus(User.getUserId()).setOffline()));
//            currentLoc.setLoc(0, 0);
//            new Thread(() -> NetworkHandler.post(serverURL + "location", JSON.toJSONString(currentLoc))).start();
            ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
            for (ActivityManager.AppTask appTask : appTaskList) {
                appTask.finishAndRemoveTask();
            }
        }).start();
        Intent serviceIntent = new Intent(this, BackgroundCameraService.class);
        stopService(serviceIntent);
        mIsDestroyed = true;
    }

//    @Override
//    protected void onPause() {
//        if (isFinishing()) {
//            doDestroy();
//        }
//        super.onPause();
//    }
//
    @Override
    public void onDestroy() {
        unregisterReceiver(uploadReceiver);
        super.onDestroy();
    }
}
