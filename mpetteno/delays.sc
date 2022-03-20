(

{
	b = Buffer.read(s, "sounds/a11wlk01.wav")


	var signal1 = PlayBuf.ar(1, b);
	//var signal2 = Impulse.ar(329.63);
	//var signal3 = Impulse.ar(392.00);
	var inputSignal = signal1;
	// var inputSignal = signal1 + signal2 + signal3;

	var globalDelayTime = 0.260;
	var feedbackAmount = 0;
	var signal1DelayTime = globalDelayTime;
	//var signal2DelayTime = globalDelayTime / 2;
	//var signal3DelayTime = globalDelayTime / 3;

	var feedbackNode = FbNode(1, globalDelayTime);

	var delaySignal1 = feedbackNode.delay(signal1DelayTime);
	//var delaySignal2 = feedbackNode.delay(signal2DelayTime);
	//var delaySignal3 = feedbackNode.delay(signal3DelayTime);
	var delaySignal = delaySignal1;
	//var delaySignal = delaySignal1 + delaySignal2 + delaySignal3;

	var feedbackSignal = delaySignal1*feedbackAmount;
	//var feedbackSignal = delaySignal*feedbackAmount;

	var outputSignal = inputSignal + delaySignal + feedbackSignal;
	feedbackNode.write(inputSignal);
	delaySignal!2;

}.play;

)


(

{
	var in = SinOsc.ar(100, Line.kr(0,2,10) );

	var fbNode = FbNode(1, 1.0);

	var signal = fbNode.delay(0.5);

	fbNode.write(in + (signal*0.1));

	// if you want, you can use FbNode as a normal multi-tap delay, just by not adding in the feedback signal here.
	//fbNode.write(in);
	//[in, signal];

	signal!2;

}.play;

)