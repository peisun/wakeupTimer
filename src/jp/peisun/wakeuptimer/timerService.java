package jp.peisun.wakeuptimer;



import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;


public class timerService extends Service {
	private static final String TAG = "timerService";
	private final long OneMinute = 60*1000;
	private final long INTERVAL_TIME = 24*60*60*1000;
	private final long THREAD_SLEEP_TIME = 1000; 
	private final long SNOOZE_DEFAULT_TIME = 10*60*000; /* 10分 */
	private static MediaPlayer player;
	private Snooze snooze = null;
	private int mSetHour = 0;
	private int mSetMinute = 0;
	
	public static final String  FILE_SETTIME = "setTime";
	
	public static final String ACTION_WAKEUP = "jp.peisun.wakeupTimer.intent.wakeup";
	public static final String INTENT_SETTIME = "jp.peisun.wakeupTimer.intent.setTime";
	public static final String SOUND_PALY = "jp.peisun.wakeupTimer.intent.soundPlay";
	public static final String SOUND_STOP = "jp.peisun.wakeupTimer.intent.soundStop";
	public static final String SNOOZE_START = "jp.peisun.wakeupTimer.intent.snoozeStart";
	public static final String SNOOZE_CANCEL = "jp.peisun.wakeupTimer.intent.snoozeCancel";
	
	public static final String SET_HOUR = "setHour";
	public static final String SET_MINUTE = "setMinute"; 
	public static final String SNOOZE = "snooze";
	private AlarmManager am = null;
	private final Intent wakeup_intent = new Intent(ACTION_WAKEUP);
	private PendingIntent mAlarmSender = null;
	
	private long mSnoozTime = SNOOZE_DEFAULT_TIME;
	private Snooze mSnoozeThread = null;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void onCreate() {
		// TODO 自動生成されたメソッド・スタブ
		// set intent
		
	    
		
		am = (AlarmManager)getSystemService(ALARM_SERVICE);
		mAlarmSender = PendingIntent.getService(this,0, wakeup_intent, 0);
        
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		// TODO 自動生成されたメソッド・スタブ
		super.onDestroy();
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO 自動生成されたメソッド・スタブ
		super.onStartCommand(intent, flags, startId);
		String Action;
		if (intent != null) {
			Action = intent.getAction();
			
		} else {
			Action = "";
		}
		/* Boot時の設定 */
		if(Action.equals(wakeupReceiver.BOOT_ACTION)){
			Log.d(TAG,"intent:"+wakeupReceiver.BOOT_ACTION);
			try {
				int[] time = readSetTimeFile();
				mSetHour = time[0];
				mSetMinute = time[1];
				alarmSetTime(mSetHour,mSetMinute);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		/* 起床時間の設定 */
		else if(Action.equals(INTENT_SETTIME)){
			Log.d(TAG,"intent:"+INTENT_SETTIME);
			mSetHour = intent.getIntExtra(SET_HOUR, 6);
			mSetMinute = intent.getIntExtra(SET_MINUTE, 30);
			alarmSetTime(mSetHour,mSetMinute);
						
		}
		
		/* アラームの鳴動 */
		else if(Action.equals(SOUND_PALY)){
			Log.d(TAG,"intent:"+SOUND_PALY);
			soundPlay();
		}
		/* アラームの開始 */
		else if(Action.equals(SNOOZE_START)){
			Log.d(TAG,"intent:"+SNOOZE_START);
			mSnoozTime = intent.getLongExtra(SNOOZE, SNOOZE_DEFAULT_TIME);
			startSnooze(mSnoozTime);
		}
		/* アラームの停止 */
		else if(Action.equals(SOUND_STOP)){
			Log.d(TAG,"intent:"+SOUND_STOP);
			soundStop();
		}
		/* スヌーズの停止 */
		else if(Action.equals(SNOOZE_CANCEL)){
			Log.d(TAG,"intent:"+SNOOZE_CANCEL);
			cancelSnooze();
		}
		else if(Action.equals(ACTION_WAKEUP)){
			Log.d(TAG,"intent:"+ACTION_WAKEUP);
			am.cancel(PendingIntent.getService(this,0, wakeup_intent, 0));
			// アラームの鳴動
			soundPlay();
			
			// CalcActivityを呼び出す
			Intent ia = new Intent(timerService.this,CalcActivity.class);
			ia.setAction("jp.peisun.wakeupTimer.intent.calcActivity");
			ia.putExtra(CalcActivity.PREVIEW, false);
			ia.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(ia);
			
		}
		return START_STICKY;
		// super.onStartCommand(intent, flags, startId);
	}
	private int[] readSetTimeFile() throws Exception {
    	InputStream in = openFileInput(timerService.FILE_SETTIME);  
    	ObjectInputStream ois = new ObjectInputStream(in);  
    	String setTime = (String)ois.readObject();
    	ois.close();
    	in.close();
    	
    	int[] time = new int[2]; 
    	if(setTime != null){
    		String[] timeSplit = setTime.split(":");
    		time[0]=Integer.parseInt(timeSplit[0]);
    		time[1]=Integer.parseInt(timeSplit[1]);
    	}
    	return time;
    }
	private void alarmSetTime(int hour, int minute){
		// Schedule the alarm!
		// 0時を超えてないかったら、翌日
		// 0時を過ぎてて、指定した時間の前だったら、その日
		Calendar rightNow = Calendar.getInstance();
		GregorianCalendar calendar;
		calendar = new GregorianCalendar();
		long currentTime = System.currentTimeMillis();
		calendar.setTimeInMillis(currentTime);
		if(rightNow.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)){
			calendar.add(Calendar.DATE, 1);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		Log.d(TAG,"setTime:"+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));
        am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),mAlarmSender);

	}


	private void soundPlay(){
		if(player == null){
			player = new MediaPlayer();
		}
		//アラーム音として設定
		player.setAudioStreamType(AudioManager.STREAM_ALARM);
		//音源を指定
		try {
			player.setDataSource(this,Settings.System.DEFAULT_NOTIFICATION_URI);
		} catch (IllegalArgumentException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		//繰り返し再生するように指定
		player.setLooping(true);
		//
		try {
			player.prepare();
		} catch (IllegalStateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		//最初に巻き戻し
		player.seekTo(0);
		//再生開始
		player.start();

	}
	private void soundReplay(){
		if(player == null){
			soundPlay();
		}
		else {
			//最初に巻き戻し
			player.seekTo(0);
			//再生開始
			player.start();
		}
	}
	private void soundStop(){
		if(player != null){
			player.stop();
		}
	}
	/*
	 * スヌーズ用のスレッド
	 */
	private void startSnooze(long snooze_time){
		Long t = new Long(snooze_time);
		mSnoozeThread = new Snooze();
		mSnoozeThread.execute(t);
	}
	private void cancelSnooze(){
		if(mSnoozeThread != null){
			mSnoozeThread.cancel(true);
		}
	}
	class Snooze extends AsyncTask<Long,Void,Boolean> {
		long alarmTime = 0;
		@Override
		protected Boolean doInBackground(Long... params) {
			// TODO 自動生成されたメソッド・スタブ
			long snoozeTime = params[0].longValue();
			while(this.isCancelled() == false && snoozeTime > 0){
				try {
					Thread.sleep(THREAD_SLEEP_TIME);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				snoozeTime -= THREAD_SLEEP_TIME;
				
			}
			if(this.isCancelled() == true) return false;
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO 自動生成されたメソッド・スタブ
			if(result){
				soundPlay();
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO 自動生成されたメソッド・スタブ
			super.onPreExecute();
		}

		@Override
		protected void onCancelled() {
			// TODO 自動生成されたメソッド・スタブ
			soundStop();
			super.onCancelled();
		}

		
		
	}
	

}
