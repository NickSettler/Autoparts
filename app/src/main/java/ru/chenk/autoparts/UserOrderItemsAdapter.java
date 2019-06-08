package ru.chenk.autoparts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserOrderItemsAdapter extends RecyclerView.Adapter<UserOrderItemsAdapter.UserOrderItemViewHolder> {
    private List<UserOrderItem> dataset;

    public static class UserOrderItemViewHolder extends RecyclerView.ViewHolder{
        public TextView itemName;
        public TextView itemCount;
        public FirebaseFirestore db = FirebaseFirestore.getInstance();

        public UserOrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.OI_name);
            itemCount = itemView.findViewById(R.id.OI_count);
        }
    }

    public UserOrderItemsAdapter(ArrayList<UserOrderItem> dataset){
        this.dataset = dataset;
    }

    @NonNull
    @Override
    public UserOrderItemsAdapter.UserOrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_view, parent, false);
        return new UserOrderItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserOrderItemsAdapter.UserOrderItemViewHolder holder, final int position) {
        if(dataset.get(position)!=null){
            holder.itemCount.setText("x"+dataset.get(position).getCount());
            holder.db.collection("parts").document(dataset.get(position).getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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