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
import java.util.Map;
import java.util.Scanner;

public class Multicast {
	
	MulticastSocket mSocket = null;
	String host;
	int port;
	InetAddress group;
	Map<String, String> onlineMap;	

	public Multicast(String host, int port) throws IOException, InterruptedException {
		
		this.host = host;
		this.port = port;
		this.group = InetAddress.getByName(this.host);
		mSocket = new MulticastSocket(this.port);
		mSocket.joinGroup(this.group);
		
		Runnable clientRun = new Runnable() {
			@Override
			public void run() {
				try {
					clientMulticast();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		Thread clientThread = new Thread(clientRun);
		clientThread.start();
		
		Runnable serverRun = new Runnable() {
			@Override
			public void run() {
				try {
					serverMulticast();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		Thread serverThread = new Thread(serverRun);
		serverThread.start();
		
		clientThread.join();
		serverThread.join();
	}
	
	private void clientMulticast() throws IOException {
		
		Scanner scanner = new Scanner(System.in);
		String nickname = new String();
		String msg = new String();
		
		System.out.print("Input your nickname: ");
		nickname = scanner.nextLine();
		
		while(!msg.equalsIgnoreCase("Fim")) {
			msg = new String(scanner.nextLine());
			msg = nickname + "|||" + msg;
			byte[] msgByte = msg.getBytes();
			DatagramPacket msgDataOut = new DatagramPacket(msgByte,  msg.length(), this.group, this.port);
			mSocket.send(msgDataOut);
		}
		
		scanner.close();
	}
	
	private void serverMulticast() throws IOException {
		String nickname = new String();
		String msg = new String();
		
		while(!msg.equalsIgnoreCase("Fim")) {
			byte[] msgByte = new byte[1000];
			DatagramPacket msgDataIn = new DatagramPacket(msgByte, msgByte.length);
			mSocket.receive(msgDataIn);
			msg = new String(msgDataIn.getData());
			nickname = msg.split("\\|\\|\\|")[0].trim();
			msg = msg.split("\\|\\|\\|")[1].trim();
			
			System.out.println(nickname + ": " + msg);
		}
		
	}
	
	public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException {
		if(args.length == 2) {
			new Multicast(args[0], Integer.parseInt(args[1]));
		} else {
			System.out.println("Insert in command line: \"java Multicast ip port\" ");
		}
	}
}
