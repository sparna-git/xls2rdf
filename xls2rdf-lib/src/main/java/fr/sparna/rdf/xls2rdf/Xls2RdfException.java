package fr.sparna.rdf.xls2rdf;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.MessageFormatter;

public class Xls2RdfException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static void when(boolean test) {
		if (test) {
			throw new Xls2RdfException("Assertion failed");
		}
	}

	public static void when(boolean test, String message) {
		if (test) {
			throw new Xls2RdfException(message);
		}
	}

	public static void when(boolean test, String message, Object... parameters) {
		if (test) {
			throw new Xls2RdfException(message, parameters);
		}
	}

	public static Xls2RdfException rethrow(Throwable exception) {
		if (exception instanceof Error) {
			throw (Error) exception;
		}
		if (exception instanceof RuntimeException) {
			throw (RuntimeException) exception;
		}
		throw new Xls2RdfException(exception);
	}

	public static <T> T failIfNotInstance(Object object, Class<T> clazz, String message, Object... parameters) {
		when(!clazz.isInstance(object), message, parameters);
		//noinspection unchecked
		return (T) object;
	}

	public static <T> T failIfNull(T value, String message, Object... parameters) {
		when(null == value, message, parameters);
		//noinspection ConstantConditions
		return value;
	}

	public static <T extends CharSequence> T failIfBlank(T value, String message, Object... parameters) {
		when(StringUtils.isBlank(value), message, parameters);
		//noinspection ConstantConditions
		return value;
	}

	public Xls2RdfException() {
	}

	public Xls2RdfException(String message) {
		super(message);
	}

	public Xls2RdfException(Throwable cause, String message, Object... parameters) {
		super(MessageFormatter.arrayFormat(message, parameters).getMessage(), cause);
	}

	public Xls2RdfException(String message, Object... parameters) {
		super(MessageFormatter.arrayFormat(message, parameters).getMessage());
	}

	public Xls2RdfException(Throwable cause) {
		super(cause);
	}
}
