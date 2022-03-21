
(
SynthDef.new(\harmonizer, 
{
arg winSize = 0.075, pitchShift1 = 1.25, pitchShift2 = 1.5, pitchShift3 = 1.75, pitchDisper = 0.0001, timeDisper = 0.075;
var in;	
var shift1, shift2, shift3;
//var winSize = 0.1, pitchShift1 = 1.25, pitchShift2 = 1.5, pitchShift3 = 1.75, pitchDisper = 0, timeDisper = 0.04;
var sum, midinum;
//var freq, hasFreq;
in = 0.6*SoundIn.ar([0,1]);
//# freq, hasFreq = Pitch.kr(in);
//in.scope;
//FreqScope.new(400, 200, 0, server: s);
shift1 = 2*PitchShift.ar(in, winSize, pitchShift1, pitchDisper, timeDisper);
shift2 = 0*PitchShift.ar(in, winSize, pitchShift2, pitchDisper, timeDisper);
shift3 = 0*PitchShift.ar(in, winSize, pitchShift3, pitchDisper, timeDisper);
	sum = Mix.new([shift1, shift2, shift3, in]);
	Out.ar(0, sum);
}
).add
)

(
var window, knob1, box, slider;
var harm;
slider = Slider.new(window, Rect(30, 150, 400, 100));
slider.action_({box.value_(slider.value)});
slider.action.value;
box = NumberBox(w, Rect(20, 20, 150, 20));
harm = Synth.new(\harmonizer);
window = Window.new("Knob", Rect(300,300,500,250));
window.front;
knob1 = Knob.new(window, Rect(30, 30, 100, 100));
knob1.valueAction_(4);
knob1.action_({arg me; harm.set(\pitchShift1 , me.value) });
)
