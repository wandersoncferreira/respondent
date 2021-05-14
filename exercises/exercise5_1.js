var r = require("../out/respondent/respondent");

var es1 = r.from_interval(30);
var take_es = r.take(es1, 5);
var token = r.subscribe(take_es, function (event) {
  console.log("Take values: ", event);
});

setTimeout(function () {
  r.dispose(token);
}, 300);
