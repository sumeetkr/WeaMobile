package sv.cmu.edu.weamobile.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

public class WEABackgroundService extends IntentService {
    private static final String FETCH_CONFIGURATION = "sv.cmu.edu.weamobile.service.action.FETCH_CONFIGURATION";
    private static final String FETCH_ALERT = "sv.cmu.edu.weamobile.service.action.FETCH_ALERT";

    private static final String EXTRA_PARAM1 = "sv.cmu.edu.weamobile.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "sv.cmu.edu.weamobile.service.extra.PARAM2";

    public static void checkServerForConfiguration(Context context, String param1, String param2) {
        Intent intent = new Intent(context, WEABackgroundService.class);
        intent.setAction(FETCH_CONFIGURATION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void checkServerForNewAlerts(Context context, String param){

    }

    public WEABackgroundService() {
        super("WEABackgroundService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
    }

    private void fetchConfiguration(String param1, String param2) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void fetchAlert(String param){
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
