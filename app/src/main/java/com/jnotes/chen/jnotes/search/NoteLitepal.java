package com.jnotes.chen.jnotes.search;

import com.jnotes.chen.jnotes.bean.Note;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class NoteLitepal {

    /**
     * 新建便签
     */
    public static void createNewNote(Note note) {
        Note newNote = note;
        newNote.save();
    }

    /**
     * 查询所有笔记
     */
    public static List<Note> queryNotesAll(int groupId) {
        List<Note> noteList = new ArrayList<Note>();
        if (groupId > 0) {
            noteList = DataSupport.where("groupId = ?", groupId + "").order("desc").find(Note.class);
        } else
            noteList = DataSupport.findAll(Note.class);
        return noteList;
    }

    public static List<Note> queryNotesAll(String groupName) {
        List<Note> noteList = new ArrayList<Note>();
        if (groupName.equals("全部便签")) {
            noteList = DataSupport.findAll(Note.class);
        } else {
            noteList = DataSupport.where("groupName = ?", groupName).find(Note.class);
        }
        return noteList;
    }


//    public static List<Note> queryNoteAll(String content) {
//        List<Note> noteList = new ArrayList<Note>();
//        noteList = DataSupport.where("title like ？","%" +content+ "%").find(Note.class);
//        return noteList;
//    }

    public static List<Note> queryNotesAll() {
        List<Note> noteList = new ArrayList<Note>();
        noteList = DataSupport.order("desc").find(Note.class);
        return noteList;
    }


    /**
     * 更新笔记
     *
     * @param note
     */
    public static void updateNote(Note note) {
        Note update_note = note;
        update_note.updateAll("id = ?", note.getId() + "");

    }

    /**
     * 删除笔记
     */
    public static int deleteNote(int noteId) {
        int ret = 1;
        try {
            DataSupport.deleteAll(Note.class, "id = ?", noteId + "");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return ret;
    }

    /**
     * 批量删除笔记
     *
     * @param mNotes
     */
    public static void deleteNote(List<Note> mNotes) {

    }
}
