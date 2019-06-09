package ru.chenk.autoparts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.List;

public class PartsEditAdapter extends RecyclerView.Adapter<PartsEditAdapter.PartsEditViewHolder> {
    private List<Part> dataset;
    private Context context;
    private RecyclerView recyclerView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static class PartsEditViewHolder extends RecyclerView.ViewHolder{
        public TextView partName, partPrice, partModel, partOrders, partCount;
        public ImageView partImage;
        public Button editButton, deleteButton;
        public FirebaseFirestore db = FirebaseFirestore.getInstance();

        public PartsEditViewHolder(@NonNull View itemView) {
            super(itemView);
            partName = itemView.findViewById(R.id.PEIV_name);
            partPrice = itemView.findViewById(R.id.PEIV_price);
            partModel = itemView.findViewById(R.id.PEIV_model);
            partOrders = itemView.findViewById(R.id.PEIV_orders);
            partCount = itemView.findViewById(R.id.PEIV_count);
            partImage = itemView.findViewById(R.id.PEIV_imageView);
            deleteButton = itemView.findViewById(R.id.PEIV_deleteButton);
        }
    }

    public PartsEditAdapter(List<Part> dataset, Context context, RecyclerView recyclerView){
        this.dataset = dataset;
        this.context = context;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public PartsEditAdapter.PartsEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.parts_edit_item_view, parent, false);
        return new PartsEditViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PartsEditAdapter.PartsEditViewHolder holder, final int position) {
        if(dataset.get(position)!=null){
            final Part part = dataset.get(position);
            holder.partName.setText(part.getName());
            holder.partPrice.setText(part.getPrice() + " руб.");
            holder.partModel.setText(part.getModel());
            holder.partCount.setText("Осталось: "+part.getCount());
            holder.partOrders.setText("Заказов: "+part.getOrders());
            Glide.with(context)
                    .load(part.getImageSrc())
                    .into(holder.partImage);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    db.collection("parts").document(part.getUid()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Snackbar.make(v, "Запчасть удалена", Snackbar.LENGTH_SHORT).show();
                                dataset.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, getItemCount());
                                notifyDataSetChanged();
                            }else{
                                Snackbar.make(v, "Запчасть не удалось удалить", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}