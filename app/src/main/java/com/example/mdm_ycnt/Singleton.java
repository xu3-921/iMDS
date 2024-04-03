package com.example.mdm_ycnt;

public class Singleton {
    private static Singleton instance;
    private Object singletonData;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    public Object getSingletonData() {
        return singletonData;
    }

    public void setSingletonData(Object singletonData) {
        this.singletonData = singletonData;
    }
}

