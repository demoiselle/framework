 
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
 
 
try {    	    	    	    	  
		 String scriptSource = "int a = X; X= a + a;";  	  //sourcecode to be compiled .  
		 String scriptName = "test.groovy";				      //id to scriptCache		
		 Integer valueX    = 1;								  //value to be passed to script
		 if( dm.getScript(scriptName) == null )	{			  //verify if is a cached script
			dm.loadEngine("groovy");						  //the name of JSR-223 engine to load.                				
		    dm.loadScript ( "test.groovy", scriptSource);     //load the script into dynamicManager cache.    					
		 }
		 Bindings context = new SimpleBindings();  		      //create the context to script where 'X' is a key in script to a dynamic variable.     	 													    
		 context.put("X", valueX );							  //The value can be a class too.	
		 System.out.println("The value of X is " + valueX + " before script execution.");
		 System.out.println("Running script...");
		 dm.eval ( "test.groovy",context);					  //run the script.		
		 valueX = (Integer) context.get("X");    	     	 
		 System.out.println("The value of X is " + valueX + " after script execution.");  
		 Assert.assertTrue(true);      	 	
	} catch (ScriptException e) {
		e.printStackTrace();
	}   
	
 ...	