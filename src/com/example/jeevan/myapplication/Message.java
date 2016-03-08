package com.example.jeevan.myapplication;
import java.io.Serializable;

public class Message implements Serializable {
		private static final long serialVersionUID = 1L;
		int type;
		public String st;

		public Message(int type, String st){
			this.type = type;
			this.st = st;
		}

	}
