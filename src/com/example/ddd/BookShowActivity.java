package com.example.ddd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.example.Types.BookInfo;
import com.example.Types.FileInfo;
import com.example.Types.FontRectInfo;
import com.example.Types.PageInfo;
import com.example.Util.FileUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class BookShowActivity extends Activity{

	private int w;
	private int h;
	private BookView mBookView;
	private BookInfo mBookInfo;
	private PageInfo mCurrentPageInfo;//保存当前页
	private PageInfo mNextPageInfo;//保存下一页
	private FileInfo mFileInfo;
	private String mString ="";//储存被选中文字
	
	/*UI*/
	private Button mSendToWeibo;
	private Button mStoreToFile;
	private LinearLayout mLinearLayout;
	private EditText mEditText;
	private AlertDialog mAlertDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.bookshowlayout);
		ensureUI();
		Intent intent = getIntent();
		mFileInfo = (FileInfo) intent.getSerializableExtra("book");
		mBookView = (BookView) this.findViewById(R.id.bookview);
		mBookView.setScreen(w, h);
		mBookInfo = new BookInfo();
		mCurrentPageInfo = new PageInfo(w,h,this);
		mNextPageInfo = new PageInfo(w,h,this);
		try {
//			String s = FileUtil.getTxtType(new File(mFileInfo.getUri()));
//			mCurrentPageInfo.setmCharsetName(s);
//			mBookInfo.openBook(mFileInfo.getUri());
			mBookInfo.openBook("/sdcard/test.txt");
			mBookInfo.onDrawPageByTag(mCurrentPageInfo);
			mCurrentPageInfo.onDraw();
			mBookInfo.onDrawPage(mNextPageInfo,mCurrentPageInfo.getmPageEnd());
			mNextPageInfo.onDraw();
			mBookView.setBitmaps(mCurrentPageInfo, mNextPageInfo);
		} catch (FileNotFoundException e) {
			finish();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			finish();
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mBookView.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if(mBookView.getState()!=3){
					mBookView.setState(1);
				}
				return true;
			}
		});
		mBookView.setOnTouchListener(new View.OnTouchListener() {
			float downX = 0;
			float downY = 0;
			float moveX = 0;
			float moveY = 0;
			FontRectInfo downRect = new FontRectInfo();
			FontRectInfo moveRect = new FontRectInfo();
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					downX = event.getX();
					downY = event.getY();
					downRect = mCurrentPageInfo.getRectByXY((int)downX, (int)downY);
					if(mBookView.getState() == 1){
						mBookView.setState(2);
						mBookView.invalidate();
						mLinearLayout.setVisibility(View.GONE);
					}
				}else if(event.getAction() == MotionEvent.ACTION_UP){
					if(mBookView.getState() == 0){
						if(mBookView.getDirection((int) event.getX())){
							mBookInfo.gotoNext(mCurrentPageInfo, mBookView, true);
						}else{
							mBookInfo.gotoAfter(mCurrentPageInfo, mBookView, true);
						}	
					}else if(mBookView.getState() == 2){
						mBookView.setState(0);
						mBookView.createRect(null);
						mString = "";
					}else if(mBookView.getState() == 1){
						mLinearLayout.setVisibility(View.VISIBLE);
						
					}
				}else if(event.getAction() == MotionEvent.ACTION_MOVE){
					moveX = event.getX();
				    moveY = event.getY();
					if(mBookView.getState() == 1){
						moveRect = mCurrentPageInfo.getRectByXY((int)moveX, (int)moveY);
						Rect[] rect = mCurrentPageInfo.returnPickFont(downRect,moveRect);
						mString = mCurrentPageInfo.returnString(downRect,moveRect);
						mBookView.createRect(rect);
					}else if(mBookView.getState() == 0){
						mBookView.setState(3);
						mBookView.calcCornerXY(downX,downY);
						mBookView.setmTouch(new PointF(moveX,moveY));
						mBookView.postInvalidate();
					}else if(mBookView.getState() == 3){
						mBookView.setmTouch(new PointF(moveX,moveY));
						mBookView.postInvalidate();
					}
				}
				return false;
			}
		});
	}
	
	private void ensureUI(){
		w = ((Myreader)getApplication()).getScreenWidth();
		h = ((Myreader)getApplication()).getScreenHeight();
		mSendToWeibo = (Button) this.findViewById(R.id.sendToWeibo);
		mSendToWeibo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BookShowActivity.this,SendTextActivity.class);
				intent.putExtra("status", mString);
				startActivity(intent);
			}
		});
		mStoreToFile = (Button) this.findViewById(R.id.storeToFile);
		mStoreToFile.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mAlertDialog.show();
			}
		});
		mLinearLayout = (LinearLayout) this.findViewById(R.id.buttons);
		mAlertDialog = (AlertDialog) createDialog();
	}
	
	protected Dialog createDialog(){
		LayoutInflater li = LayoutInflater.from(this);
		View promptView = li.inflate(R.layout.promptdialog, null);
		mEditText = (EditText) promptView.findViewById(R.id.editText_prompt);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle("");
    	builder.setView(promptView);
    	builder.setPositiveButton("OK", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					if(!mString.equals("")){
						String d = Environment.getExternalStorageDirectory()+File.separator+Myreader.dirPath;
						FileUtil.save(mEditText.getText().toString(),d,mString, BookShowActivity.this, mCurrentPageInfo.getmCharsetName());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
    	builder.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
    	AlertDialog ad = builder.create();
    	return ad;
	}

}
