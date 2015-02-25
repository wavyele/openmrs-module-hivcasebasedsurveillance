package org.openmrs.module.hivcasebasedsurveillance.advice;

import java.lang.reflect.Method;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kemricdc.constants.Triggers;
import org.kemricdc.entities.AppProperties;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.hapi.PatientHl7Service;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PatientIdsMapper;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PersonMapper;
import org.openmrs.module.hivcasebasedsurveillance.utils.AppPropertiesLoader;
import org.springframework.aop.AfterReturningAdvice;

public class PatientObsReturningAdvice implements AfterReturningAdvice {

	private static final int MARITAL_STATUS_CONCEPT_ID = 1054;
	private Log log = LogFactory.getLog(this.getClass());
	

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		if (method.getName().equals("saveObs")) {
			Obs obs = (Obs) returnValue;

			AppProperties appProperties = new AppPropertiesLoader(new AppProperties()).getAppProperties();

			if (obs.getConcept().getConceptId() == MARITAL_STATUS_CONCEPT_ID) {
				org.openmrs.Person omrsPerson = obs.getPerson();
				Patient omrsPatient = Context.getPatientService().getPatient(omrsPerson.getPersonId());
				HashSet<PersonIdentifier> patientIds = new PatientIdsMapper(omrsPatient).getPatientIds();
				Person oecPerson = new Person();
				PersonMapper personMapper = new PersonMapper(omrsPatient, oecPerson);
				
				personMapper.mapPatient(patientIds);

				System.out.println("ZZZZZZZZZZZZZZZZZZZUpdating marital statusZZZZZZZZZZZZZZZZZZZ");
				personMapper.mapPatientMaritalStatus(obs.getValueCoded().getConceptId());
				PatientHl7Service patientHl7Service = new PatientHl7Service(personMapper.getOecPerson(), appProperties);
				try {
					patientHl7Service.doWork(Triggers.A08.getValue());
				} catch (Exception Ex) {
					System.out.println("Unable to generate HL7 message. Error: " + Ex.getMessage());
					log.error(Ex.getStackTrace(), Ex);
				}
			}
		}

	}

}
