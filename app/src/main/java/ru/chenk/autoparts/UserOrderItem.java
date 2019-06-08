package ru.chenk.autoparts;

public class UserOrderItem {
    private String id;
    private Integer count;

    public UserOrderItem(String id){
        this.id = id;
    }
    public UserOrderItem(String id, Integer count){
        this.id = id;
        this.count = count;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getId() {
        return id;
    }

    public Integer getCount() {
        return count;
    }
}
