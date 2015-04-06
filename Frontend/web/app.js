window.app = angular.module('ScalaKata', ['ui.codemirror', 'ngRoute']);

app.constant('LANGUAGE', 'scala').
  constant('VERSION', '0.12.0');
