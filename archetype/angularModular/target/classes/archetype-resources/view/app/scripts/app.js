'use strict';

var app = angular.module('bookmark', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngAnimate',
    'ngTouch',
    'ui.bootstrap',
    'ngGrid',
    'swaggerUi',
    'notification',
    'config'
]).config(['$routeProvider', 'USER_ROLES',
    function ($routeProvider, USER_ROLES) {

        $routeProvider

            .when('/', {
                templateUrl: 'views/dashboard/dashboard.html',
                controller: 'DashboardController',
                data: {
                    authorizedRoles: [USER_ROLES.NOT_LOGGED]
                }
            })

            .when('/login', {
                templateUrl: 'views/login.html',
                controller: 'AuthController',
                data: {
                    authorizedRoles: [USER_ROLES.NOT_LOGGED]
                }
            })

            .when('/dashboard', {
                templateUrl: 'views/dashboard/dashboard.html',
                controller: 'DashboardController',
                data: {
                    authorizedRoles: [USER_ROLES.NOT_LOGGED]
                }
            })

            .when('/403', {
                templateUrl: 'views/403.html',
                data: {
                    authorizedRoles: [USER_ROLES.NOT_LOGGED]
                }
            })


            .when('/bookmark', {
                templateUrl: 'views/bookmark/listar.html',
                controller: 'BookmarkController',
                data: {
                    authorizedRoles: [USER_ROLES.NOT_LOGGED]
                }
            })

            .when('/bookmark/edit', {
                templateUrl: 'views/bookmark/edit.html',
                controller: 'BookmarkController',
                data: {
                    authorizedRoles: [USER_ROLES.ADMINISTRADOR]
                }
            })

            .when('/bookmark/edit/:id', {
                templateUrl: 'views/bookmark/edit.html',
                controller: 'BookmarkController',
                data: {
                    authorizedRoles: [USER_ROLES.NOT_LOGGED]
                }
            })

            .when('/usuario', {
                templateUrl: 'views/usuario/listar.html',
                controller: 'UsuarioController',
                data: {
                    authorizedRoles: [USER_ROLES.ADMINISTRADOR]
                }
            })



            .when('/swagger', {
                templateUrl: 'views/swagger.html',
                controller: 'SwaggerController',
                data: {authorizedRoles: [USER_ROLES.NOT_LOGGED]
                }
            })

            .otherwise({
                redirectTo: '/dashboard',
                data: {
                    authorizedRoles: [USER_ROLES.NOT_LOGGED]
                }
            });


    }]);

app.config(['$httpProvider', function ($httpProvider) {

        $httpProvider.interceptors.push(['$q', '$rootScope', 'AppService', 'ENV', function ($q, $rootScope, AppService, ENV) {
                return {
                    'request': function (config) {
                        $rootScope.$broadcast('loading-started');

                        var token = AppService.getToken();

                        if (ENV.name == "development") {
                            if (config.url.indexOf("api") !== -1) {
                                config.url = ENV.apiEndpoint + config.url;
                            }
                        }

                        if (token) {
                            config.headers['Authorization'] = "Token " + token;
                        }

                        return config || $q.when(config);
                    },
                    'response': function (response) {
                        $rootScope.$broadcast('loading-complete');
                        return response || $q.when(response);
                    },
                    'responseError': function (rejection) {
                        $rootScope.$broadcast('loading-complete');
                        return $q.reject(rejection);
                    },
                    'requestError': function (rejection) {
                        $rootScope.$broadcast('loading-complete');
                        return $q.reject(rejection);
                    }
                };
            }]);

        $httpProvider.interceptors.push(['$injector', function ($injector) {
                return $injector.get('AuthInterceptor');
            }]);

    }]);

app.run(['$rootScope', '$location', '$window', 'AUTH_EVENTS', 'APP_EVENTS', 'USER_ROLES', 'AuthService', 'AppService', 'AlertService',
    function ($rootScope, $location, $window, AUTH_EVENTS, APP_EVENTS, USER_ROLES, AuthService, AppService, AlertService) {

        $rootScope.$on('$routeChangeStart', function (event, next) {

            if (next.redirectTo !== '/') {
                var authorizedRoles = next.data.authorizedRoles;

                if (authorizedRoles.indexOf(USER_ROLES.NOT_LOGGED) === -1) {

                    if (!AuthService.isAuthorized(authorizedRoles)) {
                        event.preventDefault();
                        if (AuthService.isAuthenticated()) {
                            // user is not allowed
                            $rootScope.$broadcast(AUTH_EVENTS.notAuthorized);
                        } else {
                            // user is not logged in
                            $rootScope.$broadcast(AUTH_EVENTS.notAuthenticated);
                        }
                    }
                }
            }
        });


        $rootScope.$on(AUTH_EVENTS.notAuthorized, function () {
            $location.path("/403");
        });

        $rootScope.$on(AUTH_EVENTS.notAuthenticated, function () {
            $rootScope.currentUser = null;
            AppService.removeToken();
            $location.path("/login");
        });

        $rootScope.$on(AUTH_EVENTS.loginFailed, function () {
            AppService.removeToken();
            $location.path("/login");
        });

        $rootScope.$on(AUTH_EVENTS.logoutSuccess, function () {
            $rootScope.currentUser = null;
            AppService.removeToken();
            $location.path("/dashboard");
        });

        $rootScope.$on(AUTH_EVENTS.loginSuccess, function () {
            $location.path("/dashboard");
        });

        $rootScope.$on(APP_EVENTS.offline, function () {
            AlertService.clear();
            AlertService.addWithTimeout('danger', 'Servidor esta temporariamente indisponível, tente mais tarde');
        });

        // Check if a new cache is available on page load.
        $window.addEventListener('load', function (e) {
            $window.applicationCache.addEventListener('updateready', function (e) {
                if ($window.applicationCache.status === $window.applicationCache.UPDATEREADY) {
                    // Browser downloaded a new app cache.
                    $window.location.reload();
                    alert('Uma nova versão será carregada!');
                }
            }, false);
        }, false);

    }]);

app.constant('APP_EVENTS', {
    offline: 'app-events-offline'
});

app.constant('AUTH_EVENTS', {
    loginSuccess: 'auth-login-success',
    loginFailed: 'auth-login-failed',
    logoutSuccess: 'auth-logout-success',
    sessionTimeout: 'auth-session-timeout',
    notAuthenticated: 'auth-not-authenticated',
    notAuthorized: 'auth-not-authorized'
});

app.constant('USER_ROLES', {
    ADMINISTRADOR: 'ADMINISTRADOR',
    FUNCIONARIO: 'FUNCIONARIO',
    CLIENTE: 'CLIENTE',
    NOT_LOGGED: 'NOT_LOGGED'
});

app.constant('LAYOUTS', [
    {name: 'Cerulean', url: 'cerulean'},
    {name: 'Cosmos', url: 'cosmos'},
    {name: 'Cyborg', url: 'cyborg'},
    {name: 'Darkly', url: 'darkly'},
    {name: 'Default', url: 'default'},
    {name: 'Flatly', url: 'flatly'},
    {name: 'Journal', url: 'journal'},
    {name: 'Lumen', url: 'lumen'},
    {name: 'Material', url: 'material'},
    {name: 'Readable', url: 'readable'},
    {name: 'Sandstone', url: 'sandstone'},
    {name: 'Simplex', url: 'simplex'},
    {name: 'Slate', url: 'slate'},
    {name: 'Spacelab', url: 'spacelab'},
    {name: 'Superhero', url: 'superhero'},
    {name: 'United', url: 'united'},
    {name: 'Yeti', url: 'yeti'}
]);

app.factory('AuthInterceptor', ['$rootScope', '$q', 'AUTH_EVENTS', 'APP_EVENTS',
    function ($rootScope, $q, AUTH_EVENTS, APP_EVENTS) {

        return {
            responseError: function (response) {
                $rootScope.$broadcast({
                    '-1': APP_EVENTS.offline,
                    0: APP_EVENTS.offline,
                    404: APP_EVENTS.offline,
                    503: APP_EVENTS.offline,
                    401: AUTH_EVENTS.notAuthenticated,
                    //403: AUTH_EVENTS.notAuthorized,
                    419: AUTH_EVENTS.sessionTimeout,
                    440: AUTH_EVENTS.sessionTimeout
                }[response.status], response);

                return $q.reject(response);
            }
        };

    }]);





