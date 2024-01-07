# SZJevec (SZJ translator)

SZJevec is an ML-powered Slovenian sign language (slovenski znakovni jezik - SZJ) recognition and
translation app, which can detect gestures in real time and translate them to text.

The project consists of an Android app, a custom-made dataset of Slovenian sign language gestures
and a TensorFlow model that is trained on the dataset.
The Android application comes pre-bundled with the trained model.

The project was created by [Clara Stavun](https://github.com/cstavun) and
[Erazem Kokot](https://github.com/erazemk).

## Application

TODO

## Model

TODO: About the model

*The code is written for compatibility with **Python 3.11** (latest supported by TensorFlow),
but other older versions might work as well.*

### Updating the dataset

The directory structure is as follows:

- `dataset`: Top-level directory for recorded gestures
    - `<gesture>`: Directory for each gesture (letter)
        - `001.npy`: Each frame has its own file with the detected landmarks
        - ...
        - `nnn.npy`

The trained TensorFlow model is stored as `model.keras` in the root of the repository (not in Git),
while the converted TensorFlow Lite model is stored as `app/src/main/res/assets/model.tflite`,
so that it can be directly compiled into the app (also not in Git).

TODO: Instructions on running the notebook

### Training the model

TODO

### Live-testing the model

TODO

## Attribution

While working on the project, we used many different sources:
- https://www.youtube.com/watch?v=doDUihpj6ro
- https://machinelearningmastery.com/how-to-develop-rnn-models-for-human-activity-recognition-time-series-classification/
- https://www.youtube.com/watch?v=QmtSkq3DYko
- https://mediapipe.readthedocs.io/en/latest/solutions/hands.html
- https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
- https://www.tensorflow.org/lite/android/quickstart
- https://github.com/tensorflow/examples/tree/master/lite/examples/gesture_classification/android
