package com.example.MyPreferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.weibo.net.AccessToken;
import com.weibo.net.Token;

public class MyPreferences {
	public static final String TOKEN = "access_token";
	public static final String EXPIRES = "expires_in";
	public static final String SECRET = "secret";
	public static final String UID = "uid";
	
	public static boolean storeTokenAndSecret(Token accessToken,final Editor editor){
		editor.putString(TOKEN, accessToken.getToken());
        editor.putString(EXPIRES, Long.toString(accessToken.getExpiresTime()));
        editor.putString(SECRET, accessToken.getSecret());
        editor.putLong(UID, accessToken.getUid());
        if (!editor.commit()) {
            return false;
        }
        return true;
	}
	
	public static AccessToken getTokenAndSecret(SharedPreferences prefs) {
		 AccessToken accessToken = new AccessToken(prefs.getString(TOKEN, "null"),prefs.getString(SECRET, "null"));
		 accessToken.setExpiresTime(prefs.getString(EXPIRES, "0"));
		 accessToken.setUid(prefs.getLong(UID, 0));
	     return accessToken;
	 }
}
