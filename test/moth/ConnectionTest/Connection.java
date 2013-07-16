package test.moth.ConnectionTest;

import java.io.IOException;
import java.net.Socket;

import test.common.Atomic;

class Connection {

	private final Counter counter;
	private Socket socket;

	public Connection() {
		this.socket = null;
		this.counter = new Counter();
	}
	
	// BCT
	public void connect() {
		this.socket = new Socket();
	}
	
	public void disconnect() throws IOException {
		resetSocket();
		this.counter.reset();
	}
	
	//Method created to englobe the transaction
	@Atomic
	private void resetSocket() throws IOException{
		this.socket.close();
	}

	@Atomic
	public boolean isConnected() {
		return !this.socket.isClosed();
//		(this.socket != null);
	}

	@Atomic
	public void send(String msg) throws IOException {
		this.socket.getOutputStream().write(msg.getBytes());
//		socket = null;
		this.counter.increment();
	}
}