package ru.chenk.autoparts;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> dataset;
    private Context context;
    private CartController cartController;

    public static class CartViewHolder extends RecyclerView.ViewHolder{
        public ImageView itemImageView;
        public TextView itemName;
        public TextView itemCount;
        public Button minusButton, plusButton, deleteButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.CI_image);
            itemName = itemView.findViewById(R.id.CI_name);
            itemCount = itemView.findViewById(R.id.CI_count);
            minusButton = itemView.findViewById(R.id.CI_minusButton);
            plusButton = itemView.findViewById(R.id.CI_plusButton);
            deleteButton = itemView.findViewById(R.id.CI_deleteButton);
        }
    }

    public CartAdapter(Context context, CartController cartController){
        this.context = context;
        this.cartController = cartController;
        this.dataset = cartController.getCart();
    }

    @NonNull
    @Override
    public CartAdapter.CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_view, parent, false);
        CartAdapter.CartViewHolder vh = new CartAdapter.CartViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final CartViewHolder holder, final int position) {
        if(dataset.get(position)!=null){
            holder.itemCount.setText("x"+ dataset.get(position).getCount());
            holder.plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(dataset.get(position).getCount() < dataset.get(position).getMaxCount()){
                        dataset.get(position).inc();
                        cartController.increaseItem(dataset.get(position).getUid());
                        notifyDataSetChanged();
                    }else{
                        Snackbar.make(v, "Максимум для одного заказа", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
            holder.minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(dataset.get(position).getCount() > 0){
                        cartController.decreaseItem(dataset.get(position).getUid());
                        dataset.get(position).dec();
                        notifyDataSetChanged();
                    }
                }
            });
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartController.removeItem(dataset.get(position).getUid());
                    dataset.remove(position);
                    notifyDataSetChanged();
                }
            });
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("parts").document(dataset.get(position).getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot data = task.getResult();
                                if(data.get("name")!=null){
                                    holder.itemName.setText(data.get("name").toString());
                                }
                                if(data.get("image")!=null){
                                    Glide.with(context)
                                            .load(String.valueOf(data.get("image")))
                                            .into(holder.itemImageView);
                                }
                            }
                        }
                    });

        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}