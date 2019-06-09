package ru.chenk.autoparts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AllOrdersAdapter extends RecyclerView.Adapter<AllOrdersAdapter.AllOrdersViewHolder> {
    private List<Order> dataset;
    private Context context;
    private List<View> invisibleItems;
    private ProgressBar progressBar;

    public static class AllOrdersViewHolder extends RecyclerView.ViewHolder{
        public TextView title;
        public FirebaseFirestore db = FirebaseFirestore.getInstance();

        public AllOrdersViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.AOIV_title);
        }
    }

    public AllOrdersAdapter(List<Order> dataset, Context context){
        this.dataset = dataset;
        this.context = context;
        this.invisibleItems = invisibleItems;
        this.progressBar = progressBar;
    }

    @NonNull
    @Override
    public AllOrdersAdapter.AllOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_orders_item_view, parent, false);
        return new AllOrdersViewHolder(v);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onBindViewHolder(@NonNull final AllOrdersAdapter.AllOrdersViewHolder holder, final int position) {
        if(dataset.get(position)!=null){
            final Order order = dataset.get(position);
            holder.db.collection("users").document(order.getUser()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot userSnapshot = task.getResult();
                        if(userSnapshot.get("username")!=null){
                            holder.title.setText("Заказ пользователя "+userSnapshot.get("username"));
                        }
                    }else{
                        holder.title.setText("Заказ от неизвестного пользователя");
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent adminOrderActivity = new Intent(context, AdminOrderActivity.class);
                    adminOrderActivity.putExtra("order", order.getId());
                    context.startActivity(adminOrderActivity);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
