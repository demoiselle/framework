/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.demoiselle.jee.security.interceptor;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import org.demoiselle.jee.core.api.security.SecurityContext;
import org.demoiselle.jee.security.annotation.OnlyOwner;

/**
 *
 * @author 70744416353
 */
public class OnlyOwnerInterceptor {

    @Inject
    private SecurityContext sc;

    @AroundInvoke
    public Object manage(final InvocationContext ic) throws Exception {
        OnlyOwner ow = ic.getMethod().getAnnotation(OnlyOwner.class);
//        Parameter param = ic.getMethod().getParameterTypes()
//        
//        if (ow != null && !ow.field().isEmpty()) {
//            
//            if (ow.classe().getMethod())
//            
//            for (Object params : ic.getParameters()) {
//                if (params instanceof )
//                
//                if (ow.field().equalsIgnoreCase(params.toString())){
//                    
//                }
//            }
//            throw new DemoiselleRestException("Você só pode alterar seu próprio dado", 401);
//        }

        return ic.proceed();
    }
}
