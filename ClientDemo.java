package NIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class ClientDemo {
	private final ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
	private final ByteBuffer receivebuffer = ByteBuffer.allocate(1024);
	private Selector selector;

	public ClientDemo() throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(InetAddress.getLocalHost(),
				8080));
		socketChannel.configureBlocking(false);
		System.out.println("与服务器建立链接");
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_READ
				| SelectionKey.OP_WRITE);
	}

	public static void main(String[] args) {
	//final ClientDemo client= new ClientDemo();
	//Thread receive = new Thread(receiveFromUser);
	//receive.start();
	//client.talk();
}

	public void talk() throws IOException {
		while (selector.select()>0) {
			Iterator<SelectionKey> it =selector.selectedKeys().iterator();
			while (it.hasNext()) {
				SelectionKey key = it.next();
				it.remove();
				if (key.isReadable()) {
					receive(key);
				}
				if (key.isWritable()) {
					send(key);
				}
			}
		}
	}

	public void send(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		synchronized (sendBuffer) {
			sendBuffer.flip();
			while (sendBuffer.hasRemaining()) {
				socketChannel.write(sendBuffer);
			}
			sendBuffer.compact();
		}
	}

	public void receive(SelectionKey key) throws IOException {
		SocketChannel socketchannel = (SocketChannel) key.channel();
		socketchannel.read(receivebuffer);
		receivebuffer.flip();
		String receiveData = Charset.forName("utf-8").decode(receivebuffer).toString();
		System.out.println("receive sercever message"+receiveData);
		receivebuffer.clear();
	}

	public void receiveFromUser() {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String msg;
		try {
			while ((msg=bufferedReader.readLine())!= null) {
				synchronized (sendBuffer) {
					sendBuffer.put((msg+"\r\n").getBytes());
					
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
