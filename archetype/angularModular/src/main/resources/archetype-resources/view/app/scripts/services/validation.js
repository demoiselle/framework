'use strict';

app.factory('ValidationService', ['AlertService', function(AlertService) {
	var service = {};
	
	service.validation = {};

	service.add = function(nome, msg) {
		if(nome == null){
			AlertService.addWithTimeout("danger",msg);
		}else{
			service.validation[nome] = msg;
		}
	};
	
	service.remove = function(nome) {
		service.validation[nome] = null;
	};
	
	service.clear = function() {
		service.validation = {};
	};
	
	service.registrarViolacoes = function(data){

		var violations=null;
		
		if (angular.isArray(data)){
			violations = data;
		}else{
			if(angular.isArray(data.constraintViolations)){
				violations = data.constraintViolations;
			}
		}
		if (angular.isArray(violations)){
			angular.forEach(violations, function(violation){	
				if(violation.message){
					service.add(violation.property, violation.message);
				}
			});
		}else{
			service.add(null,"Falha ao executar a operação.");
		}
	};
	
	return service;
}]);	
