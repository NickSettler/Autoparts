package ru.chenk.autoparts;

import android.graphics.BitmapFactory;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {
    private List<CartItem> dataset;
    private CartController cartController;

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder{
        public TextView itemName;
        public TextView itemCount;
        public FirebaseFirestore db = FirebaseFirestore.getInstance();

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.OI_name);
            itemCount = itemView.findViewById(R.id.OI_count);
        }
    }

    public OrderItemsAdapter(CartController cartController){
        this.cartController = cartController;
        this.dataset = cartController.getCart();
    }

    @NonNull
    @Override
    public OrderItemsAdapter.OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_view, parent, false);
        return new OrderItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderItemsAdapter.OrderItemViewHolder holder, final int position) {
        if(dataset.get(position)!=null){
            holder.itemCount.setText("x"+dataset.get(position).getCount());
            holder.db.collection("parts").document(dataset.get(position).getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot doc = task.getResult();
                        if(doc.get("name")!=null){
                            holder.itemName.setText(String.valueOf(doc.get("name")));
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
