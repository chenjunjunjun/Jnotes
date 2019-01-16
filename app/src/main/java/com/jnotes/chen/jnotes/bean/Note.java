package com.jnotes.chen.jnotes.bean;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

public class Note extends DataSupport implements Serializable {

    private int id;//笔记ID
    private String title;//笔记标题
    private String content;//笔记内容
    private int groupId;//分类ID
    private String groupName;//分类名称
    private int type;//笔记类型，1纯文本，2Html，3Markdown
    //    private String bgColor;//背景颜色，存储颜色代码
    //    private int isEncrypt;//是否加密，0未加密，1加密
    private String createTime;//创建时间
    //    private String updateTime;//修改时间
    private String nowWeather;//天气
    private String nowLocation;//地点
    private String phoneInfo;//手机信息

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

//    public String getBgColor() {
//        return bgColor;
//    }

//    public void setBgColor(String bgColor) {
//        this.bgColor = bgColor;
//    }


    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getNowWeather() {
        return nowWeather;
    }

    public void setNowWeather(String nowWeather) {
        this.nowWeather = nowWeather;
    }

    public String getNowLocation() {
        return nowLocation;
    }

    public void setNowLocation(String nowLocation) {
        this.nowLocation = nowLocation;
    }

    public String getPhoneInfo() {
        return phoneInfo;
    }

    public void setPhoneInfo(String phoneInfo) {
        this.phoneInfo = phoneInfo;
    }
}