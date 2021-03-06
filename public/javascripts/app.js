var mediaApp = angular.module('mediaApp', [ 'ngTable', 'ui.bootstrap',
		'ngSanitize', 'angularFileUpload' ]);

mediaApp.controller('MovieListCtrl', function($scope, $http, $filter,
		ngTableParams) {
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

mediaApp.controller('MovieCtrl', function($scope, $http, $attrs, $modal) {
	$http.get('/movies/data/' + $attrs.movieid).success(
			function(data) {
				$scope.movie = data;
				$scope.movie.actorsWithUrl = $scope.createEntityUrl("Actor",
						$scope.movie.actors)
				$scope.movie.directorWithUrl = $scope.createEntityUrl(
						"Director", $scope.movie.director)
				$scope.movie.writerWithUrl = $scope.createEntityUrl("Writer",
						$scope.movie.writer)
			});

	$scope.back = function() {
		window.history.back();
	};

	$scope.createEntityUrl = function(entity, actors) {
		var actorArray = actors.split(", ")
		var actorWithUrlArray = []
		for (index = 0; index < actorArray.length; ++index) {
			var name = actorArray[index];
			var actorUrl = "";
			if (name.indexOf(' (') > 0) {
				var nameArray = name.split(' (');
				actorUrl = "<a href=\"/movies/findUi?entity=" + entity
						+ "&name=" + nameArray[0] + "\">" + nameArray[0]
						+ "</a> (" + nameArray[1];
			} else {
				actorUrl = "<a href=\"/movies/findUi?entity=" + entity
						+ "&name=" + name + "\">" + name + "</a>";
			}
			actorWithUrlArray.push(actorUrl);
		}
		return actorWithUrlArray.join(", ")
	}

	$scope.remove = function(size) {
		var modalInstance = $modal.open({
			templateUrl : 'deleteModalContent.html',
			controller : ModalInstanceCtrl,
			size : 'sm',
			resolve : {
			// Nothing to do here.
			}
		});

		modalInstance.result.then(function() {
			console.log("Deleting movie book " + $attrs.movieid);
			$http.get('/movies/delete/' + $attrs.movieid).success(
					function(data) {
						$scope.changeRoute('/movies');
					});
		}, function() {
			console.log("Do nothing");
		});
	};

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

	$scope.edit = function() {
		$scope.changeRoute('/movies/form/edit/' + $scope.movie.id);
	};
});

mediaApp.controller('MovieFindCtrl', function($scope, $http, $attrs,
		ngTableParams) {
	$http.get('/movies/find?entity=' + $attrs.entity + "&name=" + $attrs.name)
			.success(
					function(data) {
						$scope.movies = data;

						$scope.tableParams = new ngTableParams({
							page : 1,
							count : 10
						}, {
							total : data.length,
							getData : function($defer, params) {
								$defer.resolve(data.slice((params.page() - 1)
										* params.count(), params.page()
										* params.count()));
							}
						});
					});

	$scope.back = function() {
		window.history.back();
	};
});

mediaApp.controller('MovieActorCtrl', function($scope, $http, $attrs) {
	$http.get('/movies/actor/' + $attrs.name).success(function(data) {
		$scope.actor = data;
	});
});

mediaApp.controller('EditMovieCtrl', function($scope, $http, $attrs) {
	$scope.movie = {
		'title' : 'test'
	};

	$http.get('/movies/data/' + $attrs.model).success(function(data) {
		console.log("movie for editing: " + $attrs.model)
		$scope.movie = data;
	});

	$scope.back = function() {
		window.history.back();
	};

	$scope.changeRoute = function(url, forceReload) {
		$scope = $scope || angular.element(document).scope();
		if (forceReload || $scope.$$phase) {
			window.location = url;
		} else {
			$location.path(url);
			$scope.$apply();
		}
	};

	$scope.save = function() {
		console.log('editing ...')
		var request = $http({
			url : '/movies/edit',
			method : "POST",
			data : JSON.stringify($scope.movie),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.movieResponse = data;
			if (data.validation == true) {
				$scope.changeRoute(data.redirectPath);
			} else {
				$scope.status = data;
			}
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	}
});

mediaApp.controller('NewMovieCtrl', function($scope, $http) {
	// Setting some defaults:
	$scope.movie = {
		'language' : 'English',
	};

	$scope.back = function() {
		window.history.back();
	};

	$scope.changeRoute = function(url, forceReload) {
		$scope = $scope || angular.element(document).scope();
		if (forceReload || $scope.$$phase) { // that's right
			// TWO dollar
			// signs: $$phase
			window.location = url;
		} else {
			$location.path(url);
			$scope.$apply();
		}
	};

	$scope.save = function() {
		console.log('saving ...');
		var request = $http({
			url : '/movies/add',
			method : "POST",
			data : JSON.stringify($scope.movie),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.movieResponse = data;
			if (data.validation == true) {
				$scope.changeRoute('/movies');
			} else {
				$scope.status = data;
			}
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	};
});

mediaApp.controller('NewImdbMovieCtrl', function($scope, $http) {
	$scope.movie = {};

	$scope.back = function() {
		window.history.back();
	};

	$scope.changeRoute = function(url, forceReload) {
		$scope = $scope || angular.element(document).scope();
		if (forceReload || $scope.$$phase) {
			window.location = url;
		} else {
			$location.path(url);
			$scope.$apply();
		}
	};

	$scope.save = function() {
		console.log('saving ...');
		var request = $http({
			url : '/movies/addImdb',
			method : "POST",
			data : JSON.stringify($scope.movie),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.movieResponse = data;
			if (data.validation == true) {
				$scope.changeRoute(data.redirectPath);
			} else {
				$scope.status = data;
			}
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	};
});

mediaApp.controller('SearchMovieCtrl', function($scope, $http) {
	$scope.search = {
		'entity' : 'Name',
	};

	$scope.back = function() {
		window.history.back();
	};

	$scope.save = function() {
		console.log('saving ...');
		var request = $http({
			url : '/movies/search',
			method : "POST",
			data : JSON.stringify($scope.search),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.movieResponse = data;
			if (data.validation == true) {
				$scope.changeRoute(data.redirectPath);
			} else {
				$scope.status = data;
			}
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	};
});

mediaApp.controller('AudioListCtrl', function($scope, $http, audioService,
		$filter, ngTableParams) {
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

	$scope.setCurrAudio = function(currAudio) {
		audioService.setCurrentAudio(currAudio)
	};
	$scope.getCurrAudio = function() {
		audioService.getCurrentAudio()
	};
});

mediaApp.controller('RecentListCtrl', function($scope, $http, audioService,
		$filter, ngTableParams) {
	$http.get('/audio/recentList').success(
			function(data) {
				$scope.audioList = data;

				$scope.tableParams = new ngTableParams({
					page : 1,
					count : 10,
				// count per page
				}, {
					total : data.length,
					getData : function($defer, params) {
						params.total(data.length); // set total for
						// recalc pagination
						$defer.resolve(data.slice((params.page() - 1)
								* params.count(), params.page()
								* params.count()));

						$scope.audioDataSize = data.length
					}
				});
			});
	$scope.audioProp = 'title';

	$scope.setCurrAudio = function(currAudio) {
		audioService.setCurrentAudio(currAudio)
	};
	$scope.getCurrAudio = function() {
		audioService.getCurrentAudio()
	};
});

mediaApp.controller('EditAudioCtrl', function($scope, $http, $attrs) {
	$scope.audio = {
		'title' : 'test'
	};
	$http.get('/audio/details/' + $attrs.model).success(function(data) {
		$scope.audio = data;
	});

	$scope.back = function() {
		window.history.back();
	};

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

	$scope.save = function() {
		console.log('editing ...')
		var request = $http({
			url : '/audio/edit',
			method : "POST",
			data : JSON.stringify($scope.audio),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.audioResponse = data;
			if (data.validation == true) {
				$scope.changeRoute(data.redirectPath);
			} else {
				$scope.status = data;
			}
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	}
});

mediaApp.controller('NewAudioCtrl', function($scope, $http) {
	// Setting some defaults:
	$scope.audio = {
		'language' : 'English',
		'format' : 'mp3',
	};

	$scope.back = function() {
		window.history.back();
	};

	$scope.changeRoute = function(url, forceReload) {
		$scope = $scope || angular.element(document).scope();
		if (forceReload || $scope.$$phase) { // that's right
			// TWO dollar
			// signs: $$phase
			window.location = url;
		} else {
			$location.path(url);
			$scope.$apply();
		}
	};

	$scope.save = function() {
		console.log('saving ...');
		var request = $http({
			url : '/audio/add',
			method : "POST",
			data : JSON.stringify($scope.audio),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.audioResponse = data;
			if (data.validation == true) {
				$scope.changeRoute(data.redirectPath);
			} else {
				$scope.status = data;
			}
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	};
});

mediaApp.controller('AudioCtrl', function($scope, $http, $attrs, $modal,
		audioService) {
	$http.get('/audio/details/' + $attrs.title).success(function(data) {
		$scope.currentAudio = data;
	});

	$scope.syncData = function() {
		$scope.currentAudio = audioService.getCurrentAudio();
	};

	$scope.back = function() {
		window.history.back();
	};

	$scope.remove = function(size) {
		var modalInstance = $modal.open({
			templateUrl : 'deleteModalContent.html',
			controller : ModalInstanceCtrl,
			size : 'sm',
			resolve : {
			// Nothing to do here.
			}
		});

		modalInstance.result.then(function() {
			console.log("Deleting audio book " + $attrs.title);
			$http.get('/audio/delete/' + $attrs.title).success(function(data) {
				$scope.changeRoute('/audio');
			});
		}, function() {
			console.log("Do nothing");
		});

	};

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
	$scope.edit = function() {
		$scope.changeRoute('/audio/form/edit/' + $scope.currentAudio.id);
	};
});

var ModalInstanceCtrl = function($scope, $modalInstance) {
	$scope.ok = function() {
		$modalInstance.close();
	};

	$scope.cancel = function() {
		$modalInstance.dismiss('cancel');
	};
};

mediaApp.service('audioService', function() {
	var currentAudio = {};

	return {
		setCurrentAudio : function(currAudio) {
			currentAudio = currAudio;
			console.log(currentAudio);
		},
		getCurrentAudio : function() {
			console.log(currentAudio);
			return currentAudio;
		}
	}
});

mediaApp.controller('EbookListCtrl', function($scope, $http, $filter,
		ngTableParams) {
	$http.get('/ebooks/list').success(
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

						$scope.ebooksDataSize = orderedData.length
					}
				});
			});
	$scope.ebookProp = 'title';
});

mediaApp.controller('EbookCtrl', function($scope, $http, $attrs) {
	$http.get('/ebooks/details/' + $attrs.title).success(function(data) {
		$scope.ebook = data;
	});

	$scope.back = function() {
		window.history.back();
	};
});

mediaApp.controller('FileListCtrl', function($scope, $http, ngTableParams) {
	$http.get('/admin/file/list').success(
			function(data) {
				$scope.fileList = data;

				$scope.tableParams = new ngTableParams({
					page : 1,
					count : 10,
				// count per page
				}, {
					total : data.length,
					getData : function($defer, params) {
						params.total(data.length); // set total for
						// recalc pagination
						$defer.resolve(data.slice((params.page() - 1)
								* params.count(), params.page()
								* params.count()));
					}
				});
			});

});

mediaApp.controller('FileUploadCtrl', function($scope, $upload) {
	$scope.movie = ''
	$scope.onMovieFileSelect = function($files) {
		// $files: an array of files selected, each file has name, size, and
		// type.
		for (var i = 0; i < $files.length; i++) {
			var $file = $files[i];
			$upload.upload({
				url : '/admin/file/upload/movie',
				file : $file,
				progress : function(e) {
				}
			}).then(function(data, status, headers, config) {
				// file is uploaded successfully
				console.log(data);
				$scope.movie = data
			});
		}
	}
	$scope.audio = ''
	$scope.onAudioFileSelect = function($files) {
		// $files: an array of files selected, each file has name, size, and
		// type.
		for (var i = 0; i < $files.length; i++) {
			var $file = $files[i];
			$upload.upload({
				url : '/admin/file/upload/audio',
				file : $file,
				progress : function(e) {
				}
			}).then(function(data, status, headers, config) {
				// file is uploaded successfully
				console.log(data);
				$scope.audio = data
			});
		}
	}
});

mediaApp.controller('RequestCtrl', function($scope, $http) {
	$scope.req = {
		'subject' : 'Movie',
		'status' : 'New'
	};

	$scope.back = function() {
		window.history.back();
	};
	
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

	$scope.submit = function() {
		var request = $http({
			url : '/admin/request/add',
			method : "POST",
			data : JSON.stringify($scope.req),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.changeRoute('/admin/requestsUi');
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	};
});


mediaApp.controller('RequestsCtrl', function($scope, $http, $filter,
		ngTableParams, $attrs, $modal) {
	$http.get('/admin/requests').success(
			function(data) {
				$scope.requests = data;

				$scope.tableParams = new ngTableParams({
					page : 1,
					count : 10,
					sorting : {
						name : 'asc'
					},
					filter : {
						topic : ''
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
					}
				});
			});
	$scope.requestProp = 'topic';
	
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
	
	$scope.new = function() {
		$scope.changeRoute('/admin/requestUi');
	};
});

mediaApp.controller('EditRequestCtrl', function($scope, $http, $attrs) {
	$scope.req = {
		'title' : 'test'
	};

	$http.get('/admin/request/' + $attrs.model).success(function(data) {
		console.log("request for editing: " + $attrs.model)
		$scope.req = data;
	});

	$scope.back = function() {
		window.history.back();
	};
	
	$scope.changeRoute = function(url, forceReload) {
		$scope = $scope || angular.element(document).scope();
		if (forceReload || $scope.$$phase) {
			window.location = url;
		} else {
			$location.path(url);
			$scope.$apply();
		}
	};

	$scope.submit = function() {
		console.log('editing ...')
		var request = $http({
			url : '/admin/request/edit',
			method : "POST",
			data : JSON.stringify($scope.req),
			transformRequest : false,
			headers : {
				'Content-Type' : 'application/json'
			}
		}).success(function(data, status, headers, config) {
			$scope.changeRoute('/admin/requestsUi');
		}).error(function(data, status, headers, config) {
			$scope.status = status + ' ' + headers;
		});
	}
});

mediaApp.controller('ViewRequestCtrl', function($scope, $http, $attrs) {
	$http.get('/admin/request/' + $attrs.requestid).success(function(data) {
		$scope.req = data;
	});
	
	$scope.back = function() {
		window.history.back();
	};
	
	$scope.remove = function(size) {
		$scope.changeRoute('/admin/request/delete/' + $attrs.requestid);
	};

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

	$scope.edit = function() {
		$scope.changeRoute('/admin/editRequestUi/' + $scope.req.id);
	};
});