package ru.chenk.autoparts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PartsAdapter extends RecyclerView.Adapter<PartsAdapter.ViewHolder> {
    private List<Part> dataset;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextView descTextView;
        public ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.PI_textView);
            descTextView = itemView.findViewById(R.id.PI_desc);
            image = itemView.findViewById(R.id.PI_image);
        }
    }

    public PartsAdapter(List<Part> myDataset, Context mContext) {
        dataset = myDataset;
        context = mContext;
    }

    @NonNull
    @Override
    public PartsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.part_item_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final PartsAdapter.ViewHolder holder, final int position) {
        if(dataset.get(position)!=null){
            if(dataset.get(position).image==null){
                holder.image.setImageDrawable(null);
            }else{
                StorageReference ref = FirebaseStorage.getInstance().getReference().child(dataset.get(position).image);
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(holder.image);
                    }
                });
            }
            Log.d("ADAPTER", dataset.get(position).name);
            holder.descTextView.setText(dataset.get(position).uid);
            holder.textView.setText(dataset.get(position).name);
        }
    }

    public void clear(){
        dataset = new ArrayList<Part>();
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
