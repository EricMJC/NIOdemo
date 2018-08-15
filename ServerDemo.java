package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class ServerDemo {
	private ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
	private ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024);
	private Selector selector;

	public ServerDemo() throws IOException {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.configureBlocking(false);
		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(8080));
		System.out.println("listenig on prot 8080");
		this.selector = Selector.open();

		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	public static void main(String[] args) throws IOException {
		new ServerDemo().go();
	}

	public void go() throws IOException {
		while (selector.select() > 0) {
			Iterator<SelectionKey> iterator = selector.selectedKeys()
					.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionkey = iterator.next();
				iterator.remove();

				// 新连接
				if (selectionkey.isAcceptable()) {
					System.out.println("isaccepetable");
					ServerSocketChannel server = (ServerSocketChannel) selectionkey
							.channel();

					// 新注册
					SocketChannel socketChannel = server.accept();
					if (socketChannel == null) {
						continue;
					}
					socketChannel.configureBlocking(false);
					socketChannel.register(selector, selectionkey.OP_READ
							| selectionkey.OP_WRITE);
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					buffer.put("hello".getBytes());
					buffer.flip();
					socketChannel.write(buffer);
				}
				// 服务端根据client那边的情况进行读取
				if (selectionkey.isReadable()) {
					System.out.println("isReadable");
					SocketChannel socketChannel = (SocketChannel) selectionkey
							.channel();

					readBuffer.clear();
					socketChannel.read(readBuffer);
					readBuffer.flip();

					String receiveData = Charset.forName("utf-8")
							.decode(readBuffer).toString();
					System.out.println("receiveData" + receiveData);

					// bind value to key
					selectionkey.attach("server message echo" + receiveData);
				}

				if (selectionkey.isWritable()) {
					SocketChannel socketChannel = (SocketChannel) selectionkey
							.channel();

					String message = (String) selectionkey.attachment();
					if (message == null) {
						continue;
					}
					selectionkey.attach(null);
					writeBuffer.clear();
					writeBuffer.put(message.getBytes());
					writeBuffer.flip();
					while (writeBuffer.hasRemaining()) {
						socketChannel.write(writeBuffer);

					}
				}
			}
		}
	}
}
