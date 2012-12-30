package com.example.Types;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

import com.example.Util.StringUtil;
import com.example.ddd.BookView;

public class BookInfo {
	
	private File mBookFile = null;//book文件
	private MappedByteBuffer mMappedByteBuffer = null;//book文件内存映射
	private int mBookLong = 0;//文件数据的总大小
	private int mBookTag = 0;//书签
	private int mBookPage = 0;//当前页

	public BookInfo(){;
	}
	
	public boolean openBook(String fileuri) throws FileNotFoundException, IOException{
		mBookFile = new File(fileuri);
		if(mBookFile==null){
			return false;
		}
		mBookLong = (int) mBookFile.length();
		mMappedByteBuffer = new RandomAccessFile(mBookFile, "r").getChannel().map(
				FileChannel.MapMode.READ_ONLY, 0, mBookLong);
		return true;
	}
	
	/**@function 根据b来判断向前翻还是向后翻，对应添加相应的页面信息*/
	public void onPageTurn(PageInfo p1,PageInfo p2,boolean b){
		
	}
	
	/**@function 按照书签打开书*/
	public void onDrawPageByTag(PageInfo p){
		mBookPage = mBookTag;
		onDrawPage(p, mBookTag);
	} 
	
	/**@function 按照指定数据处添加下一页面绘制信息*/
	public void onDrawPage(PageInfo p,int i){
		if(p==null||i<0||i>mBookLong){
			return;
		}
		setPageLine(p,i);
	}
	
	/**@function 按照指定数据处添加上一页面绘制信息*/
	public void onDrawPageBack(PageInfo p,int i){
		if(p==null||i<0||i>mBookLong){
			return;
		}
		setPageLineBack(p,i);
	}
	
	/**@function 按照指定数据处添加上一页信息*/
	protected void setPageLineBack(PageInfo p,int i){
		String str = "";
		int start = i;
		int end = i;
		int lsize = 0;
		String strReturn;
		
		Vector<Vector<String>> line = new Vector<Vector<String>>();
		while(lsize < p.getmLineCount()&&start > 0){
			byte[] lineByte = readBack(p, start);
			Vector<String> lines = new Vector<String>();
			start -= lineByte.length;
			
			try {
				str = new String(lineByte,p.getmCharsetName());
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			str = str.replaceAll("\r\n", "");
			str = str.replaceAll("\n", "");
			
			if(str.length() == 0){
				lines.add(str);
			}
			
			while(str.length() > 0){
				int size = p.breakText(str);
				lines.add(str.substring(0, size));
				str = str.substring(size);
			}
			line.add(lines);
			lsize = 0;
			for(int j = 0;j < line.size(); j++){
				lsize += line.get(j).size();
			}
			
		}
		while(lsize > p.getmLineCount()){
			try {
				Vector<String> l = line.get(line.size()-1);
				start += l.get(0).getBytes(p.getmCharsetName()).length;
				l.remove(0);
				if(l.size()==0)line.remove(line.size()-1);
				lsize = 0;
				for(int j = 0;j < line.size(); j++){
					lsize += line.get(j).size();
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Vector<String> lines = new Vector<String>();
		for(int j = line.size()-1;j >= 0; j--){
			Vector<String> l = line.get(j);
			lines.addAll(l);
		}
		p.setmPageEnd(end);
		p.setmPageBegin(start);
		p.clear();
		p.setMlines(lines);
		p.setmBookLong(mBookLong);
	}
	
	/**@function 按照指定数据处添加下一页信息*/
	protected void setPageLine(PageInfo p,int i){
		String str = "";
		int start = i;
		int end = i;
		String strReturn = "";
		Vector<String> lines = new Vector<String>();
		while(lines.size() < p.getmLineCount()&&end < mBookLong){
			byte[] lineByte = readForward(p,end);
			end += lineByte.length;
			try {
				str = new String(lineByte,p.getmCharsetName());
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String[] s ={"",""};
			s = StringUtil.replaceR(str);
			str = s[0];
			strReturn = s[1];
			if(str.length() == 0){
				lines.add(str);
			}
			while(str.length() > 0){
				int nSize = p.breakText(str);
				lines.add(str.substring(0, nSize));
				str = str.substring(nSize);
				if(lines.size() >= p.getmLineCount()){
					if(str.length() != 0){
						try {
							end -= (str+strReturn).getBytes(p.getmCharsetName()).length;
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					break;
				}
				
			}
			
		}
		p.setmPageEnd(end);//end指向本页最后一个字符的下一位
		p.setmPageBegin(start);
		p.clear();
		p.setMlines(lines);
		p.setmBookLong(mBookLong);
	}
	
	/**@function 按照指定数据处返回一行的byte[]*/
	protected byte[] readForward(PageInfo p,int nFromPos) {
		int nStart = nFromPos;
		int i = nStart;
		byte b0, b1;
		//根据编码格式判断换行
		if (p.getmCharsetName().equals("UTF-16LE")) {
			while (i < mBookLong - 1) {
				b0 = mMappedByteBuffer.get(i++);
				b1 = mMappedByteBuffer.get(i++);
				if (b0 == 0x0a && b1 == 0x00) {
					break;
				}
			}
		} else if (p.getmCharsetName().equals("UTF-16BE")) {
			while (i < mBookLong - 1) {
				b0 = mMappedByteBuffer.get(i++);
				b1 = mMappedByteBuffer.get(i++);
				if (b0 == 0x00 && b1 == 0x0a) {
					break;
				}
			}
		} else {
			while (i < mBookLong) {
				b0 = mMappedByteBuffer.get(i++);
				if (b0 == 0x0a) {
					break;
				}
			}
		}
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = mMappedByteBuffer.get(nFromPos + i);
		}
		return buf;
	}
	
	/**@function 按照指定数据处返回上一行的byte[]*/
	protected byte[] readBack(PageInfo p,int i){
		String str = "";
		int end = i;
		int start = i;
		byte b0, b1;
		if (p.getmCharsetName().equals("UTF-16LE")) {
			i = end - 2;
			while (i > 0) {
				b0 = mMappedByteBuffer.get(i);
				b1 = mMappedByteBuffer.get(i + 1);
				if (b0 == 0x0a && b1 == 0x00 && i != end - 2) {
					i += 2;
					break;
				}
				i--;
			}

		} else if (p.getmCharsetName().equals("UTF-16BE")) {
			i = end - 2;
			while (i > 0) {
				b0 = mMappedByteBuffer.get(i);
				b1 = mMappedByteBuffer.get(i + 1);
				if (b0 == 0x00 && b1 == 0x0a && i != end - 2) {
					i += 2;
					break;
				}
				i--;
			}
		} else {//把i定位在后一个换行符的前一个字符，如果页面结束就定位在头字符
			i = end - 1;
			while (i > 0) {
				b0 = mMappedByteBuffer.get(i);
				if (b0 == 0x0a && i != end - 1) {
					i++;
					break;
				}
				i--;
			}
		}
		int size = end - i;
		int j;
		byte[] buf = new byte[size];
		for(j = 0;j < size; j++){
			buf[j] = mMappedByteBuffer.get(i+j);
		}
		return buf;
	}
	
	/**@function 是否有前一张*/
	public boolean isFirst(){
		return mBookPage > 0;
	}
	
	/**@function 是否有后一张*/
	public boolean isEnd(){
		return mBookPage < mBookLong;
	}
	
	/**@function 翻到下一张*/
	public void gotoNext(PageInfo p,BookView b,boolean tag){
		if(!isEnd())return;
		if(mBookPage != p.getmPageBegin()){
			this.setPageLine(p, mBookPage);
		}
		mBookPage = p.getmPageEnd();
		this.setPageLine(p, mBookPage);
		p.onDraw();
		b.setBitmaps(p, p);
		
	};
	
	/**@function 翻到上一张*/
	public void gotoAfter(PageInfo p,BookView b,boolean tag){
		if(!isFirst())return;
		this.setPageLineBack(p, mBookPage);
		mBookPage = p.getmPageBegin();
		p.onDraw();
		b.setBitmaps(p, p);
		
	}
}
