package jp.peisun.wakeuptimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;



public class wakeupReceiver extends BroadcastReceiver {
	private static final String TAG = "BroadcastReceiver";
	public static final String BROADCAST_ALARM_WAKEUP = "jp.peisun.wakeupTimer.intent.broadcast_alarm_wakeup";
	public static final String BROADCAST_SNOOZE_WAKEUP = "jp.peisun.wakeupTimer.intent.broadcast_snooze_wakeup";
	public static final String SNOOZE_INTENT = "intent_snooze_wakeup";
	private Context mContext;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 自動生成されたメソッド・スタブ
		mContext = context;
		String action = intent.getAction();
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			Intent i = new Intent(timerService.BOOT_ACTION);
			mContext.startService(i);
			Log.d(TAG,"intent:"+Intent.ACTION_BOOT_COMPLETED);
			
		}
		else if(action.equals(BROADCAST_ALARM_WAKEUP)){
			Intent i = new Intent(timerService.ACTION_WAKEUP);
			i.putExtra(SNOOZE_INTENT, false);
			mContext.startService(i);
			Log.d(TAG,"intent:"+BROADCAST_ALARM_WAKEUP);
			
		}
		else if(action.equals(BROADCAST_SNOOZE_WAKEUP)){
			Intent i = new Intent(timerService.ACTION_WAKEUP);
			i.putExtra(SNOOZE_INTENT, true);
			mContext.startService(i);
			Log.d(TAG,"intent:"+BROADCAST_SNOOZE_WAKEUP);
			
		}
		// マナーモードのときに、それを解除することもできるが、
		// 仕様としてどうするか考え中なので、コードが残っている
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
