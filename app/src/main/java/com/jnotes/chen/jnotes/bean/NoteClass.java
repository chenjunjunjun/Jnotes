package com.jnotes.chen.jnotes.bean;


import org.litepal.crud.DataSupport;

import java.io.Serializable;

public class NoteClass extends DataSupport implements Serializable{
    private int id;//ID
    private String name;//分组名称
    // private int order;//排列顺序
    //    private String color;//分类颜色，存储颜色代码
//    private int isEncrypt;//是否加密，0未加密，1加密
    private String createTime;//创建时间
    private String updateTime;//修改时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }


}
