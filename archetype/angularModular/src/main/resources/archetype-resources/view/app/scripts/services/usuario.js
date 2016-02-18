'use strict';

app.factory('UsuarioService', ['$http', function ($http) {
        var service = {};

        service.get = function () {
            return $http.get('api/user').then(function (res) {
                return res;
            });
        };

        service.get = function (id) {
            return $http.get('api/user/' + id).then(function (res) {
                return res;
            });
        };

        service.delete = function (id) {
            return $http.delete('api/user/' + id).then(function (res) {
                return res;
            });
        };

        service.save = function (bookmark) {
            return $http({
                url: 'api/user',
                method: bookmark.id ? "PUT" : "POST",
                data: bookmark
            }).then();
        };

        service.list = function (field, order, init, qtde) {
            return $http.get('api/user/list/' + field + '/' + order + '/' + init + '/' + qtde).then(
                function (res) {
                    return res;
                }
            );

        };

        service.count = function () {
            return $http.get('api/user/count').then(function (res) {
                return res;
            });
        };

        return service;
    }]);