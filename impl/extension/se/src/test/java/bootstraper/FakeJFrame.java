package bootstraper;

import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JFrame;


public class FakeJFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private InjectedResource injectedResource;
	
	public static String dataFromInjectedResource;
	
	public static String dataFromPostConstruct;
	
	public static FakeJFrame instance;
	
	public FakeJFrame() throws HeadlessException {
	}

	public FakeJFrame(GraphicsConfiguration gc) {
	}

	public FakeJFrame(String title) throws HeadlessException {
	}

	public FakeJFrame(String title, GraphicsConfiguration gc) {
	}
	
	@PostConstruct
	public void init(){
		dataFromPostConstruct = "dataFromPostConstruct";
		dataFromInjectedResource = injectedResource.getData();
		instance = this;
	}

}
