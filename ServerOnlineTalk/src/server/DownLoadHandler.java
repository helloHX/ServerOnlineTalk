package server;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import util.WriteByteData;

public class DownLoadHandler extends Thread {

	private WriteByteData filewriter;
	private long fileLength;
	private final CountDownLatch downOverSingal;
	public DownLoadHandler(long fileLength,String path,CountDownLatch downOverSingal) {
		this.downOverSingal = downOverSingal;
		filewriter = new WriteByteData(path);
		this.fileLength = fileLength;
	}

	@Override
	public void run() {
		try {
			ServerSocket fileServer = new ServerSocket(8887);
			System.out.println("文件接受器准备————");
			Socket filesocket = fileServer.accept();
			InputStream dataStream =
					filesocket.getInputStream();
			filewriter.OpenStream();
			System.out.println("fileLength" + fileLength);
			long endlength = fileLength % 1024;
			long priceNum = fileLength / 1024;
			for (int i = 0; i < priceNum; i++) {
				byte[] data = new byte[1024];
				dataStream.read(data);
				System.out.println(1);
				filewriter.SaveData(data, 0, data.length);
			}
			if (endlength != 0) {
				byte[] data = new byte[(int) endlength];
				dataStream.read(data);
				filewriter.SaveData(data, 0, data.length);
			}
			filewriter.CloseStream();
			fileServer.close();
			downOverSingal.countDown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
