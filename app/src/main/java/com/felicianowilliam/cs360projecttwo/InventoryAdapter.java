package com.felicianowilliam.cs360projecttwo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Adapter class for displaying a list of {@link InventoryItem} objects in a RecyclerView.
 *
 * This adapter is responsible for:
 * <ul>
 *     <li>Inflating the layout for each item in the RecyclerView.</li>
 *     <li>Binding {@link InventoryItem} data to the views within each item.</li>
 *     <li>Handling click events on "Edit" and "Delete" buttons for each item.</li>
 *     <li>Providing an interface ({@link OnItemActionListener}) to notify the hosting Activity or Fragment
 *         about these actions.</li>
 * </ul>
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    // List to store inventory items
    private List<InventoryItem> inventoryItems;

    // Context for the adapter
    private final Context context;

    // Interface for handling clicks in the Activity
    public interface OnItemActionListener {

        // Called when edit button is clicked
        void onEditClick(InventoryItem item);

        // Called when delete button is clicked
        void onDeleteClick(InventoryItem item);
    }

    // Listener for item actions
    private final OnItemActionListener actionListener;

    // Constructor for the adapter
    public InventoryAdapter(Context context, List<InventoryItem> inventoryItems, OnItemActionListener listener) {

        // Initialize context
        this.context = context;

        // Initialize inventory items list
        this.inventoryItems = inventoryItems;

        // Initialize action listener
        this.actionListener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link InventoryViewHolder} of the given type to represent
     * an item.
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return A new InventoryViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_inventory, parent, false);

        // Return new ViewHolder instance
        return new InventoryViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the {@link InventoryViewHolder#itemView} to reflect
     * the item at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *               item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     *
     * This method does not return a value.
     */
    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {

        // Get current item from list
        InventoryItem currentItem = inventoryItems.get(position);

        // Set item quantity in TextView
        holder.tvItemNumber.setText(String.valueOf(currentItem.getQuantity()));

        // Set item name in TextView
        holder.tvItemName.setText(currentItem.getName());

        // Set Click Listener for Edit button
        holder.btnEdit.setOnClickListener(v -> {

            // Notify listener if exists
            if (actionListener != null) {

                actionListener.onEditClick(currentItem);
            }

        });

        // Set Click Listener for Delete button
        holder.btnDelete.setOnClickListener(v -> {

            // Notify listener if exists
            if (actionListener != null) {

                actionListener.onDeleteClick(currentItem);

            }

        });

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * This method is called by RecyclerView to determine the number of items to display.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        
        return inventoryItems == null ? 0 : inventoryItems.size();
    }

    /**
     * ViewHolder class for displaying individual inventory items in a RecyclerView.
     * This class holds references to the views within each item's layout
     * and is used by the RecyclerView's adapter to efficiently manage and update
     * the displayed data.
     */
    public static class InventoryViewHolder extends RecyclerView.ViewHolder {

        // TextView for item quantity
        TextView tvItemNumber;

        // TextView for item name
        TextView tvItemName;

        // Button for editing item
        MaterialButton btnEdit;

        // Button for deleting item
        MaterialButton btnDelete;

        // Constructor for ViewHolder
        public InventoryViewHolder(@NonNull View itemView) {

            super(itemView);

            // Initialize views from layout
            tvItemNumber = itemView.findViewById(R.id.tvItemNumber);

            tvItemName = itemView.findViewById(R.id.tvItemName);

            btnEdit = itemView.findViewById(R.id.btnEdit);

            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

