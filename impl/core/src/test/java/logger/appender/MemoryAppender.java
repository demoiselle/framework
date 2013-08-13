package logger.appender;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class MemoryAppender extends AppenderSkeleton {

	@Override
	public void close() {
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent loggingEvent) {
		
		if (!LoggerMemory.class.getName().equals(loggingEvent.getLoggerName())) {
			
			LoggerMemory.addMessage(loggingEvent.getMessage().toString());
			
		}
		
	}
	
}
