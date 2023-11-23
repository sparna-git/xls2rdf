package fr.sparna.rdf.xls2rdf.app;

import java.io.File;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

/**
 * A validator that makes sure the value of the parameter is a file 
 * that exists on the filesystem.
 *
 * @author Matthew Kirkley <matt.kirkley@gmail.com>
 */
public class FileExistsValidator implements IParameterValidator {

	@Override
	public void validate(String name, String value) throws ParameterException {
		File file = getFile(value);
		if (!file.exists()) {
			throw new ParameterException("Parameter " + name + " which should represent a file that exists, does not exist.");
		}
	}

	File getFile(String value) {
		return new FileConverter().convert(value);
	}

}