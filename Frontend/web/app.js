window.app = angular.module('ScalaKata', ['ui.codemirror', 'ui.layout', 'ngRoute']);

app.constant('LANGUAGE', 'scala').
  constant('VERSION', '0.9.0');
