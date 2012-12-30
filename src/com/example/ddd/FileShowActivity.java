package com.example.ddd;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.Types.FileInfo;
import com.example.Util.FileUtil;
import com.example.ddd.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FileShowActivity extends Activity {

	private Button mReturnButton;
	private Button mFunctionButton;
	private ProgressBar mProgressBar;
	private ListView mListView;
	private ShlefAdapter mShlefAdapter;
	private HashMap<String,FileInfo> mFileInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.fileshowlayout);
		mFileInfo = new HashMap<String,FileInfo>();
		
		//mFileInfo = ((Myreader)getApplication()).getFiles();
		ensureUI();
	}
	
	@Override
	protected void onPause() {
		if(this.isFinishing()){
			
			((Myreader)getApplication()).setFiles(mFileInfo);
			
		}
		super.onPause();
	}

	private void ensureUI(){
		mReturnButton = (Button) this.findViewById(R.id.btn_leftTop);
		mFunctionButton = (Button) this.findViewById(R.id.btn_rightTop);
		mListView = (ListView) this.findViewById(R.id.listview);
		mProgressBar = (ProgressBar) this.findViewById(R.id.progressBar1);
		mShlefAdapter = new ShlefAdapter();
		mFunctionButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
			     
			     Thread thread = new Thread(new Runnable() {
			    	TaskHandler handler = new TaskHandler();
					@Override
					public void run() {
						File sdRoot=new File("/sdcard");
			   		 	ArrayList<File> temp = new ArrayList<File>();
			   		 	if(sdRoot.exists()){
			   	    	FileUtil.searchFile(sdRoot,temp);
			   	    	Message msg = new Message();
			   	    	msg.obj = temp;
			   	    	handler.sendMessage(msg);
			   		 	}
					}
				});
			    thread.start();
			    mProgressBar.setVisibility(View.VISIBLE);
			     
			}
		});
		mReturnButton.setOnClickListener(new View.OnClickListener() {
			
		
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	private class ShlefAdapter extends BaseAdapter{
		private ArrayList<File> file = null;
	
	    public void setData(ArrayList<File> files){
	    	file = files;
	    	this.notifyDataSetChanged();
	    }
	    
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return file.size();
		
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return file.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int position, View contentView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			
			ViewHolder viewHolder;
			if(contentView == null){
				viewHolder = new ViewHolder();
				contentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.fileitem, null);
				viewHolder.view = (TextView)contentView.findViewById(R.id.imageView1);
				viewHolder.checkBox = (CheckBox) contentView.findViewById(R.id.checkbox);
				contentView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) contentView.getTag();
			}

			File f = file.get(position);
			String s = f.getName();
			if(s == null){s = "Î´ÃüÃû";}
			viewHolder.view.setText(s);
			viewHolder.checkBox.setOnCheckedChangeListener(null);
			viewHolder.checkBox.setChecked(false);
			viewHolder.checkBox.setOnCheckedChangeListener(new CheckedListener(f));
			return contentView;
		}
    	
		private class ViewHolder{
			TextView view;
			CheckBox checkBox;
		}
    }
	
	private class TaskHandler extends Handler {

        public void handleMessage(Message msg) {
        	ArrayList<File> temp = (ArrayList<File>) msg.obj;
        	mShlefAdapter.setData(temp);
        	mListView.setAdapter(mShlefAdapter);
        	mProgressBar.setVisibility(View.GONE);
        }
    }
	
	private class CheckedListener implements OnCheckedChangeListener {
		private File mFile;
		
		public CheckedListener(File file){
			mFile = file;
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			if(isChecked){
				if(!mFileInfo.containsKey(mFile.getName())){
					FileInfo fileInfo = new FileInfo();
					fileInfo.setName(mFile.getName());
					fileInfo.setUri(mFile.getPath());
					fileInfo.setTitlePage(R.drawable.bookcase_book_nor_cover);
					mFileInfo.put(mFile.getName(), fileInfo);
				}
			}else{
				if(mFileInfo.containsKey(mFile.getName())){
				   mFileInfo.remove(mFile.getName());
				}
			}

			FileShowActivity.this.setResult(1);
		}
	}
}
