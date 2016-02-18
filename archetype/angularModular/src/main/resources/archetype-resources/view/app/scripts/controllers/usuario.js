'use strict';

app.controller('UsuarioController', ['$scope', '$location', '$routeParams', 'UsuarioService', 'AlertService', '$rootScope', 'ValidationService',
    function ($scope, $location, $routeParams, UsuarioService, AlertService, $rootScope, ValidationService) {

        $scope.count = function () {
            UsuarioService.count().then(
                function (res) {
                    $scope.totalServerItems = res.data;
                },
                function (res) {

                    var data = res.data;
                    var status = res.status;
                    var message = res.message;

                    if (status == 401) {
                        AlertService.addWithTimeout('warning', message);
                    } else if (status == 412 || status == 422) {
                        AlertService.addWithTimeout('danger', 'Preencha os campos obrigatórios!');
                        ValidationService.registrarViolacoes(data);
                    } else if (status == 403) {
                        AlertService.showMessageForbiden();
                    }

                }
            );
        };

        var id = $routeParams.id;
        var path = $location.$$url;

        if (path === '/usuario') {
            $scope.count();
        }
        ;

        if (path === '/usuario/edit') {
            $scope.usuario = {};
        }
        ;

        if (path === '/usuario/edit/' + id) {
            UsuarioService.get(id).then(
                function (res) {
                    $scope.usuario = res.data;
                },
                function (res) {

                    var data = res.data;
                    var status = res.status;
                    var message = res.message;

                    if (status == 401) {
                        AlertService.addWithTimeout('warning', message);
                    } else if (status == 412 || status == 422) {
                        AlertService.addWithTimeout('danger', 'Preencha os campos obrigatórios!');
                        ValidationService.registrarViolacoes(data);
                    } else if (status == 403) {
                        AlertService.showMessageForbiden();
                    }

                }

            );
        }

        $scope.pageChanged = function () {
            $scope.usuarios = [];
            var num = (($scope.currentPage - 1) * $scope.itemsPerPage);
            UsuarioService.list(num, $scope.itemsPerPage).then(
                function (res) {
                    $scope.usuarios = res.data;
                },
                function (res) {

                    var data = res.data;
                    var status = res.status;
                    var message = res.message;

                    if (status == 401) {
                        AlertService.addWithTimeout('warning', message);
                    } else if (status == 412 || status == 422) {
                        AlertService.addWithTimeout('danger', 'Preencha os campos obrigatórios!');
                        ValidationService.registrarViolacoes(data);
                    } else if (status == 403) {
                        AlertService.showMessageForbiden();
                    }

                }
            );
        };

        $scope.new = function () {
            $location.path('/usuario/edit');
        };

        $scope.save = function () {

            $("[id$='-message']").text("");

            UsuarioService.save($scope.usuario).then(
                function () {
                    AlertService.addWithTimeout('success', 'Usuario salvo com sucesso');
                    $location.path('/usuario');
                },
                function (res) {

                    var data = res.data;
                    var status = res.status;
                    var message = res.message;

                    if (status == 401) {
                        AlertService.addWithTimeout('warning', message);
                    } else if (status == 412 || status == 422) {
                        AlertService.addWithTimeout('danger', 'Preencha os campos obrigatórios!');
                        ValidationService.registrarViolacoes(data);
                    } else if (status == 403) {
                        AlertService.showMessageForbiden();
                    }

                }
            );
        };

        $scope.delete = function (id) {
            UsuarioService.delete(id).then(
                function () {
                    AlertService.addWithTimeout('success', 'Usuario removido com sucesso');
                    $location.path('/usuario');
                    $scope.count();
                    $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $scope.pagingOptions.currentPage);
                },
                function (res) {

                    var data = res.data;
                    var status = res.status;
                    var message = res.message;

                    if (status == 401) {
                        AlertService.addWithTimeout('warning', message);
                    } else if (status == 412 || status == 422) {
                        AlertService.addWithTimeout('danger', 'Preencha os campos obrigatórios!');
                        ValidationService.registrarViolacoes(data);
                    } else if (status == 403) {
                        AlertService.showMessageForbiden();
                    }

                }
            );
        };

        $scope.edit = function (id) {
            $rootScope.usuarioCurrentPage = $scope.pagingOptions.currentPage;
            $location.path('/usuario/edit/' + id);
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
                UsuarioService.list(field, order, init, pageSize).then(
                    function (res) {
                        $scope.usuarios = res.data;
                    },
                    function (res) {

                        var data = res.data;
                        var status = res.status;
                        var message = res.message;

                        if (status == 401) {
                            AlertService.addWithTimeout('warning', message);
                        } else if (status == 412 || status == 422) {
                            AlertService.addWithTimeout('danger', 'Preencha os campos obrigatórios!');
                            ValidationService.registrarViolacoes(data);
                        } else if (status == 403) {
                            AlertService.showMessageForbiden();
                        }

                    }
                );
            }, 100);
        };

        if ($rootScope.usuarioCurrentPage != undefined) {
            $scope.getPagedDataAsync($scope.pagingOptions.pageSize, $rootScope.usuarioCurrentPage);
            $scope.pagingOptions.currentPage = $rootScope.usuarioCurrentPage;
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
            data: 'usuarios',
            columnDefs: [{field: 'id', displayName: '', width: "50"},
                {field: 'name', displayName: 'Usuario'},
                {field: 'email', displayName: 'Email'}],
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
            if (next.originalPath.indexOf("usuario") === -1) {
                $rootScope.usuarioCurrentPage = 1;
            }
        });

    }]);