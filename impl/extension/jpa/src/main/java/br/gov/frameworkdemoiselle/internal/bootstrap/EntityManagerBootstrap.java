package br.gov.frameworkdemoiselle.internal.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;

import br.gov.frameworkdemoiselle.annotation.ViewScoped;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig;
import br.gov.frameworkdemoiselle.internal.configuration.EntityManagerConfig.EntityManagerScope;
import br.gov.frameworkdemoiselle.internal.implementation.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.producer.EntityManagerProducer;


public class EntityManagerBootstrap implements Extension {
	
	public void selectScopeForEntityManager(@Observes final ProcessAnnotatedType<EntityManagerProducer> event, BeanManager beanManager) {
		EntityManagerConfig config = new EntityManagerConfig();
		new ConfigurationLoader().load(config,false);
		final EntityManagerScope entityManagerScope = config.getEntityManagerScope();
		
		if (entityManagerScope != EntityManagerScope.NOSCOPE){
			AnnotatedType<EntityManagerProducer> annotatedType = new AnnotatedType<EntityManagerProducer>() {
				
				private AnnotatedType<EntityManagerProducer> delegate = event.getAnnotatedType();
	
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
					T returnedAnnotation = null;
					Class<?> expectedScope;
					
					switch(entityManagerScope){
						case APPLICATION:
							expectedScope = ApplicationScoped.class;
							break;
						case CONVERSATION:
							expectedScope = ConversationScoped.class;
							break;
						case REQUEST:
							expectedScope = RequestScoped.class;
							break;
						case SESSION:
							expectedScope = SessionScoped.class;
							break;
						case VIEW:
							expectedScope = ViewScoped.class;
							break;
						default:
							expectedScope = null;
							break;
					}
					
					if (annotationType.equals(expectedScope)){
						switch(entityManagerScope){
							case APPLICATION:
								returnedAnnotation = (T) new ApplicationScopedLiteral();
								break;
							case CONVERSATION:
								returnedAnnotation = (T) new ConversationScopedLiteral();
								break;
							case REQUEST:
								returnedAnnotation = (T) new ApplicationScopedLiteral();
								break;
							case SESSION:
								returnedAnnotation = (T) new SessionScopedLiteral();
								break;
							case VIEW:
								returnedAnnotation = (T) new ViewScopedLiteral();
								break;
							default:
								returnedAnnotation = delegate.getAnnotation(annotationType);
								break;
						}
					}
					else{
						returnedAnnotation = delegate.getAnnotation(annotationType);
					}
					
					return returnedAnnotation;
				}
	
				public Set<AnnotatedField<? super EntityManagerProducer>> getFields() {
					return delegate.getFields();
				}
	
				public Set<Annotation> getAnnotations() {
					return delegate.getAnnotations();
				}
	
				public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
					return delegate.isAnnotationPresent(annotationType);
				}
	
				
			};
			
			event.setAnnotatedType(annotatedType);
		}
	}
	
	@SuppressWarnings("all")
	class ApplicationScopedLiteral extends AnnotationLiteral<ApplicationScoped> implements ApplicationScoped {
		private static final long serialVersionUID = 1L;
		
		private ApplicationScopedLiteral() {}
	}
	
	@SuppressWarnings("all")
	class RequestScopedLiteral extends AnnotationLiteral<RequestScoped> implements RequestScoped {
		private static final long serialVersionUID = 1L;
		
		private RequestScopedLiteral(){}
	}
	
	@SuppressWarnings("all")
	class SessionScopedLiteral extends AnnotationLiteral<SessionScoped> implements SessionScoped {
		private static final long serialVersionUID = 1L;
		
		private SessionScopedLiteral(){}
	}
	
	@SuppressWarnings("all")
	class ViewScopedLiteral extends AnnotationLiteral<ViewScoped> implements ViewScoped {
		private static final long serialVersionUID = 1L;
		
		private ViewScopedLiteral(){}
	}
	
	@SuppressWarnings("all")
	class ConversationScopedLiteral extends AnnotationLiteral<ConversationScoped> implements ConversationScoped {
		private static final long serialVersionUID = 1L;
		
		private ConversationScopedLiteral(){}
	}
}
