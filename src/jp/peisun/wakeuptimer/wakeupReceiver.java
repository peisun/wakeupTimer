package jp.peisun.wakeuptimer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;



public class wakeupReceiver extends BroadcastReceiver {
	private static final String TAG = "BroadcastReceiver";
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 自動生成されたメソッド・スタブ
		mContext = context;
		String action = intent.getAction();
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			Intent i = new Intent(timerService.BOOT_ACTION);
			mContext.startService(i);
			
		}
		else if(action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)){
			if (intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1) == AudioManager.RINGER_MODE_VIBRATE) {
                // マナーモード
            } else {
                // マナーモードではない
            }
		}
		
		return;
	}

}
