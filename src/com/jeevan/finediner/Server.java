package com.jeevan.finediner;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;


public class Server {


	private static final long serialVersionUID = 1L;

	Boolean isOnline;
	ServerSocket svSock;
	ArrayList<Table> tables = new ArrayList<>(); 
	ArrayList<Waiter> waiters = new ArrayList<>(); 
	Kitchen kitchen;

	public static void main(String[] args) {
		Server server = new Server();

	}

	/**
	 * Create the application.
	 */
	public Server() {
		new ServerRunning().start();

	}

	class ServerRunning extends Thread {
		public void run() {
			startServer();

		}
	}

	void appendLog(String lg) {
		System.out.println(lg);
	}
	ArrayList<Item> setupmenu(){
		ArrayList<Item> menuItems = new ArrayList<>();
		ArrayList<String> vegan = new ArrayList<>();
		ArrayList<String> nut = new ArrayList<>();
		ArrayList<String> diary = new ArrayList<>();
		nut.add("Nuts free");
		diary.add("Vegan");
		diary.add("Dairy free");
		vegan.add("Vegan");
		Item i= new Item("Nuggets","best burger in town",2.95,44000,0.1,Item.STARTER,null);
		menuItems.add(i);
		i= new Item("OB","best burger in town",1.95,44000,0.1,Item.STARTER,null);
		menuItems.add(i);
		i= new Item("Soup","best burger in town",8.95,44000,0.1,Item.STARTER,null);
		menuItems.add(i);

		i= new Item("Burger","best burger in town",8.95,44000,0.1,Item.MAIN,null);
		menuItems.add(i);

		i = new Item("Pizza", "very nise", 12.25,9000,0.5,Item.MAIN,vegan);
		menuItems.add(i);

		i = new Item("Fish and Chips","Tasty",14.75,9000,0.1,Item.DESSERT,nut);
		menuItems.add(i);

		i = new Item("Fish","Tasty",11.75,9000,0.1,Item.DESSERT,nut);
		menuItems.add(i);

		i = new Item("Steak", "the best", 9.25,6000,0.2,Item.DESSERT,diary);
		menuItems.add(i);
		i = new Item("Steak peice", "the best", 2.25,6000,0.2,Item.DESSERT,diary);
		menuItems.add(i);
		return menuItems;
	}
	public void startServer(){

		isOnline = true;
		Table t = new Table(1,2);
		tables.add(t);
		try {
			svSock = new ServerSocket(3223);
			
			appendLog("Server Runing");
			while (isOnline) {
				Socket sock = svSock.accept();
				ObjectInputStream inn = new ObjectInputStream(sock.getInputStream());
				ObjectOutputStream ouu = new ObjectOutputStream (sock.getOutputStream());
				Client cl;
				Request re = (Request)inn.readObject();

				switch(re.getType()){

				case Request.NEW_CUSTOMER: 
					String code = (String) re.getContent();
					Table tabs = findTable(code);
					if(tabs==null){
						ouu.writeObject(new Request(Request.NEW_CUSTOMER, "Table id not found"));
						ouu.flush();
						ouu.reset();
					}
					else if(tabs.isOccupied)
					{
						ouu.writeObject(new Request(Request.NEW_CUSTOMER, "Table is occupied"));
						ouu.flush();
						ouu.reset();
					}
					else{
						tabs.setOccupied(true);;
						tabs.setupStreams(sock, inn, ouu);
						appendLog("Customer connected");
						appendLog("size "+tables.size());

						//kitchen.send(new Request(Request.NEW_CUSTOMER,re.getContent()));
						tabs.send(new Request(1,setupmenu()));
						tabs.send(new Request(1,tabs.order));

						tabs.new Listener().start();

					}
					break;
				case Request.NEW_WAITER: 
					cl = new Waiter(sock, inn, ouu);
					waiters.add((Waiter) cl);
					cl.new Listener().start();	
					break;
				case Request.KITCHEN_CONNECT: 
					cl = new Kitchen(sock, inn, ouu);
					kitchen = (Kitchen) cl;
					cl.new Listener().start();
					break;
				default: 
					break;
				}
			}
			svSock.close();
		}
		catch (IOException  | ClassNotFoundException e ){
			e.printStackTrace();
		} 

	}
	void stopServer(){
		isOnline = false;
	}

	class Client  {
		Socket sock; 
		ObjectInputStream istrm;
		ObjectOutputStream ostrm;
		Boolean isConnected;
		Client(){}
		Client(Socket sc, ObjectInputStream is, ObjectOutputStream ouu ) throws IOException {
			sock = sc;
			ostrm = ouu;
			istrm = is;
			isConnected = true;
		}

		public void stopConnection() {
			isConnected=false;


			try {
				ostrm.close();
				istrm.close();
				sock.close();
				if(this instanceof Table){
					((Table) this).setOccupied(false);
					appendLog("Customer disconnected");
					tables.remove(this);
				}
				else if(this instanceof Waiter){
					waiters.remove(this);
				}
				else if(this instanceof Kitchen){
					appendLog("Kitchen disconnected");
					kitchen = null;					
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public Request receive(){
			try {
				return (Request) istrm.readObject();
			} catch (ClassNotFoundException | IOException  e) {
				//e.printStackTrace();

				isConnected=false;
				if(this instanceof Table){
					((Table) this).setOccupied(false);
					appendLog("Customer disconnected");
				}
				return null;
			} 
		}

		public void send(Request msg) {
			try {
				ostrm.writeObject(msg);
				ostrm.flush();
				ostrm.reset();
			} catch (IOException e) {
				isConnected=false;

				e.printStackTrace();
			}

		}
		public class Listener extends Thread {
			public void run(){
				while(isConnected){
					Request req = receive();
					if(req!=null){
						if(Client.this instanceof Waiter){
							Waiter w = (Waiter)Client.this;
							switch(req.getType()){
							case Request.NEW_ORDER:
								ArrayList<Item> order = (ArrayList<Item>) req.getContent();
								for (int i=0; i<order.size();i++) {
									appendLog("New new order -- "+order.get(i).getQuantity()+" "+order.get(i).getName());
								}

							}

						}
						else if(Client.this instanceof Table){
							Table w = (Table)Client.this;
							switch(req.getType()){
							case Request.NEW_ORDER:
								ArrayList<Item> or = (ArrayList<Item>) req.getContent();
								w.order.addAll(or);
								for (int i=0; i<or.size();i++) {
									appendLog("order -- "+or.get(i).getQuantity()+" "+or.get(i).getName());
								}
								//kitchen.send(new Request(Request.NEW_ORDER,tableno,or));

							}
						}
						else {
							Kitchen w = (Kitchen)Client.this;
							switch(req.getType()){
							case Request.NEW_ORDER:
								appendLog("sss"+(int) req.getSecondContent());
								//Customer t = findTable((int) req.getContent());
								//t.send(new Request(Request.KITCHEN_CONNECT, req.getSecondContent()));
							}
						}
						
					}

				}
			}
		}
	}


	public Table findTable(String id){
		for (int i=0; i<tables.size();i++) {
			if(tables.get(i).id.equals(id)){
				return tables.get(i);
			}
		}
		return null;
	}

	public class Table extends Client{
		int noguests;
		int tableno;
		String id;
		boolean isOccupied = false;
		ArrayList<Item> order;


		void setupStreams(Socket sc, ObjectInputStream is, ObjectOutputStream ouu )  {
			sock = sc;
			ostrm = ouu;
			istrm = is;

			isConnected = true;
		}

		Table(int no, int noof){
			this.noguests = noof;
			this.tableno = no;
			boolean t = true;
			order = new ArrayList<>();

			while(t){
				id = getID();
				t=false;
				for (int i=0; i<tables.size()&&t==false;i++) {

					if(id.equals(tables.get(i).id)){
						t=true;
						break;
					}
				}

			}


		}
		
		String getID(){
			final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
			String iid = "";
			for(int i = 3; i < uuid.length() ; i+=4) { 
				iid = iid + uuid.charAt(i); 
			}
			return "asdasd";
		}
		void setOccupied(boolean r){
			isOccupied = r;
		}

	}



	public class Waiter extends Client{

		Waiter(Socket sc, ObjectInputStream is, ObjectOutputStream ouu ) throws IOException {
			super(sc,is,ouu);
			// TODO Auto-generated constructor stub
		}
		
	}
	public class Kitchen extends Client{
		public String name = "sdsd";
		Kitchen(Socket sc, ObjectInputStream is, ObjectOutputStream ouu ) throws IOException {
			super(sc,is,ouu);
			// TODO Auto-generated constructor stub
		}
		
	}
}
