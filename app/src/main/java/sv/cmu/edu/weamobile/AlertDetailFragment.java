package sv.cmu.edu.weamobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.Locale;

import sv.cmu.edu.weamobile.Data.Alert;
import sv.cmu.edu.weamobile.Data.AppConfiguration;
import sv.cmu.edu.weamobile.Data.GeoLocation;
import sv.cmu.edu.weamobile.Utility.AlertHelper;
import sv.cmu.edu.weamobile.Utility.GPSTracker;
import sv.cmu.edu.weamobile.Utility.Logger;
import sv.cmu.edu.weamobile.Utility.WEAPointInPoly;


/**
 * A fragment representing a single Alert detail screen.
 * This fragment is either contained in a {@link MainActivity}
 * in two-pane mode (on tablets) or a {@link AlertDetailActivity}
 * on handsets.
 */
public class AlertDetailFragment extends Fragment {
    private GoogleMap mMap;
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ALERTS_JSON = "alert_json";
    private Alert alert;
    private String localTime;
    private View rootView;
    private Location myLocation;
    private boolean isDialog = false;
    private Vibrator vibrator;
    private TextToSpeech tts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_alert_detail, container, false);
        return rootView;
    }

    private void setupView(){

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            if(getArguments().containsKey(ALERTS_JSON)){
                AppConfiguration configuration = AppConfiguration.fromJson(getArguments().getString(ALERTS_JSON));
                String key = getArguments().getString(ARG_ITEM_ID);
                Logger.log("AlertDetailFragment key: " + key);

                Alert selectedAlert = null;
                for(Alert alert:configuration.getAlerts()){
                    if(alert.getId() == Integer.parseInt(key)){
                        selectedAlert = alert;
                    }
                }
                alert = selectedAlert;

                updateMyLocation();
                setUpMapIfNeeded();

            }
        }

        if (alert != null) {
            Logger.log("Item is there"+ alert.getText());
            ((TextView) rootView.findViewById(R.id.alertText)).setText(AlertHelper.getTextWithStyle(
                    alert.getAlertType() +
                    " : " + alert.toString(), 30));

            localTime = alert.getScheduledForString();
            String textToShow = getTextToShow();
            ((TextView) rootView.findViewById(R.id.txtLabel)).setText(
                    AlertHelper.getTextWithStyle("Scheduled For: " + localTime + "\n" + textToShow,
                            25));
        }else{
            Logger.log("Item is null");
        }

        if(getArguments().containsKey("isDialog")){
            isDialog = getArguments().getBoolean("isDialog");
            if(isDialog){
                LinearLayout buttonLayout = (LinearLayout) rootView.findViewById(R.id.alertDialogButtons);
                buttonLayout.setVisibility(View.VISIBLE);

                addEventListenersToButtons();
                alertUserWithVibrationAndSpeech();
            }else{
                LinearLayout buttonLayout = (LinearLayout) rootView.findViewById(R.id.alertDialogButtons);
                buttonLayout.setVisibility(View.INVISIBLE);
            }

        }
    }
    private String getTextToShow() {
        double distance = WEAPointInPoly.getDistance(alert.getPolygon(), myLocation);
        return "You are at a distance " + String.valueOf(distance).substring(0,3) + " miles";
    }

    private void updateMyLocation() {
        GPSTracker tracker = new GPSTracker(this.getActivity().getApplicationContext());
        if(tracker.canGetLocation()){
            myLocation = tracker.getNetworkLocation();
        }

        if(myLocation != null) Logger.log("my location: " + myLocation.toString());
        tracker.stopUsingGPS();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupView();
        setUpMapIfNeeded();
    }

    @Override
    public void onDestroy() {
        vibrator = null;
        if(tts != null) tts.shutdown();
        super.onDestroy();
    }

    private void addEventListenersToButtons() {
        Button close_button = (Button) rootView.findViewById(R.id.buttonOk);
        if(close_button != null) close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        Button btnFeedback = (Button) rootView.findViewById(R.id.buttonFeedback);
        if(btnFeedback != null){
            final Activity activity = this.getActivity();
            btnFeedback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, FeedbackWebViewActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void alertUserWithVibrationAndSpeech() {

        vibrator = (Vibrator) getActivity().getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {200, 200, 200, 200, 200, 200, 200, 400,200,
                400,200, 400,200, 200, 200, 200, 200, 200, 400,
                200, 200, 200, 200, 200, 200,400,200, 400,
                200, 400,200, 200, 200, 200, 200, 200};

        vibrator.vibrate(pattern, -1);

        if(alert != null){
            String messageToSay = alert.getText() + getTextToShow();

            tts = new TextToSpeech(this.getActivity(),
                    new TTSListener(messageToSay , 2));
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            try{
                Fragment fragment = getFragmentManager().findFragmentById(R.id.map);
                mMap = ((SupportMapFragment) fragment).getMap();
                // Check if we were successful in obtaining the map.
                if (mMap != null) {
                    setUpMap();
                }
            }catch(Exception ex){
                Logger.log(ex.getMessage());
            }
        }
    }

    private void setUpMap() {
        mMap.clear();
        mMap.setMyLocationEnabled(true);

        if(myLocation != null){
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                    .title("Your location"));
        }
        drawPolygon();
    }

    private void drawPolygon(){
        if(mMap!=null){
            PolygonOptions polyOptions = new PolygonOptions()
                    .strokeColor(Color.RED);

            if(alert!=null){
                GeoLocation[] locations = alert.getPolygon();
                for(GeoLocation location:locations){
                    polyOptions.add(new LatLng(Double.parseDouble(location.getLat()), Double.parseDouble(location.getLng())));
                }
            }


            mMap.addPolygon(polyOptions);
            setCenter();
        }
    }

    private void setCenter(){
        double [] location = WEAPointInPoly.calculatePolyCenter(alert.getPolygon());

        CameraUpdate center=
                CameraUpdateFactory.newLatLng(new LatLng(location[0],location[1]));
        CameraUpdate zoom=CameraUpdateFactory.zoomTo(17);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);
    }

    protected class TTSListener implements TextToSpeech.OnInitListener {

        String what_to_speak = null;
        Integer how_many_times = null;

        public TTSListener(String to_speak, Integer times) {
            what_to_speak = to_speak;
            how_many_times = times;
        }

        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.speak(what_to_speak, TextToSpeech.QUEUE_FLUSH, null);
                    for (Integer n = 1; n < how_many_times; n++) {
                        tts.speak(what_to_speak, TextToSpeech.QUEUE_ADD, null);
                    }
                }
            }
        }
    }

}
