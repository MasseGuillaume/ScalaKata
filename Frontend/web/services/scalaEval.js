app.factory("scalaEval",
		    ["$q", "$rootScope", "$location", "$http",
function( $q ,  $rootScope ,  $location ,  $http) {
	return {
		"initialCode": function(){
			return $http.get("/initialCode");
		},
		"insight": function(code){
			return $http.post("/eval", {"code": code});
		},
		"autocomplete": function(code, position){
			return $http.post("/completion", {"code": code, "position": position});
		},
		"typeAt": function(code, position){
			return $http.post("/typeAt", {"code": code, "position": position});
		}
	};
}]);
