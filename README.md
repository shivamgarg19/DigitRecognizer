# Digit Recognizer
## Overview
This is an android application which can recoginize digit from images of handwritten digit taken from phone camera.

Inorder to recognize hand written digits, deep learning model was trained. I used tensorflow's offical MNIST model and MNIST data set to train the Model.

To use the MNIST trained model in android device, i convert it into tensorflow lite model(which is optimized for android mobile devices)

Before feeding image taken by the camera to model, image need to preprocessed. Because the deep learning model was well trained on clean and prepocessed dataset, but Image with hand written digit taken from camera is not clean (contains noise) and not preprocessed!  I used OpenCV for Android for preprocessing.
