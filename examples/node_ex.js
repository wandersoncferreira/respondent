var x = require("../out/respondent/respondent");

// first stream
var es1 = x.event_stream();
x.subscribe(es1, function (event) {
  console.log("first event stream emitted: ", event);
});
x.deliver(es1, 10);

// second stream
var es2 = x.map(es1, function (item) {
  return item * 2;
});
x.subscribe(es2, function (event) {
  console.log("second event stream emitted: ", event);
});

x.deliver(es1, 20);

// behavior
var timeBehavior = x.behavior(Date.now);
var timeStreams = x.sample(timeBehavior, 1000);
var token = x.subscribe(timeStreams, function (event) {
  console.log("Time is ", event);
});
