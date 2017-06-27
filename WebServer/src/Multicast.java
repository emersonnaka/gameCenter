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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class Multicast {
	
	MulticastSocket mSocket = null;
	String host;
	int port;
	InetAddress group;
	Map<String, Long> onlineMap;	

	public Multicast() throws IOException, InterruptedException {
		
		this.host = "225.1.2.3";
		this.port = 8889;
		this.onlineMap = new HashMap<String, Long>();
		this.group = InetAddress.getByName(this.host);
		mSocket = new MulticastSocket(this.port);
		mSocket.joinGroup(this.group);
		
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
		
		clientThread.join();
		serverThread.join();
		verifyOnlineServersThread.join();
	}
	
	private void clientMulticast() throws IOException, InterruptedException {
		final String msg = new String("[gameServer]");
		
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
			if(msg.equals("[gameServer]")) {
				onlineMap.put(host, Calendar.getInstance().getTimeInMillis());
				System.out.println("The server " + host + " is connected");
			}
		}
	}
	
	private void verifyOnlineServers() throws InterruptedException {
		long difference, seconds;
		
		while(true) {
			if(onlineMap.size() > 1) {
				Iterator<Map.Entry<String, Long>> it = onlineMap.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String, Long> pair = (Map.Entry<String, Long>) it.next();
					difference = Calendar.getInstance().getTimeInMillis() - pair.getValue();
					seconds = TimeUnit.MILLISECONDS.toSeconds(difference);
					if(seconds > 20) {
						onlineMap.remove(pair.getKey());
						System.out.println("The server " + pair.getKey() + " is disconnected");
					}
					it.remove();
				}
			}
			TimeUnit.SECONDS.sleep(22);
		}
	}
	
	public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException {
		new Multicast();
	}
}
