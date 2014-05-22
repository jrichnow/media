var mediaApp = angular.module('mediaApp', [ 'ngTable' ]);

mediaApp.controller('MovieListCtrl', function($scope, $http, $filter, ngTableParams) {
	$http.get('assets/movies.json').success(
			function(data) {
				$scope.movies = data;

				$scope.tableParams = new ngTableParams({
					page : 1,
					count : 10,
					sorting: {
			            name: 'asc'
			        }
				// count per page
				}, {
					total : data.length, 
					getData : function($defer, params) {
						var orderedData = params.sorting() ? $filter('orderBy')(data, params.orderBy()) : data;
						$defer.resolve(orderedData.slice((params.page() - 1)
								* params.count(), params.page()
								* params.count()));
					}
				});
			});
	$scope.movieProp = 'title';
});