package br.gov.frameworkdemoiselle.internal.proxy;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.el.ELContext;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.inject.Default;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.ResponseStream;
import javax.faces.context.ResponseWriter;
import javax.faces.event.PhaseId;
import javax.faces.render.RenderKit;

@Default
public class FacesContextProxy extends FacesContext implements Serializable {

	private static final long serialVersionUID = 1L;

	@PostConstruct
	public FacesContext getDelegate() {
		FacesContext facesContext = FacesContext.getCurrentInstance();

		if (facesContext == null) {
			// TODO Colocar a mensagem correta
			throw new ContextNotActiveException();
		}

		return facesContext;
	}

	public int hashCode() {
		return getDelegate().hashCode();
	}

	public boolean equals(Object obj) {
		return getDelegate().equals(obj);
	}

	public Application getApplication() {
		return getDelegate().getApplication();
	}

	public Map<Object, Object> getAttributes() {
		return getDelegate().getAttributes();
	}

	public PartialViewContext getPartialViewContext() {
		return getDelegate().getPartialViewContext();
	}

	public Iterator<String> getClientIdsWithMessages() {
		return getDelegate().getClientIdsWithMessages();
	}

	public String toString() {
		return getDelegate().toString();
	}

	public ELContext getELContext() {
		return getDelegate().getELContext();
	}

	public ExceptionHandler getExceptionHandler() {
		return getDelegate().getExceptionHandler();
	}

	public void setExceptionHandler(ExceptionHandler exceptionHandler) {
		getDelegate().setExceptionHandler(exceptionHandler);
	}

	public ExternalContext getExternalContext() {
		return getDelegate().getExternalContext();
	}

	public Severity getMaximumSeverity() {
		return getDelegate().getMaximumSeverity();
	}

	public Iterator<FacesMessage> getMessages() {
		return getDelegate().getMessages();
	}

	public List<FacesMessage> getMessageList() {
		return getDelegate().getMessageList();
	}

	public List<FacesMessage> getMessageList(String clientId) {
		return getDelegate().getMessageList(clientId);
	}

	public Iterator<FacesMessage> getMessages(String clientId) {
		return getDelegate().getMessages(clientId);
	}

	public RenderKit getRenderKit() {
		return getDelegate().getRenderKit();
	}

	public boolean getRenderResponse() {
		return getDelegate().getRenderResponse();
	}

	public boolean getResponseComplete() {
		return getDelegate().getResponseComplete();
	}

	public boolean isValidationFailed() {
		return getDelegate().isValidationFailed();
	}

	public ResponseStream getResponseStream() {
		return getDelegate().getResponseStream();
	}

	public void setResponseStream(ResponseStream responseStream) {
		getDelegate().setResponseStream(responseStream);
	}

	public ResponseWriter getResponseWriter() {
		return getDelegate().getResponseWriter();
	}

	public void setResponseWriter(ResponseWriter responseWriter) {
		getDelegate().setResponseWriter(responseWriter);
	}

	public UIViewRoot getViewRoot() {
		return getDelegate().getViewRoot();
	}

	public void setViewRoot(UIViewRoot root) {
		getDelegate().setViewRoot(root);
	}

	public void addMessage(String clientId, FacesMessage message) {
		getDelegate().addMessage(clientId, message);
	}

	public boolean isReleased() {
		return getDelegate().isReleased();
	}

	public void release() {
		getDelegate().release();
	}

	public void renderResponse() {
		getDelegate().renderResponse();
	}

	public boolean isPostback() {
		return getDelegate().isPostback();
	}

	public void responseComplete() {
		getDelegate().responseComplete();
	}

	public void validationFailed() {
		getDelegate().validationFailed();
	}

	public PhaseId getCurrentPhaseId() {
		return getDelegate().getCurrentPhaseId();
	}

	public void setCurrentPhaseId(PhaseId currentPhaseId) {
		getDelegate().setCurrentPhaseId(currentPhaseId);
	}

	public void setProcessingEvents(boolean processingEvents) {
		getDelegate().setProcessingEvents(processingEvents);
	}

	public boolean isProcessingEvents() {
		return getDelegate().isProcessingEvents();
	}

	public boolean isProjectStage(ProjectStage stage) {
		return getDelegate().isProjectStage(stage);
	}
}
