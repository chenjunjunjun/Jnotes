package com.jnotes.chen.jnotes.search;

import com.jnotes.chen.jnotes.bean.NoteClass;

import org.litepal.crud.DataSupport;

import java.util.List;


public class NoteClassLitepal {


    /**
     * 添加一个分类
     */
    public static void createNewGroup(NoteClass group) {
        NoteClass newGroup = group;
        newGroup.save();
    }

    /**
     * 查询所有分类列表
     *
     * @return
     */
    public static List<NoteClass> queryGroupAll() {

        List<NoteClass> groupList = DataSupport.findAll(NoteClass.class);
        return groupList;
    }

    /**
     * 根据分类名查询分类
     *
     * @param groupName
     * @return
     */
    public static NoteClass queryGroupByName(String groupName) {

        NoteClass group = DataSupport.where("name = ?", groupName).findFirst(NoteClass.class);
        return group;
    }

    /**
     * 根据分类ID查询分类
     *
     * @return
     */
    public static NoteClass queryGroupById(int groupId) {
        NoteClass group = DataSupport.where("id = ?", groupId + "").findFirst(NoteClass.class);
        return group;
    }

    /**
     * 更新一个分类
     */
    public static void updateGroup(NoteClass group) {
        NoteClass update_group = group;
        update_group.updateAll("id = ?", group.getId() + "");
    }

    /**
     * 删除一个分类
     */
    public static int deleteGroup(int groupId) {
        int ret = 1;
        try {
            DataSupport.deleteAll(NoteClass.class, "id = ?", groupId + "");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return ret;
    }
}
