package ru.chenk.autoparts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserOrdersAdapter extends RecyclerView.Adapter<UserOrdersAdapter.UserOrdersViewHolder> {
    private ArrayList<UserOrder> dataset;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static class UserOrdersViewHolder extends RecyclerView.ViewHolder{
        public TextView orderNameTextView;
        private RecyclerView itemsRecyclerView;
        private RecyclerView.LayoutManager layoutManager;

        public UserOrdersViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            orderNameTextView = itemView.findViewById(R.id.UOI_orderName);
            itemsRecyclerView = itemView.findViewById(R.id.UOI_recyclerView);
//            itemsRecyclerView.setHasFixedSize(true);

            layoutManager = new LinearLayoutManager(context);
            itemsRecyclerView.setLayoutManager(layoutManager);
        }
    }

    public UserOrdersAdapter(ArrayList<UserOrder> myDataset){
        this.dataset = myDataset;
    }

    @NonNull
    @Override
    public UserOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_order_item_view, parent, false);
        return new UserOrdersViewHolder(v, context);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserOrdersViewHolder holder, int position) {
        UserOrderItemsAdapter itemsAdapter = new UserOrderItemsAdapter(dataset.get(position).getItems());
        holder.itemsRecyclerView.setAdapter(itemsAdapter);
        if(dataset.get(position)!=null){
            UserOrder order = dataset.get(position);
            holder.orderNameTextView.setText("Заказ");
            db.collection("orders").document(order.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){

//                        holder.orderNameTextView.setText();
                    }
                }
            });
        }

        itemsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {

            }
        });
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}