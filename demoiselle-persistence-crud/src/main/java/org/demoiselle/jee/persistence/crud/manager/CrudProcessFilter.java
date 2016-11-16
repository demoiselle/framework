package org.demoiselle.jee.persistence.crud.manager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;


@Provider
@PreMatching
public class CrudProcessFilter implements ContainerRequestFilter{
	
	@Inject
	private CrudBootstrap boot;
	
	@Inject
	private Logger logger;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		processCrudFlow(requestContext);
	}

	private void processCrudFlow(ContainerRequestContext requestContext) {
		CrudContainer cc = getCrudContainer(requestContext);
		
		if(cc != null){
			
			logger.info(cc.toString());
			
			/*
			 * 1 - Verificar se a classe de destino responde a chamada.
			 * 2 - Se responder, chamar o método.
			 * 3 - Se não chamar, executar a função do crud.
			 * 
			 * 
			 * @GET = findAll
			 * @POST = persist
			 * @PUT = persist
			 * @DELETE = delete
			 */
			
			String httpMethod = requestContext.getMethod();
			
			Response response = processRest(httpMethod, cc);
			Object businessResult = processBusiness(httpMethod, cc);
			
			if(businessResult == null){
				Object objectResult = processPersistence(httpMethod, cc);
				response = Response.ok(objectResult, MediaType.APPLICATION_JSON).build();
			}
			else{
				response = Response.ok(businessResult, MediaType.APPLICATION_JSON).build();
			}
			
			if(response == null){
				response = Response.ok().type(MediaType.APPLICATION_JSON).build();
			}
			
			requestContext.abortWith(response);
		}
			
		
	}

	private Object processPersistence(String httpMethod, CrudContainer cc) {

		Object persistenceObject = CDI.current().select(cc.getPersistenceClass()).get();
		
		CrudDAO dao = null;//CDI.current().select(CrudDAO.class).get();
		try{
			dao = CDI.current().select(CrudDAO.class).get();
			Object o = dao.findAll();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		if(persistenceObject != null){
			logger.info("Executing persistence object : " + persistenceObject.toString());
			
			try{
				return executeMethod(httpMethod, cc, persistenceObject);
			}
			catch(CrudMethodNotFound e){
				return executeMethod(httpMethod, cc, dao);
			}
		}
		else{
			return executeMethod(httpMethod, cc, dao);
		}
		
		
	}

	private Object processBusiness(String httpMethod, CrudContainer cc) {
		Object bussinesObject = CDI.current().select(cc.getBusinessClass()).get();
	
		if(bussinesObject != null){
			logger.info("Executing business object : " + bussinesObject.toString());
			
			try{
				return executeMethod(httpMethod, cc, bussinesObject);
			}
			catch(CrudMethodNotFound e){
				return null;
			}
		}
		
		return null;
	}

	public Object executeMethod(String httpMethod, CrudContainer cc, Object object) throws CrudMethodNotFound{
		Method method = fetchBusinessOrPersistenceMethod(httpMethod, object.getClass().getDeclaredMethods());
		
		if(method == null){
			throw new CrudMethodNotFound();
		}
		
		try {
			
			if(httpMethod.equals(GET.class.getSimpleName())){
				return method.invoke(object, new Object[]{});
			}
			else if(httpMethod.equals(POST.class.getSimpleName()) 
					|| httpMethod.equals(PUT.class.getSimpleName())){
				return method.invoke(object, cc.getModel().newInstance());
			}
			else if(httpMethod.equals(DELETE.class.getSimpleName())){
				return method.invoke(object, new Object[]{});
			}
			
			
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	private Response processRest(String httpMethod, CrudContainer cc) {
		Method restMethod = fetchRestMethod(httpMethod, cc.getRestClass().getDeclaredMethods());
		Response response = null;
		
		if(restMethod == null){
			throw new CrudMethodNotFound();
		}
		
		logger.info("METHOD REST : " + restMethod.getName());
		
		try {
			Object obj = CDI.current().select(cc.getRestClass()).get();
			response = (Response) restMethod.invoke(obj, new Object[]{});
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;
	}

	private CrudContainer getCrudContainer(ContainerRequestContext requestContext) {
		UriInfo uriInfo = requestContext.getUriInfo();
		
		String path = uriInfo.getPath();
		if(!path.isEmpty()){
			path = path.replaceFirst("/", "");
		}
		
		return boot.getCache().get(path);
	}

	private Method fetchRestMethod(String httpMethod, Method[] methods) {
		Method methodExec =  null;
		
		for(Method method : methods){
			
			if(! method.isAnnotationPresent(Path.class)) {
				if(GET.class.getSimpleName().equals(httpMethod) && method.isAnnotationPresent(GET.class)){
					methodExec = method;
					break;
				}
				else if((POST.class.getSimpleName().equals(httpMethod) && method.isAnnotationPresent(POST.class) ) 
						|| (PUT.class.getSimpleName().equals(httpMethod) && method.isAnnotationPresent(PUT.class))){
					methodExec = method;
					break;
				}
				else if(DELETE.class.getSimpleName().equals(httpMethod) && method.isAnnotationPresent(DELETE.class)){
					methodExec = method;
					break;
				}
			}
		}
		
		return methodExec;
	}
	
	private Method fetchBusinessOrPersistenceMethod(String httpMethod, Method[] methods){
		Method methodExec =  null;
		
		for(Method method : methods){
			
			if(GET.class.getSimpleName().equals(httpMethod) && method.getName().equals("findAll")){
				methodExec = method;
				break;
			}
			else if(POST.class.getSimpleName().equals(httpMethod) && method.getName().equals("create")){
				methodExec = method;
				break;
			}
			else if(PUT.class.getSimpleName().equals(httpMethod) && method.getName().equals("update")){
				methodExec = method;
				break;
			}
			else if(DELETE.class.getSimpleName().equals(httpMethod) && method.getName().equals("delete")){
				methodExec = method;
				break;
			}
			
		}
		
		return methodExec;
	}
	

}
