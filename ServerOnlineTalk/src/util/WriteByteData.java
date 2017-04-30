package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class WriteByteData {
	private FileOutputStream outPut;
	private File file;
	public WriteByteData(){
	}
	public WriteByteData(String fileName){
		this.file = new File(fileName);
	}
	public void setFileName(String fileName){
		System.out.println("Server_setFileName" + fileName);
		this.file = new File(fileName);
	}
	
	public void OpenStream(){
		try {
			if(!file.exists()){
				createFile(file);
			}
			outPut = new FileOutputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void SaveData(byte[] data,int off,int dataLength){
		try {
			System.out.println("Server_SaveData" + dataLength);
			outPut.write(data, off, dataLength);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void CloseStream(){
		try {
			outPut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized void createFile(File file) {
		try {
			if (!file.getParentFile().exists()) {
				if (file.getParentFile().mkdirs()) {
					if (file.createNewFile()) {
						System.out.println("Writer_createFile"
								+ file.getName() + "创建成功");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
