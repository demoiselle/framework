/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.persistence.crud.producer;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

/**
 *
 * @author 70744416353
 */
@Dependent
public class EntityManagerProducer {

    @Inject
    private Logger logger;

    private static final String DEFAULT_BEAN_TEST_PERSISTENCE_UNIT = "Default";

    private EntityManagerFactory emf;

    private EntityManager em;

    @PostConstruct
    private void initializeEntityManagerFactory() {
        emf = Persistence.createEntityManagerFactory(DEFAULT_BEAN_TEST_PERSISTENCE_UNIT);
        logger.info("Entity Manager Factory was successfully initialized");
    }

    /**
     *
     * @param ip
     * @return
     */
    @Produces
    public EntityManager getEntityManager(InjectionPoint ip) {
        PersistenceContext ctx = ip.getAnnotated().getAnnotation(PersistenceContext.class);

        if (ctx == null) {
            //if @PersisteceContext is declared on method, ctx is null at this point. 
            //ctx should be retrieved from the Method. 
            Member member = ip.getMember();
            if (member instanceof Method) {
                Method method = (Method) member;
                ctx = method.getAnnotation(PersistenceContext.class);
            }
        }

        logger.fine("PersistenceContext info:");
        //This could happen if the application injects the EntityManager via @Inject instead of @PersistenceContext 
        if (ctx != null) {
            logger.log(Level.FINE, "Unit name: '{''}'{0}", ctx.unitName());
        }

        logger.log(Level.FINE, "Bean defining the injection point: '{''}'{0}", ip.getBean().getBeanClass());
        logger.log(Level.FINE, "Field to be injected: '{''}'{0}", ip.getMember());

        if (em == null) {
            em = emf.createEntityManager();
        }
        return em;
    }

    /**
     * Closes the entity manager and entity manager factory when the event
     * {@link CdiContainerShutdown} is fired.
     *
     * @param containerShutdown the event that indicates that the container is
     * about to shutdown.
     */
//    public void closeEntityManagerAndEntityManagerFactory(@Observes CdiContainerShutdown containerShutdown) {
//        closeEntityManager();
//        closeEntityManagerFactory();
//    }
    private void closeEntityManager() {
        if (em == null) {
            return;
        }
        if (em.isOpen()) {
            try {
                // In case a transaction is still open. 
                if (em.getTransaction().isActive() && !em.getTransaction().getRollbackOnly()) {
                    em.getTransaction().commit();
                }
            } finally {
                logger.fine("Closing entity manager");
                em.close();
            }

        }
    }

    private void closeEntityManagerFactory() {
        if (emf == null) {
            return;
        }
        if (emf.isOpen()) {
            logger.fine("Closing entity manager factory");
            emf.close();
        }
    }

}
