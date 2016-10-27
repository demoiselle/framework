 
DynamicManager
-------------------------------------------------- -------------------------------------------------
Responsible for Managing Scripts, its compilation and execution.

The implementation of this component is for the use of the facilities provided by the Java Scripting API to build dynamic and scripting languages ​​in which implement compatible engine with JSR-223.

Using Compiled and Invocable interfaces JSR-223 (implemented in engines) the manager can access the generically methods to compile the code and save the bytecode cache.

The script allows execution is passed a context with objects that will be accessible to the script code can be manipulated / altered by the same and made available via the same context for the calling code.

-------------------------------------------------- -------------------------------------------------
Example of use:

 @Inject DynamicManager dm;
 
 ...

 String scriptSource = "int a = 0;"                   //code to be compiled 
 String scriptName = "teste.groovy";				  //id to scriptCache
 
 if( dm.getScript(scriptName) == null )	{			  //verify if is a cached script
 	dm.loadEngine("groovy");						  //the name of JSR-223 engine to load.                
 	dm.loadScript ( "testeGroovy", scriptSource);     //load the script into dynamicManager cache.    	
 }						
 
 dm.eval ( "testeGroovy");						  	  //run the script.
 