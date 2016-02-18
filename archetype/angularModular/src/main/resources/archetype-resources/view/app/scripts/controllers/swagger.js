'use strict';

app.controller('SwaggerController', ['$scope',
    function($scope) {
        $scope.isLoading = true;
        $scope.swaggerUrl = 'api/swagger.json';
    }]);
