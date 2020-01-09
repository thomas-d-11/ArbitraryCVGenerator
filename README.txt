# Arbitrary CV Generator
Android app that lets you draw arbitrary control voltage shapes on screen and outputs them to the headphone out

This app is primarily intended for use with Eurorack-style modular synths. For all output signals, be sure to amplify
the signal to proper Eurorack levels. For output signals below ~20Hz, be sure to also process the signal with an
envelope follower or slew limiter (such as a Make Noise Maths module). This is because the headphone out on the 
smartphone I've used for testing is only able to output audio-rate signals, and so for sub-audio-rate signals the app
outputs an audio-rate signal whose amplitude envelope matches the desired sub-audio-rate signal.


Current State (as of Sept. 30, 2018): Beta. Core functionality is working. Bug where app is outputting in mono, not stereo.
To be tested whether app layout scales properly to uncommon screen sizes.

# License
Note that everything in this repo is licensed under the GNU GPLv3.
