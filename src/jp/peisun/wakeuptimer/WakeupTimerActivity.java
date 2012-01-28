package jp.peisun.wakeuptimer;

import android.app.Activity;
import android.os.Bundle;

/*
 * 各種設定をするActivity.
 * item1:アラーム時間
 * item2:スヌーズ間隔
 * 
 */
public class WakeupTimerActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}