package ru.chenk.autoparts;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SpecsAdapter extends RecyclerView.Adapter<SpecsAdapter.SpecViewHolder> {
    private List<Spec> dataset;

    public static class SpecViewHolder extends RecyclerView.ViewHolder{
        public TextView specNameTextView;
        public TextView specValueTextView;

        public SpecViewHolder(@NonNull View itemView) {
            super(itemView);
            specNameTextView = itemView.findViewById(R.id.SV_nameTextView);
            specValueTextView = itemView.findViewById(R.id.SV_valueTextView);
        }
    }

    public SpecsAdapter(List<Spec> myDataset){
        this.dataset = myDataset;
    }

    @NonNull
    @Override
    public SpecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.spec_item_view, parent, false);
        SpecViewHolder vh = new SpecViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull SpecViewHolder holder, int position) {
        if(dataset.get(position)!=null){
            if(position % 2 == 0){
                holder.itemView.setBackgroundColor(Color.parseColor("#999999"));
            }
            holder.specNameTextView.setText(dataset.get(position).getName());
            holder.specValueTextView.setText(dataset.get(position).getValue());
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}
