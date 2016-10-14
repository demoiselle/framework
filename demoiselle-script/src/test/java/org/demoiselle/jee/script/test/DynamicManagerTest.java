/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.script.test;

import javax.inject.Inject;
import javax.script.Bindings;
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
        war.addPackage("org.demoiselle.jee.script");
        //Adicionar aki as packages para teste de outros engines, nashorn ja esta embutido na jdk.
        war.addPackages(true,"groovy",                			 
                			 "org.codehaus.groovy");
        
        war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return war;     
    }
 
    
    @Test
    public void testloadScript()  {        
    	System.out.println("Teste loadScript");     
    	String groovyScriptSource = "int a = X;  X= a + a;";
    	dm.loadEngine("groovy");                 		                                         
        Assert.assertEquals( true , dm.loadScript("testeGroovy", groovyScriptSource));
    }
    
    
    public void testEvalGroovyScript()  {        
    	String groovyScriptSource = "int a = X;  X= a + a;";
		
    	System.out.println("Teste compilacao e execucao ...");     
        dm.loadEngine("groovy");                 		        
        
        //TODO: a implementacao do GroovyEngine lança uma IOException na primeira execucao provavelmente um bug.
        dm.loadScript("testeGroovy", groovyScriptSource);
         
        Bindings contexto = new SimpleBindings();
        contexto.put("X", 1);        
        dm.eval("testeGroovy", contexto);        
        
        Assert.assertEquals( 2 , contexto.get("X"));
    }
    
    @Test
    public void testEvalJavaScript() {           	
    	System.out.println("Teste compilacao e execucao ..."); 
    	
    	String javaScriptSource = "var a= X;  X=1 ; ";
        dm.loadEngine("nashorn");     
        dm.loadScript("testeJS", javaScriptSource);
        
        Bindings contexto = new SimpleBindings();  
        contexto.put("X", 1);
        dm.eval("testeJS", contexto);
       
        Assert.assertEquals( 1 , contexto.get("X"));
    }
    
}