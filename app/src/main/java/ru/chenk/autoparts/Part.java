package ru.chenk.autoparts;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Part implements Serializable {
    String name = null;
    String image = null;
    String uid = null;

    public Part(){

    }
    public Part(String name){
        this.name = name;
    }
    public Part(String name, String image){
        this.name = name;
        this.image = image;
    }
    public Part(String name, String image, String uid){
        this.name = name;
        this.image = image;
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
