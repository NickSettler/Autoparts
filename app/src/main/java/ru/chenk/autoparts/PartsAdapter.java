package ru.chenk.autoparts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class PartsAdapter extends RecyclerView.Adapter<PartsAdapter.ViewHolder> {
    private List<Part> dataset;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView descTextView;
        public ImageView image;
        public TextView priceTextView;
        public Button moreButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.PI_textView);
            descTextView = itemView.findViewById(R.id.PI_desc);
            image = itemView.findViewById(R.id.PI_image);
            priceTextView = itemView.findViewById(R.id.PI_price);
            moreButton = itemView.findViewById(R.id.PI_moreButton);
        }
    }

    public PartsAdapter(List<Part> myDataset, Context mContext) {
        dataset = myDataset;
        context = mContext;
    }

    @NonNull
    @Override
    public PartsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.part_item_view_2, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final PartsAdapter.ViewHolder holder, final int position) {
        if (dataset.get(position) != null) {
            Log.d("SPEC_CHECK", dataset.get(position).getSpecs().toString());
            holder.moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent partActivity = new Intent(context, PartActivity.class);
                    partActivity.putExtra("part", dataset.get(position));
                    context.startActivity(partActivity);
                }
            });
            if (dataset.get(position).imageSrc == null) {
                holder.image.setImageDrawable(null);
            } else {
                if (dataset.get(position).image != null) {
                    Bitmap image = BitmapFactory.decodeByteArray(dataset.get(position).image, 0, dataset.get(position).image.length);
                    holder.image.setImageBitmap(image);
                } else {
                    StorageReference ref = FirebaseStorage.getInstance().getReference().child(dataset.get(position).imageSrc);
                    ref.getBytes(Long.MAX_VALUE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                        @Override
                        public void onComplete(@NonNull Task<byte[]> task) {
                            if (task.isSuccessful()) {
                                Bitmap image = BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length);
                                dataset.get(position).image = task.getResult();
                                holder.image.setImageBitmap(image);
                            }
                        }
                    });
                }

            }
            Log.d("ADAPTER", dataset.get(position).name);
            holder.descTextView.setText(dataset.get(position).getUid());
            holder.nameTextView.setText(dataset.get(position).getName());
            holder.priceTextView.setText(String.format(context.getString(R.string.PA_partPriceTextView), String.valueOf(dataset.get(position).getPrice())));

        }
    }

    public void clear() {
        dataset = new ArrayList<Part>();
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
