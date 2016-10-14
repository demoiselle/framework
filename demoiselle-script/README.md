DynamicManager 
---------------------------------------------------------------------------------------------------
Responsavel por Gerenciar os Scripts, sua compilação e execução.

A implementação desse componente visa a utilização das facilidades providas pela Java Script API para compilação e execução dinâmica de scripts em linguagens que implementem um engine compativel com a JSR-223.

Utilizando as interfaces Compiled e Invocable da JSR-223 (implementadas nos engines) o gerenciador pode acessar os metodos de maneira genérica para compilar o codigo e guardar os bytecodes em cache.

A execucao do script permite que seja passado um contexto com objetos que serão acessiveis ao codigo do script podendo ser manipuladas/alteradas pelo mesmo e disponibilizadas via esse mesmo contexto para o código chamador.

---------------------------------------------------------------------------------------------------
Exemplo de Utilização:

 String groovScriptSource = "int a=0; //codigo a ser compilado..."; 
 DynamicManager dm = new DynamicManager("groovy");
 dm.loadScript("testeGroovy", groovyScriptSource);
 dm.eval("testeGroovy");