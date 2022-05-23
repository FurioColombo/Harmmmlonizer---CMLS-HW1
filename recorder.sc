(
var recorder;
thisProcess.platform.recordingsDir = Platform.userHomeDir +/+ "sc_recordings";
recorder = Recorder.new(s);
recorder.filePrefix = "harmmmlonizer_";
recorder.recHeaderFormat = "wav";
recorder.recSampleFormat = "int24";
recorder.record(duration: 15);
)