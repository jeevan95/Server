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
	ArrayList<String> reqItemList = new ArrayList<>(); 

	
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
	public ArrayList<String> getReqItems(){
		ArrayList<String> reqItems = new ArrayList<>();
		reqItems.add("Water");
		reqItems.add("Salt");
		reqItems.add("Pepper");
		reqItems.add("Ketchup");
		reqItems.add("Salad");
		reqItems.add("Bill");
		return reqItems;
	}
	
	public void startServer(){

		isOnline = true;
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
					appendLog("code "+code);
					if(tabs==null){
						appendLog("code not found "+code);

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


						tabs.send(new Request(1,setupmenu()));
						tabs.send(new Request(1,tabs.order));


						tabs.new Listener().start();

					}
					break;
				case Request.NEW_WAITER: 
					String uname = (String) re.getContent();
					String pass = (String) re.getSecondContent();
					Waiter waitr = findWaiter(uname);
					if(waitr==null){
						appendLog("wat not found ");

						ouu.writeObject(new Request(Request.ERROR_MESSAGE, "ID is not recognised. Please try again"));
						ouu.flush();
						ouu.reset();

					}
					else if(!waitr.password.equals(pass))
					{
						appendLog("wat pas found ");

						ouu.writeObject(new Request(Request.ERROR_MESSAGE, "Login details are incorrect. Please try again"));
						ouu.flush();
						ouu.reset();
					}
					else{
						appendLog("wat found ");

						waitr.setupStreams(sock, inn, ouu);
						appendLog("Waiter connected");
						appendLog("waiters size "+waiters.size());						
						waitr.send(new Request(Request.INITIALIZE_WAITER,"Logged in succesfully"));

						waitr.new Listener().start();

					}
					break;
				case Request.KITCHEN_CONNECT: 
					cl = new Kitchen(sock, inn, ouu);
					kitchen = (Kitchen) cl;
					kitchen.new Listener().start();
					tables.add(new Table(1,2,"asdasd"));
					tables.add(new Table(2,2,"sad"));


					Waiter w2 = new Waiter("Jack","jc","asdasd");
					Waiter w3 = new Waiter("Mike","mc","sad");
					waiters.add(w2);
					waiters.add(w3);



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
				appendLog("reader disconnected");

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
								Table tb = findTable((String) req.getSecondContent());
								if(tb!=null){
									tb.order.addAll(order);
									kitchen.send(new Request(Request.NEW_ORDER,tb.id,order));
								}
								break;
							case Request.INITIALIZE_WAITER:

								w.send(new Request(Request.MENU,setupmenu()));						
								w.send(new Request(Request.TABLE_LIST,getTableNos()));						
								w.send(new Request(Request.REQUESTABLE_ITEMS,getReqItems()));						
								w.send(new Request(Request.TASK_LIST, reqItemList));
								
								break;

							case Request.CUSTOMER_INFO:
								Table t = findTablebyNum((String)req.getContent());
								if(t!=null){
									w.send(new Request(Request.CUSTOMER_INFO,t.order, t.id));

								}
								break;
							case Request.CREATE_TABLE:
								String tbnum = (String)req.getSecondContent();
								Table tv = findTablebyNum(tbnum);
								if(tv==null){
									int tbgus = Integer.parseInt((String)req.getSecondContent());

									tables.add(new Table(Integer.parseInt(tbnum),tbgus,"werrr"));
									w.send(new Request(Request.TABLE_LIST, getTableNos()));
								}
								else
									w.send(new Request(Request.ERROR_MESSAGE, "Table number already exists"));

								break;
							case Request.PROGRESS_UPDATE_WAITER:
								String tbl = (String)req.getContent();
								kitchen.send(new Request(Request.PROGRESS_UPDATE_WAITER,tbl,w.username));
								System.out.println("tbl = "+tbl);

								break;
							case Request.REQUEST_ITEM:
								String task = "Table "+(String)req.getContent()+ " requests "+(String)req.getSecondContent();
								reqItemList.add(task);
								w.send(new Request(Request.TASK_LIST, reqItemList));
								
								break;
							case Request.REMOVE_TASK:
								String donetask = (String)req.getContent();
								ArrayList<String> newTasks = new ArrayList<>();
								for(String tsk: reqItemList){
									if(!tsk.equals(donetask)){
										newTasks.add(tsk);
									}
								}
								reqItemList = newTasks;
								w.send(new Request(Request.TASK_LIST, reqItemList));
								
								break;
							
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
								kitchen.send(new Request(Request.NEW_ORDER,w.id,or));
								break;
							case Request.PROGRESS_UPDATE_CUSTOMER:
								kitchen.send(new Request(Request.PROGRESS_UPDATE_CUSTOMER,w.id));
								break;
							}
						}
						else {
							Kitchen w = (Kitchen)Client.this;
							switch(req.getType()){

							case Request.PROGRESS_UPDATE_CUSTOMER:
								Table t = findTable((String) req.getContent());
								if(t!=null){
									if(t.isConnected){

										t.send(new Request(Request.PROGRESS_UPDATE_CUSTOMER, req.getSecondContent(),req.getThirdContent()));
									}
								}
								//appendLog(" " +req.getSecondContent()+ " " + req.getThirdContent());

								break;
							case Request.PROGRESS_UPDATE_WAITER:
								System.out.println("req frm kict to wait");
								Waiter ww = findWaiter((String) req.getContent());
								System.out.println("req frm kict to wait");
								ww.send(new Request(Request.PROGRESS_UPDATE_WAITER, req.getSecondContent(),req.getThirdContent()));
								appendLog(" " +req.getSecondContent()+ " " + req.getThirdContent());
								break;
							}

						}
					}
				}

			}

		}
	}



	public ArrayList<String> getTableNos(){
		ArrayList<String> tn = new ArrayList<>();
		for (int i=0; i<tables.size();i++) {
			tn.add(""+tables.get(i).tableno);
		}
		return tn;
	}

	public Table findTable(String id){
		for (int i=0; i<tables.size();i++) {
			if(tables.get(i).id.equals(id)){
				return tables.get(i);
			}
		}
		return null;
	}
	public Table findTablebyNum(String id){
		for (int i=0; i<tables.size();i++) {
			if(String.valueOf(tables.get(i).tableno).equals(id)){
				return tables.get(i);
			}
		}
		return null;
	}
	public Waiter findWaiter(String id){
		for (int i=0; i<waiters.size();i++) {
			if(waiters.get(i).username.equals(id)){
				return waiters.get(i);
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

		Table(int no, int noof,String sdd){
			this.noguests = noof;
			this.tableno = no;
			boolean t = true;
			id=sdd;
			kitchen.send(new Request(Request.NEW_CUSTOMER,id));
			order = new ArrayList<>();
			isConnected = false;
			
			/*
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
			 */

		}

		String getID(){
			final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
			String iid = "";
			for(int i = 3; i < uuid.length() ; i+=4) { 
				iid = iid + uuid.charAt(i); 
			}
			return iid;
		}
		void setOccupied(boolean r){
			isOccupied = r;
		}

	}



	public class Waiter extends Client{

		public String name;
		public String username; 
		public String password; 

		void setupStreams(Socket sc, ObjectInputStream is, ObjectOutputStream ouu )  {
			sock = sc;
			ostrm = ouu;
			istrm = is;

			isConnected = true;

		}

		Waiter(String nme, String un, String pass){
			name = nme;
			username = un;
			password = pass;
			isConnected = false;
		}


	}
	public class Kitchen extends Client{
		public String name = "sdsd";
		Kitchen(Socket sc, ObjectInputStream is, ObjectOutputStream ouu ) throws IOException {
			super(sc,is,ouu);
			isConnected = true;
			// TODO Auto-generated constructor stub
		}

	}
}
