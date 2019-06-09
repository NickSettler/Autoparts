package ru.chenk.autoparts;

public class Item {
    private String id;
    private int count;

    public Item(){
        this.id = id;
    }

    public Item(String id){
        this.id = id;
    }

    public Item(String id, int count){
        this.id = id;
        this.count = count;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public int getCount() {
        return count;
    }
}
