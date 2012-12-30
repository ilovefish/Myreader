package com.example.ddd;

import com.example.Types.PageInfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BookView extends View {

	private PageInfo mCurrentPageInfo;//���浱ǰҳ
	private PageInfo mNextPageInfo;//������һҳ
	private Paint mPaint;//����
	private int mRectColor = Color.WHITE;//ѡ������������ɫ
	private Rect[] mRect = null;//����������Rect
	private int mWidth;//View��
	private int mHeight;//View��
	private int state;//״̬ 0�������ҳ 1����קѡ������
	
	public BookView(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}
	
	public BookView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
		// TODO Auto-generated constructor stub
	}

	public BookView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public void init(){
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//�����
		mPaint.setColor(mRectColor);
		mPaint.setAlpha(100);
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setBitmaps(PageInfo p1, PageInfo p2) {
		mCurrentPageInfo = p1;
		mNextPageInfo = p2;
		postInvalidate();
	}
	
	protected void onDraw(Canvas canvas){
		canvas.drawColor(0xFFAAAAAA);
		canvas.save();
		canvas.drawBitmap(mCurrentPageInfo.getmBitmap(), 0, 0, null);
		onDrawRect(canvas);
		canvas.restore();
		
	}
	
	/**@function ����View��С*/
	public void setScreen(int w, int h) {
		mWidth = w;
		mHeight = h;
	}
	
	/**@function false����ȥ��һҳ��ture����ȥ��һҳ*/
	public boolean getDirection(int x){
		return x > mWidth/2;
	}
	
	/**@function ���»���ѡ�����ֵ�ͼ���rect*/
	public void createRect(Rect[] rect){
		mRect = rect;
		invalidate();
	}
	
	/**@function ���������ֵľ���*/
	private void onDrawRect(Canvas canvas){
		if(state == 1&&mRect!=null){
			for(int i = 0;i < mRect.length; i++){
				canvas.drawRect(mRect[i], mPaint);
			}
		}
	}
}
