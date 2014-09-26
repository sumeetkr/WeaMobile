package sv.cmu.edu.weamobile.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import sv.cmu.edu.weamobile.Utility.InOrOutTargetDecider;

public class WEABackgroundService extends IntentService {
    public static final String FETCH_CONFIGURATION = "sv.cmu.edu.weamobile.service.action.FETCH_CONFIGURATION";
    public static final String FETCH_ALERT = "sv.cmu.edu.weamobile.service.action.FETCH_ALERT";

    private static final String EXTRA_PARAM1 = "sv.cmu.edu.weamobile.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "sv.cmu.edu.weamobile.service.extra.PARAM2";
    private final IBinder mBinder = new LocalBinder();

    public static void checkServerForConfiguration(Context context, String param1, String param2) {
        Intent intent = new Intent(context, WEABackgroundService.class);
        intent.setAction(FETCH_CONFIGURATION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void checkServerForNewAlerts(Context context, String param){
        Intent intent = new Intent(context, WEABackgroundService.class);
        intent.setAction(FETCH_ALERT);
        intent.putExtra(EXTRA_PARAM1, param);
        context.startService(intent);
    }

    public WEABackgroundService() {
        super("WEABackgroundService");
    }

    public class LocalBinder extends Binder {
        WEABackgroundService getService() {
            return WEABackgroundService.this;
        }
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("WEA", "called with "+ intent.getAction());
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_CONFIGURATION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                fetchConfiguration(param1, param2);
            }
            if(FETCH_ALERT.equals(action)){
                final String param = intent.getStringExtra(EXTRA_PARAM1);
                fetchAlert(param);
            }

        }

        AlarmBroadcastReceiver.completeWakefulIntent(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void fetchConfiguration(String param1, String param2) {
        Log.d("WEA", "Got request to fetch new configuration");
        //read configuration and setup up new alarm
        //if problem in getting/receiving configuration, set default alarm

        //if time to show new alert
        broadcastNewAlert("Free food alert", "1222222233123113");
    }

    private void fetchAlert(String param){
        //fetch alerts from server first
        //if new alert broadcast alert
        broadcastNewAlert("Free food alert", "1222222233123113");
    }

    private void broadcastNewAlert(String message, String polygonEncoded){
        WEANewAlertIntent newAlertIntent = new WEANewAlertIntent(message, polygonEncoded);
        Log.d("WEA", "Broadcast intent: About to broadcast new Alert");
        getApplicationContext().sendBroadcast(newAlertIntent);

        //Ask InOuttargetDecider to decide
        //If in target send intent to show dialog
        //Do not send it as a broadcast, we need to keep service alive till
        //we know the is in target
        if(InOrOutTargetDecider.isInTarget(polygonEncoded)){

        }
    }
}
