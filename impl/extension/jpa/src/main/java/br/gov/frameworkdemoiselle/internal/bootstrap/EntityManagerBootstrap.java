package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;

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
	
	private EntityManagerScope selectedScope;
	
	public void replaceAnnotatedType(final ProcessAnnotatedType<EntityManagerProducer> event , BeanManager manager){
		
		if (event.getAnnotatedType().getJavaClass().equals(EntityManagerProducer.class)){
			AnnotatedType<EntityManagerProducer> wrapper = new AnnotatedType<EntityManagerProducer>() {
				
				private final AnnotatedType<EntityManagerProducer> delegate = event.getAnnotatedType();

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

				public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
					return delegate.getAnnotation(annotationType);
					
					/*T returnedAnnotation;
					switch(getConfiguredEntityManagerScope()){
						case APPLICATION:
							returnedAnnotation = (T) (annotationType.equals(ApplicationScoped.class) ? new AnnotationLiteral<ApplicationScoped>() {
								private static final long serialVersionUID = 1L;
							} : delegate.getAnnotation(annotationType));
						case CONVERSATION:
							returnedAnnotation = (T) (annotationType.equals(ConversationScoped.class) ? new AnnotationLiteral<ApplicationScoped>() {
								private static final long serialVersionUID = 1L;
							} : delegate.getAnnotation(annotationType));
						case NOSCOPE:
							returnedAnnotation = delegate.getAnnotation(annotationType);
						case REQUEST:
							returnedAnnotation = (T) (annotationType.equals(RequestScoped.class) ? new AnnotationLiteral<ApplicationScoped>() {
								private static final long serialVersionUID = 1L;
							} : delegate.getAnnotation(annotationType));
						case SESSION:
							returnedAnnotation = (T) (annotationType.equals(SessionScoped.class) ? new AnnotationLiteral<ApplicationScoped>() {
								private static final long serialVersionUID = 1L;
							} : delegate.getAnnotation(annotationType));
						case VIEW:
							returnedAnnotation = (T) (annotationType.equals(ViewScoped.class) ? new AnnotationLiteral<ApplicationScoped>() {
								private static final long serialVersionUID = 1L;
							} : delegate.getAnnotation(annotationType));
						default:
							returnedAnnotation = delegate.getAnnotation(annotationType);
					}
					
					return returnedAnnotation;*/
				}

				public Set<AnnotatedField<? super EntityManagerProducer>> getFields() {
					return delegate.getFields();
				}

				public Set<Annotation> getAnnotations() {
					return delegate.getAnnotations();
				}

				public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
					switch(getConfiguredEntityManagerScope()){
						case APPLICATION:
							return annotationType.equals(ApplicationScoped.class) ? true : delegate.isAnnotationPresent(annotationType);
						case CONVERSATION:
							return annotationType.equals(ConversationScoped.class) ? true : delegate.isAnnotationPresent(annotationType);
						case NOSCOPE:
							return delegate.isAnnotationPresent(annotationType);
						case REQUEST:
							return annotationType.equals(RequestScoped.class) ? true : delegate.isAnnotationPresent(annotationType);
						case SESSION:
							return annotationType.equals(SessionScoped.class) ? true : delegate.isAnnotationPresent(annotationType);
						case VIEW:
							return annotationType.equals(ViewScoped.class) ? true : delegate.isAnnotationPresent(annotationType);
						default:
							return delegate.isAnnotationPresent(annotationType);
					}
				}
			};
			
			event.setAnnotatedType(wrapper);
		}
	}
	
	public void configureBean(ProcessBean<EntityManagerProducer> event , BeanManager manager){
		Class<? extends Annotation> beanScope = event.getBean().getScope();
		System.out.println(beanScope.toString());
	}
	
	private EntityManagerScope getConfiguredEntityManagerScope() {
		if (selectedScope==null){
			EntityManagerScope entityManagerScope = null;
			URL configURL = getClass().getResource("demoiselle.properties");
	
			if (configURL != null) {
				try {
					org.apache.commons.configuration.Configuration config = new PropertiesConfiguration(
							configURL);
					Configuration configAnnotation = EntityManagerConfig.class
							.getAnnotation(Configuration.class);
					Name nameAnnotation = EntityManagerConfig.class.getDeclaredField("entityManagerScope")
							.getAnnotation(Name.class);
	
					String prefix = configAnnotation.prefix();
					String sufix = nameAnnotation.value();
	
					String property = prefix.endsWith(".") ? prefix + sufix : prefix + "." + sufix;
	
					String scopeValue = config.getString(property, EntityManagerScope.REQUEST.name())
							.toUpperCase();
	
					for (EntityManagerScope currentScope : EntityManagerScope.values()) {
						if (currentScope.name().equals(scopeValue)) {
							entityManagerScope = currentScope;
							break;
						}
					}
	
					if (entityManagerScope == null) {
						entityManagerScope = EntityManagerScope.REQUEST;
					}
				} catch (Exception e) {
					getLogger().debug(getBundle().getString(""));
					entityManagerScope = EntityManagerScope.REQUEST;
				}
			}
			else{
				entityManagerScope = EntityManagerScope.REQUEST;
			}
			
			this.selectedScope = entityManagerScope;
		}

		return selectedScope;
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = LoggerProducer.create(EntityManagerBootstrap.class);
		}

		return logger;
	}

	private ResourceBundle getBundle() {
		if (bundle == null) {
			bundle = new ResourceBundle("demoiselle-jpa-bundle.properties", Locale.getDefault());
		}

		return bundle;
	}
}
