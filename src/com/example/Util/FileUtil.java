package com.example.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.util.Log;

public class FileUtil {
	
	/**@function 保存文件  
	 */  
	public static void save(String fileName,String dirPath,String content,Context context,String charsetName) throws Exception {   
		 if(!fileName.endsWith(".txt")){
			fileName = fileName + ".txt";
		 }   		 	   
		 Log.d("tag", content);
		 byte[] buf = fileName.getBytes(charsetName);      
	     fileName = new String(buf,charsetName);   
	     File file = new File(dirPath);
	     file.mkdirs();
	     File newfile = new File(dirPath + File.separator + fileName);
	     if(file.exists()){
	    	 newfile.createNewFile();
	    	 FileOutputStream fos = new FileOutputStream(newfile);
		     fos.write(content.getBytes());   
		     fos.close();  
		 }
	 }   
	  
	/**@function 读取文件内容  
	 *   
	 */  
	public String read(String fileName,Context context) throws Exception{   
		 if(!fileName.endsWith(".txt")){   
			 fileName = fileName + ".txt";   
		 }   
		 FileInputStream fis = context.openFileInput(fileName);   
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();   	  
		 byte[] buf = new byte[1024];   
		 int len = 0;   
		 while ((len = fis.read(buf)) != -1) {   
			 baos.write(buf, 0, len);   
		 }   
		 fis.close();   
		 baos.close();   
		 return baos.toString();   
	 }   
		  
	/**@function 返回单个目录所有文件夹与txt文件
	 * @param 指定目录
	 * 
	 * @return 指定目录中的排序后的所有文件夹与txt文件
	 *	
	 * */
	public static File[] parseDirectory(File directory){
		if(directory == null||!directory.exists()||!directory.isDirectory()){
			return null;
		}
		File[] temp = directory.listFiles();
		File[] files = new File[temp.length];
		//分类目录与文件
		ArrayList<File> tempFolder=new ArrayList<File>();
    	ArrayList<File> tempFile=new ArrayList<File>();
    	for(int i=0;i<temp.length;i++){
    		File file=temp[i];
    		if(file.isDirectory()){
    			tempFolder.add(file);
    		}else if(file.isFile()){
    			if(file.getName().endsWith(".txt")){
    				tempFile.add(file);
    			}
    		}
    	}
    	//排序
    	Comparator<File> comparator = new MyComparator();
    	Collections.sort(tempFolder, comparator);
    	Collections.sort(tempFile,comparator);
    	
    	System.arraycopy(tempFolder.toArray(), 0, files, 0, tempFolder.size());
    	System.arraycopy(tempFile.toArray(), 0, files, tempFolder.size(), tempFile.size());
		return files;
	}
	
	/**@function 搜索所有txt文件
	 * @param directory 指定目录 
	 * 
	 * @param files 保存txt文件
	 * 
	 * */
	public static void searchFile(File directory,ArrayList<File> files){
		if(directory == null||!directory.exists()||!directory.isDirectory()){
			return;
		}
		File[] temp = directory.listFiles();
		ArrayList<File> tempFolder=new ArrayList<File>();
    	ArrayList<File> tempFile=new ArrayList<File>();
    	for(int i=0;i<temp.length;i++){
    		File file=temp[i];
    		if(file.isDirectory()){
    			tempFolder.add(file);
    		}else if(file.isFile()){
    			if(file.getName().endsWith(".txt")){
    				files.add(file);
    			}
    		}
    	}
    	if(!tempFolder.isEmpty()){
    		for(int i=0;i<tempFolder.size();i++){
    			searchFile(tempFolder.get(i),files);
    		}
    	}
	}
	
	//ANSI： 无格式定义 Unicode：  前两个字节为FFFE Unicode文档以0xFFFE开头 Unicode big endian： 前两字节为FEFF UTF-8： 前两字节为EFBB UTF-8以0xEFBBBF开
	public static String getTxtType(File file) throws IOException {
		// TODO Auto-generated method stub
		InputStream inputStream = new FileInputStream(file);
		byte[] head = new byte[3];  
		String s = new String(head);
        inputStream.read(head);   
        inputStream.close();
        Log.d("tag", "head:"+head[0]+"1:"+head[1]+"2:"+head[2]);
        String code = "";  
        code = "GBK";  
        if (head[0] == -1 && head[1] == -2 )  
            code = "UTF-16";  
        if (head[0] == -2 && head[1] == -1 )  
            code = "Unicode";  
        if(head[0]==-25 && head[1]==-84 && head[2] ==-84)  
            code = "UTF-8";  
		return code;
	}

	
	static class MyComparator implements Comparator<File>{
		@Override
		public int compare(File lhs, File rhs) {
			return lhs.getName().compareTo(rhs.getName());
		}
    	
    }
	
}
