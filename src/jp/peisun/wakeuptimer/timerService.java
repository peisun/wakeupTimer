package jp.peisun.wakeuptimer;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;


import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;



public class timerService extends Service {
	private static final String TAG = "timerService";
	private static  boolean debug = true;
	
	/* 定数 */
	
	/* ファイル名 */
	public static final String  FILE_SETTIME = "setTime";
	/*
	 * インテント
	 */
	public static final String ACTION_WAKEUP = "jp.peisun.wakeupTimer.intent.wakeup";
	public static final String SOUND_PALY = "jp.peisun.wakeupTimer.intent.soundPlay";
	public static final String SOUND_STOP = "jp.peisun.wakeupTimer.intent.soundStop";
	public static final String SOUND_SET = "jp.peisun.wakeupTimer.intent.soundSet";
	public static final String SNOOZE_START = "jp.peisun.wakeupTimer.intent.snoozeStart";
	public static final String SNOOZE_CANCEL = "jp.peisun.wakeupTimer.intent.snoozeCancel";
	public static final String BOOT_ACTION = "jp.peisun.wakeupTimer.intent.boot_completed";
	public static final String ACTION_FINISH = "jp.peisun.wakeupTimer.intent.finish";
	public static final String SET_CONFIG = "jp.peisun.wakeupTimer.intent.setconfig";
	public static final String FORCE_FINISH = "jp.peisun.wakeupTimer.intent.force";
	
	/*
	 * インテント引数
	 */
	public static final String SET_HOUR = "setHour";
	public static final String SET_MINUTE = "setMinute"; 
	public static final String SNOOZE = "snooze";
	public static final String SOUND = "sound";
	public static final String SOUND_URI = "ringtonepath";
	public static final String VIBRATION = "vibration";
	public static final String CONFIG = "config";
	
	/*
	 * Alarmを再設定するまでの時間稼ぎ
	 */
	private static final long DELAY_TIME = 60*1000; // 60秒
	private static final int MSG = 1;
	private WaitHandler mWaitHandler = new WaitHandler();
	
	private ConfigData mConfig = new ConfigData();
	
	
	/*
	 * 起床時間のアラーム関連
	 */
	private AlarmManager mAmWakeup = null;
	private final Intent wakeup_intent = new Intent(ACTION_WAKEUP);
	private PendingIntent mAlarmSender = null;
	/* アラーム音 */
//	private RingtoneManager mRingtoneManager = null;
//	private Ringtone mRingtone = null;
	public final static int RINGTON_STREAMTYPE = RingtoneManager.TYPE_ALARM;
	public final static int AUDIO_STREAMTYPE = AudioManager.STREAM_ALARM;
	MediaPlayer mMediaPlayer = null;
	/* バイブレーション */
	private Vibrator vibrator=null;
	// 0秒後に3秒振動、1秒待って3秒振動、1秒待って3秒振動...
	private final long[] pattern = {0,3000, 1000, 3000, 1000, 3000, 1000}; // OFF/ON/OFF/ON/OFF...
	
	/*
	 * スヌーズのアラーム関連
	 */
	private AlarmManager mAmSnooze = null;
	private PendingIntent mSnoozSender = null;
	
	/* 画面ロック解除 */
	private PowerManager.WakeLock mWakeLock;
	private KeyguardLock keylock;
	
	/*
	 * Activityの状態
	 */
	private boolean mActivityStarted = false;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void onCreate() {
		// TODO 自動生成されたメソッド・スタブ
		//am = (AlarmManager)getSystemService(ALARM_SERVICE);
		//mAlarmSender = PendingIntent.getService(this,0, wakeup_intent, 0);
		
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
		if(Action.equals(BOOT_ACTION)){
			Log.d(TAG,"intent:"+BOOT_ACTION);
				
				InputStream is=null;
				// ファイルから起床時間を読み出し設定する

				String filepath = this.getFilesDir().getAbsolutePath() + "/" +  FileConfig.xmlfile;
				File file = new File(filepath);
				try {
					is = new FileInputStream(file);
					mConfig = FileConfig.readConfig(is);
				} catch (FileNotFoundException e) {
					// TODO 自動生成された catch ブロック
					mConfig = setConfigDefault();

				}
				
				try {
					if(is != null ){
						is.close();
					}
				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

				alarmSetTime(mConfig.hour,mConfig.minute);
				
		}
		else if(Action.equals(SET_CONFIG)){

			mConfig.mSnoozTime = intent.getLongExtra(SNOOZE, mConfig.mSnoozTime);
			mConfig.mCalcRepeat = intent.getIntExtra(CalcActivity.REPEAT, mConfig.mCalcRepeat);
			mConfig.mLimitTime = intent.getLongExtra(CalcActivity.LIMITTIME, mConfig.mLimitTime);
			mConfig.mRingtonePosition = intent.getIntExtra(SOUND, mConfig.mRingtonePosition);
			mConfig.mRingtonePath = intent.getStringExtra(SOUND_URI);
			mConfig.mVabration = intent.getBooleanExtra(VIBRATION, mConfig.mVabration);
			int hour = intent.getIntExtra(SET_HOUR, mConfig.hour);
			int minute = intent.getIntExtra(SET_MINUTE, mConfig.minute);
			if(hour != mConfig.hour){
				mConfig.hour = hour;
			}
			if(minute != mConfig.minute){
				mConfig.minute = minute;
			}
			alarmSetTime(mConfig.hour,mConfig.minute);
			
			writeFile(mConfig);
			Log.d(TAG,"intent:"+SET_CONFIG+" time= "+mConfig.hour+":"+mConfig.minute);
			Log.d(TAG,"intent:"+SET_CONFIG+" mSnoozTime= "+mConfig.mSnoozTime);
			Log.d(TAG,"intent:"+SET_CONFIG+" mVabration= "+mConfig.mVabration);
			Log.d(TAG,"intent:"+SET_CONFIG+" mRingtonePosition= "+mConfig.mRingtonePosition);
			Log.d(TAG,"intent:"+SET_CONFIG+" mRingtonePath"+mConfig.mRingtonePath);
			Log.d(TAG,"intent:"+SET_CONFIG+" mRepeat= "+mConfig.mCalcRepeat);
			Log.d(TAG,"intent:"+SET_CONFIG+" mLimittime= "+mConfig.mLimitTime);
		}

		/* アラームの鳴動 */
		else if(Action.equals(SOUND_PALY)){
			Log.d(TAG,"intent:"+SOUND_PALY);
			soundPlay(mConfig.mRingtonePosition);
			vabrationStart();
		}
		/* アラームの停止 */
		else if(Action.equals(SOUND_STOP)){
			Log.d(TAG,"intent:"+SOUND_STOP);
			soundStop();
			vavrationStop();
		}

		/* スヌーズの開始 */
		else if(Action.equals(SNOOZE_START)){
			Log.d(TAG,"intent:"+SNOOZE_START);
			releaseWakelock();
			startSnooze(mConfig.mSnoozTime);
		}
		
		/* スヌーズのキャンセル */
		else if(Action.equals(SNOOZE_CANCEL)){
			Log.d(TAG,"intent:"+SNOOZE_CANCEL);
			releaseWakelock();
			cancelSnooze();
		}

		else if(Action.equals(ACTION_WAKEUP)){
			Log.d(TAG,"intent:"+ACTION_WAKEUP);
			// 画面ロックを外す
			returnFromSleep();
			// アラームのキャンセル
			alarmSetCancel();
			
			// アラームの鳴動
			soundPlay(mConfig.mRingtonePosition);
			vabrationStart();
			
			// CalcActivityを呼び出す

			// Activityをすでに起動してたら、新たには起動しない
			Intent ia = new Intent(getApplicationContext(),CalcActivity.class);
			ia.setAction("jp.peisun.wakeupTimer.intent.calcActivity");
			ia.putExtra(CalcActivity.PREVIEW, false);
    		ia.putExtra(CalcActivity.REPEAT, mConfig.mCalcRepeat);
    		ia.putExtra(CalcActivity.LIMITTIME, mConfig.mLimitTime);
    		Log.d(TAG,"startActivity Repeat" + mConfig.mCalcRepeat+ " LimitTime "+mConfig.mLimitTime);
			ia.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(ia);
			
			
		}
		else if(Action.equals(ACTION_FINISH)){
			mWaitHandler.sleep();
			cancelSnooze();
			mActivityStarted = false;
		}
		else if(Action.equals(FORCE_FINISH)){
			soundStop();
			vavrationStop();
			cancelSnooze();
		}
		return START_STICKY;
		// super.onStartCommand(intent, flags, startId);
	}
	
	private ConfigData setConfigDefault(){
		ConfigData config = new ConfigData();
		config.hour = Integer.parseInt(getString(R.string.wakeupHourDefault));
		config.minute = Integer.parseInt(getString(R.string.wakeupMinuteDefault));
		config.mCalcRepeat = Integer.parseInt(getString(R.string.calcRepeatDefault));
		config.mLimitTime = Long.parseLong(getString(R.string.limittimeDefault));
		config.mSnoozTime = Long.parseLong(getString(R.string.snoozeTimeDefault));
		config.mRingtonePosition = Integer.parseInt(getString(R.string.selectAlarmDefaultIndex));
		config.mVabration = Boolean.parseBoolean(getString(R.string.vibrationDefault));
		return config;
	}
	private void writeFile(ConfigData config){
		OutputStream os = null;
		try {
			String filepath = this.getFilesDir().getAbsolutePath() + "/" +  FileConfig.xmlfile;  
			File file = new File(filepath);
			os = new FileOutputStream(file);
			//os = openFileOutput(FileConfig.xmlfile,MODE_PRIVATE);
			FileConfig.writeConfig(os,config);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	private void returnFromSleep(){
		// PowerManagerを取得する
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		// スクリーンが暗いままならlockを解除する
		if(pm.isScreenOn()==false){
			//スリープ状態から復帰する
			mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
					| PowerManager.ACQUIRE_CAUSES_WAKEUP 
					| PowerManager.ON_AFTER_RELEASE, "disableLock");
			mWakeLock.acquire();

			//スクリーンロックを解除する
			KeyguardManager keyguard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
			keylock = keyguard.newKeyguardLock("disableLock");
			keylock.disableKeyguard();
		}
	}
	private void releaseWakelock(){
		if(mWakeLock != null){
			mWakeLock.release();
			mWakeLock = null;
		}
		
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
		if(debug == false){
			if(rightNow.get(Calendar.HOUR_OF_DAY) == calendar.get(Calendar.HOUR_OF_DAY)){
				calendar.add(Calendar.DATE, 1);
			}
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
		}
		else {
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
			if(calendar.getTimeInMillis() <= currentTime){
				calendar.add(Calendar.DATE,1);
			}
			
		}
		
		Log.d(TAG,"setTime:"+calendar.get(Calendar.DAY_OF_MONTH)+ " " +calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));
		mAlarmSender = PendingIntent.getService(this,0, wakeup_intent, 0);
		mAmWakeup =(AlarmManager)getSystemService(ALARM_SERVICE);
		mAmWakeup.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),mAlarmSender);

	}
	private void alarmSetCancel(){
		if(mAmWakeup!=null){
			mAmWakeup.cancel(mAlarmSender);
		}
	}
	
//	private void RingtonePlay(){
//		if(mRingtone != null){
//			mRingtone.play();
//		}
//	}
//	
//	private void RingtoneStop(){
//		if(mRingtone != null){
//			mRingtone.stop();
//		}
//	}
	public void soundPlay(int position){
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		RingtoneManager mRingtoneManager = new RingtoneManager(this);
		mRingtoneManager.setType(RINGTON_STREAMTYPE);
		Uri uri = mRingtoneManager.getRingtoneUri(position);
		try {
			mMediaPlayer.setDataSource(this, uri);
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
		mMediaPlayer.setLooping(true);
		try {
			mMediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		mMediaPlayer.start();
	}
	public void soundStop(){
		mMediaPlayer.stop();
	}
	private void vabrationStart(){
		if(mConfig.mVabration){
			if(vibrator == null){
				vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);	
			}
			vibrator.vibrate(pattern, 0);
		}
	}
	private void vavrationStop(){
		if(vibrator != null){
			vibrator.cancel();
		}
	}
	class WaitHandler extends Handler {
		@Override
        public void handleMessage(Message msg) {
			alarmSetTime(mConfig.hour,mConfig.minute);
		}
		public void sleep(){
			this.removeMessages(MSG);  
			sendMessageDelayed(obtainMessage(MSG), DELAY_TIME); 
		}
	}
	/*
	 * スヌーズの為のAlarmをセット
	 */
	private void startSnooze(long snooze_time){
		 
		long snooze = System.currentTimeMillis() + mConfig.mSnoozTime;

		Log.d(TAG,"Snooze Set");
		mSnoozSender = PendingIntent.getService(this,0, wakeup_intent, 0);
		mAmSnooze =(AlarmManager)getSystemService(ALARM_SERVICE);
		mAmSnooze.set(AlarmManager.RTC_WAKEUP,snooze,mSnoozSender);
		
	}
	private void cancelSnooze(){
		if(mAmSnooze != null){
		mAmSnooze.cancel(mSnoozSender);
		}
	}

}
