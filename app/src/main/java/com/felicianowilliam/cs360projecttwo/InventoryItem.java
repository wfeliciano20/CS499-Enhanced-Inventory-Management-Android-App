package com.felicianowilliam.cs360projecttwo;


import androidx.annotation.NonNull;

/**
 * Represents an item in the inventory.
 * This class stores details about a specific item, including its quantity, name,
 * user association, unique identifier, and database version.
 */
public class InventoryItem {

    private int quantity;

    private String name;

    private String userId;

    private String id;

    private int dbVersion;


    /**
     * Constructs a new InventoryItem.
     *
     * @param id        The unique identifier for the inventory item.
     * @param name      The name or description of the inventory item.
     * @param quantity  The current quantity of the inventory item in stock.
     * @param userId    The identifier of the user associated with this inventory item.
     * @param dbVersion The version of the database record for optimistic locking or tracking changes.
     */
    public InventoryItem(String id, String name, int quantity,String userId, int dbVersion) {

        this.id = id;

        this.quantity = quantity;

        this.name = name;

        this.userId = userId;

        this.dbVersion = dbVersion;

    }

    /**
     * Returns the unique identifier of this object.
     *
     * @return The ID as a String.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the quantity of the item.
     *
     * @return The quantity of the item.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the name of the entity.
     *
     * @return The name of the entity.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the unique identifier for the user.
     *
     * @return A string representing the user's ID. This ID is typically used
     *         to uniquely identify the user within the system or application.
     *         It might be a username, a numerical ID, or another unique string.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Retrieves the current version of the database schema.
     *
     * This method returns the integer value representing the version of the database
     * being used by the application. This version number is typically used during
     * database migrations to determine if upgrades or downgrades are necessary.
     *
     * @return The current database schema version as an integer.
     */
    public int getDbVersion() {
        return dbVersion;
    }

    /**
     * Returns a string representation of the InventoryItem object.
     * This method is primarily used for debugging and logging purposes.
     *
     * @return A string containing the values of all fields of the InventoryItem object.
     *         The format is: "InventoryItem{quantity=VALUE, name='VALUE', userId='VALUE', id='VALUE', dbVersion=VALUE}"
     */
    @NonNull
    @Override
    public String toString() {

        return "InventoryItem{" +
                "quantity=" + quantity +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", id='" + id + '\'' +
                ", dbVersion=" + dbVersion +
                '}';
    }
}

