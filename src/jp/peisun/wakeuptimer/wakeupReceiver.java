package jp.peisun.wakeuptimer;

import java.io.IOException;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;



public class wakeupReceiver extends BroadcastReceiver {
	private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 自動生成されたメソッド・スタブ
		mContext = context;
		String action = intent.getAction();
		if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
			Intent i = new Intent();
			i.putExtra(timerService.SET_HOUR, 0);
			i.putExtra(timerService.SET_MINUTE, 0);
			mContext.startService(i);
			
		}
		if (action.equals(timerService.WAKEUP_ACTION)) {
			// アラームの鳴動
			Intent i = new Intent();
			i.setAction(timerService.SOUND_PALY);
			mContext.startService(i);
			// CalcActivityの起動
			
		}
		return;
	}
	

}
