# HarMMMLonizer  -   HW #1

HarMMMLonizer is a real-time harmonizer implemented in SuperCollide. The software implements a DSP system featuring mono input and stereo output. The DSP chain includes a delay line block which supports different feedback setups. Furthermore, a graphical user interface enables the musician to control available parameters, each specifically related to pitch shifting, delay effect and master.

## Requirements
To run the code and start experimenting with HarMMMLonizer, the user should first install the Feedback Quark. A complete guide to SuperCollider Quarks and their installation is available at the following link: 
[Quarks guide](https://github.com/supercollider-quarks/quarks)
A Quark is a package of SuperCollider code containing classes, extension methods, documentation and server UGen plugins. Installation requires a few simple steps.
For a quicker installation procedure, you should install git if you do not already have it on your computer. Otherwise, you can also download community contributed Quarks and install them manually (see link above).
A very simple way to install Quarks is using the gui. Running Quarks.gui, a window opens up showing many community contributed packages most of which are hosted on github. Thus, select Feedback Quark for installation.

When running the code, if you run into the following error ‘ERROR: Non Boolean in test.’, you can temporarily solve it following these steps:
-   	Open Quarks folder: Quarks.openFolder
-   	Open the file Fb.sc inside the Feedback folder
-   	Comment out lines 94 to 97 and save
-   	Launch Language -> Recompile Class Library
For more info see the [documentation](http://supercollider.sourceforge.net/wiki/index.php/If_statements_in_a_SynthDef)

## Software Architecture
The architecture of the whole software consists of two main components: the audio processing block, responsible for signal processing, and the graphical user interface (GUI) which implements the graphical component of the software itself.
The audio processor in turn is composed of three functional blocks which are the Harmonizer, a Delay Line and a Mixer. These blocks were implemented through SuperCollider language as SynthDefs (Synthesizer Definitions) with specific available arguments with respect to what parameters the musician needs to control through the GUI. An additional block was implemented with the purpose of collecting the input signal coming from the sound card and writing it on a specific bus so that the implemented synthesizers can read it and compute processing operations on the signal.
The graphical user interface shows three main sections, one for each harmonized voice so that the user can select the desired values for the parameters of the harmonizer and delay line. Furthermore, a mixer section is made available in order to allow the musician to control master volume of the final output and dry/wet balance between the clean input signal and harmonized voices.
The figure below illustrates the diagram of the entire software architecture.

[Architecture diagram](docs/CONTRIBUTING.md)

## Features
As disclosed before, the software features mono input and stereo output. The input could be an analogue instrument such as a guitar or a microphone, potentially routed through an external sound card. HarMMMLonizer supports three additional pitched voices to build the harmony, but a global variable within the code enables the programmer to change the number of voices.  As the software is designed, this can be made without changing the software architecture and design (see figure above). The application features a pitch shifter block, which is responsible for pitch shifting and single voices generation, and a delay line block, devoted to delay effect to be computed on the signal. As said before, each of the two blocks were implemented as SynthDefs and three instances for both are loaded into the server. Moreover, three different feedback modes are implemented within the delay line block. 

### Normal Feedback
Any input signal is routed to the Delay Line synthesizer, attenuated or boosted by feedbackAmount parameter and then again routed to the Delay Line synth, recursively.

[Architecture diagram](docs/CONTRIBUTING.md)

### Pitch Feedback
Each recursive iteration does not simply delay the signal, but applies the pitch-shift too. The result is an echo where pitch increases (or decreases) each iteration by the Pitch Ratio parameter set through the GUI. The result is finally reproduced, along with the original signal, resembling an echo-like effect.

[Architecture diagram](docs/CONTRIBUTING.md)

### Cross Feedback
The third mode was designed to generate an effect that would bounce the two output channels, exploiting stereo Pan control related to the single voice, but featuring the possibility of signal interchange between channels.

[Architecture diagram](docs/CONTRIBUTING.md)


## Graphical User Interface
The graphical user interface of the software provides the musician with three main sections:
- Master
- Pitch Shifter
- Voices section

[Architecture diagram](docs/CONTRIBUTING.md)

Master section: 
- Gain sets master volume of the final output.
- Dry/Wet controls dry/wet balance between the clean input signal and the pitched voices.
- Two meters related to input and output signals respectively are rendered so that the user is aware of potential distortion.
 
Pitch shifter control section allows the user to set Grain Size, Pitch Dispersion and Time Dispersion values. These parameters are shared between all three pitch shifter instances which process the different voices individually.

Voices section is divided into three panels, one for each voice generated by the pitch shifter. Every panel shows the following controls:
- Gain sets the gain to be applied to the single voice.
- Pan manages the stereo pan of the voice.
- Pitch Ratio slider makes it possible to set the target pitch of the voice by selecting the pitch ratio value given in number of semitones above or below the original pitch.
- Delay Time sets the delay time in milliseconds.
- Feedback is related to the feedback amount value.
- Feedback Mode button enables the artist to choose between the three different feedback setups implemented: Normal, Pitch Feedback and Cross Feedback.


***You’re now ready to jump into wonderful HarMMMLonizer world. 
Enjoy the experience!***
