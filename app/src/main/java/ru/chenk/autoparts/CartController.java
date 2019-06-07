package ru.chenk.autoparts;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class CartController {
    private ArrayList<CartItem> cartList;
    private SharedPreferences pref;
    private SharedPreferences.Editor prefEditor;

    public CartController(SharedPreferences pref){
        this.pref = pref;
        this.prefEditor = this.pref.edit();
        cartList = getCart();
    }

    public ArrayList<CartItem> getCart(){
        Gson gson = new Gson();
        String json = pref.getString("CART", "");
        if(json.equals("")){
            return new ArrayList<CartItem>();
        }else{
            ArrayList<CartItem> cartList = gson.fromJson(json, new TypeToken<ArrayList<CartItem>>(){}.getType());
            return cartList;
        }
    }

    public void saveCart(){
        Gson gson = new Gson();
        String json = gson.toJson(cartList);
        prefEditor.putString("CART", json);
        prefEditor.commit();
        cartList = getCart();
    }

    public CartItem getItemByUid(String uid){
        CartItem cartItem = new CartItem("uid");
        boolean found = false;
        for(CartItem ci : cartList){
            if(ci.getUid().equals(uid)){
                cartItem = ci;
                found = true;
            }
        }
        return !found ? null : cartItem;
    }

    public int getItemIndexByUid(String uid){
        CartItem cartItem = new CartItem("uid");
        boolean found = false;
        int c = 0;
        int index = 0;
        for(CartItem ci : cartList){
            if(ci.getUid().equals(uid)){
                found = true;
                index = c;
            }
            c++;
        }
        return !found ? -1 : index;
    }



    public boolean exists(String uid){
        boolean found = false;
        for (CartItem ci : cartList){
            found = ci.getUid().equals(uid) || found;
        }
        return found;
    }

    public void increaseItem(String uid){
        CartItem cartItem = this.getItemByUid(uid);
        if(cartItem.getCount() < cartItem.getMaxCount()){
            cartItem.inc();
        }
        saveCart();
    }

    public void decreaseItem(String uid){
        CartItem cartItem = this.getItemByUid(uid);
        cartItem.dec();
        saveCart();
    }

    public void removeItem(String uid){
        int index = this.getItemIndexByUid(uid);
        cartList.remove(index);
        saveCart();
    }

    public void clear(){
        cartList = new ArrayList<CartItem>();
        saveCart();
    }

    public int getSize(){
        return cartList.size();
    }

    public void addItem(String uid){
        CartItem cartItem = new CartItem(uid, 1);
        cartList.add(cartItem);
        saveCart();
    }

    public void addItem(String uid, int count){
        CartItem cartItem = new CartItem(uid, count);
        cartList.add(cartItem);
        saveCart();
    }
    public void addItem(String uid, int count, int maxCount){
        CartItem cartItem = new CartItem(uid, count, maxCount);
        cartList.add(cartItem);
        saveCart();
    }
}
