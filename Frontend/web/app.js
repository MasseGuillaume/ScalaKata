window.app = angular.module('ScalaKata', ['ui.codemirror', 'ui.layout', 'ngRoute']);
app.constant('LANGUAGE', 'scala');
app.constant('VERSION', '0.5.0-SNAPHOT');
//
// app.config(['$routeProvider', '$locationProvider',
//   function ( $routeProvider ,  $locationProvider) {
//   $locationProvider.html5Mode(true);
// }]);

app.config( ['$routeProvider', '$locationProvider',
   function ( $routeProvider ,  $locationProvider) {
      $locationProvider.html5Mode(true);
}]);
