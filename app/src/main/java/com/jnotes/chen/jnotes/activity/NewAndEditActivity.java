package com.jnotes.chen.jnotes.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.jnotes.chen.jnotes.MyApplication;
import com.jnotes.chen.jnotes.R;
import com.jnotes.chen.jnotes.jsonbean.JsonRootBean;
import com.jnotes.chen.jnotes.util.HttpUtil;
import com.jnotes.chen.jnotes.util.ImageUtils;
import com.jnotes.chen.jnotes.util.JsonUtil;
import com.jnotes.chen.jnotes.util.MyGlideEngine;
import com.jnotes.chen.jnotes.util.SDCardUtil;
import com.sendtion.xrichtext.RichTextEditor;
import com.jnotes.chen.jnotes.bean.Note;
import com.jnotes.chen.jnotes.util.CommonUtil;
import com.jnotes.chen.jnotes.bean.NoteClass;
import com.jnotes.chen.jnotes.search.NoteClassLitepal;
import com.jnotes.chen.jnotes.search.NoteLitepal;
import com.jnotes.chen.jnotes.util.StringUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.iwf.photopicker.PhotoPreview;
import okhttp3.Callback;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * 新建笔记
 */
public class NewAndEditActivity extends BaseActivity {
    private static final String TAG = "NewAndEditActivity";
    private static final int REQUEST_CODE_CHOOSE = 23;//定义请求码常量
    private static final int cutTitleLength = 20;//截取的标题长度
    public LocationClient mLocationClient;
//    private EditText et_new_title;
    private RichTextEditor et_new_content;
//     private TextView tv_new_group;


    private Note note;//笔记对象
    private String myGroupName;//组名
    //    private String myTitle;//标题
    private String myContent;//内容
    private String myNoteTime;//时间
    private int flag;//区分是新建笔记还是编辑笔记


    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private int screenWidth;
    private int screenHeight;
    private Subscription subsLoading;
    private Subscription subsInsert;
    private TextView /*tv_info_time,*/tv_info_group, tv_info_weather, tv_info_location, tv_info_phinfo;
    //private ImageView iv_info_time, iv_info_weather, iv_info_location, iv_info_phinfo;
    private StringBuilder currentPosition = new StringBuilder();
    // private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_and_edit);
        initView();
        initGetLocation();
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
//         bingPicImg = findViewById(R.id.new_bing_pic_img);
        changeStatusBarTextColor(true);
        Toolbar toolbar = findViewById(R.id.new_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealwithExit();
            }
        });

        FloatingActionButton fab_insert = findViewById(R.id.new_fab_insert_image);
        fab_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                closeSoftKeyInput();//关闭软键盘
//                callGallery();   //调用图片库
            }
        });
        FloatingActionButton fab_save = findViewById(R.id.new_fab_save);
        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNoteData(false);
            }
        });

        note = new Note();

        screenWidth = CommonUtil.getScreenWidth(this);
        screenHeight = CommonUtil.getScreenHeight(this);

        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);


        tv_info_group = findViewById(R.id.new_group_info_text);
        tv_info_weather = findViewById(R.id.new_weather_info_text);
        tv_info_location = findViewById(R.id.new_location_info_text);
        tv_info_phinfo = findViewById(R.id.new_phone_info_text);
        et_new_content = findViewById(R.id.et_new_content);


//         图片删除事件
        et_new_content.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {

            @Override
            public void onRtImageDelete(String imagePath) {
                if (!TextUtils.isEmpty(imagePath)) {
                    boolean isOK = SDCardUtil.deleteFile(imagePath);
                    if (isOK) {
                        // showToast("删除成功：" + imagePath);
                    }
                }
            }
        });
//         图片点击事件
        et_new_content.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(String imagePath) {
                myContent = getEditData();
                if (!TextUtils.isEmpty(myContent)) {
                    ArrayList<String> imageList = StringUtils.getTextFromHtml(myContent, true);
                    if (!TextUtils.isEmpty(imagePath)) {
                        int currentPosition = imageList.indexOf(imagePath);
                        //点击图片预览
                        PhotoPreview.builder()
                                .setPhotos(imageList)
                                .setCurrentItem(currentPosition)
                                .setShowDeleteButton(false)
                                .start(NewAndEditActivity.this);
                    }
                }
            }
        });

        openSoftKeyInput();//打开软键盘显示
        showNoteInfo();//显示info数据
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
//        String bingPic = prefs.getString("bing_pic", null);
//        if (bingPic != null) {
//            Glide.with(this).load(bingPic).into(bingPicImg);
//        } else {
//            loadBingPic();
//        }

    }

    private void initGetLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(NewAndEditActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
//        if (ContextCompat.checkSelfPermission(NewAndEditActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.READ_PHONE_STATE);
//        }
        if (ContextCompat.checkSelfPermission(NewAndEditActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(NewAndEditActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(NewAndEditActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(MyApplication.getContext(), "权限不足将无法使用！", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(MyApplication.getContext(), "权限不足将无法使用！", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void showNoteInfo() {
        Intent intent = getIntent();
        flag = intent.getIntExtra("flag", 0);//0新建，1编辑
        if (flag == 1) {//编辑
            Bundle bundle = intent.getBundleExtra("data");
            note = (Note) bundle.getSerializable("note");//获取对象


//            myTitle = note.getTitle();
            myContent = note.getContent();
            myNoteTime = note.getCreateTime();
            //NoteGroup group = NoteGroupLitepal.queryGroupById(note.getGroupId());
            //myGroupName = group.getName();
            myGroupName = note.getGroupName();

            loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage("数据加载中...");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();

            setTitle(note.getCreateTime());//显示存储的时间，点击修改完成后再赋予新值

//            tv_new_group.setText(myGroupName);//显示存储的组名
            tv_info_group.setText(myGroupName);

            tv_info_weather.setText(note.getNowWeather());//显示存储的天气

            tv_info_location.setText(note.getNowLocation());//显示存储的地址

            tv_info_phinfo.setText(note.getPhoneInfo());//显示存储的手机信息

//            et_new_title.setText(note.getTitle());//显示存储的标题
            et_new_content.post(new Runnable() {
                @Override
                public void run() {
                    //showEditData(note.getContent());
                    et_new_content.clearAllLayout();
                    showDataSync(note.getContent());//显示存储的内容
                }
            });
        } else {//新建
            String groupName = intent.getStringExtra("groupName");

            note.setType(1);//存储默认note类型

//            tv_new_group.setText(groupName);//新建，显示组名
            tv_info_group.setText(groupName);

            note.setPhoneInfo(CommonUtil.getSystemModel());//存储note手机型号

//            tv_info_phinfo.setText(note.getPhoneInfo());//新建，显示手机信息

            myNoteTime = CommonUtil.date2string(new Date());//获取当前时间

            setTitle(myNoteTime);//新建，显示当前时间
        }


    }

    /**
     * 关闭软键盘
     */
    private void closeSoftKeyInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            //imm.hideSoftInputFromInputMethod();//据说无效
            //imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0); //强制隐藏键盘
            //如果输入法在窗口上已经显示，则隐藏，反之则显示
            //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 打开软键盘
     */
    private void openSoftKeyInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (!imm.isActive()) {
            et_new_content.requestFocus();
            //第二个参数可设置为0
            //imm.showSoftInput(et_content, InputMethodManager.SHOW_FORCED);//强制显示
            imm.showSoftInputFromInputMethod(getCurrentFocus().getWindowToken(),
                    InputMethodManager.SHOW_FORCED);
        }
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
                        //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                        et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null) {
                            loadingDialog.dismiss();
                        }
//                        showToast("解析错误：图片不存在或已损坏");
                    }

                    @Override
                    public void onNext(String text) {
                        if (text.contains("<img") && text.contains("src=")) {
                            //imagePath可能是本地路径，也可能是网络地址
                            String imagePath = StringUtils.getImgSrc(text);
                            //插入空的EditText，以便在图片前后插入文字
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                            et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), imagePath);
                        } else {
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
                        }
                    }
                });
    }

    /**
     * 显示数据
     */
    protected void showEditData(Subscriber<? super String> subscriber, String html) {
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

    /**
     * 负责处理编辑数据提交等事宜，请自行实现
     */
    private String getEditData() {
        List<RichTextEditor.EditData> editList = et_new_content.buildEditData();
        StringBuffer content = new StringBuffer();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
            } else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
                note.setType(2);
            }
        }
        return content.toString();
    }

    /**
     * 保存数据,=0销毁当前界面，=1不销毁界面，为了防止在后台时保存笔记并销毁，应该只保存笔记
     */
    private void saveNoteData(boolean isBackground) {
//        String noteTitle = et_new_title.getText().toString();//获取标题
        String noteContent = getEditData();//获取内容
//        String groupName = tv_new_group.getText().toString();
        String groupName = tv_info_group.getText().toString();
        String noteTime = CommonUtil.date2string(new Date());//获取当前时间
        NoteClass group = NoteClassLitepal.queryGroupByName(groupName);
        Log.d(TAG, "saveNoteData: " + group);
        if (group != null) {
            int groupId = group.getId();
//            note.setTitle(noteTitle);//保存标题
            note.setContent(noteContent);
            note.setGroupId(groupId);
            note.setGroupName(groupName);
//        note.setBgColor("#FFFFFF");
            note.setCreateTime(noteTime);//存储当前时间
            if (flag == 0) {//新建笔记
                if (noteContent.length() == 0) {
                    if (!isBackground) {
                        Toast.makeText(NewAndEditActivity.this, "不能空白喔~", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ArrayList<String> imageList = StringUtils.getTextFromHtml(note.getContent(), true);
                    if (imageList.size() == 0) {
                        note.setType(1);
                    }
                    NoteLitepal.createNewNote(note);
                    flag = 1;//插入以后只能是编辑
                    if (!isBackground) {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            } else if (flag == 1) {//编辑笔记
                if (noteContent.equals("")) {
                    NoteLitepal.deleteNote(note.getId());
                } else if (!noteContent.equals(myContent) || !groupName.equals(myGroupName) || !noteTime.equals(myNoteTime)) {
                    ArrayList<String> imageList = StringUtils.getTextFromHtml(note.getContent(), true);
                    if (imageList.size() == 0) {
                        note.setType(1);
                    }
                    NoteLitepal.updateNote(note);
                }
                if (!isBackground) {
                    finish();
                }
            }
        }
    }

    /**
     * 调用图库选择
     */
    private void callGallery() {
//        //调用系统图库
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");// 相片类型
//        startActivityForResult(intent, 1);

        Matisse.from(this)
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))//照片视频全部显示MimeType.allOf()
                .countable(true)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(9)//最大选择数量为9
                //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片显示表格的大小
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向
                .thumbnailScale(0.85f)//缩放比例
                .theme(R.style.Matisse_Zhihu)//主题  暗色主题 R.style.Matisse_Dracula
                .imageEngine(new MyGlideEngine())//图片加载方式，Glide4需要自定义实现
                .capture(true) //是否提供拍照功能，兼容7.0系统需要下面的配置
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .captureStrategy(new CaptureStrategy(true, "com.luyucheng.lightnotes.fileprovider"))//存储到哪里
                .forResult(REQUEST_CODE_CHOOSE);//请求码
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == 1) {
                    //处理调用系统图库
                } else if (requestCode == REQUEST_CODE_CHOOSE) {
                    //异步方式插入图片
//                    insertImagesSync(data);

                }
            }
        }
    }

    /**
     * 异步方式插入图片
     *
     * @param data
     */
    private void insertImagesSync(final Intent data) {
        insertDialog.show();

        subsInsert = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    et_new_content.measure(0, 0);
                    List<Uri> mSelected = Matisse.obtainResult(data);
                    // 可以同时插入多张图片
                    for (Uri imageUri : mSelected) {
                        String imagePath = SDCardUtil.getFilePathFromUri(NewAndEditActivity.this, imageUri);
                        //Log.e(TAG, "###path=" + imagePath);
                        Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, screenWidth, screenHeight);//压缩图片
                        //bitmap = BitmapFactory.decodeFile(imagePath);
                        imagePath = SDCardUtil.saveToSdCard(bitmap);
                        //Log.e(TAG, "###imagePath="+imagePath);
                        subscriber.onNext(imagePath);
                    }

                    // 测试插入网络图片 http://p695w3yko.bkt.clouddn.com/18-5-5/44849367.jpg
                    //subscriber.onNext("http://p695w3yko.bkt.clouddn.com/18-5-5/30271511.jpg");

                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        //showToast("图片插入成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        showToast("图片插入失败:" + e.getMessage());
                    }

                    @Override
                    public void onNext(String imagePath) {
                        et_new_content.insertImage(imagePath, et_new_content.getMeasuredWidth());
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //如果APP处于后台，或者手机锁屏，则保存数据
        if (CommonUtil.isAppOnBackground(getApplicationContext()) ||
                CommonUtil.isLockScreeen(getApplicationContext())) {
            saveNoteData(true);//处于后台时保存数据
        }
    }

    /**
     * 退出处理
     */
    private void dealwithExit() {
        //  String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String groupName = tv_info_group.getText().toString();
        String noteTime = getTitle().toString();
        if (flag == 0) {//新建笔记
            if (noteContent.length() > 0) {
                saveNoteData(false);
            }
        } else if (flag == 1) {//编辑笔记
            if (noteContent.equals("")) {
                NoteLitepal.deleteNote(note.getId());
            } else if (!noteContent.equals(myContent) || !noteTime.equals(myNoteTime) || !groupName.equals(myGroupName)) {
                saveNoteData(false);
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        dealwithExit();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mLocationClient.stop();
    }

    void requestWeather(final String countyName) {
        String weatherUrl = "http://api.map.baidu.com/telematics/v3/weather?output=json&ak=???&location=" + countyName;//???处是ak
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.i(TAG, "onFailure: 失败！");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final JsonRootBean jsonRootBean = JsonUtil.handleWeatherResponse(responseText);
                Log.i(TAG, "onResponse: " + responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeatherInfo(jsonRootBean);//分析天气数据
                    }
                });
            }
        });
    }

    void showWeatherInfo(JsonRootBean jsonRootBean) {
//        note.setNowWeather(jsonRootBean.results.get(0).weather_data.get(0).date + "  " + jsonRootBean.results.get(0).weather_data.get(0).weather);
        note.setNowWeather("多云");
        tv_info_weather.setText(note.getNowWeather());        //显示天气数据
    }

    /**
     * 加载必应每日一图
     */
//    private void loadBingPic() {
//        String requestBingPic = "http://guolin.tech/api/bing_pic";
//        HttpUtil.sendOkhttpRequest(requestBingPic, new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                final String bingPic = response.body().string();
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
//                editor.putString("bing_pic", bingPic);
//                editor.apply();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Glide.with(NewAndEditActivity.this).load(bingPic).into(bingPicImg);
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            if (flag == 1) {//编辑
                tv_info_location.setText(note.getNowLocation());//显示存储的位置
                tv_info_weather.setText(note.getNowWeather());//显示存储的天气

            } else {
                currentPosition = new StringBuilder();
                currentPosition.append(location.getStreet());
                Log.d(TAG, "onReceiveLocation: " + currentPosition);
                note.setNowLocation(currentPosition.toString());
                tv_info_location.setText(note.getNowLocation());//显示查询到位置
                requestWeather(location.getDistrict());//请求天气，并显示
            }
        }
    }
}