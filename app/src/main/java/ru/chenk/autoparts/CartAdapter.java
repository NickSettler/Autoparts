package ru.chenk.autoparts;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> dataset;

    public static class CartViewHolder extends RecyclerView.ViewHolder{
        public ImageView itemImageView;
        public TextView itemName;
        public Button minusButton, plusButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.CI_image);
            itemName = itemView.findViewById(R.id.CI_name);
        }
    }

    public CartAdapter(List<CartItem> myDataset){
        this.dataset = myDataset;
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
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("parts").document(dataset.get(position).getUid()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot data = task.getResult();
                                if(data.get("name")!=null){
                                    Log.d("CARTITEM", data.get("name").toString());
                                    holder.itemName.setText(data.get("name").toString());
                                    StorageReference ref = FirebaseStorage.getInstance().getReference().child(data.get("image").toString());
                                    ref.getBytes(Long.MAX_VALUE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                                        @Override
                                        public void onComplete(@NonNull Task<byte[]> task) {
                                            if(task.isSuccessful()){
                                                holder.itemImageView.setImageBitmap(BitmapFactory.decodeByteArray(task.getResult(), 0, task.getResult().length));
                                            }
                                        }
                                    });
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
