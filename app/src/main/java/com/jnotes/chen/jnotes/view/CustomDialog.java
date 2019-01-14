package com.jnotes.chen.jnotes.view;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.jnotes.chen.jnotes.R;
import com.jnotes.chen.jnotes.bean.NoteClass;
import com.jnotes.chen.jnotes.search.NoteClassLitepal;
import com.jnotes.chen.jnotes.util.CommonUtil;

import java.util.Date;

public class CustomDialog extends AlertDialog implements View.OnClickListener {
    Context mContext;
    private EditText editText;
    private Button mBtnCancel, mBtnConfirm;

    public CustomDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group);
        editText = findViewById(R.id.edit_new_group);
        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnConfirm = findViewById(R.id.btn_confirm);
        mBtnCancel.setOnClickListener(this);
        mBtnConfirm.setOnClickListener(this);
        Window window = this.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));// 有白色背景，加这句代码
        //保证EditText能弹出键盘
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        this.setCancelable(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                this.dismiss();
                break;
            case R.id.btn_confirm:
                if (TextUtils.isEmpty(editText.getText())) {
                    Toast.makeText(mContext, "不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, editText.getText().toString(), Toast.LENGTH_SHORT).show();
                    NoteClass group = new NoteClass();
                    group.setCreateTime(CommonUtil.date2string(new Date()));
                    group.setUpdateTime(group.getCreateTime());
                    group.setName(editText.getText().toString());
                    NoteClassLitepal.createNewGroup(group);
                    this.dismiss();
                }
                break;
            default:
                break;
        }
    }
}
