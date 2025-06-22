//package com.example.users;
//
//import android.annotation.SuppressLint;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.EditText;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.content.ContextCompat;
//
//import com.example.relativeclient.Mythread.MythreadGetexist;
//import com.example.relativeclient.Mythread.MythreadPost;
//import com.example.soniceyes.R;
//import com.mob.MobSDK;
//
////验证码部分引入的包
//import android.os.Handler;
//import android.os.Message;
//import android.text.TextUtils;
//
//import cn.smssdk.EventHandler;
//import cn.smssdk.SMSSDK;
//
//public class RegisterActivity extends AppCompatActivity {
//    static EditText user;
//    static EditText pwo;
//    EditText rpw;
//    EditText edit_phone,edit_cord;
//    Button buttonR, buttonB,btn_getCord;
//    RadioGroup roleq;
//    CheckBox checkBox3;
//    static String role = "";
//    String phone_number,cord_number;
//
//    EventHandler eventHandler;
//    private boolean flag=true;
//    private boolean check=false;
//    private boolean registersuccess=false;
//
//    TextView agreement;
//
//    /////////////使用Handler来分发Message对象到主线程中，处理事件///////////
//
//    @SuppressLint("HandlerLeak")
//    Handler handler=new Handler()
//    {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            int event=msg.arg1;
//            int result=msg.arg2;
//            Object data=msg.obj;
//
////            //这一段要重新改，判断该账号是否已经注册过要通过向服务器发出申请检查
//            if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
//                if (result == SMSSDK.RESULT_COMPLETE) {
//                    boolean smart = (Boolean) data;
//                    if (smart) {
//                        Toast.makeText(getApplicationContext(), "该手机号已经注册过，请重新输入",
//                                Toast.LENGTH_LONG).show();
//                        edit_phone.requestFocus();
//                        return;
//                    }
//                }
//            }
//            if(result==SMSSDK.RESULT_COMPLETE)
//            {
//                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
//                    check=true;
////                    Toast.makeText(getApplicationContext(),
////                            "验证码输入正确，再次点击注册完成注册",
////                            Toast.LENGTH_LONG).show();
//
//                    String res = "欢迎你，你的注册信息如下：\n" + "用户名：" + user.getText().toString().trim() + "\n密码：" + pwo.getText().toString().trim() + "\n电话：" + edit_phone.getText().toString().trim() + "\n身份" + role + "\n";
//                    Toast.makeText(RegisterActivity.this, res, Toast.LENGTH_LONG).show();
//
//                    //向服务器传送数据
//                    MythreadPost t=new MythreadPost(user.getText().toString().trim(),//用户名
//                            pwo.getText().toString().trim(),//密码
//                            edit_phone.getText().toString().trim(),//手机号（账号）
//                            role);//身份
//                    t.start();
//
//                    registersuccess=true;
//
//                    //跳转到登录界面
//                    finish();
//                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
//                    startActivity(intent);
//                }
//            }
//            else
//            {
//                if(flag)
//                {
//                    btn_getCord.setVisibility(View.VISIBLE);
//                    Toast.makeText(getApplicationContext(),
//                            "验证码获取失败请重新获取", Toast.LENGTH_LONG).show();
//                    edit_phone.requestFocus();
//
//                }
//                else
//                {
//                    if(!registersuccess)
//                    {Toast.makeText(getApplicationContext(),
//                            "验证码输入错误", Toast.LENGTH_LONG).show();}
//                }
//            }
//
//        }
//
//    };
//
//
//
//    @SuppressLint("WrongViewCast")
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//这句直接删除刘海
//        setContentView(R.layout.register);
//        //解决刘海颜色
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
//        }
//        getId();
//
//        eventHandler = new EventHandler() {
//            public void afterEvent(int event, int result, Object data) {
//                Message msg=new Message();
//                msg.arg1=event;
//                msg.arg2=result;
//                msg.obj=data;
//                handler.sendMessage(msg);
//
//            }
//        };
//
//        SMSSDK.registerEventHandler(eventHandler);//handler:传入默认值
//
//
//
//    }
//
//
//
//
//    //按键事件
//    public void onClick(@NonNull View v)
//    {
//        if(v.getId()==R.id.btn_getcode)
//        {
//
//            //点击获取验证码后先判断手机号是否已经被注册
//            MythreadGetexist t=new MythreadGetexist(edit_phone.getText().toString().trim());
//            t.start();
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//
//            if(judPhone())//去掉左右空格获取字符串
//            {
//                SMSSDK.getVerificationCode("86",phone_number);
//                Toast.makeText(getApplicationContext(),"验证码已发送，可能速度较慢请耐心等待", Toast.LENGTH_LONG).show();
//                edit_cord.requestFocus();
//            }
//        }
//        else if (v.getId()==R.id.buttonR) {
//            if (checkBox3.isChecked()) {
//                if (rpw.getText().toString().trim().equals("") || edit_phone.getText().toString().trim().equals("") || pwo.getText().toString().trim().equals("") || user.getText().toString().trim().equals("") || role.isEmpty()) {
//                    Toast.makeText(RegisterActivity.this, "请填写所有内容", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                if (!rpw.getText().toString().trim().equals(pwo.getText().toString().trim())) {
//                    Toast.makeText(RegisterActivity.this, "两次密码输入不一致，请重新输入", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                //密码的限制：不能小于6位
//                if(pwo.getText().toString().trim().length()<6)
//                {
//                    Toast.makeText(RegisterActivity.this, "密码不能小于6位数", Toast.LENGTH_LONG).show();
//                    return;
//                }
//                //核对验证码信息
//                if(judCord())
//                    SMSSDK.submitVerificationCode("86",phone_number,cord_number);
//                flag=false;
//
//                eventHandler = new EventHandler() {
//                    public void afterEvent(int event, int result, Object data) {
//                        Message msg=new Message();
//                        msg.arg1=event;
//                        msg.arg2=result;
//                        msg.obj=data;
//                        handler.sendMessage(msg);
//
//                    }
//                };
//
//                SMSSDK.registerEventHandler(eventHandler);//handler:传入默认值
//
////                if(check)//如果验证码输入正确
////                {
////                    check=false;
////                    String res = "欢迎你，你的注册信息如下：\n" + "用户名：" + user.getText().toString().trim() + "\n密码：" + pwo.getText().toString().trim() + "\n电话：" + edit_phone.getText().toString().trim() + "\n身份" + role + "\n";
////                    Toast.makeText(RegisterActivity.this, res, Toast.LENGTH_LONG).show();
////
////                    //向服务器传送数据
////                    MythreadPost t=new MythreadPost(user.getText().toString().trim(),//用户名
////                            pwo.getText().toString().trim(),//密码
////                            edit_phone.getText().toString().trim(),//手机号（账号）
////                            role);//身份
////                    t.start();
////
////                    registersuccess=true;
////
////                    //跳转到登录界面
////                    finish();
////                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
////                    startActivity(intent);
////                }
//
//
//            } else {
//                Toast.makeText(RegisterActivity.this, "请阅读会员协议并勾选", Toast.LENGTH_LONG).show();
//            }
//
//        }
//        else if(v.getId()==R.id.textView19)
//        {
//            Intent intent = new Intent(RegisterActivity.this,AgreementActivity.class);
//            startActivity(intent);
//        }
//        else if (v.getId()==R.id.buttonB) {
//            finish();
//        }
//    }
//
//    ///////初始化///////////
//    private void getId() {
//        buttonB = findViewById(R.id.buttonB);
//        buttonB.setOnClickListener(this::onClick);
//        buttonR = findViewById(R.id.buttonR);
//        buttonR.setOnClickListener(this::onClick);
//        btn_getCord=findViewById(R.id.btn_getcode);
//        btn_getCord.setOnClickListener(this::onClick);
//        edit_phone= findViewById(R.id.phone_number);//用户手机号
//        edit_cord=findViewById(R.id.editcode);//用户输入验证码
//        pwo = findViewById(R.id.pwo);//用户密码
////        rpw = findViewById(R.id.rpw);//再次输入的用户密码
//        user = findViewById(R.id.user);//用户名
//        roleq = findViewById(R.id.roleq);//用户身份：视障人士，志愿者，亲属
//        checkBox3 = findViewById(R.id.checkBox3);//用户协议勾选框
//        agreement=findViewById(R.id.textView19);
//        agreement.setOnClickListener(this::onClick);
//        roleq.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                int radioId = radioGroup.getCheckedRadioButtonId();
//                int btn = R.id.ButtonN;
//                int btv = R.id.ButtonV;//分别取得三个端口的对应数是多少
//                int btre = R.id.ButtonRe;
//                if (radioId == btn) {
//                    role = "blind";
//                } else if (radioId == btv) {
//                    role = "volunteer";
//                } else if (radioId == btre) {
//                    role = "guardian";
//                }
//
//            }
//        });
//    }
//
//
//
//    /////////////////////手机号和验证码的判断//////////////////////////
//    private boolean judPhone()//用于判断手机号是否合理---->对于手机号重复的问题
//    {
//        //mobTech的使用权限认证
//        if(checkBox3.isChecked())
//        {     MobSDK.submitPolicyGrantResult(true);
//        }
//        else {Toast.makeText(RegisterActivity.this,
//                "获取验证码前请阅读会员协议并勾选", Toast.LENGTH_LONG).show();
//            return false;
//        }
//        if(User.isExisting())//已经存在
//        {
//            Toast.makeText(RegisterActivity.this,
//                    "该手机号已被注册，请重新输入",Toast.LENGTH_LONG).show();
//            return false;
//        }
//
//        if(TextUtils.isEmpty(edit_phone.getText().toString().trim()))
//        {
//            Toast.makeText(RegisterActivity.this,
//                    "请输入您的电话号码",Toast.LENGTH_LONG).show();
//            edit_phone.requestFocus();
//            return false;
//        }
//        else if(edit_phone.getText().toString().trim().length()!=11)
//        {
//
//            Toast.makeText(RegisterActivity.this,
//                    "您的电话号码位数不正确",Toast.LENGTH_LONG).show();
//            edit_phone.requestFocus();
//            return false;
//        }
//        else
//        {
//            phone_number=edit_phone.getText().toString().trim();
//            String num="[1][345678]\\d{9}";
//            if(phone_number.matches(num))
//                return true;
//            else
//            {
//                Toast.makeText(RegisterActivity.this,
//                        "请输入正确的手机号码",Toast.LENGTH_LONG).show();
//                return false;
//            }
//        }
//
//
//
//
//    }
//
//    private boolean judCord()
//    {
//        judPhone();
//        if(TextUtils.isEmpty(edit_cord.getText().toString().trim()))
//        {//确保用户输入了验证码
//            Toast.makeText(RegisterActivity.this,
//                    "请输入您的验证码",Toast.LENGTH_LONG).show();
//            edit_cord.requestFocus();
//            return false;
//        }
//        else if(edit_cord.getText().toString().trim().length()!=4)
//        {//确保用户输入验证码为4位数
//            Toast.makeText(RegisterActivity.this,
//                    "您的验证码位数不正确",Toast.LENGTH_LONG).show();
//            edit_cord.requestFocus();
//
//            return false;
//        }
//        else
//        {//将格式正确的验证码赋值给变量cord_number，将用于后续判断
//            cord_number=edit_cord.getText().toString().trim();
//            return true;
//        }
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        SMSSDK.unregisterEventHandler(eventHandler);
//    }
//
//}
//
//
//


package com.example.users;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.relativeclient.Mythread.MythreadGetexist;
import com.example.relativeclient.Mythread.MythreadPost;
import com.example.soniceyes.R;
import android.text.TextUtils;

public class RegisterActivity extends AppCompatActivity {
    static EditText user;
    static EditText pwo;
    EditText rpw;
    EditText edit_phone, edit_cord;
    Button buttonR, buttonB, btn_getCord;
    RadioGroup roleq;
    CheckBox checkBox3;
    static String role = "";
    String phone_number, cord_number;

    private boolean check = false;
    private boolean registersuccess = false;

    TextView agreement;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // 状态栏颜色设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.defaultblue));
        }

        getId();
    }

    // 按钮点击事件处理
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.btn_getcode) {
            // 检查手机号是否已注册
            MythreadGetexist t = new MythreadGetexist(edit_phone.getText().toString().trim(),RegisterActivity.this);
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (judPhone()) {
                // 显示默认验证码并提示用户
                Toast.makeText(this, "验证码：1234", Toast.LENGTH_LONG).show();
                edit_cord.requestFocus();
            }
        } else if (v.getId() == R.id.buttonR) {
            if (checkBox3.isChecked()) {
                // 检查所有字段是否填写
                if (validateInputs()) {
                    return;
                }

                // 验证验证码
                if (judCord()) {
                    // 验证码正确，完成注册
                    String res = "欢迎你，你的注册信息如下：\n" + "用户名：" + user.getText().toString().trim()
                            + "\n密码：" + pwo.getText().toString().trim() + "\n电话：" + edit_phone.getText().toString().trim()
                            + "\n身份" + role + "\n";
                    Toast.makeText(RegisterActivity.this, res, Toast.LENGTH_LONG).show();

                    // 提交注册信息到服务器
                    MythreadPost t = new MythreadPost(
                            user.getText().toString().trim(),
                            pwo.getText().toString().trim(),
                            edit_phone.getText().toString().trim(),
                            role,RegisterActivity.this
                    );
                    t.start();

                    registersuccess = true;

                    // 跳转到登录界面
                    finish();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                }
            } else {
                Toast.makeText(this, "请阅读并同意用户协议", Toast.LENGTH_LONG).show();
            }
        } else if (v.getId() == R.id.textView19) {
            startActivity(new Intent(this, AgreementActivity.class));
        } else if (v.getId() == R.id.buttonB) {
            finish();
        }
    }

    // 初始化视图组件
    private void getId() {
        buttonB = findViewById(R.id.buttonB);
        buttonB.setOnClickListener(this::onClick);
        buttonR = findViewById(R.id.buttonR);
        buttonR.setOnClickListener(this::onClick);
        btn_getCord = findViewById(R.id.btn_getcode);
        btn_getCord.setOnClickListener(this::onClick);
        edit_phone = findViewById(R.id.phone_number);
        edit_cord = findViewById(R.id.editcode);
        pwo = findViewById(R.id.pwo);
        user = findViewById(R.id.user);
        roleq = findViewById(R.id.roleq);
        checkBox3 = findViewById(R.id.checkBox3);
        agreement = findViewById(R.id.textView19);
        agreement.setOnClickListener(this::onClick);

        // 身份选择监听
        roleq.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.ButtonN) {
                role = "blind";
            } else if (checkedId == R.id.ButtonV) {
                role = "volunteer";
            } else if (checkedId == R.id.ButtonRe) {
                role = "guardian";
            }
        });
    }

    // 验证手机号格式和是否已注册
    private boolean judPhone() {
        if (User.isExisting()) {
            Toast.makeText(this, "该手机号已被注册", Toast.LENGTH_LONG).show();
            return false;
        }

        phone_number = edit_phone.getText().toString().trim();
        if (TextUtils.isEmpty(phone_number)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_LONG).show();
            return false;
        } else if (!phone_number.matches("[1][345678]\\d{9}")) {
            Toast.makeText(this, "手机号格式不正确", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // 验证验证码是否正确
    private boolean judCord() {
        cord_number = edit_cord.getText().toString().trim();
        if (TextUtils.isEmpty(cord_number)) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_LONG).show();
            return false;
        } else if (!cord_number.equals("1234")) {
            Toast.makeText(this, "验证码错误，请输入1234", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    // 验证输入字段
    private boolean validateInputs() {
        if (user.getText().toString().trim().isEmpty() ||
                pwo.getText().toString().trim().isEmpty() ||
                edit_phone.getText().toString().trim().isEmpty() ||
                role.isEmpty()) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_LONG).show();
            return true;
        }

        if (pwo.getText().toString().trim().length() < 6) {
            Toast.makeText(this, "密码至少需要6位", Toast.LENGTH_LONG).show();
            return true;
        }

        return false;
    }
}