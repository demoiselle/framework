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
package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.DemoiselleException;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig.EntityManagerScope;
import br.gov.frameworkdemoiselle.internal.producer.EntityManagerProducer;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

public class EntityManagerBootstrap implements Extension {

	private Logger logger;

	private transient ResourceBundle bundle;
	
	private static final String DEMOISELLE_PROPERTIES_FILE_NAME = "demoiselle.properties";
	
	public void makeScoped(@Observes ProcessAnnotatedType<EntityManagerProducer> event){
		final AnnotatedType<EntityManagerProducer> type = event.getAnnotatedType();
		final EntityManagerScope scope = getConfiguredEntityManagerScope();
		
		AnnotatedType<EntityManagerProducer> newType = new AnnotatedType<EntityManagerProducer>() {
			
			private AnnotatedType<EntityManagerProducer> delegate = type;
			private AnnotationLiteral<? extends Annotation> scopedLiteral;
			private Class<? extends Annotation> expectedScopedAnnotationType;
			private boolean initialized = false;
			private Set<Annotation> annotations;
			
			private void initialize(){
				if (!initialized){				
					switch (scope) {
						case APPLICATION:
							expectedScopedAnnotationType = ApplicationScoped.class;
							scopedLiteral = new ApplicationLiteral();
							break;
						case CONVERSATION:
							expectedScopedAnnotationType = ConversationScoped.class;
							scopedLiteral = new ConversationLiteral();
							break;
						case NOSCOPE:
							expectedScopedAnnotationType = null;
							scopedLiteral = null;
							break;
						case REQUEST:
							expectedScopedAnnotationType = RequestScoped.class;
							scopedLiteral = new RequestLiteral();
							break;
						case SESSION:
							expectedScopedAnnotationType = SessionScoped.class;
							scopedLiteral = new SessionLiteral();
							break;
						case VIEW:
							expectedScopedAnnotationType = ViewScoped.class;
							scopedLiteral = new ViewLiteral();
							break;
						default:
							expectedScopedAnnotationType = null;
							scopedLiteral = null;
							break;
					}
					
					initialized = true;
				}
			}

			public Class<EntityManagerProducer> getJavaClass() {
				return delegate.getJavaClass();
			}

			public Type getBaseType() {
				return delegate.getBaseType();
			}

			public Set<AnnotatedConstructor<EntityManagerProducer>> getConstructors() {
				return delegate.getConstructors();
			}

			public Set<Type> getTypeClosure() {
				return delegate.getTypeClosure();
			}

			public Set<AnnotatedMethod<? super EntityManagerProducer>> getMethods() {
				return delegate.getMethods();
			}

			@SuppressWarnings("unchecked")
			public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
				initialize();
				
				if (expectedScopedAnnotationType!=null && expectedScopedAnnotationType.isAssignableFrom(annotationType)){
					return (T) scopedLiteral;
				}
				else{
					return delegate.getAnnotation(annotationType);
				}
			}

			public Set<AnnotatedField<? super EntityManagerProducer>> getFields() {
				return delegate.getFields();
			}

			public Set<Annotation> getAnnotations() {
				initialize();
				
				if (annotations==null){
					HashSet<Annotation> myAnnotations = new HashSet<Annotation>();
					myAnnotations.addAll(delegate.getAnnotations());
					if (scopedLiteral!=null && !myAnnotations.contains(scopedLiteral)){
						myAnnotations.add(scopedLiteral);
					}
					
					annotations = Collections.unmodifiableSet(myAnnotations);
				}
				
				return annotations;
			}

			public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
				if (expectedScopedAnnotationType!=null && expectedScopedAnnotationType.isAssignableFrom(annotationType)){
					return true;	
				}
				else{
					return delegate.isAnnotationPresent(annotationType);
				}
			}
		};
		
		event.setAnnotatedType(newType);
	}
	
	private EntityManagerScope getConfiguredEntityManagerScope() {
		EntityManagerScope entityManagerScope = null;
		
		try {
			org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(DEMOISELLE_PROPERTIES_FILE_NAME);
			Configuration configAnnotation = EntityManagerConfig.class.getAnnotation(Configuration.class);
			Name nameAnnotation = EntityManagerConfig.class.getDeclaredField("entityManagerScope").getAnnotation(
					Name.class);

			String prefix = configAnnotation.prefix();
			String sufix = nameAnnotation.value();

			String property = prefix.endsWith(".") ? prefix + sufix : prefix + "." + sufix;

			String scopeValue = config.getString(property, EntityManagerScope.REQUEST.name()).toUpperCase();

			for (EntityManagerScope currentScope : EntityManagerScope.values()) {
				if (currentScope.name().equals(scopeValue)) {
					entityManagerScope = currentScope;
					getLogger().info(getBundle().getString("defining-entity-manager-scope",entityManagerScope.name()));
					break;
				}
			}

			if (entityManagerScope == null) {
				getLogger().info(getBundle().getString("entity-manager-scope-not-defined",EntityManagerScope.REQUEST.name()));
				entityManagerScope = EntityManagerScope.REQUEST;
			}
		} catch (ConfigurationException e) {
			getLogger().info(getBundle().getString("entity-manager-scope-not-defined",EntityManagerScope.REQUEST.name()));
			entityManagerScope = EntityManagerScope.REQUEST;
		} catch (Exception e){
			throw new DemoiselleException(e);
		}

		return entityManagerScope;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(EntityManagerBootstrap.class);
		}

		return logger;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = new ResourceBundle("demoiselle-jpa-bundle", Locale.getDefault());
		}

		return bundle;
	}
	
	class RequestLiteral extends AnnotationLiteral<RequestScoped> implements RequestScoped{private static final long serialVersionUID = 1L;}
	class SessionLiteral extends AnnotationLiteral<SessionScoped> implements SessionScoped{private static final long serialVersionUID = 1L;}
	class ApplicationLiteral extends AnnotationLiteral<ApplicationScoped> implements ApplicationScoped{private static final long serialVersionUID = 1L;}
	class ViewLiteral extends AnnotationLiteral<ViewScoped> implements ViewScoped{private static final long serialVersionUID = 1L;}
	class ConversationLiteral extends AnnotationLiteral<ConversationScoped> implements ConversationScoped{private static final long serialVersionUID = 1L;}
}
