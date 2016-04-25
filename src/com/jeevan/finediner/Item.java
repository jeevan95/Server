package com.jeevan.finediner;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by JEEVAN on 12/03/2016.
 */
public class Item implements Serializable {

    private String name;
    private String desc;
    private double price;
    private int quantity =1;
    private long time;
    private double timefactor;
    private int category;
    private ArrayList<String> props;
    public static int STARTER = 0, MAIN = 1, SIDE = 2, DESSERT = 3, DRINK = 4, DEAL = 5;


    public Item(Item i ){
        name = i.name;
        desc = i.desc;
        price = i.price;
        quantity = i.quantity;
        time = i.time;
        timefactor = i.timefactor;
        category = i.category;
        props = i.props;
    }
    public Item(String n, String ds, double pr, long ti, double f, int cat, ArrayList<String> props) {
        name = n;
        desc = ds;
        price = pr;
        time = ti;
        timefactor = f;
        category = cat;
        this.props = props;
    }
    public int getCategory() {return category;}
    public void setQuantity(int q){
        quantity = q;
    }

    public boolean hasProperty(ArrayList<String> pro){
        if (props == null){
            return false;
        }
        boolean boo = false;
        for (String s:pro){
            for(String ss:props ) {
                if (s.equals(ss)) {
                    boo = true;
                }
            }

            if(boo){
                boo = false;}
            else {
                return false;
            }
        }
        return true;
    }
    public String getName(){return name;}
    public String getDesc(){return desc;}
    public double getPrice(){return price;}
    public int getQuantity(){return quantity;}
    public long getTime(){
        return Math.round(time + (getQuantity()-1)*(time*timefactor));
    }

    public static Comparator<Item> ItNameComp = new Comparator<Item>() {
        @Override
        public int compare(Item lhs, Item rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    };
    public static Comparator<Item> ItPriceComp = new Comparator<Item>() {
        @Override
        public int compare(Item lhs, Item rhs) {
            if (lhs.getPrice() < rhs.getPrice()) return -1;
            else if (lhs.getPrice() < rhs.getPrice()) return 1;
            else
                return 0;
        }
    };
}

