package ru.chenk.autoparts;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String uid;
    private int count;

    public CartItem(String uid){
        this.uid = uid;
        this.count = 1;
    }

    public void inc(){
        this.count++;
    }

    public void dec(){
        this.count--;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUid() {
        return uid;
    }

    public int getCount() {
        return count;
    }
}
