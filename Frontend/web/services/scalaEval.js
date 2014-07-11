app.factory("scalaEval",
		["$q", "$rootScope", "$location", "$http",
function( $q ,  $rootScope ,  $location ,  $http) {

	var url;
	if($location.host() === "scalakata.com") {
		url = "https://codebrew.io/eval";
	} else {
		url = "/";
	}
	
	return {
		"insight": function(code){
			return $http.post(url + "eval", {"code": code});
		},
		"autocomplete": function(code, position){
			return $http.post(url + "completion", {"code": code, "position": position});
		}
	};
}]);