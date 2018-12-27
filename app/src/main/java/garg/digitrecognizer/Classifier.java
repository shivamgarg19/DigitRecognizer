package garg.digitrecognizer;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.core.Mat;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Created by Shivam Garg on 27-12-2018.
 */

public class Classifier {

    private static final String TAG = "Classifier";
    private String ModelFile = "mnist.tflite";
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE =1;
    private static final int  DIM_HEIGHT =28;
    private static final int DIM_WIDTH = 28;
    private static final int BYTES =4;
    private Interpreter mInterpreter;
    private ByteBuffer mImgData;
    private float[][] mResult = new float[1][10];


    Classifier(Activity activity) throws IOException {
        mInterpreter = new Interpreter(loadModelFile(activity));
        mImgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_HEIGHT * DIM_WIDTH * DIM_PIXEL_SIZE * BYTES);
        mImgData.order(ByteOrder.nativeOrder());
        Log.d(TAG, " Tensorflow Lite Classifier.");
    }

    Result classify(Mat mat) {
        convertMattoTfLiteInput(mat);
        long startTime = SystemClock.uptimeMillis();
        mInterpreter.run(mImgData, mResult);
        long endTime = SystemClock.uptimeMillis();
        long timeCost = endTime - startTime;
        Log.v(TAG, "run(): result = " + Arrays.toString(mResult[0]) + ", timeCost = " + timeCost);
        return new Result(mResult[0], timeCost);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        Log.e(TAG, Arrays.toString(activity.getAssets().getLocales()));
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(ModelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertMattoTfLiteInput(Mat mat)
    {
        mImgData.rewind();
        for (int i = 0; i < DIM_HEIGHT; ++i) {
            for (int j = 0; j < DIM_WIDTH; ++j) {
                mImgData.putFloat((float)mat.get(i,j)[0]);
            }
        }
    }

}
