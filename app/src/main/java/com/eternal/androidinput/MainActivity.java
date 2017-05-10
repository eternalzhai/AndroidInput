package com.eternal.androidinput;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.eternal.widget.input.Input;

public class MainActivity extends AppCompatActivity {

    private Input in_user;
    private Input in_pwd;
    private Button btn_verify;

    private boolean isUserNotEmpty = false, isPwdNotEmpty = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {
        in_user = (Input) findViewById(R.id.in_user);
        in_pwd = (Input) findViewById(R.id.in_pwd);
        btn_verify = (Button) findViewById(R.id.btn_verify);


        in_user.setOnEmptyChanged(new Input.OnEmptyChanged() {
            @Override
            public void changed(boolean notEmpty) {
                isUserNotEmpty = notEmpty;
                changeBtn();
            }
        });
        in_pwd.setOnEmptyChanged(new Input.OnEmptyChanged() {
            @Override
            public void changed(boolean notEmpty) {
                isPwdNotEmpty = notEmpty;
                changeBtn();
            }
        });

        btn_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (in_user.isInputError())
                    return;

                if (in_pwd.isInputError())
                    return;

                String user = in_user.getInput().toString().trim();
                String pwd = in_pwd.getInput().toString().trim();


//                login(user, pwd);

            }
        });

    }

    private void changeBtn() {
        if (isUserNotEmpty && isPwdNotEmpty) {
            btn_verify.setEnabled(true);
        } else {
            btn_verify.setEnabled(false);
        }
    }

}
