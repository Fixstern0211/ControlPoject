package com.octopus.controlproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.smailnet.emailkit.EmailKit;

public class LoginActivity extends AppCompatActivity {

    private Button mBtnLogin;
    private EditText mEtUser, mEtPassword;
    private TextView mTvChang;

    private int counter = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEtUser = findViewById(R.id.et_user);
        mEtPassword = findViewById(R.id.et_password);
        mBtnLogin = findViewById(R.id.btn_login);
        mTvChang  = findViewById(R.id.tv_change_mode);

        // chang mode of password (visible and invisible)
        mTvChang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEtPassword.getInputType() == 128){ //if visible
                    mEtPassword.setInputType(129);//Set to hide password
                    mTvChang.setText("invisible");
                }else {
                    mEtPassword.setInputType(128);//Set to password visible
                    mTvChang.setText("visible");
                }
                mEtPassword.setSelection(mEtPassword.getText().length());//Set the cursor position to the end
            }
        });


        //Authenticate email and login
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String inputUser = mEtUser.getText().toString();
                String inputPassword = mEtPassword.getText().toString();

                EmailKit.Config config = new EmailKit.Config()
                        .setSMTP("smtp.gmail.com", 465, true)  //set host、port and ssl of SMTP
                        .setIMAP("imap.gmail.com", 993, true)  //set host、port and ssl of IMAP
                        .setAccount(inputUser)          //from which E-mail Address
                        .setPassword(inputPassword);    //The App-Password of the sender's E-mail


                if (inputUser.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter all the details correctly", Toast.LENGTH_SHORT).show();
                } else {

                    EmailKit.auth(config, new EmailKit.GetAuthCallback() {
                        @Override
                        public void onSuccess() {
                            Log.i("TAG", "properties successfully!");
                            Toast.makeText(LoginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                            //new a Bundle
                            Bundle bundle=new Bundle();
                            bundle.putString("user", inputUser);//put key-values in bundle
                            bundle.putString("password",inputPassword);

                            //new a Intent，put Bundle/data in the Intent
                            intent.putExtras(bundle);
                            startActivity(intent);

                        }

                        @Override
                        public void onFailure(String errMsg) {
                            Log.i("TAG", errMsg);
                            counter--;
                            Toast.makeText(LoginActivity.this, "Incorrect entered", Toast.LENGTH_SHORT).show();

                            if (counter == 0) {
                                mBtnLogin.setEnabled(false);
                            }
                        }
                    });

                }
            }
        });

    }
}