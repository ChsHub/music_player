package com.example.music_player;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrackListAdapter extends RecyclerView.Adapter
{
    List<TrackItem> trackItemList;
    public TrackListAdapter(List<TrackItem> trackList)
    {
        super(); // TODO necessary?
        trackItemList = trackList;
    }

    class TrackListViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView textView1;
        TextView textView2;
        // Holds references to single TrackItem views
        public TrackListViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imageView = itemView.findViewById(R.id.track_image_view);
            textView1 = itemView.findViewById(R.id.track_text_view_1);
            textView2 = itemView.findViewById(R.id.track_text_view_2);
        }
    }

    @NonNull
    @Override
    /**
     * Called for each new ViewHolder
     * @param parent
     * @param viewType Used for different types of ViewHolders
     */
    public TrackListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.track_item, parent, false);
        return new TrackListViewHolder(itemView);
    }

    /**
     * Called whenever ViewHolder comes into view, or on ViewHolder update.
     * Fill ViewHolder with data from the data-set trackItemList.
     *
     * @param holder   Recycled ViewHolder
     * @param position List position
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        TrackItem currentItem = trackItemList.get(position);
        // Use cached views in TrackListViewHolder, to avoid findById calls
        ((TrackListViewHolder) holder).imageView.setImageResource(currentItem.imageResource);
        ((TrackListViewHolder) holder).textView1.setText(currentItem.text1);
        ((TrackListViewHolder) holder).textView2.setText(currentItem.text2);
    }

    @Override
    public int getItemCount()
    {
        return trackItemList.size();
    }
}
