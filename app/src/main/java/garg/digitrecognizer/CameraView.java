package garg.digitrecognizer;

import android.content.Context;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

/**
 * Created by Shivam Garg on 27-12-2018.
 */

public class CameraView extends JavaCameraView{

    public CameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void startCamera(){connectCamera(getWidth(), getHeight());}

    public void stopCamera(){disconnectCamera();}

}
