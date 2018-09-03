package com.example.kennck.mirroring.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.kennck.mirroring.R;
import com.example.kennck.mirroring.objects.Video;

import java.util.List;

public class VideoAdapter extends ArrayAdapter<Video> {

    private Context mContext;
    private List<Video> mVideos;

    public VideoAdapter(@NonNull Context context, @NonNull List<Video> objects) {
        super(context, R.layout.content_text_view, objects);
        mContext = context;
        mVideos = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.content_text_view, null);
            holder = new ViewHolder();
            holder.filename = (TextView) convertView.findViewById(R.id.videoFilename);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        /***get clicked view and play video url at this position**/
        try {
            Video video = mVideos.get(position);
            //play video using android api, when video view is clicked.
            holder.filename.setText(video.getFilename());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    public static class ViewHolder {
        TextView filename;
    }
}
