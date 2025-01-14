package com.mapbox.mapboxsdk.testapp.activity.offline;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import timber.log.Timber;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.testapp.R;
import com.mapbox.mapboxsdk.testapp.model.other.OfflineDownloadRegionDialog;
import com.mapbox.mapboxsdk.testapp.model.other.OfflineListRegionsDialog;
import com.mapbox.mapboxsdk.testapp.utils.OfflineUtils;

import java.util.ArrayList;

public class OfflineActivity extends AppCompatActivity
        implements OfflineDownloadRegionDialog.DownloadRegionDialogListener {

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    // Style URL
    public static final String STYLE_URL = Style.MAPBOX_STREETS;

    /*
     * UI elements
     */
    private MapView mapView;
    private MapboxMap mapboxMap;
    private ProgressBar progressBar;
    private Button downloadRegion;
    private Button listRegions;

    private boolean isEndNotified;

    /*
     * Offline objects
     */
    private OfflineManager offlineManager;
    private OfflineRegion offlineRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // You can use MapboxAccountManager.setConnected(Boolean) to manually set the connectivity
        // state of your app. This will override any checks performed via the ConnectivityManager.
        //MapboxAccountManager.getInstance().setConnected(false);
        Boolean connected = MapboxAccountManager.getInstance().isConnected();
        Timber.d(String.format(MapboxConstants.MAPBOX_LOCALE,
                "MapboxAccountManager is connected: %b", connected));

        // Set up map
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setStyleUrl(STYLE_URL);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                Timber.d("Map is ready");
                OfflineActivity.this.mapboxMap = mapboxMap;

                // Set initial position to UNHQ in NYC
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(new LatLng(40.749851, -73.967966))
                                .zoom(14)
                                .bearing(0)
                                .tilt(0)
                                .build()));
            }
        });

        // The progress bar
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        // Set up button listeners
        downloadRegion = (Button) findViewById(R.id.button_download_region);
        downloadRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleDownloadRegion();
            }
        });

        listRegions = (Button) findViewById(R.id.button_list_regions);
        listRegions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleListRegions();
            }
        });

        // Set up the OfflineManager
        offlineManager = OfflineManager.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * Buttons logic
     */

    private void handleDownloadRegion() {
        Timber.d("handleDownloadRegion");

        // Show dialog
        OfflineDownloadRegionDialog offlineDownloadRegionDialog = new OfflineDownloadRegionDialog();
        offlineDownloadRegionDialog.show(getSupportFragmentManager(), "download");
    }

    private void handleListRegions() {
        Timber.d("handleListRegions");

        // Query the DB asynchronously
        offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                // Check result
                if (offlineRegions == null || offlineRegions.length == 0) {
                    Toast.makeText(OfflineActivity.this, "You have no regions yet.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get regions info
                ArrayList<String> offlineRegionsNames = new ArrayList<>();
                for (OfflineRegion offlineRegion : offlineRegions) {
                    offlineRegionsNames.add(OfflineUtils.convertRegionName(offlineRegion.getMetadata()));
                }

                // Create args
                Bundle args = new Bundle();
                args.putStringArrayList(OfflineListRegionsDialog.ITEMS, offlineRegionsNames);

                // Show dialog
                OfflineListRegionsDialog offlineListRegionsDialog = new OfflineListRegionsDialog();
                offlineListRegionsDialog.setArguments(args);
                offlineListRegionsDialog.show(getSupportFragmentManager(), "list");
            }

            @Override
            public void onError(String error) {
                Timber.e("Error: " + error);
            }
        });
    }

    /*
     * Dialogs
     */

    @Override
    public void onDownloadRegionDialogPositiveClick(final String regionName) {
        if (TextUtils.isEmpty(regionName)) {
            Toast.makeText(OfflineActivity.this, "Region name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start progress bar
        Timber.d("Download started: " + regionName);
        startProgress();

        // Definition
        LatLngBounds bounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;
        double minZoom = mapboxMap.getCameraPosition().zoom;
        double maxZoom = mapboxMap.getMaxZoomLevel();
        float pixelRatio = this.getResources().getDisplayMetrics().density;
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                STYLE_URL, bounds, minZoom, maxZoom, pixelRatio);

        // Sample way of encoding metadata from a JSONObject
        byte[] metadata =  OfflineUtils.convertRegionName(regionName);

        // Create region
        offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                Timber.d("Offline region created: " + regionName);
                OfflineActivity.this.offlineRegion = offlineRegion;
                launchDownload();
            }

            @Override
            public void onError(String error) {
                Timber.e("Error: " + error);
            }
        });
    }

    private void launchDownload() {
        // Set an observer
        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                // Compute a percentage
                double percentage = status.getRequiredResourceCount() >= 0
                    ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                        0.0;

                if (status.isComplete()) {
                    // Download complete
                    endProgress("Region downloaded successfully.");
                    return;
                } else if (status.isRequiredResourceCountPrecise()) {
                    // Switch to determinate state
                    setPercentage((int) Math.round(percentage));
                }

                // Debug
                Timber.d(String.format("%s/%s resources; %s bytes downloaded.",
                        String.valueOf(status.getCompletedResourceCount()),
                        String.valueOf(status.getRequiredResourceCount()),
                        String.valueOf(status.getCompletedResourceSize())));
            }

            @Override
            public void onError(OfflineRegionError error) {
                Timber.e("onError reason: " + error.getReason());
                Timber.e("onError message: " + error.getMessage());
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                Timber.e("Mapbox tile count limit exceeded: " + limit);
            }
        });

        // Change the region state
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }

    /*
     * Progress bar
     */

    private void startProgress() {
        // Disable buttons
        downloadRegion.setEnabled(false);
        listRegions.setEnabled(false);

        // Start and show the progress bar
        isEndNotified = false;
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }

        // Enable buttons
        downloadRegion.setEnabled(true);
        listRegions.setEnabled(true);

        // Stop and hide the progress bar
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        // Show a toast
        Toast.makeText(OfflineActivity.this, message, Toast.LENGTH_LONG).show();
    }

}
