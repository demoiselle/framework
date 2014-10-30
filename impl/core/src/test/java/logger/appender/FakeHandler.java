package logger.appender;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class FakeHandler extends Handler {

	private String name;

	private String message;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public void publish(LogRecord record) {
		this.name = record.getLoggerName();
		this.message = record.getMessage();
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}
}
