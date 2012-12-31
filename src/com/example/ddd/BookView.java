package com.example.ddd;

import com.example.Types.PageInfo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Scroller;

public class BookView extends View {

	private PageInfo mCurrentPageInfo;//保存当前页
	private PageInfo mNextPageInfo;//保存下一页
	private Paint mPaintFont;//画笔
	private Paint mPaint;//画笔
	private int mRectColor = Color.WHITE;//选中文字遮拦颜色
	private Rect[] mRect = null;//文字遮拦的Rect
	private int mWidth;//View宽
	private int mHeight;//View高
	private int state = 0;//状态 0：点击换页 1：拖拽选中文字
	private PointF mTouch = new PointF(); // 拖拽点
	private PointF mBezierStart1 = new PointF(); //cxy
	private PointF mBezierControl1 = new PointF(); //exy
	private PointF mBeziervertex1 = new PointF(); //dxy
	private PointF mBezierEnd1 = new PointF(); //bxy
	private PointF mBezierStart2 = new PointF(); //jxy
	private PointF mBezierControl2 = new PointF();//hxy
	private PointF mBeziervertex2 = new PointF();//ixy
	private PointF mBezierEnd2 = new PointF();//kxy
	private float mMiddleX;//gx
	private float mMiddleY;//gy
	private int mCornerX = 0; // 拖拽点对应的页脚x
	private int mCornerY = 0; // 拖拽点对应的页脚y
	private Path mPath0;
	private Path mPath1;
	private float mDegrees;
	private float mTouchToCornerDis;//拖拽点与页脚的距离
	private ColorMatrixColorFilter mColorMatrixFilter;//颜色过滤
	private boolean mIsRTandLB; // 是否属于右上左下
	private GradientDrawable mBackShadowDrawableLR;
	private GradientDrawable mBackShadowDrawableRL;
	private GradientDrawable mFolderShadowDrawableLR;
	private GradientDrawable mFolderShadowDrawableRL;
	private GradientDrawable mFrontShadowDrawableHBT;
	private GradientDrawable mFrontShadowDrawableHTB;
	private GradientDrawable mFrontShadowDrawableVLR;
	private GradientDrawable mFrontShadowDrawableVRL;
	private Scroller mScroller;;
	private float mMaxLength = (float) Math.hypot(mWidth, mHeight);
	private Matrix mMatrix;
	private float[] mMatrixArray = { 0, 0, 0, 0, 0, 0, 0, 0, 1.0f };
	int[] mBackShadowColors;
	int[] mFrontShadowColors;
	private boolean mTurnType = false;
	
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
		mPaintFont = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
		mPaintFont.setColor(mRectColor);
		mPaintFont.setAlpha(100);
		mPath0 = new Path();
		mPath1 = new Path();
		createDrawable();

		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);//实心

		ColorMatrix cm = new ColorMatrix();
		float array[] = { 0.55f, 0, 0, 0, 80.0f, 0, 0.55f, 0, 0, 80.0f, 0, 0,
				0.55f, 0, 80.0f, 0, 0, 0, 0.2f, 0 };
		cm.set(array);
		mColorMatrixFilter = new ColorMatrixColorFilter(cm);
		mMatrix = new Matrix();
		mScroller = new Scroller(getContext());

		mTouch.x = 0.01f; // 不让x,y为0,否则在点计算时会有问题
		mTouch.y = 0.01f;
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setmTouch(PointF mTouch) {
		this.mTouch = mTouch;
	}
	
	public void setBitmaps(PageInfo p1, PageInfo p2) {
		mCurrentPageInfo = p1;
		mNextPageInfo = p2;
		postInvalidate();
	}
	
	protected void onDraw(Canvas canvas){
		
		onDrawRect(canvas);
		if(mTurnType){
			onVerticalDraw(canvas);
		}else{
			onRoleDraw(canvas);
		}
	}
	
	/**@function 设置View大小*/
	public void setScreen(int w, int h) {
		mWidth = w;
		mHeight = h;
		mMaxLength = (float) Math.hypot(mWidth, mHeight);
		setTurnType(false);
	}
	
	public void setTurnType(boolean type){
		if(type){
			mTouch.x = mWidth;
			mTouch.y = mHeight;
			mTurnType = type;
		}else{
			mTouch.x = 0;
			mTouch.y = 0;
			mTurnType = type;
		}
	}
	
	/**@function false代表去上一页，ture代表去下一页*/
	public boolean getDirection(int x){
		return x > mWidth/2;
	}
	
	/**@function 更新绘制选中文字的图层的rect*/
	public void createRect(Rect[] rect){
		mRect = rect;
		invalidate();
	}
	
	/**@function 画遮罩文字的矩形*/
	private void onDrawRect(Canvas canvas){
		if(state == 1&&mRect!=null){
			for(int i = 0;i < mRect.length; i++){
				canvas.drawRect(mRect[i], mPaint);
			}
		}
	}
	
	/**@function 裁剪当前页图片贴到canvas*/
	private void drawCurrentPageArea(Canvas canvas, Bitmap bitmap, Path path) {
		mPath0.reset();
		mPath0.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath0.quadTo(mBezierControl1.x, mBezierControl1.y, mBezierEnd1.x,
				mBezierEnd1.y);
		mPath0.lineTo(mTouch.x, mTouch.y);
		mPath0.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath0.quadTo(mBezierControl2.x, mBezierControl2.y, mBezierStart2.x,
				mBezierStart2.y);
		mPath0.lineTo(mCornerX, mCornerY);
		mPath0.close();

		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}
	
	private void drawCurrentPageAreaVertical(Canvas canvas, Bitmap bitmap) {
		mPath0.reset();
		int i = (int) ((mWidth - mTouch.x)/2.5);
		if(i < 0){
			i = 0;
		}
		mPath0.lineTo(mTouch.x + i, 0);
		mPath0.lineTo(mTouch.x + i, mHeight);
		mPath0.lineTo(0, mHeight);
		mPath0.lineTo(0, 0);
		mPath0.close();

		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();
	}
	
	/**@function 裁剪下一页图片贴到canvas*/
	private void drawNextPageAreaAndShadow(Canvas canvas, Bitmap bitmap) {
		mPath1.reset();
		mPath1.moveTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.lineTo(mCornerX, mCornerY);
		mPath1.close();

		mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
				- mCornerX, mBezierControl2.y - mCornerY));
		int leftx;
		int rightx;
		GradientDrawable mBackShadowDrawable;
		if (mIsRTandLB) {
			leftx = (int) (mBezierStart1.x);
			rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
			mBackShadowDrawable = mBackShadowDrawableLR;
		} else {
			leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
			rightx = (int) mBezierStart1.x;
			mBackShadowDrawable = mBackShadowDrawableRL;
		}
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
		mBackShadowDrawable.setBounds(leftx, (int) mBezierStart1.y, rightx,
				(int) (mMaxLength + mBezierStart1.y));
		mBackShadowDrawable.draw(canvas);
		canvas.restore();
	}
	
	/**@function 裁剪下一页图片贴到canvas*/
	private void drawNextPageAreaAndShadowVertical(Canvas canvas, Bitmap bitmap) {
		mPath1.reset();
		mPath1.moveTo(0, 0);
		mPath1.lineTo(mTouch.x, 0);
		mPath1.lineTo(mTouch.x, mHeight);
		mPath1.lineTo(0, mHeight);
		mPath1.lineTo(0, 0);
		mPath1.close();

//		mDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl1.x
//				- mCornerX, mBezierControl2.y - mCornerY));
		int i = (int) ((mWidth - mTouch.x)/2.5);
		if(i < 0){
			i = 0;
		}
		int leftx = (int) (mTouch.x + (i));
		if(leftx - mTouch.x < 20){
			leftx -= (i);
		}else{
			leftx -= 20;
		}
		int rightx = leftx + 40;
		GradientDrawable mBackShadowDrawable;
//		if (mIsRTandLB) {
//			leftx = (int) (mBezierStart1.x);
//			rightx = (int) (mBezierStart1.x + mTouchToCornerDis / 4);
			mBackShadowDrawable = mBackShadowDrawableLR;
//		} else {
//			leftx = (int) (mBezierStart1.x - mTouchToCornerDis / 4);
//			rightx = (int) mBezierStart1.x;
//			mBackShadowDrawable = mBackShadowDrawableRL;
//		}
		canvas.save();
		mBackShadowDrawable.setBounds(leftx, 0, rightx,mHeight);
		mBackShadowDrawable.draw(canvas);
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		canvas.drawBitmap(bitmap, 0, 0, null);
		
		canvas.restore();
	}
	
	/**@function 绘制翻起页的阴影
	 * Author : hmg25 Version: 1.0 Description : 绘制翻起页的阴影
	 */
	public void drawCurrentPageShadow(Canvas canvas) {
		double degree;
		if (mIsRTandLB) {
			degree = Math.PI
					/ 4
					- Math.atan2(mBezierControl1.y - mTouch.y, mTouch.x
							- mBezierControl1.x);
		} else {
			degree = Math.PI
					/ 4
					- Math.atan2(mTouch.y - mBezierControl1.y, mTouch.x
							- mBezierControl1.x);
		}
		// 翻起页阴影顶点与touch点的距离
		double d1 = (float) 25 * 1.414 * Math.cos(degree);
		double d2 = (float) 25 * 1.414 * Math.sin(degree);
		float x = (float) (mTouch.x + d1);
		float y;
		if (mIsRTandLB) {
			y = (float) (mTouch.y + d2);
		} else {
			y = (float) (mTouch.y - d2);
		}
		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl1.x, mBezierControl1.y);
		mPath1.lineTo(mBezierStart1.x, mBezierStart1.y);
		mPath1.close();
		float rotateDegrees;
		canvas.save();

		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		int leftx;
		int rightx;
		GradientDrawable mCurrentPageShadow;
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl1.x);
			rightx = (int) mBezierControl1.x + 25;
			mCurrentPageShadow = mFrontShadowDrawableVLR;
		} else {
			leftx = (int) (mBezierControl1.x - 25);
			rightx = (int) mBezierControl1.x + 1;
			mCurrentPageShadow = mFrontShadowDrawableVRL;
		}

		rotateDegrees = (float) Math.toDegrees(Math.atan2(mTouch.x
				- mBezierControl1.x, mBezierControl1.y - mTouch.y));
		canvas.rotate(rotateDegrees, mBezierControl1.x, mBezierControl1.y);
		mCurrentPageShadow.setBounds(leftx,
				(int) (mBezierControl1.y - mMaxLength), rightx,
				(int) (mBezierControl1.y));
		mCurrentPageShadow.draw(canvas);
		canvas.restore();

		mPath1.reset();
		mPath1.moveTo(x, y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierControl2.x, mBezierControl2.y);
		mPath1.lineTo(mBezierStart2.x, mBezierStart2.y);
		mPath1.close();
		canvas.save();
		canvas.clipPath(mPath0, Region.Op.XOR);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);
		if (mIsRTandLB) {
			leftx = (int) (mBezierControl2.y);
			rightx = (int) (mBezierControl2.y + 25);
			mCurrentPageShadow = mFrontShadowDrawableHTB;
		} else {
			leftx = (int) (mBezierControl2.y - 25);
			rightx = (int) (mBezierControl2.y + 1);
			mCurrentPageShadow = mFrontShadowDrawableHBT;
		}
		rotateDegrees = (float) Math.toDegrees(Math.atan2(mBezierControl2.y
				- mTouch.y, mBezierControl2.x - mTouch.x));
		canvas.rotate(rotateDegrees, mBezierControl2.x, mBezierControl2.y);
		float temp;
		if (mBezierControl2.y < 0)
			temp = mBezierControl2.y - mHeight;
		else
			temp = mBezierControl2.y;

		int hmg = (int) Math.hypot(mBezierControl2.x, temp);
		if (hmg > mMaxLength)
			mCurrentPageShadow
					.setBounds((int) (mBezierControl2.x - 25) - hmg, leftx,
							(int) (mBezierControl2.x + mMaxLength) - hmg,
							rightx);
		else
			mCurrentPageShadow.setBounds(
					(int) (mBezierControl2.x - mMaxLength), leftx,
					(int) (mBezierControl2.x), rightx);

		// Log.i("hmg", "mBezierControl2.x   " + mBezierControl2.x
		// + "  mBezierControl2.y  " + mBezierControl2.y);
		mCurrentPageShadow.draw(canvas);
		canvas.restore();
	}
	
	/**@function 绘制翻起页背面
	 * Author : hmg25 Version: 1.0 Description : 绘制翻起页背面
	 */
	private void drawCurrentBackArea(Canvas canvas, Bitmap bitmap) {
		int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
		float f1 = Math.abs(i - mBezierControl1.x);
		int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
		float f2 = Math.abs(i1 - mBezierControl2.y);
		float f3 = Math.min(f1, f2);
		mPath1.reset();
		mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath1.close();
		GradientDrawable mFolderShadowDrawable;
		int left;
		int right;
		if (mIsRTandLB) {
			left = (int) (mBezierStart1.x - 1);
			right = (int) (mBezierStart1.x + f3 + 1);
			mFolderShadowDrawable = mFolderShadowDrawableLR;
		} else {
			left = (int) (mBezierStart1.x - f3 - 1);
			right = (int) (mBezierStart1.x + 1);
			mFolderShadowDrawable = mFolderShadowDrawableRL;
		}
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);

		mPaint.setColorFilter(mColorMatrixFilter);

		float dis = (float) Math.hypot(mCornerX - mBezierControl1.x,
				mBezierControl2.y - mCornerY);
		float f8 = (mCornerX - mBezierControl1.x) / dis;
		float f9 = (mBezierControl2.y - mCornerY) / dis;

		mMatrix.reset();
		mMatrixArray[0] = 1 - 2 * f9 * f9;
		mMatrixArray[1] = 2 * f8 * f9;
		mMatrixArray[3] = mMatrixArray[1];
		mMatrixArray[4] = 1 - 2 * f8 * f8;
		mMatrix.reset();
		mMatrix.setValues(mMatrixArray);
		mMatrix.preTranslate(0, 0);
		mMatrix.postTranslate(0, 0);
		//canvas.drawBitmap(bitmap, mMatrix, mPaint);
		canvas.drawBitmap(bitmap, mMatrix, null);
		mPaint.setColorFilter(null);
//		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
//		mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right,
//				(int) (mBezierStart1.y + mMaxLength));
//		mFolderShadowDrawable.draw(canvas);
		canvas.restore();
	}
	
	private void drawCurrentBackAreaVertical(Canvas canvas, Bitmap bitmap) {
		int i = (int) (mBezierStart1.x + mBezierControl1.x) / 2;
		float f1 = Math.abs(i - mBezierControl1.x);
		int i1 = (int) (mBezierStart2.y + mBezierControl2.y) / 2;
		float f2 = Math.abs(i1 - mBezierControl2.y);
		float f3 = Math.min(f1, f2);
		mPath1.reset();
		mPath1.moveTo(mBeziervertex2.x, mBeziervertex2.y);
		mPath1.lineTo(mBeziervertex1.x, mBeziervertex1.y);
		mPath1.lineTo(mBezierEnd1.x, mBezierEnd1.y);
		mPath1.lineTo(mTouch.x, mTouch.y);
		mPath1.lineTo(mBezierEnd2.x, mBezierEnd2.y);
		mPath1.close();
		GradientDrawable mFolderShadowDrawable;
		int left;
		int right;
		if (mIsRTandLB) {
			left = (int) (mBezierStart1.x - 1);
			right = (int) (mBezierStart1.x + f3 + 1);
			mFolderShadowDrawable = mFolderShadowDrawableLR;
		} else {
			left = (int) (mBezierStart1.x - f3 - 1);
			right = (int) (mBezierStart1.x + 1);
			mFolderShadowDrawable = mFolderShadowDrawableRL;
		}
		canvas.save();
		canvas.clipPath(mPath0);
		canvas.clipPath(mPath1, Region.Op.INTERSECT);

		mPaint.setColorFilter(mColorMatrixFilter);

		float dis = (float) Math.hypot(mCornerX - mBezierControl1.x,
				mBezierControl2.y - mCornerY);
		float f8 = (mCornerX - mBezierControl1.x) / dis;
		float f9 = (mBezierControl2.y - mCornerY) / dis;

		mMatrix.reset();
		mMatrixArray[0] = 1 - 2 * f9 * f9;
		mMatrixArray[1] = 2 * f8 * f9;
		mMatrixArray[3] = mMatrixArray[1];
		mMatrixArray[4] = 1 - 2 * f8 * f8;
		mMatrix.reset();
		mMatrix.setValues(mMatrixArray);
		mMatrix.preTranslate(0, 0);
		mMatrix.postTranslate(0, 0);
		//canvas.drawBitmap(bitmap, mMatrix, mPaint);
		canvas.drawBitmap(bitmap, mMatrix, null);
		mPaint.setColorFilter(null);
//		canvas.rotate(mDegrees, mBezierStart1.x, mBezierStart1.y);
//		mFolderShadowDrawable.setBounds(left, (int) mBezierStart1.y, right,
//				(int) (mBezierStart1.y + mMaxLength));
//		mFolderShadowDrawable.draw(canvas);
		canvas.restore();
	}
	
	/**@function 根据触点和页脚坐标求出需要的坐标点*/
	private void calcPoints() {
		Log.d("TAG", "mTouch.x:"+mTouch.x +"mTouch.y:"+mTouch.y +"mCornerX:"+mCornerX +"mCornerY:"+mCornerY);
		mMiddleX = (mTouch.x + mCornerX) / 2;//gx
		mMiddleY = (mTouch.y + mCornerY) / 2;//gy
		mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
				* (mCornerY - mMiddleY) / (mCornerX - mMiddleX);//ex
		mBezierControl1.y = mCornerY;//ey
		mBezierControl2.x = mCornerX;//hx
		mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
				* (mCornerX - mMiddleX) / (mCornerY - mMiddleY);//hy

		// Log.i("hmg", "mTouchX  " + mTouch.x + "  mTouchY  " + mTouch.y);
		// Log.i("hmg", "mBezierControl1.x  " + mBezierControl1.x
		// + "  mBezierControl1.y  " + mBezierControl1.y);
		// Log.i("hmg", "mBezierControl2.x  " + mBezierControl2.x
		// + "  mBezierControl2.y  " + mBezierControl2.y);

		mBezierStart1.x = mBezierControl1.x - (mCornerX - mBezierControl1.x)
				/ 2;//cx
		mBezierStart1.y = mCornerY;//cy

		// 当mBezierStart1.x < 0或者mBezierStart1.x > 480时
		// 如果继续翻页，会出现BUG故在此限制
		if (mTouch.x > 0 && mTouch.x < mWidth) {
			if (mBezierStart1.x < 0 || mBezierStart1.x > mWidth) {
				if (mBezierStart1.x < 0)
					mBezierStart1.x = mWidth - mBezierStart1.x;

				float f1 = Math.abs(mCornerX - mTouch.x);
				float f2 = mWidth * f1 / mBezierStart1.x;
				mTouch.x = Math.abs(mCornerX - f2);

				float f3 = Math.abs(mCornerX - mTouch.x)
						* Math.abs(mCornerY - mTouch.y) / f1;
				mTouch.y = Math.abs(mCornerY - f3);

				mMiddleX = (mTouch.x + mCornerX) / 2;
				mMiddleY = (mTouch.y + mCornerY) / 2;

				mBezierControl1.x = mMiddleX - (mCornerY - mMiddleY)
						* (mCornerY - mMiddleY) / (mCornerX - mMiddleX);
				mBezierControl1.y = mCornerY;

				mBezierControl2.x = mCornerX;
				mBezierControl2.y = mMiddleY - (mCornerX - mMiddleX)
						* (mCornerX - mMiddleX) / (mCornerY - mMiddleY);
				// Log.i("hmg", "mTouchX --> " + mTouch.x + "  mTouchY-->  "
				// + mTouch.y);
				// Log.i("hmg", "mBezierControl1.x--  " + mBezierControl1.x
				// + "  mBezierControl1.y -- " + mBezierControl1.y);
				// Log.i("hmg", "mBezierControl2.x -- " + mBezierControl2.x
				// + "  mBezierControl2.y -- " + mBezierControl2.y);
				mBezierStart1.x = mBezierControl1.x
						- (mCornerX - mBezierControl1.x) / 2;
			}
		}
		mBezierStart2.x = mCornerX;//jx
		mBezierStart2.y = mBezierControl2.y - (mCornerY - mBezierControl2.y)
				/ 2;//jy

		mTouchToCornerDis = (float) Math.hypot((mTouch.x - mCornerX),
				(mTouch.y - mCornerY));

		mBezierEnd1 = getCross(mTouch, mBezierControl1, mBezierStart1,
				mBezierStart2);//bxy
		mBezierEnd2 = getCross(mTouch, mBezierControl2, mBezierStart1,
				mBezierStart2);//kxy

		// Log.i("hmg", "mBezierEnd1.x  " + mBezierEnd1.x + "  mBezierEnd1.y  "
		// + mBezierEnd1.y);
		// Log.i("hmg", "mBezierEnd2.x  " + mBezierEnd2.x + "  mBezierEnd2.y  "
		// + mBezierEnd2.y);

		/*
		 * mBeziervertex1.x 推导
		 * ((mBezierStart1.x+mBezierEnd1.x)/2+mBezierControl1.x)/2 化简等价于
		 * (mBezierStart1.x+ 2*mBezierControl1.x+mBezierEnd1.x) / 4
		 */
		mBeziervertex1.x = (mBezierStart1.x + 2 * mBezierControl1.x + mBezierEnd1.x) / 4;//dx
		mBeziervertex1.y = (2 * mBezierControl1.y + mBezierStart1.y + mBezierEnd1.y) / 4;//dy
		mBeziervertex2.x = (mBezierStart2.x + 2 * mBezierControl2.x + mBezierEnd2.x) / 4;//ix
		mBeziervertex2.y = (2 * mBezierControl2.y + mBezierStart2.y + mBezierEnd2.y) / 4;//iy
	}
	
	/**@function 已知4点，求每两点组成直线的交点
	 *
	 */
	public PointF getCross(PointF P1, PointF P2, PointF P3, PointF P4) {
		PointF CrossP = new PointF();
		// 二元函数通式： y=ax+b
		float a1 = (P2.y - P1.y) / (P2.x - P1.x);
		float b1 = ((P1.x * P2.y) - (P2.x * P1.y)) / (P1.x - P2.x);

		float a2 = (P4.y - P3.y) / (P4.x - P3.x);
		float b2 = ((P3.x * P4.y) - (P4.x * P3.y)) / (P3.x - P4.x);
		CrossP.x = (b2 - b1) / (a1 - a2);
		CrossP.y = a1 * CrossP.x + b1;
		return CrossP;
	}
	
	/**@function 计算哪个角翻起
	 * 
	 */
	public void calcCornerXY(float x, float y) {
		if (x <= mWidth / 2)
			mCornerX = 0;
		else
			mCornerX = mWidth;
		if (y <= mHeight / 2)
			mCornerY = 0;
		else
			mCornerY = mHeight;
		if ((mCornerX == 0 && mCornerY == mHeight)
				|| (mCornerX == mWidth && mCornerY == 0))
			mIsRTandLB = true;
		else
			mIsRTandLB = false;
	}
	
	/**@function 创建各种梯度颜色变化的对象
	 * Author : hmg25 Version: 1.0 Description : 创建阴影的GradientDrawable
	 */
	private void createDrawable() {
		int[] color = { 0x333333, 0xb0333333 };
		mFolderShadowDrawableRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, color);
		mFolderShadowDrawableRL
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFolderShadowDrawableLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, color);
		mFolderShadowDrawableLR
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowColors = new int[] { 0xbf111111, 0x111111 };
		mBackShadowDrawableRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, mBackShadowColors);
		mBackShadowDrawableRL.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mBackShadowDrawableLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors);
		mBackShadowDrawableLR.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowColors = new int[] { 0x80111111, 0x111111 };
		mFrontShadowDrawableVLR = new GradientDrawable(
				GradientDrawable.Orientation.LEFT_RIGHT, mFrontShadowColors);
		mFrontShadowDrawableVLR
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mFrontShadowDrawableVRL = new GradientDrawable(
				GradientDrawable.Orientation.RIGHT_LEFT, mFrontShadowColors);
		mFrontShadowDrawableVRL
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHTB = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, mFrontShadowColors);
		mFrontShadowDrawableHTB
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);

		mFrontShadowDrawableHBT = new GradientDrawable(
				GradientDrawable.Orientation.BOTTOM_TOP, mFrontShadowColors);
		mFrontShadowDrawableHBT
				.setGradientType(GradientDrawable.LINEAR_GRADIENT);
	}
	
	/**@function 绘制角翻页*/
	public void onRoleDraw(Canvas canvas){
		canvas.drawColor(0xFFAAAAAA);
		calcPoints();
		drawCurrentPageArea(canvas, mCurrentPageInfo.getmBitmap(), mPath0);
		drawNextPageAreaAndShadow(canvas, mNextPageInfo.getmBitmap());
		drawCurrentPageShadow(canvas);
		drawCurrentBackArea(canvas, mCurrentPageInfo.getmBitmap());
	}
	
	public void onVerticalDraw(Canvas canvas){
		canvas.drawColor(0xFFAAAAAA);
		calcPoints();
		drawCurrentPageAreaVertical(canvas, mCurrentPageInfo.getmBitmap());
		drawNextPageAreaAndShadowVertical(canvas, mNextPageInfo.getmBitmap());
		//drawCurrentPageShadow(canvas);
		drawCurrentBackArea(canvas, mCurrentPageInfo.getmBitmap());
	}
	
	/**@function 绘制当前页*/
	public void onGeneralDraw(Canvas canvas){
		canvas.drawColor(0xFFAAAAAA);
		canvas.save();
		canvas.drawBitmap(mCurrentPageInfo.getmBitmap(), 0, 0, null);
		canvas.restore();
	}
	
}
