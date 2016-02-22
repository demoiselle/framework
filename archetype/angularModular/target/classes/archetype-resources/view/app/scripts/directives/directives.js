'use strict';
 
app.directive('uiLinhabar', ['$rootScope', '$anchorScroll', function($rootScope, $anchorScroll) {
    return {
        restrict: 'AC',
        template: '<span class="bar"></span>',
        link: function(scope, el, attrs) {
            el.addClass('linhabar hide');

            scope.$on('$routeChangeStart', function(e) {
                $anchorScroll();
                el.removeClass('hide').addClass('active');
            });
            
            scope.$on('$routeChangeSuccess', function(event, toState, toParams, fromState) {
                event.targetScope.$watch('$viewContentLoaded', function() {
                    el.addClass('hide').removeClass('active');
                })
            });

            scope.$on("loading-started", function(e) {
                el.removeClass('hide').addClass('active');
            });

            scope.$on("loading-complete", function(e) {
                el.addClass('hide').removeClass('active');
            });
        }
    }
}]);

app.directive('backButton', function() {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            element.bind('click', function() {
                history.back();
                scope.$apply();
            });
        }
    };
});

app.directive('alerts', function() {
    return {
        restrict: 'E',
        templateUrl: 'partials/alerts.html'
    };
});

app.directive("autofill", function() {
    return {
        require: "ngModel",
        link: function(scope, element, attrs, ngModel) {
            scope.$on("autofill:update", function() {
                ngModel.$setViewValue(element.val());
            });
        }
    };
});

app.directive("appVersion", ["version", function(version) {
    return function(scope, elm, attrs) {
        elm.text(version);
    };
}]);

app.directive("hasRoles", ["AuthService", function(AuthService) {
    return {
        restrict: "A",
        link: function(scope, element, attributes) {

            var paramRoles = attributes.hasRoles.split(",");

            if (!AuthService.isAuthorized(paramRoles)) {
                element.remove();
            }
        }
    };
}]);

app.directive("isLogged", ["AuthService", function(AuthService) {
    return {
        restrict: "A",
        link: function(scope, element, attributes) {
            if(!AuthService.isAuthenticated()){
                element.remove();
            }
        }
    };
}]);

app.directive("confirmButton", function($timeout) {
    return {
        restrict: 'A',
        scope: {
            actionOK: '&confirmAction',
            actionCancel: '&cancelAction'
        },
        link: function(scope, element, attrs) {
            var buttonId, html, message, nope, title, yep;
            buttonId = Math.floor(Math.random() * 10000000000);
            attrs.buttonId = buttonId;
            message = attrs.message || "Tem certeza?";
            yep = attrs.yes || "Sim";
            nope = attrs.no || "Não";
            title = attrs.title || "Confirmação";

            element.bind('click', function(e) {

                var box = bootbox.dialog({
                    message: message,
                    title: title,
                    buttons: {
                        success: {
                            label: yep,
                            className: "btn-success",
                            callback: function() {
                                $timeout(function() {
                                    scope.$apply(scope.actionOK);
                                });
                            }
                        },
                        danger: {
                            label: nope,
                            className: "btn-danger",
                            callback: function() {
                                scope.$apply(scope.actionCancel);
                            }
                        }
                    }
                });

            });
        }
    };
});

app.directive('validationMsg', ['ValidationService', function(ValidationService) {
    return {
        restrict: 'E',
        scope: {
            propriedade: '@'
        },
        template: "<div class='error text-danger' ng-show='msg'><small class='error' >{{msg}}</small></div>",
        controller: function($scope) {
            $scope.$watch(function() {
                return ValidationService.validation[$scope.propriedade];
            },
                    function(msg) {
                        $scope.msg = msg;
                    }
            );
        }
    };
}]);

app.directive("maxLength", ['$compile', 'AlertService', function($compile, AlertService) {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, elem, attrs, ctrl) {
            attrs.$set("ngTrim", "false");
            var maxlength = parseInt(attrs.maxLength, 10);
            ctrl.$parsers.push(function(value) {
                if (value !== undefined && value.length !== undefined) {
                    if (value.length > maxlength) {
                        AlertService.addWithTimeout('warning', 'O valor máximo de caracteres (' + maxlength + ') para esse campo já foi alcançado');
                        value = value.substr(0, maxlength);
                        ctrl.$setViewValue(value);
                        ctrl.$render();
                    }
                }
                return value;
            });
        }
    };
}]);

app.directive("hasRolesDisable", ["AuthService", function(AuthService) {
    return {
        restrict: "A",
        link: function(scope, element, attributes) {

            var paramRoles = attributes.hasRolesDisable.split(",");

            if (!AuthService.isAuthorized(paramRoles)) {
                angular.forEach(element.find('input, select, textarea, button, a'), function(node){ 
                    var ele = angular.element(node);
                    ele.attr("disabled", "true");                    
                });
            }
        }
    };
}]);

app.directive('ngEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.ngEnter);
                });

                event.preventDefault();
            }
        });
    };
});
