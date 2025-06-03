//package com.felicianowilliam.cs360projecttwooriginalartifact;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.felicianowilliam.cs360projecttwo.InventoryItem;
//import com.felicianowilliam.cs360projecttwo.R;
//import com.google.android.material.button.MaterialButton;
//
//import java.util.List;
//
//public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
//
//    private List<InventoryItem> inventoryItems;
//    private final Context context;
//
//    // Interface for handling clicks in the Activity
//    public interface OnItemActionListener {
//        void onEditClick(InventoryItem item);
//        void onDeleteClick(InventoryItem item);
//    }
//
//    private final OnItemActionListener actionListener;
//
//
//    public InventoryAdapter(Context context, List<InventoryItem> inventoryItems, OnItemActionListener listener) {
//        this.context = context;
//        this.inventoryItems = inventoryItems;
//        this.actionListener = listener;
//    }
//
//    @NonNull
//    @Override
//    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_inventory, parent, false);
//        return new InventoryViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
//        InventoryItem currentItem = inventoryItems.get(position);
//
//        holder.tvItemNumber.setText(String.valueOf(currentItem.getQuantity()));
//        holder.tvItemName.setText(currentItem.getItemName());
//
//        // Set Click Listeners
//        holder.btnEdit.setOnClickListener(v -> {
//            if (actionListener != null) {
//                actionListener.onEditClick(currentItem);
//            }
//            Toast.makeText(context, "Edit: " + currentItem.getItemName(), Toast.LENGTH_SHORT).show();
//        });
//
//        holder.btnDelete.setOnClickListener(v -> {
//            if (actionListener != null) {
//                actionListener.onDeleteClick(currentItem);
//            }
//             Toast.makeText(context, "Delete: " + currentItem.getItemName(), Toast.LENGTH_SHORT).show();
//        });
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return inventoryItems == null ? 0 : inventoryItems.size();
//    }
//
//    // ViewHolder Class
//    public static class InventoryViewHolder extends RecyclerView.ViewHolder {
//        TextView tvItemNumber;
//        TextView tvItemName;
//        MaterialButton btnEdit;
//        MaterialButton btnDelete;
//
//        public InventoryViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvItemNumber = itemView.findViewById(R.id.tvItemNumber);
//            tvItemName = itemView.findViewById(R.id.tvItemName);
//            btnEdit = itemView.findViewById(R.id.btnEdit);
//            btnDelete = itemView.findViewById(R.id.btnDelete);
//        }
//    }
//
//    // Helper method to update the list if data changes
//    public void setItems(List<InventoryItem> newItems) {
//        this.inventoryItems = newItems;
//        notifyDataSetChanged();
//    }
//}
//
