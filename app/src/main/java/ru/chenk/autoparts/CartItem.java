package ru.chenk.autoparts;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String uid;
    private int count;
    private int maxCount;

    public CartItem(String uid){
        this.uid = uid;
        this.count = 0;
    }
    public CartItem(String uid, int count){
        this.uid = uid;
        this.count = count;
    }
    public CartItem(String uid, int count, int maxCount){
        this.uid = uid;
        this.count = count;
        this.maxCount = maxCount;
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

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public String getUid() {
        return uid;
    }

    public int getCount() {
        return count;
    }

    public int getMaxCount() {
        return maxCount;
    }
}
