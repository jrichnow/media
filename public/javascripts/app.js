var mediaApp = angular.module('mediaApp', [ 'ngTable' ]);

mediaApp.controller('MovieListCtrl', function($scope, $http, $filter,
		ngTableParams) {
	// $http.get('assets/movies.json').success(
	$http.get('/movies/list').success(
			function(data) {
				$scope.movies = data;

				$scope.tableParams = new ngTableParams({
					page : 1,
					count : 10,
					sorting : {
						name : 'asc'
					},
					filter : {
						title : ''
					}
				// count per page
				}, {
					total : data.length,
					getData : function($defer, params) {
						var filteredData = params.filter() ? $filter('filter')(
								data, params.filter()) : data;
						var orderedData = params.sorting() ? $filter('orderBy')
								(filteredData, params.orderBy()) : data;
						params.total(orderedData.length); // set total for
															// recalc pagination
						$defer.resolve(orderedData.slice((params.page() - 1)
								* params.count(), params.page()
								* params.count()));

						$scope.movieDataSize = orderedData.length
					}
				});
			});
	$scope.movieProp = 'title';
});

mediaApp.controller('MovieCtrl', function($scope, $http, $attrs) {
	$http.get('/movies/imdb/' + $attrs.model).success(function(data) {
		$scope.movie = data;
	});

	$scope.back = function() {
		window.history.back();
	};
});

mediaApp.controller('AudioListCtrl', function($scope, $http, $filter,
		ngTableParams) {
	$http.get('/audio/list').success(
			function(data) {
				$scope.audioList = data;

				$scope.tableParams = new ngTableParams({
					page : 1,
					count : 10,
					sorting : {
						name : 'asc'
					},
					filter : {
						title : ''
					}
				// count per page
				}, {
					total : data.length,
					getData : function($defer, params) {
						var filteredData = params.filter() ? $filter('filter')(
								data, params.filter()) : data;
						var orderedData = params.sorting() ? $filter('orderBy')
								(filteredData, params.orderBy()) : data;
						params.total(orderedData.length); // set total for
						// recalc pagination
						$defer.resolve(orderedData.slice((params.page() - 1)
								* params.count(), params.page()
								* params.count()));

						$scope.audioDataSize = orderedData.length
					}
				});
			});
	$scope.audioProp = 'title';
});

mediaApp.controller('NewAudioCtrl', function($scope, $http) {
	$scope.changeRoute = function(url, forceReload) {
		$scope = $scope || angular.element(document).scope();
		if (forceReload || $scope.$$phase) { // that's right TWO dollar
			// signs: $$phase
			window.location = url;
		} else {
			$location.path(url);
			$scope.$apply();
		}
	};
	// Setting some defaults:
	$scope.audio = { 
		'title' : 'Dreamland',
		'language' : 'English',
		'format' : 'mp3'
	};
	
	$scope.audio.doList = function() {
		$scope.changeRoute('/audio')
	}
	
	$scope.audio.doClick = function(item, event) {
		var request = $http({
			url : '/audio/add',
			method : "POST",
			data : JSON.stringify($scope.audio),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.changeRoute('/audio')
			$scope.status = data
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	};
});