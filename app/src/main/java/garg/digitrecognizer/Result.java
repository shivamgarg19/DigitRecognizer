package garg.digitrecognizer;

/**
 * Created by Shivam Garg on 27-12-2018.
 */

class Result {

    private int mDigit;
    private float mProbability;
    private long mTimeCost;

    Result(float[] result, long timeCost) {
        mDigit = maxProbIndex(result);
        mProbability = result[mDigit];
        mTimeCost = timeCost;
    }

    public int getDigit(){return mDigit;}

    public float getProbability(){return mProbability;}

    public long getTimeCost(){return mTimeCost;}

    private  int maxProbIndex(float[] probs) {
        int maxIndex = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}