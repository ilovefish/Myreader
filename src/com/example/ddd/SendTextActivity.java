package com.example.ddd;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.example.Util.StringUtil;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Token;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SendTextActivity extends Activity {
	/*Authorize parameter*/
	public static final String UID = "uid";
	private static final String KEY = "4008560357";
	private static final String SECRET = "645017fd8bc0c8cfbbb3a05038811c3a";
	private static final String CALLBACK_URL = "http://weibo.com/";
	private String mToken = null;
	private String mExpires_in = null;
	private long mId = 0;
	
	/*Weibo*/
	private Weibo mWeibo = null;
	
	/*UI*/
	private Button mButton1;//ok
	private Button mButton2;//cancel
	private StateHolder mStateHolder;
	private EditText mEditText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(R.layout.weibosdk_share_mblog_view);
		
		if(getLastNonConfigurationInstance() != null){
			mStateHolder = (StateHolder) getLastNonConfigurationInstance();
			mStateHolder.setActivity(this);
		}else{
			mStateHolder = new StateHolder();
			String s = this.getIntent().getStringExtra("status");
			s = StringUtil.subString(s, 140);
			mStateHolder.setmString(s);
		}
//		prefs = PreferenceManager
//        		.getDefaultSharedPreferences(SendTextActivity.this);
//		mAccessToken = MyPreferences.getTokenAndSecret(prefs);
		ensureUI();
	}
	
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		if (isFinishing()) {
		mStateHolder.cancel();
		}
	}
	
	public Object onRetainNonConfigurationInstance() {
		return mStateHolder;
	}
	
	/**@function 初始化UI*/
	public void ensureUI(){
		mButton1 = (Button) this.findViewById(R.id.weibosdk_btnSend);
		mButton1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mStateHolder.getmAccessToken()==null||mStateHolder.getmAccessToken().getToken().equals("null")){
					Toast.makeText(getApplicationContext(),
							"请先授权新浪微博", Toast.LENGTH_SHORT)
							.show();
					getAuthorize();
				}else{
					mStateHolder.startWeiboTask(SendTextActivity.this);
				}
			}
		});
		mButton2 = (Button) this.findViewById(R.id.weibosdk_btnClose);
		mButton2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SendTextActivity.this.finish();
			}
		});
		mEditText = (EditText) this.findViewById(R.id.weibosdk_etEdit);
		mEditText.setText(mStateHolder.getmString());
		mEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable e) {
				// TODO Auto-generated method stub
				String s = e.toString();
				
				s = StringUtil.subString(s, 140);
				mStateHolder.setmString(s);
			}
		});
	}
	
	/**@function 授权*/
	public void getAuthorize(){
		mWeibo = Weibo.getInstance();
		mWeibo.setupConsumerConfig(KEY, SECRET);
		mWeibo.setRedirectUrl(CALLBACK_URL);
		mWeibo.authorize(SendTextActivity.this,
				new AuthDialogListener());
	}
	
	/**@function activity状态保存*/
	private class StateHolder {
		private String mString = null;
		private Token mAccessToken = null;
		private WeiboTask mWeiboTask = null;

		public Token getmAccessToken() {
			return mAccessToken;
		}

		public void setmAccessToken(Token mAccessToken) {
			this.mAccessToken = mAccessToken;
		}

		public String getmString() {
			return mString;
		}

		public void setmString(String mString) {
			this.mString = mString;
		}
		
		public void cancel(){
			if(mWeiboTask!=null){
				mWeiboTask.cancel(true);
				mWeiboTask = null;
			}
		}
		
		public void setActivity(SendTextActivity mainActivity) {
			if(mWeiboTask!=null){
				if(mWeiboTask.getStatus() == AsyncTask.Status.RUNNING){
					mWeiboTask.setActivity(mainActivity);
				}else if(mWeiboTask.getStatus() == AsyncTask.Status.FINISHED){
					if(mWeiboTask.getmBoolean()){
						Toast.makeText(getApplicationContext(),
								"消息发送成功", Toast.LENGTH_LONG)
								.show();
						mainActivity.finish();
					}else{
						Toast.makeText(getApplicationContext(),
								"消息发送失败", Toast.LENGTH_LONG)
								.show();
					}
				}
			}
		}
		
		public void startWeiboTask(SendTextActivity mainActivity){
			if(mWeiboTask==null){
				mWeiboTask= new WeiboTask();
				mWeiboTask.setActivity(mainActivity);
				mWeiboTask.execute();
			}else{
				if(mWeiboTask.getStatus() == AsyncTask.Status.FINISHED){
					mWeiboTask= new WeiboTask();
					mWeiboTask.setActivity(mainActivity);
					mWeiboTask.execute();
				}
			}
		}
	}
	
	/**@function 回调函数，返回请求用户权限*/
	private class AuthDialogListener implements WeiboDialogListener{
		
		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			mToken = values.getString("access_token");//access_token
			mExpires_in = values.getString("expires_in");//access_token的生命周期
			mId = Long.parseLong(values.getString(UID));
			AccessToken mAccessToken = new AccessToken(mToken, SECRET);
			mAccessToken.setExpiresTime(mExpires_in);
			mAccessToken.setUid(mId);
			mStateHolder.setmAccessToken(mAccessToken);
//            Editor editor = prefs.edit();
//			MyPreferences.storeTokenAndSecret(mAccessToken, editor);
			if(mStateHolder.getmAccessToken().getToken().equals("null")){
				Toast.makeText(getApplicationContext(),
						"授权失败", Toast.LENGTH_SHORT)
						.show();
			}else{
				Toast.makeText(getApplicationContext(),
						"已授权成功", Toast.LENGTH_SHORT)
						.show();
			}
		}

		public void onWeiboException(Exception e) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
			
		}

		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(),
					"已取消授权", Toast.LENGTH_LONG)
					.show();
		}

		@Override
		public void onWeiboException(com.weibo.net.WeiboException e) {
			// TODO Auto-generated method stub
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}
		
	}
	
	/**@function post请求*/
	private class WeiboTask extends AsyncTask<Void, Void, Boolean> {

		private SendTextActivity activity;
		private Boolean mBoolean = false;
		
		public Boolean getmBoolean() {
			return mBoolean;
		}

		public void setmBoolean(Boolean mBoolean) {
			this.mBoolean = mBoolean;
		}

		public void setActivity(SendTextActivity activity) {
			this.activity = activity;
		}
		
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			HttpClient client =  getNewHttpClient();
			HttpPost request = new HttpPost("https://api.weibo.com/2/statuses/update.json");
			List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			postParameters.add(new BasicNameValuePair("access_token", activity.mStateHolder.getmAccessToken().getToken()));
			postParameters.add(new BasicNameValuePair("status", activity.mStateHolder.getmString()));
			UrlEncodedFormEntity formEntity;
			try {
				formEntity = new UrlEncodedFormEntity(postParameters);
				request.setEntity(formEntity);
				HttpResponse response = client.execute(request);
				int statusCode = response.getStatusLine().getStatusCode();
				Log.d("tag", statusCode+"");
				 switch (statusCode) {
				 
		            case 200:
		            	return true;
		            default:
		            	return false;
		        }
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			if(result){
				setmBoolean(result);
				Toast.makeText(getApplicationContext(),
						"消息发送成功", Toast.LENGTH_LONG)
						.show();
				finish();
			}else{
				setmBoolean(result);
				Toast.makeText(getApplicationContext(),
						"消息发送失败", Toast.LENGTH_LONG)
						.show();
			}
		}
		
		
		
	}
	
	/**@function 支持ssl client*/
	public static HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	       
	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	    
	        return new DefaultHttpClient();
	    }
	}
	
	public static class MySSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");
	 public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}
}
