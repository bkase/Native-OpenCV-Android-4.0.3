package org.opencv.samples.tutorial3;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;

public class Sample3Native extends Activity {
    private static final String TAG = "Sample::Activity";

    public Sample3Native() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        SurfaceView fakeview = (SurfaceView)this.findViewById(R.id.fakeCameraView); 
        fakeview.setZOrderMediaOverlay(false); // This is the magic call that states things may be drawn over this surface 
        SurfaceView trueview = (SurfaceView) this.findViewById(R.id.cvsurface); 
        trueview.setZOrderMediaOverlay(true); // This is the magic call that tells this surface it's an overlay 
    }
}
