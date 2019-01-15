package com.jnotes.chen.jnotes.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jnotes.chen.jnotes.view.CustomDialog;
import com.jnotes.chen.jnotes.adapter.NoteAdapter;
import com.jnotes.chen.jnotes.R;
import com.jnotes.chen.jnotes.view.SpacesItem;
import com.jnotes.chen.jnotes.view.SpinerPopWindow;
import com.jnotes.chen.jnotes.bean.Note;
import com.jnotes.chen.jnotes.bean.NoteClass;
import com.jnotes.chen.jnotes.search.NoteClassLitepal;
import com.jnotes.chen.jnotes.search.NoteLitepal;
import com.jnotes.chen.jnotes.util.CommonUtil;
import com.simple.spiderman.CrashModel;
import com.simple.spiderman.SpiderMan;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements  View.OnClickListener, PopupMenu.OnMenuItemClickListener{

    private static final String TAG = "MainActivity";
    private FloatingActionButton fab;
    public static CustomDialog customDialog;
    public static SpinerPopWindow<String> mSpinerPopWindow;
    private static List<String> grouplistName;
    // Storage Permissions
    private RecyclerView rv_list_main;
    private NoteAdapter mNoteAdapter;
    private List<Note> noteList;
    private String groupName;//分类名字
    private TextView spinner;




    private PopupWindow.OnDismissListener dismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            setTextImage(R.drawable.drop_down);
        }
    };
    /**
     * popupwindow显示的ListView的item点击事件
     */
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mSpinerPopWindow.dismiss();
            spinner.setText(grouplistName.get(position));
            changeGroup(grouplistName.get(position));
        }
    };

    /**
     * 初始化数据
     */
    public static void initData() {
        grouplistName = new ArrayList<String>();
        List<NoteClass> grouplist = NoteClassLitepal.queryGroupAll();
        for (Iterator iter = grouplist.iterator(); iter.hasNext(); ) {
            NoteClass noteGroup = (NoteClass) iter.next();
            Log.d(TAG, "分组名: " + noteGroup.getName());
            grouplistName.add(noteGroup.getName());
        }
    }

    public void changeGroup(String groupName) {
        noteList = NoteLitepal.queryNotesAll(groupName);
        refreshAdapter();
        showData();
        this.groupName = groupName;
        //showToast("切换到了" + groupName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGroup();
        initView();
//        Intent intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(this);
    }


    void initGroup() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        boolean isInit = prefs.getBoolean("isInit", false);
        if (!isInit) {
            NoteClass group = new NoteClass();
            group.setCreateTime(CommonUtil.date2string(new Date()));
            group.setUpdateTime(group.getCreateTime());
            group.setName("全部便签");
            NoteClassLitepal.createNewGroup(group);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putBoolean("isInit", true);
            editor.apply();
            groupName = "全部便签";
            Log.d(TAG, "initGroup: " + "第一次初始化添加全部便签分组");
        } else {
            Log.d(TAG, "initGroup: " + "已经存在全部便签分组");
            groupName = "全部便签";
        }
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


    //初始化界面
    private void initView() {
        changeStatusBarTextColor(true);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, NewAndEditActivity.class);
//                intent.putExtra("groupName", groupName);
//                intent.putExtra("flag", 0);//新建还是编辑，flag
//                startActivity(intent);
//            }
//        });
        fab.setOnClickListener(this);
        spinner = findViewById(R.id.group_spinner);
        setTextImage(R.drawable.drop_down);
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        final int width = wm.getDefaultDisplay().getWidth();//获取屏幕宽度
        spinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initData();
                mSpinerPopWindow = new SpinerPopWindow<String>(getApplicationContext(), grouplistName, itemClickListener);
                mSpinerPopWindow.setOnDismissListener(dismissListener);
                mSpinerPopWindow.setWidth(width);
                setTextImage(R.drawable.drop_up);
                mSpinerPopWindow.showAsDropDown(toolbar);
            }
        });
        customDialog = new CustomDialog(this);
        //弹出崩溃信息展示界面
        SpiderMan.getInstance()
                .init(this)
                //设置是否捕获异常，不弹出崩溃框
                .setEnable(true)
                //设置是否显示崩溃信息展示页面
                .showCrashMessage(true)
                //是否回调异常信息，友盟等第三方崩溃信息收集平台会用到,
                .setOnCrashListener(new SpiderMan.OnCrashListener() {
                    @Override
                    public void onCrash(Thread t, Throwable ex, CrashModel model) {
                        //CrashModel 崩溃信息记录，包含设备信息
                    }
                });

        rv_list_main = findViewById(R.id.recycler_view);

        rv_list_main.addItemDecoration(new SpacesItem(0));//设置item间距

    }

    //刷新笔记列表
    private void refreshNoteList() {
        noteList = NoteLitepal.queryNotesAll(spinner.getText().toString());
        refreshAdapter();
        showData();
    }

    private void refreshAdapter() {
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rv_list_main.setLayoutManager(layoutManager);
        mNoteAdapter = new NoteAdapter();
        mNoteAdapter.setmNotes(noteList);
        rv_list_main.setAdapter(mNoteAdapter);

        mNoteAdapter.setOnItemClickListener(new NoteAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }
        });
        mNoteAdapter.setOnItemLongClickListener(new NoteAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final Note note) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("确定删除笔记？");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret = NoteLitepal.deleteNote(note.getId());
                        if (ret > 0) {
                            refreshNoteList();
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
            }
        });
    }

    private void showData() {
        for (int i = 0; i < noteList.size(); i++) {
            Log.d(TAG, "refreshNoteList: " + "第" + i + "个");
            Log.d(TAG, "getId: " + noteList.get(i).getId());
            Log.d(TAG, "getGroupName " + noteList.get(i).getGroupName());
            Log.d(TAG, "getCreateTime " + noteList.get(i).getCreateTime());
            Log.d(TAG, "getContent: " + noteList.get(i).getContent());
            Log.d(TAG, "getType: " + noteList.get(i).getType());
            Log.d(TAG, "getPhoneInfo: " + noteList.get(i).getPhoneInfo());
            Log.d(TAG, "getNowLocation: " + noteList.get(i).getNowLocation());
            Log.d(TAG, "getNowWeather: " + noteList.get(i).getNowWeather());
        }
    }

    /**
     * 给TextView右边设置图片
     *
     * @param resId
     */
    private void setTextImage(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());// 必须设置图片大小，否则不显示
        spinner.setCompoundDrawables(null, null, drawable, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNoteList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    //创建弹出式菜单
    @Override
    public void onClick(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.mbtn_login:
                intent = new Intent(MainActivity.this, LoginActivity.class);
//                Toast.makeText(this, "登陆", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mbtn_text:
                intent = new Intent(MainActivity.this, NewAndEditActivity.class);
                intent.putExtra("groupName", groupName);
                intent.putExtra("flag", 0);
                break;
            default:
                break;
        }
        startActivity(intent);
        return false;
    }

}