/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle;

import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.reflect.Method;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JFrame;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import br.gov.frameworkdemoiselle.bootstrap.MainClass;
import br.gov.frameworkdemoiselle.lifecycle.AfterShutdownProccess;
import br.gov.frameworkdemoiselle.lifecycle.AfterStartupProccess;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * Central class to bootstrap Demoiselle SE applications.
 * 
 * @author serpro
 */
public class Demoiselle {

	private static final String BUNDLE_NAME = "demoiselle-se-bundle";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	protected Demoiselle() {
		throw new UnsupportedOperationException(getString("se-util-instantiation-error"));
	}

	/**
	 * <p>
	 * Bootstrapper that initializes the framework and call a main method. This method will ensure that the framework
	 * facilities (CDI, etc.) are started before calling the application's real main method and that they are shut down
	 * before the application's end.
	 * </p>
	 * <p>
	 * To start an application using this method, execute the following code on a terminal:
	 * </p>
	 * 
	 * <pre>
	 * ~$ java br.gov.frameworkdemoiselle.Demoiselle com.application.ClassWithMainMethod
	 * </pre>
	 * <p>
	 * As you can see, you must pass the full qualified name of the class that has a main method as the first argument
	 * of your application.
	 * </p>
	 * <p>
	 * If your application needs more arguments, pass them normally after the class name. This method will remove the
	 * class name of the argument list, so your real main method will se the first argument after the class name as
	 * index 0 (zero) of the <code>args</code> parameter. Ex:
	 * </p>
	 * 
	 * <pre>
	 * ~$ java br.gov.frameworkdemoiselle.Demoiselle com.application.ClassWithMainMethod firstArg secondArg
	 * </pre>
	 * <p>
	 * Your application's main method will run as follow:
	 * <p>
	 * 
	 * <pre>
	 * <code>
	 * package com.application;
	 * public class ClassWithMainMethod {
	 *   public static void main(String[] args){
	 *     System.out.println(args[0]); //will print "firstArg"
	 *     System.out.println(args[1]); //will print "secondArg"
	 *   }
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param args
	 *            Arguments array. The first argument must be the full qualified name of the real class that has the
	 *            desired main method. All following arguments will be passed to the real main method.
	 */
	public static void main(String[] args) {
		if (args == null || args.length <= 0) {
			throw new DemoiselleException(getString("se-util-no-main-defined"));
		}

		Class<?> mainClass;
		try {
			mainClass = Class.forName(args[0]);
		} catch (ClassNotFoundException e) {
			throw new DemoiselleException(getString("se-util-invalid-main-defined"), e);
		}

		Weld weld = new Weld();
		WeldContainer container = weld.initialize();

		// Fire the AfterStartupProccess event. Methods annotated with @Startup will run now
		container.getBeanManager().fireEvent(new AfterStartupProccess() {
		});

		String[] passedArgs = null;
		if (args.length > 1) {
			passedArgs = new String[args.length - 1];
			System.arraycopy(args, 1, passedArgs, 0, args.length - 1);
		}

		Method mainMethod;
		try {
			mainMethod = mainClass.getMethod("main", String[].class);
		} catch (Exception e) {
			throw new DemoiselleException(getString("se-util-invalid-main-defined"), e);
		}

		try {
			mainMethod.invoke(null, ((Object) passedArgs));
		} catch (Exception e) {
			throw new DemoiselleException(getString("se-util-error-calling-main"), e);
		} finally {
			container.getBeanManager().fireEvent(new AfterShutdownProccess() {
			});
			weld.shutdown();
		}
	}

	/**
	 * <p>
	 * Bootstrapper that initializes the framework and call a predefined user method. This method will ensure that the
	 * framework facilities (CDI, etc.) are started before calling the application's {@link MainClass#run(String[] args)}
	 * method.
	 * <p>
	 * To use this method you need to do two things:
	 * </p>
	 * <ul>
	 * <li>Create a concrete class that implements the {@link MainClass} interface</li>
	 * <li>Create a <code>main</code> class that calls this method passing your <code>MainClass</code> as argument</li>
	 * </ul>
	 * <p>
	 * Here is an example of bootstrap with this method.
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * package com.application;
	 * 
	 * import br.gov.frameworkdemoiselle.bootstrap.MainClass;
	 * 
	 * public class MyStarterClass implements MainClass {
	 *   
	 *   public static void main(String[] args){
	 *     Demoiselle.runStarterClass(MyStarterClass.class , args);
	 *   }
	 *   
	 *   public void run(String[] args){
	 *     //Real startup code runs here
	 *   }
	 *   
	 *   &#64;Startup
	 *   protected void init(){
	 *     //This method will be called by the framework before the <code>run</code> method runs
	 *   }
	 *   
	 *   &#64;Shutdown
	 *   protected void cleanup(){
	 *     //This method will be called by the framework after the <code>run</code> method
	 *     //finishes and before the application closes.
	 *   }
	 *   
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param args
	 *            Arguments array. It will be passed unmodified to the {@link MainClass#run(String[] args)} method.
	 */
	public static void runStarterClass(final Class<? extends MainClass> mainClass, String[] args) {
		Weld weld = new Weld();
		WeldContainer container = weld.initialize();

		// Fire the AfterStartupProccess event. Methods annotated with @Startup will run now
		container.getBeanManager().fireEvent(new AfterStartupProccess() {
		});

		MainClass mainClassInstance = null;
		try {
			mainClassInstance = Beans.getReference(mainClass);
			mainClassInstance.run(args);
		} catch (Exception e) {
			mainClassInstance = null;
			throw new DemoiselleException(getString("se-util-error-calling-run"), e);
		} finally {
			container.getBeanManager().fireEvent(new AfterShutdownProccess() {
			});
			weld.shutdown();

			mainClassInstance = null;
		}
	}

	/**
	 * <p>
	 * Bootstraps CDI and starts a main window. This window will be instantiated using the default constructor and have
	 * all of it's injections resolved before instantiation. Also any methods annotated with <code>&#64;Startup</code>
	 * and <code>&#64;Shutdown</code> will run before this window is created and after this window is closed,
	 * respectively.
	 * </p>
	 * <p>
	 * The new window will be started on the AWT event dispatcher thread, so it follows the recomendation of creating a
	 * single thread to show the user interface and dispatch events since Swing components aren't thread-safe.
	 * </p>
	 * <p>
	 * <p>
	 * A typical use case of this method is:
	 * </p>
	 * 
	 * <pre>
	 * <code>
	 * package com.application;
	 * 
	 * public class MyMainWindow extends JFrame {
	 *   
	 *   //Main method
	 *   public static void main(String args[]){
	 *     Demoiselle.runMainWindow(MyMainWindow.class);
	 *   }
	 * 
	 *   //Configures the window
	 *   public MyMainWindow(){
	 *     super("My Main Window");
	 *   }
	 *   
	 *   //This method will be called after the window is constructed
	 *   &#64;PostConstruct
	 *   public void initComponents(){
	 *     //Initializes components.
	 *     
	 *     setVisible(true);
	 *   }
	 * 
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param jFrameClass
	 *            Type of the window that will be created.
	 */
	public static void runMainWindow(final Class<? extends JFrame> jFrameClass) {
		final Weld weld = new Weld();
		final WeldContainer container = weld.initialize();

		EventQueue.invokeLater(new Runnable() {

			public void run() {
				container.getBeanManager().fireEvent(new AfterStartupProccess() {
				});

				JFrame jframe = null;
				try {
					jframe = Beans.getReference(jFrameClass);

					if (jframe.getDefaultCloseOperation() == JFrame.HIDE_ON_CLOSE) {
						jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					}

					jframe.addWindowListener(new WindowListener() {

						@Override
						public void windowOpened(WindowEvent e) {
						}

						@Override
						public void windowIconified(WindowEvent e) {
						}

						@Override
						public void windowDeiconified(WindowEvent e) {
						}

						@Override
						public void windowDeactivated(WindowEvent e) {
						}

						@Override
						public void windowClosing(WindowEvent e) {
							container.getBeanManager().fireEvent(new AfterShutdownProccess() {
							});
							weld.shutdown();
						}

						@Override
						public void windowClosed(WindowEvent e) {
						}

						@Override
						public void windowActivated(WindowEvent e) {
						}
					});
				} catch (Exception e) {
					throw new DemoiselleException(getString("se-util-error-starting-jframe"), e);
				}
			}
		});
	}

	private static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

}
