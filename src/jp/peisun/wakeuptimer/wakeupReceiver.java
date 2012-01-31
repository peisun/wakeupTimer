package jp.peisun.wakeuptimer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.util.Log;



public class wakeupReceiver extends BroadcastReceiver {
	private static final String TAG = "BroadcastReceiver";
	private Context mContext;
	public static final String BOOT_ACTION = "jp.peisun.wakeupTimer.intent.boot_completed";
	public static final String CALCACTIVITY_VIEW = "jp.peisun.wakeupTimer.intent.calcActivity.VIEW";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 自動生成されたメソッド・スタブ
		mContext = context;
		String action = intent.getAction();
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			Intent i = new Intent(BOOT_ACTION);
			mContext.startService(i);
			
		}
		
		return;
	}

}
