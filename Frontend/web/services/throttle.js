app.factory('throttle', ['$timeout', '$interval', function($timeout, $interval) {
	var defaults = {
		average: 1000,
		variance: 500,
		minWait: 500,
		maxWait: 1000
	};
	var average = defaults.average;
	var variance = defaults.variance;
	var events = [];
	var timeout = null;
	
	// we sample typing rate each seconds
	$interval(function(){
		var diffSum, previousTime, size, diffList, cummulVariance, i, j, diff, tmp;
		diffSum = 0;
		previousTime = 0;
		size = events.length;

		if (size > 1) {
			previousTime = events[0];

			diffList = [];
			for ( i = 1; i < size; i++) {
				diff = events[i] - previousTime
				diffSum += diff;
				previousTime = events[i];

				diffList.push(diff);
			}

			average = diffSum / (size - 1);

			size = diffList.length;
			cummulVariance = 0;
			for (j = 0; j < size; j++) {
				tmp = diffList[j] - average;
				cummulVariance += tmp * tmp;
			}

			variance = Math.sqrt(cummulVariance / size);
			events = [];
		} else {
			average = defaults.average;
			variance = defaults.variance;
		}
	}, 1000)

	return {
		stats: function(){
			return {
				avg: average / 1000,
				va: variance / 1000,
				wait: Math.min(defaults.minWait + average + 2 * variance, defaults.maxWait) / 1000
			}
		},
		event: function(f, minWait, maxWait) {
			minWait = minWait || defaults.minWait;
			maxWait = maxWait || defaults.maxWait;
			var interval = Math.min(minWait + average + 2 * variance, maxWait);
			events.push(new Date().getTime());

			if (timeout !== null) {
				$timeout.cancel(timeout);
			}

			timeout = $timeout(function(){
				events = [];
				f();
			}, interval);
		}
	};
}]);