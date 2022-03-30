(
~inputAudioBus = Bus.audio(s, 1);
~voiceOutputBuses = Array.fill(3, {arg i; Bus.audio(s, 2)});
~feedbackBuses = Array.fill(3, {arg i; Bus.audio(s, 1)});
~maxDelayTime = 2.0;
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
SynthDef.new(\voiceChannel, { arg voiceOutputBus, feedbackBus, gain = 0.0, stereoPan = 0.0, pitchRatio = 0, delayTime = 0.014, feedbackAmount = 0.0, isCrossFeedback = 0, mode_to_understand=0;

	var input, voiceOutputSignal, feedbackNode, delayedSignal, feedbackSignal, pitchShiftedSignal, crossdelayedSignal;

	input = In.ar(~inputAudioBus, 1);
	feedbackNode = FbNode(numChannels: 1, maxdelaytime: ~maxDelayTime, interpolation: 2);
	delayedSignal = feedbackNode.delay(delayTime);


	~feedbackBuses.do({ arg item, i;
		if (item !== feedbackBus, {
			var crossFeedbackSignal = In.ar(item, 1);
			crossdelayedSignal = delayedSignal + feedbackAmount*crossFeedbackSignal;
		});
	});
	
	// Choose Modes
	delayedSignal = Select.ar(
		isCrossFeedback,
		[
		feedbackNode.delay(delayTime),
		crossdelayedSignal
		],
		
	);
	
	pitchShiftedSignal = Select.ar(
		isCrossFeedback,
		[
		PitchShift.ar(
			in: input + delayedSignal, 
			windowSize: 0.075,
			pitchRatio: (1.059463094).pow(pitchRatio),
			pitchDispersion: 0.0001,
			timeDispersion: 0.075,
			mul: gain
		),
		PitchShift.ar(
			in: input, 
			windowSize: 0.075,
			pitchRatio: (1.059463094).pow(pitchRatio),
			pitchDispersion: 0.0001,
			timeDispersion: 0.075,
			mul: gain
		)
		],
	);
	
	voiceOutputSignal = Select.ar(
		isCrossFeedback,
		[
		pitchShiftedSignal,
		delayedSignal + pitchShiftedSignal
		]
	);
	
	feedbackSignal = Select.ar(
		mode_to_understand,
		[
		input + feedbackAmount*voiceOutputSignal,
		feedbackAmount*voiceOutputSignal
		]
	);

	feedbackNode.write(feedbackSignal);
	Out.ar(feedbackBus, feedbackSignal);
	Out.ar(voiceOutputBus, Pan2.ar(voiceOutputSignal, stereoPan));
}).add;
);

(
SynthDef.new(\mixer, { arg master = 1, wet = 0;
	var stereoInput, stereoOutput, voiceStereoSignals;
	stereoInput = Pan2.ar((1 - wet) * In.ar(~inputAudioBus, 1), 0.0);
	voiceStereoSignals = ~voiceOutputBuses.collect({ arg item, i; wet * In.ar(item, 2) });
	stereoOutput = Mix.new(stereoInput ++ voiceStereoSignals);
	Out.ar(0, master * stereoOutput);
}).add;
);

(
var voiceChannelsGroup, voiceChannels, outputMixer, window, windowWidth, windowHeight, titleWidth, titleHeight, knobWidth, knobHeight, sliderWidth, sliderHeight, margin, voiceSectionWidth, voiceSectionYOffset, voiceSectionMargin, currentXPos, currentYPos, xOffset, masterTitle, button, buttonWidth, buttonHeight;
// GUI
Window.closeAll;
windowWidth = 1225;
windowHeight = 850;
titleWidth = 200;
titleHeight = 70;
knobWidth = 125;
knobHeight = knobWidth;
sliderWidth = 300;
sliderHeight = 50;
buttonWidth = 100;
buttonHeight = 40;
margin = 5@5;


//x = Synth(\testInputSignalGenerator);
x = Synth(\soundIn);
voiceChannelsGroup = ParGroup.after(x);
voiceChannels = Array.fill(3, { arg i;
	Synth.head(voiceChannelsGroup, \voiceChannel,
		[\voiceOutputBus, ~voiceOutputBuses[i], \feedbackBus, ~feedbackBuses[i]]);
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
margin = 5@5;

window = Window(
	name: "Harmonizer",
	bounds: Rect(100, 100, windowWidth, windowHeight),
	resizable: false,
	border: true,
	scroll: false
);
window.view.background_(Color.new255(0, 180, 232, 255));

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

	var title, pitchfbModeCheckbox, crossfbModeCheckbox;
	xOffset = 55 + (400*index);

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
		action: {arg thisKnob; voiceChannel.set(\gain, thisKnob.value)},
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
		action: {arg thisKnob; voiceChannel.set(\stereoPan, thisKnob.value)},
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
		action: {arg thisSlider; voiceChannel.set(\pitchRatio, thisSlider.value)},
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
		action: {arg thisKnob; voiceChannel.set(\delayTime, thisKnob.value)},
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
	/*pitchfbModeCheckbox = CheckBox(parent: window, bounds: Rect(105, 100, 100, 100));
	pitchfbModeCheckbox.action_({
	voiceChannel.set(\isPitchFeedbackMode, pitchfbModeCheckbox.value);
	});*/
	/* ----- Feedback Amount Knob ----- */
	currentXPos = currentXPos + knobWidth;
	EZKnob(
		parent: window,
		bounds: Rect(currentXPos, currentYPos, knobWidth, knobHeight),
		label: "Feedback",
		controlSpec: ControlSpec.new(minval: 0.0, maxval: 2.0, warp: \lin, default: 0.0),
		action: {arg thisKnob; voiceChannel.set(\feedbackAmount, thisKnob.value)},
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
	/* ----- Cross Feedback Mode Checkbox ----- */
	/*crossfbModeCheckbox = CheckBox(parent: window, bounds: Rect(95, 100, 100, 100));
	crossfbModeCheckbox.action_({
	voiceChannel.set(\isCrossFeedbackMode, crossfbModeCheckbox.value);
	});*/
	currentYPos = currentYPos + knobHeight + 30;
	button = Button.new(window, Rect(currentXPos - (buttonWidth/2) - 2.5, currentYPos, buttonWidth, buttonHeight)) ;
	button.states = [ [ "Cross-Correlation", Color.black ], ["Normal", Color.black]] ;
	button.action = ({ arg me;
		me.value.postln;
		voiceChannel.set(\isCrossFeedback, me.value);
	});

	

});

window.front;
window.onClose_({x.free; voiceChannelsGroup.freeAll; outputMixer.free;});

)
