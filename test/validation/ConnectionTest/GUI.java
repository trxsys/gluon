package test.validation.ConnectionTest;

import java.io.IOException;
import java.util.Random;

class GUI extends Thread {

	Connection connection;

	public GUI(Connection connection) {
		this.connection = connection;
	}

	public static void main(String args[]) {
		Connection connection = new Connection();
		connection.connect();
		for (int i = 0; i < 10; i++)
			new GUI(connection).start();
	}

	boolean trySendMsg(String msg) throws IOException {
		if (this.connection.isConnected()) {
			this.connection.send(msg);
			return true;
		} else {
			return false;
		}
	}

    private void disconnect()
    {
        try {
            this.connection.resetSocket();
            this.connection.resetCounter();
        }
        catch (Exception _)
        {

        }
    }

	public void run() {
		Random rand = new Random();
		int command;
		try {
			do {
				command = rand.nextInt(10);
				if (command == 0)
                    disconnect();
				else {
					byte[] bytes = new byte[rand.nextInt(10)];
					rand.nextBytes(bytes);
					String msg = new String(bytes);
					this.trySendMsg(msg);
				}
			} while (command != 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
