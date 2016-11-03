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

import org.demoiselle.jee.script.DynamicManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DynamicManagerTest {

    @Inject
    private DynamicManager dm;

    @Deployment
    public static Archive<?> createDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "teste.war");
        war.addPackage(DynamicManager.class.getPackage());        
        // Add here the packages for testing other engines, nashorn is already embedded in the jdk.
        war.addPackages(true,"groovy", "org.codehaus.groovy");        
        war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return war;           
    }
      
    @Test
    public void testloadEngine()  {        
    	System.out.println("LoadEngine test" );         	                		                                         
        Assert.assertNotNull(dm.loadEngine("groovy"));
    }
    
    @Test
    public void testloadEngineNotvalid()  {            	         	                		                                         
        Assert.assertNull(dm.loadEngine("Engine Fail"));
    }
    
    @Test
    public void testloadScriptNotValidEngine() throws ScriptException  {            	     
    	String groovyScriptSource = "int a = 1;";
    	dm.loadEngine("Not valid Engine");                 		                                         
        Assert.assertEquals( false, dm.loadScript("testGroovy", groovyScriptSource));
    }
    
    @Test
    public void testloadScriptAlreadyInCache() throws ScriptException  {            	     
    	String groovyScriptSource = "int a = X;  X= a + a;";
    	dm.loadEngine("groovy");         
    	dm.loadScript("test", groovyScriptSource);    	
        Assert.assertEquals( true , dm.loadScript("test", groovyScriptSource) );
    }
    
    @Test
    public void testloadScript() throws ScriptException  {        
    	System.out.println("LoadScript test");     
    	String groovyScriptSource = "int a = X;  X= a + a;";
    	dm.loadEngine("groovy");                 		                                         
        Assert.assertEquals( true , dm.loadScript("testGroovy", groovyScriptSource));
    }
     
    @Test
    public void testCacheSize()  {        
    	System.out.println("CacheSize test" );  
    	dm.loadEngine("groovy");   
    	
        Assert.assertEquals(1,dm.getCacheSize());
    }
    
    
    @Test
    public void testGetScript() throws ScriptException {
    	System.out.println("GetScript test");
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("test1", javaScriptSource);
        dm.getScript("test1");   
        Assert.assertNotNull(dm.getScript("test1") );
    }
    
    @Test
    public void testRemoveScript() throws ScriptException {
    	System.out.println("RemoveScript test");
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("test2", javaScriptSource);
        dm.removeScript("test2");
        Assert.assertNull( dm.getScript("test2"));
    }
       
    @Test
    public void testEvalContext() throws ScriptException {           	
    	System.out.println("Compilation and execution test..."); 
    	
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("teste3", javaScriptSource);
        
        Bindings contexto = new SimpleBindings();  
        contexto.put("X", 1);
        dm.eval("teste3", contexto);
       
        Assert.assertEquals( 1 , contexto.get("X"));
    }
    
    @Test
    public void testEvalNoContext() throws ScriptException {           	
    	    	
    	String javaScriptSource = "var a= 5; a; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("teste4", javaScriptSource);        
       
        Assert.assertEquals(5, dm.eval("teste4", null));
    }
        
}