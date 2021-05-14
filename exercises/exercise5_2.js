var r = require("../out/respondent/respondent");

var es1 = r.from_interval(1000);
var es2 = r.map(r.from_interval(1000), function (event) {
  return event * 2;
});
var zipped = r.zip(es1, es2);
var token = r.subscribe(zipped, function (event) {
  console.log("Zipped values: ", event.toString());
});

setTimeout(function () {
  r.dispose(token);
}, 6000);
