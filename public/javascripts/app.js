var mediaApp = angular.module('mediaApp', []);

mediaApp.controller('MovieListCtrl', function ($scope) {
	$scope.movies = [
	                 {'title': 'Gone with the Wind', 'genre':'Romance, Drama', 'year':1939},
	                 {'title': 'The Matrix', 'genre':'Action, Sci-Fi', 'year':1999},
	                 {'title': 'Kill Bill: Vol. 1', 'genre':'Action, Crime', 'year':2003},
	                 {'title': 'The Bourne Identity', 'genre':'Action, Adventure, Mystery', 'year':2002}
	                 ];
	
	$scope.movieProp = 'title';
}); 