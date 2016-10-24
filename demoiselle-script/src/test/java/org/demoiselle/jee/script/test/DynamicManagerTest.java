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
        //Adicionar aki as packages para teste de outros engines, nashorn ja esta embutido na jdk.
        war.addPackages(true,"groovy", "org.codehaus.groovy");
        
        war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return war;     
    }
    
    @Test
    public void testNew()  {                 	
    	System.out.println("Teste constructor");
        Assert.assertNotNull(new DynamicManager("groovy"));
    }
    
    @Test
    public void testloadEngine()  {        
    	System.out.println("Teste loadEngine");         	                		                                         
        Assert.assertNotNull(dm.loadEngine("groovy"));
    }
        
    @Test
    public void testloadScript() throws ScriptException  {        
    	System.out.println("Teste loadScript");     
    	String groovyScriptSource = "int a = X;  X= a + a;";
    	dm.loadEngine("groovy");                 		                                         
        Assert.assertEquals( true , dm.loadScript("testeGroovy", groovyScriptSource));
    }
    
    @Test
    public void testGetScript() throws ScriptException {
    	System.out.println("Teste getScript ...");
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("teste1", javaScriptSource);
        dm.getScript("teste1");   
        Assert.assertNotNull(dm.getScript("teste1") );
    }
    
    @Test
    public void testRemoveScript() throws ScriptException {
    	System.out.println("Teste remocao script ...");
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("teste2", javaScriptSource);
        dm.removeScript("teste2");
        Assert.assertNull( dm.getScript("teste2"));
    }
       
    @Test
    public void testEvalContexto() throws ScriptException {           	
    	System.out.println("Teste compilacao e execucao ..."); 
    	
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("teste3", javaScriptSource);
        
        Bindings contexto = new SimpleBindings();  
        contexto.put("X", 1);
        dm.eval("teste3", contexto);
       
        Assert.assertEquals( 1 , contexto.get("X"));
    }
        
}