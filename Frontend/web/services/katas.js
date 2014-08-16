app.factory("katas",["$http", function($http) {
  return function(path){
    return $http.get("/kata/scala" + path + ".scala");
  };
}]);
