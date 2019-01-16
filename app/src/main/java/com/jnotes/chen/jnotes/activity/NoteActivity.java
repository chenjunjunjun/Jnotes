package com.jnotes.chen.jnotes.activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jnotes.chen.jnotes.R;
import com.jnotes.chen.jnotes.bean.Note;
import com.jnotes.chen.jnotes.util.CommonUtil;
import com.jnotes.chen.jnotes.util.StringUtils;
import com.sendtion.xrichtext.RichTextView;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPreview;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class NoteActivity extends AppCompatActivity {
    private static final String TAG = "NoteActivity";

    private RichTextView tv_note_content;//笔记内容
    private TextView tv_note_title, tv_info_group, tv_info_weather, tv_info_location, tv_info_phinfo;
    //private ScrollView scroll_view;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;
    private ProgressDialog loadingDialog;
    private String myGroupName;
    private Subscription subsLoading;
//    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        initView();

    }

    private void changeStatusBarTextColor(boolean isBlack) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (isBlack) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//设置状态栏黑色字体
            } else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);//恢复状态栏白色字体
            }
        }
    }

    private void initView() {
//        bingPicImg = findViewById(R.id.note_bing_pic_img);
        changeStatusBarTextColor(true);
        Toolbar toolbar = findViewById(R.id.note_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FloatingActionButton fab_share = findViewById(R.id.note_fab_share);
        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtil.shareTextAndImage(NoteActivity.this, note.getContent(), null);//分享图文
            }
        });
        FloatingActionButton fab_edit = findViewById(R.id.note_fab_edit);
        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoteActivity.this, NewAndEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                intent.putExtra("flag", 1);//编辑笔记
                startActivity(intent);
                finish();
            }
        });

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
        tv_note_title = findViewById(R.id.tv_note_title);//标题
        tv_note_content = findViewById(R.id.tv_note_content);//内容

        // 图片点击事件
        tv_note_content.setOnRtImageClickListener(new RichTextView.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(String imagePath) {
                ArrayList<String> imageList = StringUtils.getTextFromHtml(myContent, true);
                int currentPosition = imageList.indexOf(imagePath);
                //showToast("点击图片：" + currentPosition + "：" + imagePath);

                //点击图片预览
                PhotoPreview.builder()
                        .setPhotos(imageList)
                        .setCurrentItem(currentPosition)
                        .setShowDeleteButton(false)
                        .start(NoteActivity.this);
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");

        myTitle = note.getTitle();
        myContent = note.getContent();
        tv_note_title.setText(myTitle);

        tv_note_content.post(new Runnable() {
            @Override
            public void run() {
                //showEditData(myContent);
                tv_note_content.clearAllLayout();
                showDataSync(myContent);
            }
        });
        tv_info_group = findViewById(R.id.note_group_info_text);
        tv_info_weather = findViewById(R.id.note_weather_info_text);
        tv_info_location = findViewById(R.id.note_location_info_text);
        tv_info_phinfo = findViewById(R.id.note_phone_info_text);


        myGroupName = note.getGroupName();

        setTitle(note.getCreateTime());

        tv_info_group.setText(myGroupName);

        tv_info_weather.setText(note.getNowWeather());

        tv_info_location.setText(note.getNowLocation());

        tv_info_phinfo.setText(note.getPhoneInfo());

    }

    /**
     * 异步方式显示数据
     *
     * @param html
     */
    private void showDataSync(final String html) {

        subsLoading = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                showEditData(subscriber, html);
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        if (loadingDialog != null) {
                            loadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null) {
                            loadingDialog.dismiss();
                        }
                        //showToast("解析错误：图片不存在或已损坏");
                        Log.e(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onNext(String text) {
                        if (text.contains("<img") && text.contains("src=")) {
                            //imagePath可能是本地路径，也可能是网络地址
                            String imagePath = StringUtils.getImgSrc(text);
                            tv_note_content.addImageViewAtIndex(tv_note_content.getLastIndex(), imagePath);
                        } else {
                            tv_note_content.addTextViewAtIndex(tv_note_content.getLastIndex(), text);
                        }
                    }
                });

    }

    /**
     * 显示数据
     *
     * @param html
     */
    private void showEditData(Subscriber<? super String> subscriber, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                subscriber.onNext(text);
            }
            subscriber.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            subscriber.onError(e);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}