package com.example.ddd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.example.Types.FileInfo;
import com.example.ddd.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private HashMap<String,FileInfo> files = null;
	private ArrayList<FileInfo> replaceFiles = new ArrayList<FileInfo>();
	/*UI*/
	private GridView bookShelf;
	private Button mReturnButton;
	private Button mFunctionButton;
	private ShlefAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		files = ((Myreader)this.getApplication()).getFiles();
		Iterator iterator = files.keySet().iterator();
		while(iterator.hasNext()) {
			FileInfo f = (FileInfo)files.get(iterator.next());
			replaceFiles.add(f);
		}
		ensureUI();
		Intent intent = new Intent(MainActivity.this, BookShowActivity.class);
		MainActivity.this.startActivity(intent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0){
			files = ((Myreader)this.getApplication()).getFiles();
			Iterator iterator = files.keySet().iterator();
			replaceFiles.removeAll(replaceFiles);
			while(iterator.hasNext()) {
				FileInfo f = (FileInfo)files.get(iterator.next());
				replaceFiles.add(f);
			}
			adapter.setData(replaceFiles);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void ensureUI(){
		bookShelf = (GridView) findViewById(R.id.bookShelf);
		bookShelf.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(MainActivity.this, BookShowActivity.class);
				FileInfo fileInfo = (FileInfo) arg0.getItemAtPosition(arg2);
				if(fileInfo!=null){
					intent.putExtra("book", fileInfo);
					MainActivity.this.startActivity(intent);
				}
					
			}
		});
		mReturnButton = (Button) this.findViewById(R.id.btn_leftTop);
		mFunctionButton = (Button) this.findViewById(R.id.btn_rightTop);
        adapter=new ShlefAdapter();
        adapter.setData(replaceFiles);
        bookShelf.setAdapter(adapter);
        mFunctionButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, FileShowActivity.class);
				MainActivity.this.startActivityForResult(intent, 0);
			}
		});
		mReturnButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
					
			}
		});
	}
	
	private class ShlefAdapter extends BaseAdapter{
		private ArrayList<FileInfo> file = null;
	
	    public void setData(ArrayList<FileInfo> files){
	    	file = files;
	    	this.notifyDataSetChanged();
	    	bookShelf.invalidate();
	    }
	    
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(file.size()>12){
				return file.size() + 3 - file.size()%3;
			}else{
				return 12;
			}
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			if(file.size()>arg0){
				return file.get(arg0);
			}
			return null;
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
				contentView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bookitem, null);
				viewHolder.view = (TextView)contentView.findViewById(R.id.imageView1);
				contentView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) contentView.getTag();
			}
			if(file.size()>position){
				FileInfo fileInfo = file.get(position);
				Log.d("tag", "onActivityResult:"+fileInfo.getName());
				viewHolder.view.setVisibility(View.VISIBLE);
				viewHolder.view.setText(fileInfo.getName());
				viewHolder.view.setBackgroundResource(fileInfo.getTitlePage());
			}else{
				viewHolder.view.setClickable(false);
				viewHolder.view.setVisibility(View.GONE);
			}
			return contentView;
		}
		private class ViewHolder{
			TextView view;
		}
    }
	
}
