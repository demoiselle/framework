package security.athentication.custom;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;

import br.gov.frameworkdemoiselle.security.AfterLoginSuccessful;

@RequestScoped
public class EventObserver {

	private AfterLoginSuccessful event;

	public void observer(@Observes AfterLoginSuccessful event) {
		this.event = event;
	}

	public AfterLoginSuccessful getEvent() {
		return this.event;
	}
}
