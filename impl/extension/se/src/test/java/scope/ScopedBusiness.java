package scope;

import javax.inject.Inject;

public class ScopedBusiness {
	
	@Inject
	private RequestScopedBean requestScopedBean;
	
	@Inject
	private ViewScopedBean viewScopedBean;
	
	@Inject
	private SessionScopedBean sessionScopedBean;
	
	@Inject
	private ConversationScopedBean conversationScopedBean;

	
	public String getValueFromRequest(){
		return requestScopedBean.getValue();
	}
	
	public void setValueToRequest(String value){
		requestScopedBean.setValue(value);
	}
	
	public String getValueFromSession(){
		return sessionScopedBean.getValue();
	}
	
	public void setValueToSession(String value){
		sessionScopedBean.setValue(value);
	}
	
	public String getValueFromView(){
		return viewScopedBean.getValue();
	}
	
	public void setValueToView(String value){
		viewScopedBean.setValue(value);
	}
	
	public String getValueFromConversation(){
		return conversationScopedBean.getValue();
	}
	
	public void setValueToConversation(String value){
		conversationScopedBean.setValue(value);
	}
	
}
