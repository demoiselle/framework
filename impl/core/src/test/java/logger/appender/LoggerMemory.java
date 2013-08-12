package logger.appender;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

public class LoggerMemory {

	@Inject
	private Logger logger;
	
	private static List<String> messages = new ArrayList<String>();

	public boolean checkMessage(String loggerMessage) {

		logger.info("Verificando a mensagem [" + loggerMessage + "]");
		this.showMessages();
		
		for (String mensagem : messages) {

			if (mensagem.equals(loggerMessage)) {
				
				logger.info("Mensagem encontrada");
				return true;
				
			}

		}

		logger.info("Mensagem não encontrada");
		return false;

	}

	private void showMessages() {
		
		logger.debug("Inicio da listagem de mensagens armazenadas...");		
		for (String message : messages) {
			
			logger.debug("\"" + message + "\"");
			
		}		
		logger.debug("Fim da listagem de mensagens armazenadas...");
		
	}

	public static void addMessage(String mensagem) {

		messages.add(mensagem);

	}

}
