'use strict';

app.controller('AuthController', ['$scope', '$rootScope', 'AUTH_EVENTS', 'AuthService',
    function ($scope, $rootScope, AUTH_EVENTS, AuthService) {

        $scope.credentials = {
            username: '',
            password: ''
        };

        function error(data, status) {
            $("[id$='-message']").text("");

            switch (status) {
                case 412:
                case 422:
                    $.each(data, function (i, violation) {
                        $("#" + violation.property + "-message").text(violation.message);
                    });
                    break;
                case 401:
                    $("#message").html("Usuário ou senha inválidos.");
                    break;
            }
        }


        $scope.login = function (credentials) {

            if (credentials.username && credentials.password) {

                AuthService.login(credentials).then(function () {
                    $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);

                },
                    function (response) {
                        error(response.data, response.status);
                    });
            } else {
                $("#message").html("Preencha os campos usuário e senha.");
            }
        };

        $scope.logout = function () {
            $rootScope.$broadcast(AUTH_EVENTS.logoutSuccess);
        };

    }]);