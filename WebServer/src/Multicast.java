/*
 * Sistemas Distribuídos
 * Profº. Rodrigo Campiolo
 * Emerson Yudi Nakashima 1451600
 * Multicast
 * Descrição: chat em grupo
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Multicast {
	
	MulticastSocket mSocket = null;
	String host;
	int port;
	InetAddress group;
	Map<String, Long> onlineMap;
	private List<String> hostsList;

	public Multicast() throws IOException, InterruptedException {
		
		this.host = "225.1.2.3";
		this.port = 8889;
		this.onlineMap = new HashMap<String, Long>();
		this.group = InetAddress.getByName(this.host);
		mSocket = new MulticastSocket(this.port);
		mSocket.joinGroup(this.group);
		
		hostsList = new ArrayList<String>();
		networkInterfaces();
		
		Runnable clientRun = new Runnable() {
			@Override
			public void run() {
				try {
					clientMulticast();
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Thread clientThread = new Thread(clientRun);
		clientThread.start();
		System.out.println("Client is running");
		
		Runnable serverRun = new Runnable() {
			@Override
			public void run() {
				try {
					serverMulticast();
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		Thread serverThread = new Thread(serverRun);
		serverThread.start();
		System.out.println("Server is running");
		
		Runnable verifyOnlineServersRun = new Runnable() {
			@Override
			public void run() {
				try {
					verifyOnlineServers();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Thread verifyOnlineServersThread = new Thread(verifyOnlineServersRun);
		verifyOnlineServersThread.start();
		System.out.println("Verify online servers is running");
		
	}
	
	private void networkInterfaces() throws SocketException {
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
		while(e.hasMoreElements())
		{
		    NetworkInterface n = (NetworkInterface) e.nextElement();
		    Enumeration<InetAddress> ee = n.getInetAddresses();
		    while (ee.hasMoreElements())
		    {
		        InetAddress i = (InetAddress) ee.nextElement();
		        hostsList.add(i.getHostAddress());
		    }
		}
	}
	
	public void clientMulticast() throws IOException, InterruptedException {
		final String msg = new String("GameServer");
		
		while(true) {
			byte[] msgByte = msg.getBytes();
			DatagramPacket msgDataOut = new DatagramPacket(msgByte,  msg.length(), this.group, this.port);
			mSocket.send(msgDataOut);
			TimeUnit.SECONDS.sleep(20);
		}
	}

	
	private void serverMulticast() throws IOException, InterruptedException {
		String msg = new String();
		String host = new String();
		
		while(true) {
			byte[] msgByte = new byte[1000];
			DatagramPacket msgDataIn = new DatagramPacket(msgByte, msgByte.length);
			mSocket.receive(msgDataIn);
			msg = new String(msgDataIn.getData(), 0, msgDataIn.getLength());
			host = msgDataIn.getAddress().getHostAddress();
			if(msg.contains("GameServer") && !hostsList.contains(host)) {
				onlineMap.put(host, Calendar.getInstance().getTimeInMillis());
				System.out.println("The server " + host + " is connected");
			}
		}
	}
	
	private void verifyOnlineServers() throws InterruptedException {
		long difference, seconds;
		boolean remove = false;
		List<String> removeHost;
		
		while(true) {
			removeHost = new ArrayList<String>();
			if(onlineMap.size() > 1) {
				for(Map.Entry<String, Long> entry : onlineMap.entrySet()) {
					difference = Calendar.getInstance().getTimeInMillis() - entry.getValue();
					seconds = TimeUnit.MILLISECONDS.toSeconds(difference);
					if(seconds > 20) {
						remove = true;
						removeHost.add(entry.getKey());
					}
				}
				if(remove) {
					for(String host : removeHost) {
						onlineMap.remove(host);
						System.out.println("The server " + host + " is disconnected");
					}
					remove = false;
				}
			}
			TimeUnit.SECONDS.sleep(5);
		}
	}

	public void sendRequest(String request, String key){
		System.out.println("Fazer");
	}
	
	public Map<String, Long> getOnlineMap() {
		return onlineMap;
	}

	public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException {
		new Multicast();
	}
}
