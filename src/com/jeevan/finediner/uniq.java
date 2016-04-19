package com.jeevan.finediner;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class uniq {

	static String getID(){
		final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		String iid = "";
		for(int i = 3; i < uuid.length() ; i+=4) { 
			iid = iid + uuid.charAt(i); 
		}
		return iid;
	}
	public static void main(String args[]){
		ArrayList<String> tablelist = new ArrayList<>();
		tablelist.add("asddsadsa");
		tablelist.add("sdd");
		tablelist.add("asdads");
		String id ="";

		boolean t = true;
		while(t){
			id = getID();
			t=false;
			for (int i=0; i<tablelist.size()&&t==false;i++) {

				if(id.equals(tablelist.get(i))){
					t=true;
					break;
				}
			}
			
		}
	}

}
