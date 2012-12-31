package com.example.Types;

import java.text.DecimalFormat;
import java.util.Vector;

import com.example.ddd.R;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

public class PageInfo {
	
	private Bitmap mBitmap;//ҳ��
	private Canvas mCanvas;//���ڻ���ҳ��
	private Paint mPaint;//����
	private int mWidth;//page��
	private int mHeight;//page��
	private int mMarginWidth = 15; // ���ұ�Ե�ľ���
	private int mMarginHeight = 20; // ���±�Ե�ľ���
	private int mFontMarginWidth = 1;//���ּ����
	private int mFontMarginHeight = 1;//�м��
	private String mCharsetName = "GBK";//���ֱ���
	private Align mAlign = Align.LEFT;//���ֶ��뷽ʽ
	private int mFontSize = 20;//���ִ�С
	private int mTextColor = Color.BLACK;//������ɫ
	private float mVisibleHeight;// �������ݵĸ�
	private float mVisibleWidth;// �������ݵĿ�
	private int mLineCount; // ÿҳ������ʾ������
	private int mPageBegin;//ҳ�濪ʼ���ֽ�
	private int mPageEnd;//ҳ���β���ֽ�
	private int mBookLong;//������ĳ���
	private Vector<String> mlines = new Vector<String>();//���浱ǰҳ���е�string
	private int mBackColor = 0xffff9e85; // ������ɫ
	private Bitmap mBookBg = null;//ҳ�汳��ͼƬ
		
	public PageInfo(int w,int h,Context context){
		mWidth = w;
		mHeight = h;
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//�����
		mPaint.setTextAlign(mAlign);
		mPaint.setTextSize(mFontSize);
		mPaint.setColor(mTextColor);
		mVisibleWidth = mWidth - mMarginWidth * 2;
		mVisibleHeight = mHeight - mMarginHeight * 2;
		mLineCount = (int) (mVisibleHeight/(mFontSize+mFontMarginHeight));
	}
	
	/**@function �жϸ�ҳ����ʵ���ܰ��ݶ���str*/
	public int breakText(String str){
		return mPaint.breakText(str, true, mVisibleWidth, null);
	}
	
	/**@function ����ַ������*/
	public float getTextWidths(String str){
		float[] f = new float[str.length()];;
		mPaint.getTextWidths(str, f);
		return f[0];
	}

	/**@function ���Ƹ�ҳ��*/
	public void onDraw(){
		if (mBookBg == null){
			mCanvas.drawColor(mBackColor);
		}else{
			mCanvas.drawBitmap(mBookBg, 0, 0, null);	
		}
		onDrawFont();
		onDrawProgress();
	}
	
	/**@function ��������*/
	private void onDrawFont(){
		int y = mMarginHeight;
		if(mlines.size()>0){
			for(String str : mlines){
				y += mFontSize + mFontMarginHeight;
				mCanvas.drawText(str, mMarginWidth, y, mPaint);
			}
		}
	}
	
	/**@function ������ʾ���ȵİٷֱ�*/
	private void onDrawProgress(){
		if(mBookLong!=0){
			mPaint.setTextSize(12);
			float fPercent = (float) (mPageBegin * 1.0 / mBookLong);//Ŀǰҳ
			DecimalFormat df = new DecimalFormat("#0.0");
			String strPercent = df.format(fPercent * 100) + "%";//С�����һλ���ַ���
			int nPercentWidth = (int) mPaint.measureText("999.9%") + 1;//���ؽ����ַ����Ŀ��
			mCanvas.drawText(strPercent, mWidth - nPercentWidth, mHeight - 5, mPaint);//��canvas��д��
			mPaint.setTextSize(mFontSize);
		}
	}
	
	/**@function ����xy�����귵�ض�Ӧ���ַ�������*/
	public FontRectInfo getRectByXY(int x,int y){
		FontRectInfo f = new FontRectInfo();
 		int lineNum = 0;
		int lineD = mMarginHeight;
		int fontD = mMarginWidth;
		String s = "";
		if(mlines!=null){
			lineNum = mlines.size();
			for(int i = 0;i < lineNum; i++){
				lineD += mFontSize + mFontMarginHeight;
				if(y <= lineD){
					lineNum = i;
					f.setLine(lineNum);
					s = mlines.get(lineNum);
					break;
				}else{
					if(i == lineNum - 1){
						lineNum = i;
						f.setLine(lineNum);
						s = mlines.get(lineNum);
					}
				}
			}
			
			if(!s.equals("")){
				for(int i = 0;i < s.length(); i++){
					fontD += getTextWidths(s.substring(i,i+1));
					if(x <= fontD){
						f.setIndex(i);
						break;
					}else{
						if(i == s.length()-1){
							f.setIndex(i);
						}
					}	
				}
			}
			return f;
		}
		return null;
	}
	
	/**@funtion ͨ��ָ�����ַ�λ�÷�����Ҫ���Ƶ�rect[]*/
	public Rect[] returnPickFont(FontRectInfo f,FontRectInfo t){
		FontRectInfo fromRect = null;
		FontRectInfo toRect = null;
		if(t.getLine() > f.getLine()){
			fromRect = f;
			toRect = t;
		}else if(t.getLine() == f.getLine()){
			if(t.getIndex() >= f.getIndex()){
				fromRect = f;
				toRect = t;
			}else{
				fromRect = t;
				toRect = f;
			}
		}else{
			fromRect = t;
			toRect = f;
		}
		
		int num = toRect.getLine() - fromRect.getLine() + 1;
		Rect[] rect = new Rect[num];
		for(int i = 0;i < num; i++){
			rect[i] = new Rect();
			if(num == 1){
				Rect from = returnFontRect(fromRect.getLine(),fromRect.getIndex());
				Rect to = returnFontRect(toRect.getLine(), toRect.getIndex());
				rect[0].left = from.left;
				rect[0].right = to.right;
				rect[0].top = from.top;
				rect[0].bottom = from.bottom;
			}else{
				if(i == 0){
					Rect from = returnFontRect(fromRect.getLine(),fromRect.getIndex());
					String s = mlines.get(fromRect.getLine());
					Rect to = returnFontRect(fromRect.getLine(), s.length() - 1);
					rect[i].left = from.left;
					rect[i].right = to.right;
					rect[i].top = from.top;
					rect[i].bottom = from.bottom;
				}else if(i == num-1){
					Rect to = returnFontRect(toRect.getLine(),toRect.getIndex());
					Rect from = returnFontRect(fromRect.getLine(), 0);
					rect[i].left = from.left;
					rect[i].right = to.right;
					rect[i].top = to.top;
					rect[i].bottom = to.bottom;
				}else{
					Rect from = returnFontRect(fromRect.getLine() + i, 0);
					String s = mlines.get(fromRect.getLine() + i);
					Rect to = returnFontRect(fromRect.getLine() + i,s.length() - 1);
					rect[i].left = from.left;
					rect[i].right = to.right;
					rect[i].top = to.top;
					rect[i].bottom = to.bottom;
				}
			}
		}
		return rect;
	}
	
	/**@function ���ص�������rect*/
	public Rect returnFontRect(int line,int index){
		Rect rect = new Rect();
		rect.bottom = mMarginHeight + (mFontSize + mFontMarginHeight)*(line + 1) + 3;
		rect.top = rect.bottom - (mFontSize + mFontMarginHeight - 1);
		rect.right = mMarginWidth;
		String s = mlines.get(line);
		for(int i = 0;i < index + 1; i++){
			rect.right += getTextWidths(s.substring(i,i+1));
			rect.left = (int) (rect.right - getTextWidths(s.substring(i,i+1)));
		}
		return rect;
	}
	
	/**@function ����ѡ������*/
	public String returnString(FontRectInfo f,FontRectInfo t){
		FontRectInfo fromRect = null;
		FontRectInfo toRect = null;
		if(t.getLine() > f.getLine()){
			fromRect = f;
			toRect = t;
		}else if(t.getLine() == f.getLine()){
			if(t.getIndex() >= f.getIndex()){
				fromRect = f;
				toRect = t;
			}else{
				fromRect = t;
				toRect = f;
			}
		}else{
			fromRect = t;
			toRect = f;
		}
		String s = new String();
		int num = toRect.getLine() - fromRect.getLine() + 1;
		for(int i = 0;i < num; i++){
			if(num == 1){
				String pro = mlines.get(fromRect.getLine());
				s += pro.substring(fromRect.getIndex(), toRect.getIndex()+1);
			}else{
				if(i == 0){
					String pro = mlines.get(fromRect.getLine());
					s += pro.substring(fromRect.getIndex(), pro.length());
				}else if(i == num - 1){
					String pro = mlines.get(toRect.getLine());
					s += pro.substring(0, toRect.getIndex()+1);
				}else{
					String pro = mlines.get(fromRect.getLine()+i);
					s += pro.substring(0, pro.length());
				}	
			}
		}
		return s;	
	}
	
	/**@function ����ҳline*/
	public void clear(){
		mlines.removeAllElements();
	}
	
	public int getmLineCount() {
		return mLineCount;
	}

	public String getmCharsetName() {
		return mCharsetName;
	}

	public void setmCharsetName(String mCharsetName) {
		this.mCharsetName = mCharsetName;
	}

	public int getmPageBegin() {
		return mPageBegin;
	}

	public void setmPageBegin(int mPageBegin) {
		this.mPageBegin = mPageBegin;
	}

	public int getmPageEnd() {
		return mPageEnd;
	}

	public void setmPageEnd(int mPageEnd) {
		this.mPageEnd = mPageEnd;
	}

	public Vector<String> getMlines() {
		return mlines;
	}

	public void setMlines(Vector<String> mlines) {
		this.mlines = mlines;
	}

	public int getmBookLong() {
		return mBookLong;
	}

	public void setmBookLong(int mBookLong) {
		this.mBookLong = mBookLong;
	}
	
	public Bitmap getmBitmap() {
		return mBitmap;
	}
}
