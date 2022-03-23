/*
To run this example you need to install the 'Feedback' quark.
You can find a guide here: https://github.com/supercollider-quarks/quarks

Note: there's an 'ERROR: Non Boolean in test.' if you use FbNode inside a SynthDef.
For more info see: http://supercollider.sourceforge.net/wiki/index.php/If_statements_in_a_SynthDef
To solve (temporarily):
	- Open quarks folder: Quarks.openFolder
	- Open the file Fb.sc inside the Feedback folder
	- Comment out lines 94 to 97 and save
	- Launch Language -> Recompile Class Library
*/

(
SynthDef.new(\feedbackDelayLine, { arg inputBus, outBus = 0, delayTime = 0.014, feedbackAmount = 0;
	var input, feedbackNode, delayedSignal, feedbackSignal;
	input = In.ar(inputBus, 1);
	feedbackNode = FbNode(1, delayTime);
	delayedSignal = feedbackNode.delay;
	feedbackSignal = delayedSignal*feedbackAmount;
	feedbackNode.write(input + feedbackSignal);
	Out.ar(outBus, delayedSignal);
}).send(s);
);


(
~testSignalsAudioBuses = Array.fill(3, {Bus.audio(s, 1)});

SynthDef.new(\testSignalGenerator, {
	3.do({ arg index;
		Out.ar(
			bus: ~testSignalsAudioBuses[index],
			channelsArray: SinOsc.ar(100*(2**index)) * Line.kr(1,0,0.1)
		);
	});
}).send(s);
);


(
x = Synth(\testSignalGenerator);
a = Synth.after(x, \feedbackDelayLine, [\inputBus, ~testSignalsAudioBuses[0], \delayTime, 1.0]);
b = Synth.after(x, \feedbackDelayLine, [\inputBus, ~testSignalsAudioBuses[1], \delayTime, 1.0.rand]);
c = Synth.after(x, \feedbackDelayLine, [\inputBus, ~testSignalsAudioBuses[2], \delayTime, 1.0.rand]);
);
