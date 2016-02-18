'use strict';

app.controller('BookmarkController', ['$scope', '$location', '$routeParams', 'BookmarkService', 'AlertService', '$rootScope', 'ValidationService',
    function ($scope, $location, $routeParams, BookmarkService, AlertService, $rootScope, ValidationService) {

        var id = $routeParams.id;

        $scope.count = function () {
            BookmarkService.count().then(
                function (data) {
                    $scope.totalServerItems = data;
                },
                function (error) {
                    var data = error[0];
                    var status = error[1];

                    if (status === 401) {
                        AlertService.addWithTimeout('warning', data.message);
                    }

                }
            );
        };

        var id = $routeParams.id;
        var path = $location.$$url;

        if (path === '/bookmark') {
            $scope.count();
        }
        ;

        if (path === '/bookmark/edit') {
            $scope.bookmark = {};
        }
        ;

        if (path === '/bookmark/edit/' + id) {
            BookmarkService.get(id).then(
                function (data) {
                    $scope.bookmark = data;
                },
                function (error) {
                    var data = error[0];
                    var status = error[1];

                    if (status === 401) {
                        AlertService.addWithTimeout('warning', data.message);
                    }

                }

            );
        }

        $scope.pageChanged = function () {
            $scope.bookmarks = [];
            var num = (($scope.currentPage - 1) * $scope.itemsPerPage);
            BookmarkService.list(num, $scope.itemsPerPage).then(
                function (data) {
                    $scope.bookmarks = data;
                },
                function (error) {
                    if (error.status === 401) {
                        AlertService.addWithTimeout('warning', error.data.message);
                    }

                }
            );
        };

        $scope.new = function () {
            $location.path('/bookmark/edit');
        };

        $scope.save = function () {

            $("[id$='-message']").text("");

            BookmarkService.save($scope.bookmark).then(
                function (data) {
                    AlertService.addWithTimeout('success', 'Bookmark salvo com sucesso');
                    $location.path('/bookmark');
                },
                function (error) {

                    var data = error[0];
                    var status = error[1];

                    if (status === 401) {
                        AlertService.addWithTimeout('danger', 'Não foi possível executar a operação');
                    } else if (status === 412 || status === 422) {
                        ValidationService.registrarViolacoes(data);
                    }

                }
            );
        };

        $scope.delete = function (id) {
            BookmarkService.delete(id).then(
                function (data) {
                    AlertService.addWithTimeout('success', 'Bookmark removido com sucesso');
                    $location.path('/bookmark');
                    $scope.count();
                    $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
                },
                function (error) {
                    var data = error[0];
                    var status = error[1];

                    if (status === 401) {
                        AlertService.addWithTimeout('warning', data.message);
                    }

                }
            );
        };

        $scope.edit = function (id) {
            $rootScope.bookmarkCurrentPage = $scope.pagingOptions.currentPage;
            $location.path('/bookmark/edit/' + id);
        };

        function buscaElemento(elemento, lista) {
            var index = -1;
            for (var i = 0; i < lista.length; i++) {
                if (lista[i].nome === elemento.nome) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        $scope.filterOptions = {
            filterText: '',
            externalFilter: 'searchText',
            useExternalFilter: true
        };

        $scope.pagingOptions = {
            pageSizes: [15],
            pageSize: 15,
            currentPage: 1
        };

        $scope.setPagingData = function (data, page, pageSize) {
            var pagedData = data.slice((page - 1) * pageSize, page * pageSize);
            $scope.myData = pagedData;
            $scope.totalServerItems = data.length;
            if (!$scope.$$phase) {
                $scope.$apply();
            }
        };

        $scope.getPagedDataAsync = function (pageSize, page) {
            var field;
            var order;
            if (typeof ($scope.sortInfo) === "undefined") {
                field = "id";
                order = "asc";
            } else {
                field = $scope.sortInfo.fields[0];
                order = $scope.sortInfo.directions[0];
            }

            setTimeout(function () {
                var init = (page - 1) * pageSize;
                BookmarkService.list(field, order, init, pageSize).then(
                    function (data) {
                        $scope.bookmarks = data;
                    },
                    function (error) {
                        var data = error[0];
                        var status = error[1];

                        if (status === 401) {
                            AlertService.addWithTimeout('warning', data.message);
                        }
                    }
                );
            }, 100);
        };

        if ($rootScope.bookmarkCurrentPage != undefined) {
            $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $rootScope.bookmarkCurrentPage);
            $scope.pagingOptions.currentPage = $rootScope.bookmarkCurrentPage;
        } else {
            $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
        }


        $scope.$watch('pagingOptions', function (newVal, oldVal) {
            if (newVal !== oldVal && newVal.currentPage !== oldVal.currentPage) {
                $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
            }
        }, true);

        $scope.$watch('filterOptions', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
            }
        }, true);

        $scope.$watch('sortInfo', function (newVal, oldVal) {
            if (newVal !== oldVal) {
                $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
            }
        }, true);

        $scope.$on('ngGridEventSorted', function (event, sortInfo) {
            $scope.sortInfo = sortInfo;
        });

        $scope.gridOptions = {
            data: 'bookmarks',
            columnDefs: [{field: 'id', displayName: '', width: "50"},
                {field: 'description', displayName: 'Descrição', width: "150"},
                {field: 'link', displayName: 'Bookmark'},
                {displayName: 'Ação', cellTemplate: '<a ng-show="!currentUser" ng-click="edit(row.entity.id)" class="btn btn-primary btn-xs"><i class="glyphicon glyphicon-eye-open"></i> Visualizar</a>\n\
                                                 <a ng-show="currentUser" ng-click="edit(row.entity.id)" class="btn btn-success btn-xs"><i class="glyphicon glyphicon-plus-sign"></i> Alterar</a>\n\
                                                 <a has-roles="ADMINISTRADOR" confirm-button title="Excluir?" confirm-action="delete(row.entity.id)" class="btn btn-danger btn-xs"><i class="glyphicon glyphicon-minus-sign"></i> Excluir</a>', width: "200"}],
            selectedItems: [],
            keepLastSelected: true,
            sortInfo: $scope.sortInfo,
            multiSelect: false,
            enablePaging: true,
            showFooter: true,
            totalServerItems: 'totalServerItems',
            pagingOptions: $scope.pagingOptions,
            enableSorting: true,
            useExternalSorting: true,
            i18n: "pt"
        };

        $scope.$on('$routeChangeStart', function (event, next) {
            if (next.originalPath.indexOf("bookmark") === -1) {
                $rootScope.bookmarkCurrentPage = 1;
            }
        });

    }]);