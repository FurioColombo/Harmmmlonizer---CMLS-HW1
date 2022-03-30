(
~voiceNumber = 3;
~maxDelayTime = 2.0;
~inputAudioBus = Bus.audio(s, 1);
~pitchRatioControlBuses = Array.fill(~voiceNumber, {arg i; Bus.control(s, 1)});
~stereoPanControlBuses = Array.fill(~voiceNumber, {arg i; Bus.control(s, 1)});
~modeSelectionBuses = Array.fill(~voiceNumber, {arg i; Bus.control(s, 1)}); // This bus controls which mode is active
~pitchfbModeControlBuses = Array.fill(~voiceNumber, {arg i; Bus.control(s, 1)}); 
~delayedVoiceBuses = Array.fill(~voiceNumber, {arg i; Bus.audio(s, 1)});
// ~feedbackBuses = Array.fill(~voiceNumber, {arg i; Bus.audio(s, 1)});
~pitchShiftedVoiceBuses = Array.fill(~voiceNumber, {arg i; Bus.audio(s, 1)});

);

(
SynthDef.new(\testInputSignalGenerator, {
	Out.ar(
		bus: ~inputAudioBus,
		channelsArray: SinOsc.ar(200) * Line.kr(1,0,120)
	);
}).add;
);

(
SynthDef.new(\soundIn, {
	Out.ar(
		bus: ~inputAudioBus,
		channelsArray: SoundIn.ar(0);
	);
}).add;
);

(
SynthDef.new(\pitchDetection, {
	var input, freq, hasFreq;
	input = In.ar(~inputAudioBus, 1);
	//# freq, hasFreq = Tartini.kr(in: input, threshold: 0.93, n: 2048, k: 0, overlap: 1024, smallCutoff: 0.5);
	# freq, hasFreq = Pitch.kr(
		input: input,
		initFreq: 440.0,
		minFreq: 60.0,
		maxFreq: 4000.0,
		execFreq: 100.0,
		maxBinsPerOctave: 16,
		median: 1,
		peakThreshold: 0.5,
		downSample: 1,
		clar: 0
	);
	// Calculate pitch ratios based on freq and knobs values and write them to buses


}).add
);

(
SynthDef.new(\pitchShifter, { arg channelIndex, gain = 0.0;

	var input, delayedSignal, pitchShiftInput, isPitchShiftFeedbackMode, pitchRatio, pitchShiftedSignal, isCrossFeedback, selectedMode;

	input = In.ar(~inputAudioBus, 1);
	pitchRatio = In.kr(Select.kr(channelIndex, ~pitchRatioControlBuses), 1);
	postln('pitch ratio: ');
	postln(pitchRatio.value);
	isPitchShiftFeedbackMode = In.kr(Select.kr(channelIndex, ~pitchfbModeControlBuses), 1);

	/*
	if (false, // todo select
		{ var delayedSignal = In.ar(Select.kr(channelIndex, ~delayedVoiceBuses), 1);
			pitchShiftInput = input + delayedSignal },
		{ pitchShiftInput = input }
	);
	*/

	delayedSignal = In.ar(Select.kr(channelIndex, ~delayedVoiceBuses), 1);
	selectedMode = In.kr(Select.kr(channelIndex, ~modeSelectionBuses), 1);

	pitchShiftInput = Select(selectedMode,
		[
		input + delayedSignal,
		input
		]
	);
	//pitchShiftInput = input;

	pitchShiftedSignal = PitchShift.ar(
			in: pitchShiftInput,
			windowSize: 0.075,
			pitchRatio: (1.059463094).pow(pitchRatio),
			pitchDispersion: 0.0001,
			timeDispersion: 0.075,
			mul: gain
	);

	Out.ar(Select.kr(channelIndex, ~pitchShiftedVoiceBuses), pitchShiftedSignal);
}).add;
);

(
SynthDef.new(\feedbackDelayLine, { arg channelIndex, delayTime = 0.014, feedbackAmount = 0.0;

	var input, feedbackNode, delayedSignal, feedbackSignal, pitchShiftedSignal, selectedMode;

	input = In.ar(~inputAudioBus, 1);
	feedbackNode = FbNode(numChannels: 1, maxdelaytime: ~maxDelayTime, interpolation: 2);
	delayedSignal = feedbackNode.delay(delayTime);
	pitchShiftedSignal = In.ar(Select.kr(channelIndex, ~pitchShiftedVoiceBuses), 1);

	/*
	if (false, // todo: select
		{ feedbackSignal = input + feedbackAmount*pitchShiftedSignal },
		{ feedbackSignal = feedbackAmount*(pitchShiftedSignal + delayedSignal) }
	);
	*/

	selectedMode = In.kr(Select.kr(channelIndex, ~modeSelectionBuses), 1);

	feedbackSignal = Select( selectedMode,
		[
		input + feedbackAmount*pitchShiftedSignal,
		feedbackAmount*(pitchShiftedSignal + delayedSignal)
		]
	);
	//todo remove
	feedbackSignal = feedbackAmount*(pitchShiftedSignal + delayedSignal);

	/*if (false, {
		~feedbackBuses.do({ arg item, i;
			if (item !== feedbackBus, {
				var crossFeedbackSignal = In.ar(item, 1);
				delayedSignal = delayedSignal + feedbackAmount*crossFeedbackSignal;
			});
		});
	});*/

	feedbackNode.write(feedbackSignal);
	Out.ar(Select.kr(channelIndex, ~delayedVoiceBuses), delayedSignal);
	// Out.ar(Select.kr(channelIndex, ~feedbackBuses), feedbackSignal);
}).add;
);

(
SynthDef.new(\mixer, { arg master = 1, wet = 0;
	var stereoInput, stereoOutput, voiceStereoSignals, selectedMode;

	stereoInput = Pan2.ar((1 - wet) * In.ar(~inputAudioBus, 1), 0.0);
	voiceStereoSignals = ~pitchShiftedVoiceBuses.collect({
		arg pitchShiftedVoiceBus, i, delayedSignal;
		var pitchShiftedVoice, stereoPan, voiceOutput;
		stereoPan = In.kr(~stereoPanControlBuses[i], 1);
		pitchShiftedVoice = In.ar(pitchShiftedVoiceBus, 1);

		/*
		if (false,
			{ voiceOutput = pitchShiftedVoice },
			{ var delayedSignal = In.ar(~delayedVoiceBuses[i], 1);
				voiceOutput = pitchShiftedVoice + delayedSignal }
		);
		*/
		delayedSignal = In.ar(~delayedVoiceBuses[i], 1);
		selectedMode = In.kr(Select.kr(i, ~modeSelectionBuses), 1);
		/*voiceOutput = Select( 1,
			[
				pitchShiftedVoice,
				pitchShiftedVoice + delayedSignal
			]
		);*/
		//todo remove
		voiceOutput = pitchShiftedVoice + delayedSignal;
		
		Pan2.ar(wet * voiceOutput, stereoPan);
	});

	stereoOutput = Mix.new(stereoInput ++ voiceStereoSignals);
	Out.ar(0, master * stereoOutput);
}).add;
);

(
var voiceChannelsGroup, voiceChannels, outputMixer, window, windowWidth, windowHeight, titleWidth, titleHeight, knobWidth, knobHeight, sliderWidth, sliderHeight, margin, voiceSectionWidth, voiceSectionYOffset, voiceSectionMargin, currentXPos, currentYPos, xOffset, masterTitle, button, buttonWidth, buttonHeight;

//x = Synth(\testInputSignalGenerator);
x = Synth(\soundIn);
voiceChannelsGroup = ParGroup.after(x);
voiceChannels = Array.fill(~voiceNumber, {
	arg i;
	var pitchShifter, feedbackDelayLine;
	pitchShifter = Synth.head(voiceChannelsGroup, \pitchShifter, [\channelIndex, i]);
	feedbackDelayLine = Synth.tail(voiceChannelsGroup, \feedbackDelayLine, [\channelIndex, i]);
	[pitchShifter, feedbackDelayLine];
});
outputMixer = Synth.after(voiceChannelsGroup, \mixer);

// GUI
Window.closeAll;
windowWidth = 1225;
windowHeight = 800;
titleWidth = 200;
titleHeight = 70;
knobWidth = 125;
knobHeight = knobWidth;
sliderWidth = 300;
sliderHeight = 50;
buttonWidth = 100;
buttonHeight = 40;
margin = 5@5;

window = Window(
	name: "Harmonizer",
	bounds: Rect(100, 100, windowWidth, windowHeight),
	resizable: false,
	border: true,
	scroll: false
);

/* ----- Master Section ----- */
currentXPos = (1200 - titleWidth)/2;
currentYPos = 50;
masterTitle = StaticText(
	parent: window,
	bounds: Rect(currentXPos, currentYPos, titleWidth, titleHeight)
);
masterTitle.string = "Master";
masterTitle.font = Font("Monaco", 30);
masterTitle.align = \center;
currentYPos = currentYPos + titleHeight;
/* ----- Master Gain Knob ----- */
currentXPos = 1200/2 - knobWidth;
EZKnob(
	parent: window,
	bounds: Rect(currentXPos, currentYPos, knobWidth, knobHeight),
	label: "Gain",
	controlSpec: ControlSpec.new(minval: 0.0, maxval: 2.0, warp: \lin, default: 1.0),
	action: {arg thisKnob; outputMixer.set(\master, thisKnob.value)},
	initVal: nil,
	initAction: false,
	labelWidth: 60,
	// knobSize: an instance of Point,
	unitWidth: 0,
	labelHeight: 20,
	layout: \vert2,
	// gap: an instance of Point,
	margin: margin
);
/* ----- Dry/Wet Knob ----- */
currentXPos = currentXPos + knobWidth;
EZKnob(
	parent: window,
	bounds: Rect(currentXPos, currentYPos, knobWidth, knobHeight),
	label: "Dry/Wet",
	controlSpec: ControlSpec.new(minval: 0.0, maxval: 1.0, warp: \lin, default: 0.5),
	action: {arg thisKnob; outputMixer.set(\wet, thisKnob.value)},
	initVal: nil,
	initAction: false,
	labelWidth: 60,
	// knobSize: an instance of Point,
	unitWidth: 0,
	labelHeight: 20,
	layout: \vert2,
	// gap: an instance of Point,
	margin: margin
);

/* ----- Voice Channels Section ----- */
voiceChannels.do({ arg voiceChannel, index;

	var title, pitchShifter, feedbackDelayLine, pitchfbModeCheckbox, crossfbModeCheckbox;
	xOffset = 55 + (400*index);

	pitchShifter = voiceChannel[0];
	feedbackDelayLine = voiceChannel[1];

	/* ----- Title ----- */
	currentXPos = xOffset + ((sliderWidth - titleWidth)/2);
	currentYPos = 280;
	title = StaticText(
		parent: window,
		bounds: Rect(currentXPos, currentYPos, titleWidth, titleHeight)
	);
	title.string = "Voice " ++ (index + 1);
	title.font = Font("Monaco", 30);
	title.align = \center;

	/* ----- First Line ----- */
	currentYPos = currentYPos + titleHeight;
	/* ----- Amount Knob ----- */
	currentXPos = xOffset + (sliderWidth/2 - knobWidth);
	EZKnob(
		parent: window,
		bounds: Rect(currentXPos, currentYPos, knobWidth, knobHeight),
		label: "Gain",
		controlSpec: ControlSpec.new(minval: 0.0, maxval: 2.0, warp: \lin, default: 0.0),
		action: {arg thisKnob; pitchShifter.set(\gain, thisKnob.value)},
		initVal: nil,
		initAction: false,
		labelWidth: 60,
		// knobSize: an instance of Point,
		unitWidth: 0,
		labelHeight: 20,
		layout: \vert2,
		// gap: an instance of Point,
		margin: margin
	);
	/* ----- Stereo Pan Knob ----- */
	currentXPos = currentXPos + knobWidth;
	EZKnob(
		parent: window,
		bounds: Rect(currentXPos, currentYPos, knobWidth, knobHeight),
		label: "Pan",
		controlSpec: ControlSpec.new(minval: -1.0, maxval: 1.0, warp: \lin, default: 0.0),
		action: {arg thisKnob; ~stereoPanControlBuses[index].set(thisKnob.value)},
		initVal: nil,
		initAction: false,
		labelWidth: 60,
		// knobSize: an instance of Point,
		unitWidth: 0,
		labelHeight: 20,
		layout: \vert2,
		// gap: an instance of Point,
		margin: margin
	);

	/* ----- Third Line ----- */
	currentYPos = currentYPos + knobHeight + 30;
	/* ----- Pitch Ratio Slider ----- */
	currentXPos = xOffset;
	EZSlider(
		parent: window,
		bounds: Rect(currentXPos, currentYPos, sliderWidth, sliderHeight),
		label: "Pitch Ratio",
		controlSpec: ControlSpec(minval: -24, maxval: 24, warp: \lin, step: 1, default: 0, units: \st),
		action: {arg thisSlider; ~pitchRatioControlBuses[index].set(thisSlider.value)},
		initVal: nil,
		initAction: false,
		labelWidth: 60,
		numberWidth: 60,
		// knobSize: an instance of Point,
		unitWidth: 30,
		labelHeight: 20,
		layout: \line2,
		// gap: an instance of Point,
		margin: margin
	);

	/* ----- Fourth Line ----- */
	currentYPos = currentYPos + sliderHeight + 30;
	/* ----- Delay Time Knob ----- */
	currentXPos = xOffset  + (sliderWidth/2 - knobWidth);
	EZKnob(
		parent: window,
		bounds: Rect(currentXPos, currentYPos, knobWidth, knobHeight),
		label: "Delay Time",
		controlSpec: ControlSpec.new(minval: 0.014, maxval: ~maxDelayTime, warp: \lin, default: 0.0),
		action: {arg thisKnob; feedbackDelayLine.set(\delayTime, thisKnob.value)},
		initVal: nil,
		initAction: false,
		labelWidth: 60,
		// knobSize: an instance of Point,
		unitWidth: 0,
		labelHeight: 20,
		layout: \vert2,
		// gap: an instance of Point,
		margin: margin
	);
	/* ----- Pitch Feedback Mode Checkbox ----- */
	/*pitchfbModeCheckbox = CheckBox(parent: window, bounds: Rect(currentXPos, currentYPos, knobWidth, knobHeight));
	pitchfbModeCheckbox.action_({
		~pitchfbModeControlBuses[index].set(\isPitchFeedbackMode, pitchfbModeCheckbox.value);
	});*/
	/* ----- Feedback Amount Knob ----- */
	currentXPos = currentXPos + knobWidth;
	EZKnob(
		parent: window,
		bounds: Rect(currentXPos, currentYPos, knobWidth, knobHeight),
		label: "Feedback",
		controlSpec: ControlSpec.new(minval: 0.0, maxval: 2.0, warp: \lin, default: 0.0),
		action: {arg thisKnob; feedbackDelayLine.set(\feedbackAmount, thisKnob.value)},
		initVal: nil,
		initAction: false,
		labelWidth: 60,
		// knobSize: an instance of Point,
		unitWidth: 0,
		labelHeight: 20,
		layout: \vert2,
		// gap: an instance of Point,
		margin: margin
	);
	/* ----- Cross Feedback Mode Button ----- */
	currentYPos = currentYPos + knobHeight + 30;
	button = Button.new(window, Rect(currentXPos - (buttonWidth/2) - 2.5, currentYPos, buttonWidth, buttonHeight)) ;
	button.states = [ ["PitchFeedback", Color.black ], ["Normal", Color.black]] ;
	button.action = ({ arg me;
		me.value.postln;
		~modeSelectionBuses[index].set(me.value);
	});

});

window.front;
window.onClose_({x.free; voiceChannelsGroup.freeAll; outputMixer.free;});

)


{SoundIn.ar(0,1)}.play;
