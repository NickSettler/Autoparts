package ru.chenk.autoparts;

import java.util.ArrayList;

public class UserOrder {
    private String id;
    private ArrayList<UserOrderItem> items;

    public UserOrder(){

    }

    public UserOrder(String id){
        this.id = id;
        this.items = new ArrayList<>();
    }

    public UserOrder(String id, ArrayList<UserOrderItem> items){
        this.id = id;
        this.items = items;
    }

    public void setItems(ArrayList<UserOrderItem> items) {
        this.items = items;
    }

    public ArrayList<UserOrderItem> getItems() {
        return items;
    }

    public String getId() {
        return id;
    }

    public void addItem(UserOrderItem item){
        items.add(item);
    }
}
