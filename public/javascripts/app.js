var mediaApp = angular.module('mediaApp', [ 'ngTable' ]);

mediaApp.controller('MovieListCtrl', function($scope, $http, $filter, ngTableParams) {
//	$http.get('assets/movies.json').success(
	$http.get('/movies/list').success(
			function(data) {
				$scope.movies = data;

				$scope.tableParams = new ngTableParams({
					page : 1,
					count : 10,
					sorting: {
			            name: 'asc'
			        },
					filter: {
						title: ''
					}
				// count per page
				}, {
					total : data.length, 
					getData : function($defer, params) {
						var filteredData = params.filter() ? $filter('filter')(data, params.filter()) : data;
						var orderedData = params.sorting() ? $filter('orderBy')(filteredData, params.orderBy()) : data;
						params.total(orderedData.length); // set total for recalc pagination
						$defer.resolve(orderedData.slice((params.page() - 1)
								* params.count(), params.page()
								* params.count()));
						
						$scope.movieDataSize = orderedData.length
					}
				});
			});
	$scope.movieProp = 'title';
});