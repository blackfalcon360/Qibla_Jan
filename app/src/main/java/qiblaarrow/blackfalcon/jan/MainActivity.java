package qiblaarrow.blackfalcon.jan;

import android.Manifest;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/** Full-screen black Qibla arrow with Refresh Location button. Works offline. */
public class MainActivity extends Activity
        implements HeadingProvider.Listener, LocationHelper.Callback {

    private static final int REQ_LOCATION = 1;

    private ArrowView arrowView;
    private HeadingProvider provider;
    private LocationHelper location;

    private boolean hasLoc;
    private double qiblaBearing;   // from true north
    private double distanceKm;
    private float declination;     // magnetic -> true north
    private String coordText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 1. Arrow View setup
        arrowView = new ArrowView(this);
        arrowView.setOnClickListener(v -> {
            if (!hasLoc) requestLocation();
        });

        // 2. Refresh Location Button Setup
        TextView refreshBtn = new TextView(this);
        refreshBtn.setText("↻ Refresh Location");
        refreshBtn.setTextColor(Color.parseColor("#2ECC71"));
        refreshBtn.setTextSize(14);
        refreshBtn.setTypeface(Typeface.DEFAULT_BOLD);
        refreshBtn.setPadding(dp(16), dp(10), dp(16), dp(10));
        refreshBtn.setGravity(Gravity.CENTER);

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#1A0D1F14"));
        btnBg.setStroke(dp(1), Color.parseColor("#2ECC71"));
        btnBg.setCornerRadius(dp(20));
        refreshBtn.setBackground(btnBg);
        refreshBtn.setClickable(true);

        refreshBtn.setOnClickListener(v -> {
            requestLocation();
            Toast.makeText(this, "Refreshing Location...", Toast.LENGTH_SHORT).show();
        });

        // 3. Position button arrow ke neeche aur qibla info ke ooper
        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        btnParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        btnParams.setMargins(0, 0, 0, dp(300)); // Screen ke bottom-info ke hisab se height adjust ki gayi hai

        // 4. Combine into FrameLayout
        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.addView(arrowView);
        mainLayout.addView(refreshBtn, btnParams);

        setContentView(mainLayout);

        provider = new HeadingProvider(this, this, 0.15);
        location = new LocationHelper(this, this);
        if (location.hasSaved) onLocation(location.lat, location.lon); // works offline from the last position

        showState(0f, 0f);
        if (!HeadingProvider.isSupported(this)) {
            arrowView.setState(false, 0, 0, 0, "", "", "", "This device has no compass sensor");
        } else {
            requestLocation();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void requestLocation() {
        if (location.hasPermission()) {
            location.start();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            location.start();
        }
    }

    /** New position from GPS (or the saved one). */
    @Override
    public void onLocation(double lat, double lon) {
        hasLoc = true;
        qiblaBearing = Qibla.bearing(lat, lon);
        distanceKm = Qibla.distanceKm(lat, lon);
        declination = new GeomagneticField((float) lat, (float) lon, 0f,
                System.currentTimeMillis()).getDeclination();
        coordText = LocationHelper.dms(lat, true) + "    " + LocationHelper.dms(lon, false);
    }

    private void showState(float trueHeading, float rel) {
        if (hasLoc) {
            arrowView.setState(true, qiblaBearing, rel, trueHeading,
                    String.format(Locale.US, "%,d km", Math.round(distanceKm)),
                    coordText, location.statusLine(), "");
        } else {
            String message;
            if (!location.hasPermission()) {
                message = "Allow location (GPS) to find the Qibla\n(tap the screen or refresh button to try again)";
            } else if (!location.isLocationOn()) {
                message = "Turn on Location (GPS) in your phone settings";
            } else {
                message = "Searching for GPS…\nGo outside or near a window for the first fix.\nNo internet needed.";
            }
            arrowView.setState(false, 0, 0, 0, "", "", location.statusLine(), message);
        }
    }

    @Override
    public void onHeading(float magneticDegrees) {
        float trueHeading = (magneticDegrees + declination + 360f) % 360f;
        float rel = hasLoc ? Qibla.relative(qiblaBearing, trueHeading) : 0f;
        showState(trueHeading, rel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        provider.start(SensorManager.SENSOR_DELAY_GAME);
        if (location.hasPermission()) location.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        provider.stop();
        location.stop();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
