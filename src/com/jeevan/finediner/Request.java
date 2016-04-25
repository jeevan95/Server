package com.jeevan.finediner;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int  NEW_CUSTOMER = 1,
            NEW_WAITER = 2,
            KITCHEN_CONNECT = 3,
            NEW_ORDER = 4,
            PROGRESS_UPDATE_WAITER = 5,
            PROGRESS_UPDATE_CUSTOMER = 6,
            CREATE_TABLE =7,
            TABLE_LIST=8,
            ERROR_MESSAGE = 9,
            MENU = 10,
            INITIALIZE_WAITER = 11,
            CUSTOMER_INFO = 12,
            REQUESTABLE_ITEMS = 13,
            REQUEST_ITEM = 14,
            TASK_LIST = 15,
    REMOVE_TASK = 16

                    ;

    private int type;
    private Object content;
    private Object content2;
    private Object content3;

    public Request(int type, Object o){
        this.type = type;
        this.content = o;
    }
    public Object getContent(){
        return content;
    }
    public Request(int type, Object o, Object o2){
        this.type = type;
        this.content = o;
        this.content2 = o2;

    } public Request(int type, Object o, Object o2, Object o3){
        this.type = type;
        this.content = o;
        this.content2 = o2;
        this.content3 = o3;

    }
    public Object getSecondContent(){
        return content2;
    }
    public Object getThirdContent(){
        return content3;
    }
    public int getType(){
        return type;
    }

}