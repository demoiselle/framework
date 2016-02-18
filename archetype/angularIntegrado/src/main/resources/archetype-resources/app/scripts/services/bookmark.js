'use strict';

app.factory('BookmarkService', ['$http', '$q', function ($http, $q) {
        var service = {};

        service.get = function () {
            return $http.get('api/bookmark').then(function (res) {
                return res.data;
            });
        };

        service.get = function (id) {
            return $http.get('api/bookmark/' + id).then(function (res) {
                return res.data;
            });
        };

        service.delete = function (id) {
            return $http.delete('api/bookmark/' + id).then(function (res) {
                return res.data;
            });
        };

        service.save = function (bookmark) {
            return $http({
                url: 'api/bookmark',
                method: bookmark.id ? "PUT" : "POST",
                data: bookmark
            }).then();
        };

        service.list = function (field, order, init, qtde) {
            return $http.get('api/bookmark/list/' + field + '/' + order + '/' + init + '/' + qtde).then(
                function (res) {
                    return res.data;
                }
            );

        };

        service.count = function () {
            return $http.get('api/bookmark/count').then(function (res) {
                return res.data;
            });
        };

        return service;
    }]);