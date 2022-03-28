
(
~inputAudioBus = Bus.audio(s, 1);
~voiceOutputBuses = Array.fill(3, {arg index; Bus.audio(s, 2)});
~feedbackBuses = Array.fill(3, {arg index; Bus.audio(s, 1)});

);

(
SynthDef.new(\testInputSignalGenerator, {
	Out.ar(
		bus: [0, ~inputAudioBus],
		channelsArray: SinOsc.ar(200) * Line.kr(1,0,2);
	);
}).add;
);


(
SynthDef.new(\testSoundIn, {
	Out.ar(
		bus: [[0,1], ~inputAudioBus],
		channelsArray: SoundIn.ar([0,1]);
	);
}).add;
);


(
SynthDef.new(\voiceChannel, { arg voiceOutputBus, feedbackBus, gain = 1, stereoPan = 0.0, pitchRatio = 0, delayTime = 0.014, feedbackAmount = 0;

	var input, voiceOutputSignal, feedbackNode, delayedSignal, feedbackSignal, pitchShiftedSignal;

	input = In.ar(~inputAudioBus, 1);
	feedbackNode = FbNode(1, delayTime);
	delayedSignal = feedbackNode.delay;

	if (false, {
		~feedbackBuses.do({ arg item, index;
			if (item.index !== feedbackBus, {
				var crossFeedbackSignal = In.ar(item, 1);
				delayedSignal = delayedSignal + feedbackAmount*crossFeedbackSignal;
			});
		});
	});

	if (true,
		{ pitchShiftedSignal = PitchShift.ar(
			in: input + delayedSignal,
			windowSize: 0.075,
			pitchRatio: (1.059463094).pow(pitchRatio),
			pitchDispersion: 0.0001,
			timeDispersion: 0.075,
			mul: gain
		);
		voiceOutputSignal = pitchShiftedSignal;
		feedbackSignal = input + feedbackAmount*voiceOutputSignal;},
		{ pitchShiftedSignal = PitchShift.ar(
			in: input,
			windowSize: 0.075,
			pitchRatio: (1.059463094).pow(pitchRatio),
			pitchDispersion: 0.0001,
			timeDispersion: 0.075,
			mul: gain
		);
		voiceOutputSignal = delayedSignal + pitchShiftedSignal;
		feedbackSignal = feedbackAmount*voiceOutputSignal;}
	);

	feedbackNode.write(feedbackSignal);
	Out.ar(feedbackBus, feedbackSignal);
	Out.ar(voiceOutputBus, Pan2.ar(voiceOutputSignal, stereoPan));
}).add;
);

(
SynthDef.new(\mixer, { arg master = 1, wet = 0;
	var input, stereoOutput, firstBusIndex;
	input = Pan2.ar(In.ar(~inputAudioBus, 1), 0);
	stereoOutput = Array.fill(2, { arg i;
		Mix.new([input[i], In.ar(~voiceOutputBuses.collect({ arg item, j;
			item.index + i;
		}), 1)]);
	});
	Out.ar([0, 1], master * stereoOutput);
}).add;
);

(
// FLOW CONTROL
var voiceChannelsGroup, voiceChannels, outputMixer, window, windowWidth, windowHeight, titleWidth, titleHeight, knobWidth, knobHeight, sliderWidth, sliderHeight, margin, voiceSectionWidth, voiceSectionYOffset, voiceSectionMargin, currentXPos, currentYPos, xOffset, masterTitle;

x = Synth(\testSoundIn);
//x = Synth(\testInputSignalGenerator);
voiceChannelsGroup = ParGroup.after(x);
voiceChannels = Array.fill(3, { arg index;
	Synth.head(voiceChannelsGroup, \voiceChannel,
		[\voiceOutputBus, ~voiceOutputBuses[index].index, \feedbackBus, ~feedbackBuses[index].index]);
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
	controlSpec: ControlSpec.new(0, 1, \lin),
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
	controlSpec: ControlSpec.new(0, 1, \lin),
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
		controlSpec: ControlSpec.new(0, 1, \lin),
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
		controlSpec: ControlSpec(-1, 1, \lin, default: 0),
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
		controlSpec: ControlSpec(-24, 24, \lin, 1, 0, \st),
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
		controlSpec: ControlSpec.new(0, 1, \lin),
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
		controlSpec: ControlSpec.new(0, 1, \lin),
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

});

window.front;
window.onClose_({x.free; voiceChannelsGroup.free; outputMixer.free;});

)

{SoundIn.ar([0,1]);}.play;