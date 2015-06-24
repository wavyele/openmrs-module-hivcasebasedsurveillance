package org.openmrs.module.hivcasebasedsurveillance.advice;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kemricdc.constants.Triggers;
import org.kemricdc.entities.AppProperties;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.hapi.PatientHl7Service;
import org.kemricdc.hapi.util.OruFiller;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.hivcasebasedsurveillance.constants.Event;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PatientIdsMapper;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PersonMapper;
import org.openmrs.module.hivcasebasedsurveillance.utils.AppPropertiesLoader;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

public class PatientAroundAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {

	private static final long serialVersionUID = -6843825997193392439L;
	private Log log = LogFactory.getLog(this.getClass());
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);

	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		if (method.getName().equals("savePatient") || method.getName().equals("savePatientIdentifier")
				|| method.getName().equals("saveEncounter")) {
			return true;
		}
		return false;
	}

	@Override
	public Advice getAdvice() {
		return new PatientAroundAdvice();
	}

	private class PatientAroundAdvice implements MethodInterceptor {

		private static final int PATIENT_CLINIC_NUMBER_ID = 4;

		public Object invoke(MethodInvocation invocation) throws Throwable {
			Object args[] = invocation.getArguments();
			Object o = null;

			AppProperties appProperties = new AppPropertiesLoader(new AppProperties()).getAppProperties();

			if (invocation.getMethod().getName().equals("savePatient")) {

				Patient patient = (Patient) args[0];
				Boolean newPatient = false;

				if (patient.getPatientId() == null) {
					newPatient = true;
				} else {
					newPatient = false;
				}
				System.out.println("XXXXXXXXXXXXXXXXXXUpdating patient detailsXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
				o = invocation.proceed();
				Person person = new Person();
				PersonMapper personMapper = new PersonMapper(patient, person);

				Set<PersonIdentifier> patientIds = new PatientIdsMapper(patient).getPatientIds();

				personMapper.mapPatient(patientIds);

				PatientHl7Service patientHl7Service = new PatientHl7Service(personMapper.getOecPerson(), appProperties);
				try {
					if (newPatient) {
						patientHl7Service.doWork(Triggers.A04.getValue());
					} else {
						patientHl7Service.doWork(Triggers.A08.getValue());
					}
				} catch (Exception Ex) {
					System.out.println("Unable to generate HL7 message. Error: " + Ex.getMessage());
					log.error(Ex.getStackTrace(), Ex);
				}
				
/*				if (patient.getDead()) {
					PersonMapper personMapper1 = new PersonMapper(patient, person);
					personMapper1.mapPatient(patientIds);
					List<OruFiller> fillers = new ArrayList<OruFiller>();
					OruFiller oruFiller = new OruFiller();
					oruFiller.setCodingSystem((String) appProperties.getProperty("coding_system"));
					oruFiller.setObservationValue(sdf.format(patient.getDeathDate()));
					oruFiller.setObservationIdentifierText(Event.DECEASED.getValue());

					xxxxxxxxxxx
				}*/

			} else if (invocation.getMethod().getName().equals("savePatientIdentifier")) {
				o = invocation.proceed();

				PatientIdentifier pid = (PatientIdentifier) args[0];

				HashSet<PersonIdentifier> patientIds = new PatientIdsMapper(pid.getPatient()).getPatientIds();
				System.out.println("ZZZZZZZZZZZZZZZZZZZUpdating Patient IdentifiersZZZZZZZZZZZZZZZZZZZ");
				Person person = new Person();
				Patient patient = pid.getPatient();
				PersonMapper personMapper = new PersonMapper(patient, person);
				personMapper.mapPatient(patientIds);
				PatientHl7Service patientHl7Service = new PatientHl7Service(personMapper.getOecPerson(), appProperties);
				try {
					patientHl7Service.doWork(Triggers.A08.getValue());
				} catch (Exception Ex) {
					System.out.println("Unable to generate HL7 message. Error: " + Ex.getMessage());
					log.error(Ex.getStackTrace(), Ex);
				}
			}
			return o;
		}
	}
}
