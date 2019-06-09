package ru.chenk.autoparts;

import java.util.ArrayList;

public class Order {
    private String id;
    private String user;
    private ArrayList<Item> items;

    public Order(){

    }

    public Order(String id){
        this.id = id;
    }

    public Order(String id, String user){
        this.id = id;
        this.user = user;
    }

    public Order(String id, String user, ArrayList<Item> items){
        this.id = id;
        this.user = user;
        this.items = items;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public void addItem(Item item){
        items.add(item);
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public ArrayList<Item> getItems() {
        return items;
    }
}
