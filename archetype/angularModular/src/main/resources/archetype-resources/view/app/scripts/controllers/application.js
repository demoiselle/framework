'use strict';

app.controller('ApplicationController', ['$rootScope', 'USER_ROLES', 'AuthService', 'LAYOUTS',
    function ($rootScope, USER_ROLES, AuthService, LAYOUTS) {

        $rootScope.userRoles = USER_ROLES;
        $rootScope.isAuthorized = AuthService.isAuthorized;
        $rootScope.bootstraps = LAYOUTS;
        $rootScope.logados = '0';

        // set the default bootswatch name
        $rootScope.css = AuthService.getCss();

        $rootScope.setCss = function (css) {
            AuthService.setCss(css);
        };


    }]);
