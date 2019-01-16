package com.jnotes.chen.jnotes.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jnotes.chen.jnotes.R;
import com.jnotes.chen.jnotes.bean.Note;
import com.jnotes.chen.jnotes.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = "NoteAdapter";
    private Context mContext;
    private List<Note> mNotes;
    private OnRecyclerViewItemClickListener mOnItemClickListener;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener;

    public NoteAdapter() {
        mNotes = new ArrayList<>();
    }

    public void setmNotes(List<Note> notes) {
        this.mNotes = notes;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (Note) v.getTag());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemLongClickListener.onItemLongClick(v, (Note) v.getTag());
        }
        return true;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.i(TAG, "###onCreateViewHolder: ");
        //inflate(R.layout.list_item_record,parent,false) 如果不这么写，cardview不能适应宽度
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "位置: " + position);
        Note note = mNotes.get(position);
        holder.itemView.setTag(note);
        if (note.getType() == 1) {
//            holder.tv_list_type.setText("文字便签");
            holder.tv_list_summary.setText(note.getContent());
        } else {
            ArrayList<String> imageList = StringUtils.getTextFromHtml(note.getContent(), true);
            String content = note.getContent().substring(0, note.getContent().indexOf("<img src="));
            if (content.equals("")) {
                holder.tv_list_summary.setText("空白的喔~");
            } else
                holder.tv_list_summary.setText(content);
            holder.iv_list_image.setImageURI(Uri.parse(imageList.get(0)));
//            holder.tv_list_type.setText("图片便签");
        }
        holder.tv_list_title.setText(note.getTitle());
        holder.tv_list_time.setText(note.getCreateTime());
        holder.tv_list_group.setText(note.getGroupName());
    }

    @Override
    public int getItemCount() {
        //Log.i(TAG, "###getItemCount: ");
        if (mNotes != null && mNotes.size() > 0) {
            return mNotes.size();
        }
        return 0;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Note note);
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, Note note);
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_list_image;//图片预览
        public TextView tv_list_summary;//笔记摘要
//        public TextView tv_list_type;//创建类型
        public TextView tv_list_time;//创建时间
        public TextView tv_list_group;//笔记分类
        public CardView card_view_note;//笔记分组
        public TextView tv_list_title;//笔记标题

        public ViewHolder(View view) {
            super(view);
            card_view_note = view.findViewById(R.id.card_view_note);
            iv_list_image = view.findViewById(R.id.iv_list_image);
            tv_list_summary = view.findViewById(R.id.tv_list_summary);
//            tv_list_type = view.findViewById(R.id.tv_list_type);
            tv_list_time = view.findViewById(R.id.tv_list_time);
            tv_list_group = view.findViewById(R.id.tv_list_group);
            tv_list_title = view.findViewById(R.id.tv_list_title);
        }
    }
}