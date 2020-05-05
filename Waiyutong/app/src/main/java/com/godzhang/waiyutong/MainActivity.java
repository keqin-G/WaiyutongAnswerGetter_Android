package com.godzhang.waiyutong;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.*;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.godzhang.waiyutong.Waiyutong;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText paramEditText;
    private Button loginButton;
    private Waiyutong w;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            Bundle data = message.getData();
            String val = data.getString("answer");
            new AlertDialog.Builder(MainActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("注意").setMessage(val).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            }).create().show();
            loginButton.setEnabled(true);
        }
    };
    Runnable runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void run() {
            Looper.prepare();
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String param = paramEditText.getText().toString();
            if (username.isEmpty() || password.isEmpty())
            {
                new AlertDialog.Builder(MainActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("注意").setMessage("用户名或密码不能为空").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).create().show();
                return;
            }

            w = new Waiyutong();
            boolean ok = w.Login(username, password);
            if (!ok)
            {
                new AlertDialog.Builder(MainActivity.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("注意").setMessage("登陆失败!请检查你的账户和网络设置").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).create().show();
                return;
            }

            boolean a;
            if (param.isEmpty())
            {
                //Log.d("WAIYUTONG_DEBUG", "GETLASTESTSAAA");
                a = w.GetLastestHomework();
            }
            else if(param.indexOf('-') > 0)
            {
                a = w.GetHomeworkByDate(param);
            }
            else
            {
                a = w.GetHomeworkByHid(Integer.valueOf(param).intValue());
            }
            if (a)
            {
                //Log.d("WAIYUTONG_DEBUG", "GETHOMEWORKSUCCEED");
                List<List<String>> answer = w.GetAnswer();
                StringBuilder answerStr = new StringBuilder("");
                for (List<String> list : answer)
                {
                    for (String str : list)
                    {
                        if (str.length() == 1)
                        {
                            answerStr.append(str);
                        }
                        else
                        {
                            answerStr.append(str).append('\n');
                        }
                    }
                    answerStr.append("\n");
                }
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("answer", answerStr.toString());
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
            Looper.loop();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = (EditText) findViewById(R.id.usenameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        paramEditText    = (EditText) findViewById(R.id.paramEditText);
        loginButton      = (Button)   findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            if (v.getId() == R.id.loginButton)
            {
                loginButton.setEnabled(false);
                new Thread(runnable).start();
            }

        });
    }
}
