# SZJevec (SZJ translator)

SZJevec is an ML-powered Slovenian sign language (slovenski znakovni jezik - SZJ) recognition and
translation app, which can detect gestures in real time and translate them to text.

The project consists of an Android app, a custom-made dataset of Slovenian sign language gestures
and a TensorFlow model that is trained on the dataset.
The Android application comes pre-bundled with the trained model.

The project was created by [Clara Stavun](https://github.com/cstavun) and
[Erazem Kokot](https://github.com/erazemk).

## Attribution

While working on the project, we used many different sources:

- https://www.youtube.com/watch?v=doDUihpj6ro
- https://machinelearningmastery.com/how-to-develop-rnn-models-for-human-activity-recognition-time-series-classification/
- https://www.youtube.com/watch?v=QmtSkq3DYko
- https://mediapipe.readthedocs.io/en/latest/solutions/hands.html
- https://github.com/googlesamples/mediapipe/tree/main/examples/gesture_recognizer/android
- https://www.tensorflow.org/lite/android/quickstart
- https://github.com/tensorflow/examples/tree/master/lite/examples/gesture_classification/android
