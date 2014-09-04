window.app = angular.module('ScalaKata', ['ui.codemirror', 'ui.layout', 'ngRoute']);
app.constant('LANGUAGE', 'scala');
app.constant('VERSION', '0.7.0');
//
// app.config(['$routeProvider', '$locationProvider',
//   function ( $routeProvider ,  $locationProvider) {
//   $locationProvider.html5Mode(true);
// }]);

app.config( ['$routeProvider', '$locationProvider',
   function ( $routeProvider ,  $locationProvider) {
      $locationProvider.html5Mode(true);
}]);
