package garg.digitrecognizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

import static java.lang.System.exit;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";
    private CameraView mOpenCVCameraView;
    private TextView mDigit, mProbabilty, mTimeCost;
    private LinearLayout mDigitLL, mProbabiltyLL, mTimeCostLL;
    private Button mDetect;
    private Mat mRGBA, mInput, mIntermediate;
    private Classifier mClassifier;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCVCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mOpenCVCameraView = (CameraView) findViewById(R.id.camera_view);
        mOpenCVCameraView.setCvCameraViewListener(this);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);

        mDigit = (TextView) findViewById(R.id.digit);
        mProbabilty = (TextView) findViewById(R.id.probability);
        mTimeCost = (TextView) findViewById(R.id.timecost);
        mDetect = (Button) findViewById(R.id.detect);

        mDigitLL = (LinearLayout) findViewById(R.id.linear_layout_digit);
        mProbabiltyLL = (LinearLayout) findViewById(R.id.linear_layout_prob);
        mTimeCostLL = (LinearLayout) findViewById(R.id.linear_layout_time);

        mDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Button Clicked");
                Toast.makeText(MainActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
                if (mClassifier != null) {
                    Result result = mClassifier.classify(mInput);
                    mDigitLL.setVisibility(View.VISIBLE);
                    mProbabiltyLL.setVisibility(View.VISIBLE);
                    mTimeCostLL.setVisibility(View.VISIBLE);
                    mDigit.setText(String.valueOf(result.getDigit()));
                    String prob = String.valueOf(result.getProbability());
                    mProbabilty.setText(prob.substring(0, 4 < prob.length() ? 4 : prob.length()));
                    mTimeCost.setText(String.valueOf(result.getTimeCost()) + " ms");

                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPermission()) requestPermission();

        //Intialize Opencv
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        //hideSystemUI();

        try {
            mClassifier = new Classifier(MainActivity.this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize an image classifier.", e);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat();
        mInput = new Mat();
        mIntermediate = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        if (mRGBA != null) mRGBA.release();
        if (mIntermediate != null) mIntermediate.release();
        if (mInput != null) mInput.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();

        // Preprocessing image before sending to classifier
        int top = mRGBA.rows() / 2 - 140;
        int left = mRGBA.cols() / 2 - 140;
        int height = 140 * 2;
        int width = 140 * 2;

        Mat gray = inputFrame.gray();
        Imgproc.rectangle(mRGBA, new Point(left, top), new Point(left + width, top + height), new Scalar(255, 255, 255), 2);
        mIntermediate = gray.submat(top, top + height, left, left + width);
        Imgproc.GaussianBlur(mIntermediate, mIntermediate, new org.opencv.core.Size(7, 7), 2, 2);
        Imgproc.adaptiveThreshold(mIntermediate, mIntermediate, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 5, 5);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(9, 9));
        Imgproc.dilate(mIntermediate, mIntermediate, element);
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(3, 3));
        Imgproc.erode(mIntermediate, mIntermediate, element1);
        Imgproc.resize(mIntermediate, mInput, new org.opencv.core.Size(28, 28));

        //Testing Only
        //Mat topConner = mRGBA.submat(0, height, 0, width);
        //Imgproc.cvtColor(mIntermediate, topConner, Imgproc.COLOR_GRAY2BGRA, 4);

        return mRGBA;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Camera Permission Granted");
                } else {
                    Log.e(TAG, "Camera Permission Denied. Closing the App.");
                    exit(0);
                }
            }
        }
    }

    private void hideSystemUI() {
        int windowVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        getWindow().getDecorView().setSystemUiVisibility(windowVisibility);
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
    }
}
