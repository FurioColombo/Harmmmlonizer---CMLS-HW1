s.boot;
s.quit;

(
~harmonizerBus = Bus.audio(s, 1);
~delayBus = Bus.audio(s, 1);

~reverbBus = Bus.audio(s, 1);
{ Out.ar([0, 1], FreeVerb.ar(In.ar(~reverbBus, 1), mix: 0.5, room: 0.9, mul: 0.4)) }.play;
)

// HARMONIZER BLOCK

(
SynthDef.new(\harmonizer,
	{
		|
		inputBus,
		outputBus,
		winSize = 0.075,
		pitchShift1 = 1.25, pitchShift2 = 1.5, pitchShift3 = 1.875,
		pitchDisper = 0.0001, timeDisper = 0.075,
		wet = 0,
		amp1=1, amp2=1, amp3=1,
		master = 1
		|
var in;
var shift1, shift2, shift3;
//var winSize = 0.1, pitchShift1 = 1.25, pitchShift2 = 1.5, pitchShift3 = 1.75, pitchDisper = 0, timeDisper = 0.04;
var sum;
//var freq, hasFreq;
in = In.ar(inputBus, 1);
//in = 0.6*(SinOsc.ar(440, 0, 1, 0));
//# freq, hasFreq = Pitch.kr(in);
//in.scope;
//FreqScope.new(400, 200, 0, server: s);
shift1 = amp1*PitchShift.ar(in, winSize, ((1.059463094).pow(pitchShift1)), pitchDisper, timeDisper);
shift2 = amp2*PitchShift.ar(in, winSize, ((1.059463094).pow(pitchShift2)), pitchDisper, timeDisper);
shift3 = amp3*PitchShift.ar(in, winSize, ((1.059463094).pow(pitchShift3)), pitchDisper, timeDisper);
sum = Mix.new([wet*shift1, wet*shift2, wet*shift3, (1-wet)*in]);
Out.ar(outputBus, master*sum);
}
).add
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
	//feedbackSignalFlow = "flatFeedback"
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

	/*
	switch(feedbackSignalFlow.value,
		"flatFeedback", { feedbackNodeL.write(feedbackSignalL);
			feedbackNodeR.write(feedbackSignalR); },
		//"beforeHarmonizer", { Out.ar(~harmonizer, Mix.ar([delayedSignalL, delayedSignalR])) },
		"crossFeedback", { feedbackNodeL.write(feedbackSignalR);
			feedbackNodeR.write(feedbackSignalL); }
	);
	*/

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

h = Synth(\harmonizer, [\inputBus, ~harmonizerBus, \outputBus, ~delayBus]);
d = Synth(\delayLine, [\inputBus, ~delayBus, \effectBus, ~reverbBus]);
//d.set(\delayTimeL, 1, \delayTimeR, 2);

{ Out.ar(~harmonizerBus, SoundIn.ar(0)*0.6) }.play;

// GUI

GUI.current;

(
var w;
var knobOutputDry;
var knobFeedbackAmount;
var knobDelayTimeL, knobDelayTimeR;
var knobDelayWetL, knobDelayWetR;
var knobReverbWet;
var feedbackModePopUp;

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

feedbackModePopUp = PopUpMenu(w, Rect(770, 50, 150, 40));
feedbackModePopUp.items = ["Flat Feedback", "Before Harmonizer", "Cross Feedback"];
feedbackModePopUp.action({
	arg menu;
	switch(menu.value,
		"Flat Feedback", { d.set(\feedbackSignalFlow, "flatFeedback") },
		"Before Harmonizer", {d.set(\feedbackSignalFlow, "beforeHarmonizer") },
		"Cross Feedback", { d.set(\feedbackSignalFlow, "crossFeedback") }
	);
});


w.front;

w.onClose_({CmdPeriod.run;});

)


