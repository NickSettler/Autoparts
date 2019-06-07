package ru.chenk.autoparts;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Part implements Serializable {
    String name = null;
    String imageSrc = null;
    byte[] image = null;
    String uid = null;
    int count = 0;
    int orders = 0;
    double price = 0;
    List<Spec> specs = new ArrayList<>();

    public Part(){

    }
    public Part(String name){
        this.name = name;
    }
    public Part(String name, String imageSrc){
        this.name = name;
        this.imageSrc = imageSrc;
    }
    public Part(String name, String imageSrc, String uid){
        this.name = name;
        this.imageSrc = imageSrc;
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void deleteSpecs(){this.specs.clear();}

    public void addSpec(Spec spec){
        this.specs.add(spec);
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public byte[] getImage() {
        return image;
    }

    public int getCount() {
        return count;
    }

    public int getOrders() {
        return orders;
    }

    public double getPrice() {
        return price;
    }

    public List<Spec> getSpecs() {
        return specs;
    }
}
