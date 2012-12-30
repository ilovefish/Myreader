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
	
	private File mBookFile = null;//book�ļ�
	private MappedByteBuffer mMappedByteBuffer = null;//book�ļ��ڴ�ӳ��
	private int mBookLong = 0;//�ļ����ݵ��ܴ�С
	private int mBookTag = 0;//��ǩ
	private int mBookPage = 0;//��ǰҳ

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
	
	/**@function ����b���ж���ǰ��������󷭣���Ӧ�����Ӧ��ҳ����Ϣ*/
	public void onPageTurn(PageInfo p1,PageInfo p2,boolean b){
		
	}
	
	/**@function ������ǩ����*/
	public void onDrawPageByTag(PageInfo p){
		mBookPage = mBookTag;
		onDrawPage(p, mBookTag);
	} 
	
	/**@function ����ָ�����ݴ������һҳ�������Ϣ*/
	public void onDrawPage(PageInfo p,int i){
		if(p==null||i<0||i>mBookLong){
			return;
		}
		setPageLine(p,i);
	}
	
	/**@function ����ָ�����ݴ������һҳ�������Ϣ*/
	public void onDrawPageBack(PageInfo p,int i){
		if(p==null||i<0||i>mBookLong){
			return;
		}
		setPageLineBack(p,i);
	}
	
	/**@function ����ָ�����ݴ������һҳ��Ϣ*/
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
	
	/**@function ����ָ�����ݴ������һҳ��Ϣ*/
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
		p.setmPageEnd(end);//endָ��ҳ���һ���ַ�����һλ
		p.setmPageBegin(start);
		p.clear();
		p.setMlines(lines);
		p.setmBookLong(mBookLong);
	}
	
	/**@function ����ָ�����ݴ�����һ�е�byte[]*/
	protected byte[] readForward(PageInfo p,int nFromPos) {
		int nStart = nFromPos;
		int i = nStart;
		byte b0, b1;
		//���ݱ����ʽ�жϻ���
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
	
	/**@function ����ָ�����ݴ�������һ�е�byte[]*/
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
		} else {//��i��λ�ں�һ�����з���ǰһ���ַ������ҳ������Ͷ�λ��ͷ�ַ�
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
	
	/**@function �Ƿ���ǰһ��*/
	public boolean isFirst(){
		return mBookPage > 0;
	}
	
	/**@function �Ƿ��к�һ��*/
	public boolean isEnd(){
		return mBookPage < mBookLong;
	}
	
	/**@function ������һ��*/
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
	
	/**@function ������һ��*/
	public void gotoAfter(PageInfo p,BookView b,boolean tag){
		if(!isFirst())return;
		this.setPageLineBack(p, mBookPage);
		mBookPage = p.getmPageBegin();
		p.onDraw();
		b.setBitmaps(p, p);
		
	}
}
