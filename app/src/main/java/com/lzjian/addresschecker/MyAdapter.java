package com.lzjian.addresschecker;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * @Description:
 */

public class MyAdapter extends BaseAdapter {

    private Context context;
    private List<String> data;
    private int itemLayoutId;

    private String selectTxt;

    public MyAdapter(Context context, List<String> data) {
        this.context = context;
        this.data = data;
        this.itemLayoutId = R.layout.item;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            convertView = View.inflate(context, itemLayoutId, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        String item = data.get(position);
        holder.tv_item.setText(item);
        if((!TextUtils.isEmpty(selectTxt)) && selectTxt.equals(item)){
            holder.iv_selected.setVisibility(View.VISIBLE);
            holder.tv_item.setTextColor(context.getResources().getColor(R.color.blue5d9ff5));
        }else {
            holder.iv_selected.setVisibility(View.GONE);
            holder.tv_item.setTextColor(context.getResources().getColor(R.color.gray979da5));
        }
        return convertView;
    }

    private class ViewHolder{

        private TextView tv_item;
        private ImageView iv_selected;

        ViewHolder(View view) {
            tv_item = (TextView) view.findViewById(R.id.tv_item);
            iv_selected = (ImageView) view.findViewById(R.id.iv_selected);
        }
    }
}
