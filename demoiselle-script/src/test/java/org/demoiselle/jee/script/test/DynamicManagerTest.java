/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.script.test;

import javax.inject.Inject;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.script.DynamicManager;
import org.demoiselle.jee.script.exception.DemoiselleScriptException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class DynamicManagerTest {

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	    
    @Inject DynamicManager dm = new DynamicManager();
         
    @Test
    public void testloadEngine() throws ScriptException  {        
        Assert.assertNotNull(dm.loadEngine("nashorn"));
    }
    
    @Test
    public void testUnloadEngine() throws ScriptException  {
    	expectedEx.expect(DemoiselleScriptException.class);    	
    	dm.loadEngine("nashorn");
    	dm.unloadEngine("nashorn");
    	dm.clearCache("nashorn");    	       
    }
    
    @Test
    public void testloadEngineNotvalid() throws ScriptException  {    	
    	expectedEx.expect(DemoiselleScriptException.class);    	
        dm.loadEngine("randomEngine");        
    }
       
    @Test
    public void testloadScriptAlreadyInCache() throws ScriptException  {            	         	
    	String javaScriptSource = "var a= X;  X=1 ; ";
    	         
    	dm.loadScript("nashorn","test", javaScriptSource);
        Assert.assertEquals( false , dm.loadScript("nashorn", "test", javaScriptSource) );
    }
    
    @Test
    public void testloadScript() throws ScriptException  {        
    	String javaScriptSource = "var a= X;  X=1 ; ";
    	dm.loadEngine("nashorn");                 		                                         
        Assert.assertEquals( true , dm.loadScript("nashorn","testJS", javaScriptSource));
    }
     
    @Test
    public void testCacheSize() throws ScriptException  {        
    	dm.loadEngine("nashorn");   
    	dm.clearCache("nashorn");
    	
        Assert.assertEquals(0,dm.getCacheSize("nashorn"));
    }
    
    @Test
    public void testCacheSizeNotLoadEngine()  throws ScriptException  {     
    	expectedEx.expect(DemoiselleScriptException.class);    
    	dm.getCacheSize("invalidEngine");    	
    }
    
    @Test
    public void testClearCacheNotLoadEngine()  throws ScriptException  {     
    	expectedEx.expect(DemoiselleScriptException.class);    
    	dm.clearCache("invalidEngine");    	
    }
    
    @Test
    public void compile() throws ScriptException {
    	String javaScriptSource = "var a= X;  X=1 ; ";                      
        Assert.assertNotNull(dm.compile("nashorn", javaScriptSource) );
    }
     
    @Test
    public void testGetScript() throws ScriptException {
    	String javaScriptSource = "var a= X;  X=1 ; ";
             
        dm.loadScript("nashorn","test1", javaScriptSource);
        dm.getScript("nashorn","test1");   
        Assert.assertNotNull(dm.getScript("nashorn","test1") );
    }
    
    @Test
    public void testGetScriptNotLoadEngine()  throws ScriptException  {     
    	expectedEx.expect(DemoiselleScriptException.class);    
    	dm.getScript("invalidEngine","invalidScript");    	
    }
      
    @Test
    public void testRemoveScript() throws ScriptException {
    	String javaScriptSource = "var a= X;  X=1 ; ";
             
        dm.loadScript("nashorn","test2", javaScriptSource);
        dm.removeScript("nashorn","test2");
               
        Assert.assertNull(dm.getScript("nashorn","test2"));
    }
    
    @Test
    public void testRemoveScriptNotLoadEngine()  throws ScriptException  {     
    	expectedEx.expect(DemoiselleScriptException.class);    
    	dm.removeScript("invalidEngine","invalidScript");    	
    }
        
    @Test
    public void testEvalNotLoadEngine()  throws ScriptException  {     
    	expectedEx.expect(DemoiselleScriptException.class);                     
        dm.eval("invalidEngine","teste3", null);    	    	
    }
        
    @Test
    public void testEvalContext() throws ScriptException {           	
    	String javaScriptSource = "var a= X;  X=1 ; ";           
        dm.loadScript("nashorn", "teste3", javaScriptSource);
        
        Bindings contexto = new SimpleBindings();  
        contexto.put("X", 1);
        dm.eval("nashorn","teste3", contexto);
       
        Assert.assertEquals( 1 , contexto.get("X"));
    }
     
    @Test
    public void testEvalContextNoCache() throws ScriptException {           	
    	String javaScriptSource = "var str= 'test' +X; str ; ";           
        dm.loadScript("nashorn", "teste3", javaScriptSource);
        
        SimpleBindings contexto = new SimpleBindings();  
        contexto.put("X", 1);  
        
        Assert.assertEquals( "test1", dm.evalSource("nashorn",javaScriptSource, contexto));
    }
    
    @Test
    public void testEvalNoContextNoCache() throws ScriptException {           	
    	String javaScriptSource = "var a= 1;  a ; ";           
        dm.loadScript("nashorn", "teste3", javaScriptSource);                
        dm.evalSource("nashorn",javaScriptSource, null);
       
        Assert.assertEquals( 1 ,  dm.evalSource("nashorn",javaScriptSource, null));
    }
    
    @Test
    public void testEvalNoContext() throws ScriptException {           	    	
    	String javaScriptSource = "var a=1;  a; ";            	     
        dm.loadScript("nashorn","teste77", javaScriptSource);                      
        Assert.assertEquals( 1 , dm.eval("nashorn","teste77", null));
    }
     
    @Test
    public void testEvalScriptNotValid() throws ScriptException {           	   	    	
        dm.loadEngine("nashorn");        
        expectedEx.expect(DemoiselleScriptException.class);
                      
        dm.eval("nashorn","teste555", null);
    }
            
    @Test
    public void testListEngines() throws ScriptException {         
    	Assert.assertTrue(  dm.listEngines().size() > 0);                                      
    }
           
}