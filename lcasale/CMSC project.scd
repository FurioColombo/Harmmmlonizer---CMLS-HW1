s.boot;
Server.killAll;



(
SynthDef.new(\harmonizer,
{
arg winSize = 0.075, pitchShift1 = 1.25, pitchShift2 = 1.5, pitchShift3 = 1.875, pitchDisper = 0.0001, timeDisper = 0.075, wet = 0, amp1=1, amp2=1, amp3=1, master = 1;
var in;
var shift1, shift2, shift3;
//var winSize = 0.1, pitchShift1 = 1.25, pitchShift2 = 1.5, pitchShift3 = 1.75, pitchDisper = 0, timeDisper = 0.04;
var sum;
//var freq, hasFreq;
in = 0.6*SoundIn.ar([0,1]);
//in = 0.6*(SinOsc.ar(440, 0, 1, 0));
//# freq, hasFreq = Pitch.kr(in);
//in.scope;
//FreqScope.new(400, 200, 0, server: s);
shift1 = amp1*PitchShift.ar(in, winSize, ((1.059463094).pow(pitchShift1)), pitchDisper, timeDisper);
shift2 = amp2*PitchShift.ar(in, winSize, ((1.059463094).pow(pitchShift2)), pitchDisper, timeDisper);
shift3 = amp3*PitchShift.ar(in, winSize, ((1.059463094).pow(pitchShift3)), pitchDisper, timeDisper);
sum = Mix.new([wet*shift1, wet*shift2, wet*shift3, (1-wet)*in]);
Out.ar(0, master*sum);
}
).add
)

(
var window, knobPitch1, knobPitch2, knobPitch3, knobWet, wetText, box;
var harm, shift1, shift2, shift3, knobAmp1, knobAmp2, knobAmp3, knobMaster;

/*slider = Slider.new(window, Rect(30, 150, 400, 100));
slider.action_({box.value_(slider.value)});
slider.action.value;*/
box = NumberBox(w, Rect(20, 20, 150, 20));
harm = Synth.new(\harmonizer);
window = Window.new("Knob", Rect(300,100,1000,800));
window.front;

/*knobPitch1 = Knob.new(window, Rect(30, 30, 100, 100));
knobPitch1.valueAction_(1);
knobPitch1.action_({arg me; harm.set(\pitchShift1 , me.value) });

// ---

knobPitch2 = Knob.new(window, Rect(150, 30, 100, 100));
knobPitch2.valueAction_(1);
knobPitch2.action_({arg me; harm.set(\pitchShift2 , me.value) });

// ---

knobPitch3 = Knob.new(window, Rect(270, 30, 100, 100));
knobPitch3.valueAction_(1);
knobPitch3.action_({arg me; harm.set(\pitchShift3 , me.value) });

// ---


*/

knobAmp1 = Knob.new(window, Rect(8, 60, 40, 40));
knobAmp1.valueAction_(1);
knobAmp1.action_({arg me; harm.set(\amp1 , me.value) });

knobAmp2 = Knob.new(window, Rect(8, 120, 40, 40));
knobAmp2.valueAction_(1);
knobAmp2.action_({arg me; harm.set(\amp2 , me.value) });

knobAmp3 = Knob.new(window, Rect(8, 180, 40, 40));
knobAmp3.valueAction_(1);
knobAmp3.action_({arg me; harm.set(\amp3 , me.value) });


wetText = StaticText(window, Rect(130, 340, 150, 40));
wetText.string = " Dry - Wet ";
wetText.font = Font("Monaco", 14);
wetText.align = \center;

knobWet = Knob.new(window, Rect(155, 250, 100, 100));
knobWet.valueAction_(1);
knobWet.action_({arg me; harm.set(\wet , me.value) });

knobMaster = Knob.new(window, Rect(850, 250, 100, 100));
knobMaster.valueAction_(1);
knobMaster.action_({arg me; harm.set(\master , me.value) });

wetText = StaticText(window, Rect(850, 340, 100, 40));
wetText.string = " Master ";
wetText.font = Font("Monaco", 14);
wetText.align = \center;

// ---

wetText = StaticText(window, Rect(55, 10, 300, 40));
wetText.string = " Harmonization ";
wetText.font = Font("Monaco", 20);
wetText.align = \center;

        // all features
shift1=EZSlider(window, Rect(55, 50, 300, 50)," shift 1 ", ControlSpec(-24, 24, \lin, 1, 4, \st), {|ez| harm.set( \pitchShift1, ez.value )}, unitWidth:30, numberWidth:60,layout:\line2, margin: m);

        //g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
            //Color.white, Color.yellow,nil,nil, Color.grey(0.7));
        //g.window.bounds = g.window.bounds.moveBy(180,2000);


     // all features
shift2=EZSlider(window, Rect(55, 110, 300, 50)," shift 2 ", ControlSpec(-24, 24, \lin, 1, 7, \st), {|ez| harm.set( \pitchShift2, ez.value )}, unitWidth:30, numberWidth:60,layout:\line2, margin: m);

        //g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
            //Color.white, Color.yellow,nil,nil, Color.grey(0.7));
        //g.window.bounds = g.window.bounds.moveBy(180,2000);


        // all features
shift3=EZSlider(window, Rect(55, 170, 300, 50)," shift 3 ", ControlSpec(-24, 24, \lin, 1, 11, \st), {|ez| harm.set( \pitchShift3, ez.value )}, unitWidth:30, numberWidth:60,layout:\line2, margin: m);

        //g.setColors(Color.grey,Color.white, Color.grey(0.7),Color.grey,
            //Color.white, Color.yellow,nil,nil, Color.grey(0.7));
        //g.window.bounds = g.window.bounds.moveBy(180,2000);


)



harm.free;


