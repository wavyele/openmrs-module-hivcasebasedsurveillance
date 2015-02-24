package org.openmrs.module.hivcasebasedsurveillance.utils;

import java.io.File;
import java.util.Properties;

import org.kemricdc.entities.AppProperties;
import org.openmrs.api.context.Context;

public class AppPropertiesLoader {
	private static final String PROPERTIES_FILE_LOCATION_GLOBAL_PROPERTY = "hcbsurveillance.propertiesFileLocation";
	private AppProperties appProperties;
	
	public AppPropertiesLoader(AppProperties appProperties){
		this.appProperties = appProperties;
	}
	
	public AppProperties getAppProperties() {
		String propertiesFileLocation = Context.getAdministrationService().getGlobalProperty(PROPERTIES_FILE_LOCATION_GLOBAL_PROPERTY);
		File propertiesFile = new File(propertiesFileLocation);
		this.setAppProperties(new AppProperties(new Properties(), propertiesFile));

		return appProperties;
	}
	public void setAppProperties(AppProperties appProperties) {
		this.appProperties = appProperties;
	}
	
	
	
}
