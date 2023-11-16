package com.phuoc.chatapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.hbb20.CountryCodePicker;

public class LoginPhoneNumberActivity extends AppCompatActivity {
    CountryCodePicker countryCodePicker;
    EditText phoneInput;
    Button sendOtpBtn;
//    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone_number);

        countryCodePicker = findViewById(R.id.login_countrycode);
        phoneInput = findViewById(R.id.login_mobile_number);
        sendOtpBtn = findViewById(R.id.send_otp_btn);
//        progressBar = findViewById(R.id.login_progress_bar);

        countryCodePicker.registerCarrierNumberEditText(phoneInput);
        sendOtpBtn.setOnClickListener(v -> {
            if (!countryCodePicker.isValidFullNumber()) {
                phoneInput.setError("Phone number not valid");
                return;
            }
            Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOtpActivity.class);
            intent.putExtra("phone", countryCodePicker.getFullNumberWithPlus());
            startActivity(intent);
        });

//        countryCodePicker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Khởi tạo dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(LoginPhoneNumberActivity.this);
//                // Đặt Content View của dialog là CountryCodePicker
//                builder.setView(countryCodePicker);
//                AlertDialog dialog = builder.create();
//                dialog.setContentView(countryCodePicker);
//                // Lấy đối tượng Window của dialog
//                Window window = dialog.getWindow();
//                if (window != null) {
//                    // Đặt màu nền của window
//                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//                    // Đặt màu mờ (dim) của window
//                    window.setDimAmount(0.7f);
//                }
//                // Hiển thị dialog
//                dialog.show();
//            }
//        });
    }
}