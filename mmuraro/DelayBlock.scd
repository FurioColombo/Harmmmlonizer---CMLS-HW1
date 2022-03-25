// DELAY BLOCK IMPLEMENTATION

/* The code implements the delay block of the harmonizer. 
Two different feedback nodes are implemented within the DelayLine SynthDef 
so that it's possible for the user to control the delay to the left channel and the right channel separately. 
In addition to this, I implemented a GUI to control all the available parameters 
and test the different setup the user can create. 
Moreover, a bus implementing a reverb node is available for the delayed signal. */

s.boot;
s.quit;

(
~inBus = Bus.audio(s, 1);

~reverbBus = Bus.audio(s, 1);
{ Out.ar([0, 1], FreeVerb.ar(In.ar(~reverbBus, 1), mix: 0.5, room: 0.9, mul: 0.4)) }.play;
)

// DELAY BLOCK

(
SynthDef(\delayLine, {
	|
	inputBus,
	delayTimeL = 0.8,
	delayTimeR = 0.6,
	feedbackAmount = 0.2,
	effectBus,
	outputDry = 0.8,
	delayWetL = 0.4,
	delayWetR = 0.4,
	reverbWet = 0.25
	|
	var inputSignal;
	var feedbackNodeL, feedbackNodeR;
	var delayedSignalL, delayedSignalR;
	var feedbackSignalL, feedbackSignalR;
	var mixedOutputSignal;

	inputSignal = In.ar(inputBus, 1);
	feedbackNodeL = FbNode(numChannels: 1, maxdelaytime: delayTimeL, interpolation: 2);
	feedbackNodeR = FbNode(numChannels: 1, maxdelaytime: delayTimeR, interpolation: 2);
	delayedSignalL = feedbackNodeL.delay * delayWetL;
	delayedSignalR = feedbackNodeR.delay * delayWetR;
	feedbackSignalL = delayedSignalL * feedbackAmount + (inputSignal * 0.4);
	feedbackSignalR = delayedSignalR * feedbackAmount + (inputSignal * 0.4);

	feedbackNodeL.write(feedbackSignalL);
	feedbackNodeR.write(feedbackSignalR);

	mixedOutputSignal = Mix.ar([delayedSignalL, delayedSignalR, inputSignal * 0.8]);

	Out.ar(effectBus, mixedOutputSignal * reverbWet);
	Out.ar([0, 1], inputSignal * outputDry);
	Out.ar(0, delayedSignalL);
	Out.ar(1, delayedSignalR);
	// Out.ar to write on the input bus of the Harmonizer so that we can route the feedback signal before the pitch shifter

}).add;
)

d = Synth(\delayLine, [\inputBus, ~inBus, \effectBus, ~reverbBus]);
//d.set(\delayTimeL, 1, \delayTimeR, 2);

{ Out.ar(~inBus, SoundIn.ar(1)*0.6) }.play;

// GUI

GUI.current;

(
var w;
var knobOutputDry;
var knobFeedbackAmount;
var knobDelayTimeL, knobDelayTimeR;
var knobDelayWetL, knobDelayWetR;
var knobReverbWet;

w = Window("Delay Block", Rect(100, 100, 1055, 300));

/* ----- Output Dry Knob ----- */
g = ControlSpec.new(0, 1, \lin);
knobOutputDry = EZKnob(w, Rect(30, 100, 100, 100), "Output Dry", g, initVal: 0.8);

knobOutputDry.action_({
	d.set(\outputDry, knobOutputDry.value);
});

/* ----- Feedback Amount Knob ----- */
g = ControlSpec.new(0, 1, \lin);
knobFeedbackAmount = EZKnob(w, Rect(170, 100, 100, 100), "Feedback", g, initVal: 0.2);

knobFeedbackAmount.action_({
	d.set(\feedbackAmount, knobFeedbackAmount.value);
});

/* ----- Delay Time LEFT Knob ----- */
g = ControlSpec.new(0, 1, \lin);
knobDelayTimeL = EZKnob(w, Rect(320, 100, 100, 100), "Delay Time L", g, initVal: 0.8);

knobDelayTimeL.action_({
	d.set(\delayTimeL, knobDelayTimeL.value);
});

/* ----- Delay Time RIGHT Knob ----- */
g = ControlSpec.new(0, 1, \lin);
knobDelayTimeR = EZKnob(w, Rect(470, 100, 100, 100), "Delay Time R", g, initVal: 0.6);

knobDelayTimeR.action_({
	d.set(\delayTimer, knobDelayTimeR.value);
});

/* ----- Delay Wet LEFT Knob ----- */
g = ControlSpec.new(0, 1, \lin);
knobDelayWetL = EZKnob(w, Rect(620, 100, 100, 100), "Delay Wet L", g, initVal: 0.4);

knobDelayWetL.action_({
	d.set(\delayWetL, knobDelayWetL.value);
});

/* ----- Delay Wet RIGHT Knob ----- */
g = ControlSpec.new(0, 1, \lin);
knobDelayWetR = EZKnob(w, Rect(770, 100, 100, 100), "Delay Wet R", g, initVal: 0.4);

knobDelayWetR.action_({
	d.set(\delayWetR, knobDelayWetR.value);
});

/* ----- Reverb Wet Knob ----- */
g = ControlSpec.new(0, 1, \lin);
knobReverbWet = EZKnob(w, Rect(920, 100, 100, 100), "Reverb Wet", g, initVal: 0.25);

knobReverbWet.action_({
	d.set(\reverbWet, knobReverbWet.value);
});

w.front;

w.onClose_({CmdPeriod.run;});

)


