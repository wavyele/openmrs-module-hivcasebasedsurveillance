package org.openmrs.module.hivcasebasedsurveillance.advice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kemricdc.constants.Triggers;
import org.kemricdc.entities.AppProperties;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.hapi.EventsHl7Service;
import org.kemricdc.hapi.util.OruFiller;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.module.hivcasebasedsurveillance.constants.Event;
import org.openmrs.module.hivcasebasedsurveillance.mappers.OruFillerMapper;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PatientIdsMapper;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PersonMapper;
import org.openmrs.module.hivcasebasedsurveillance.utils.AppPropertiesLoader;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

public class PatientEncounterAroundAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3842820316745098958L;
	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		if (method.getName().equalsIgnoreCase("saveEncounter")) {
			return true;
		}
		return false;
	}

	@Override
	public Advice getAdvice() {
		return new PatientEncounterAroundAdvice();
	}

	private class PatientEncounterAroundAdvice implements MethodInterceptor {
		private static final int DISEASED_DIAGNOSED_CONCEPT_ID = 1661;
		private static final int NO_CONCEPT_ID = 1256;
		private static final int YES_CONCEPT_ID = 1255;
		private static final String NO_STRING = "NO";
		private static final String YES_STRING = "YES";
		private static final int DECEASED_CONCEPT_ID = 160034;
		private static final int TRANSFER_OUT_CONCEPT_ID = 159492;
		private static final int LOST_TO_FOLLOWUP_CONCEPT_ID = 5240;
		private static final int CHILD_WHO_STAGE_2_ID = 1221;
		private static final int CHILD_WHO_STAGE_3_ID = 1222;
		private static final int CHILD_WHO_STAGE_4_ID = 1223;
		private static final int ADULT_WHO_STAGE_4_ID = 1207;
		private static final int ADULT_WHO_STAGE_3_ID = 1206;
		private static final int ADULT_WHO_STAGE_2_ID = 1205;
		private static final int CHILD_WHO_STAGE_1_ID = 1220;
		private static final int ADULT_WHO_STAGE_1_ID = 1204;

		public Object invoke(MethodInvocation invocation) throws Throwable {
			Object args[] = invocation.getArguments();
			Encounter savedEncounter = (Encounter) args[0];
			Boolean newEncounter = false;
			if (savedEncounter.getId() == null) {
				newEncounter = true;
			}

			Object o = null;
			o = invocation.proceed();

			AppProperties appProperties = new AppPropertiesLoader(new AppProperties()).getAppProperties();
			Patient omrsPatient = savedEncounter.getPatient();
			Person oecPerson = new Person();

			HashSet<PersonIdentifier> patientIds = new PatientIdsMapper(omrsPatient).getPatientIds();

			PersonMapper personMapper = new PersonMapper(omrsPatient, oecPerson);
			personMapper.mapPatient(patientIds);
			List<OruFiller> fillers = new ArrayList<OruFiller>();
			for (Obs obs : savedEncounter.getObs()) {
				OruFiller oruFiller = new OruFiller();
				OruFillerMapper oruFillerMapper = new OruFillerMapper(obs, oruFiller);
				oruFiller.setCodingSystem((String) appProperties.getProperty("coding_system"));

				switch (obs.getConcept().getConceptId()) {
				case 160554:// HIV Diagnosis
					oruFillerMapper.setEvent(Event.HIV_DIAGNOSIS.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;
				case 160555: // HIV Care Initiation
					oruFillerMapper.setEvent(Event.HIV_CARE_INITIATION.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;
				case 159599: // ART Start Date
					oruFillerMapper.setEvent(Event.HIV_CARE_INITIATION.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;
				case 160534:// Transfer in
					oruFillerMapper.setEvent(Event.TRANSFER_IN.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;
				case 5497:// CD4 Count
					oruFillerMapper.setEvent(Event.CD4_COUNT.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;

				case 730:// CD4 Percent
					oruFillerMapper.setEvent(Event.CD4_PERCENT.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;

				case 856:// Viral Load
					oruFillerMapper.setEvent(Event.VIRAL_LOAD.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;
				case 5356:// WHO Stage
					oruFillerMapper.setEvent(Event.WHO_STAGE.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					int whoStageConceptId = obs.getValueCoded().getConceptId();
					Integer whoStage = -1;
					if (whoStageConceptId == ADULT_WHO_STAGE_1_ID || whoStageConceptId == CHILD_WHO_STAGE_1_ID) {
						whoStage = 1;
					} else if (whoStageConceptId == ADULT_WHO_STAGE_2_ID || whoStageConceptId == CHILD_WHO_STAGE_2_ID) {
						whoStage = 2;
					} else if (whoStageConceptId == ADULT_WHO_STAGE_3_ID || whoStageConceptId == CHILD_WHO_STAGE_3_ID) {
						whoStage = 3;
					} else if (whoStageConceptId == ADULT_WHO_STAGE_4_ID || whoStageConceptId == CHILD_WHO_STAGE_4_ID) {
						whoStage = 4;
					}
					oruFillerMapper.mapObs(whoStage.toString());
					break;
				case 161555:// Lost to follow up
					if (obs.getValueCoded().getConceptId() == LOST_TO_FOLLOWUP_CONCEPT_ID) {
						oruFillerMapper.setEvent(Event.LOST_TO_FOLLOWUP.getValue());
						fillers.add(oruFillerMapper.getOruFiller());
					} else if (obs.getValueCoded().getConceptId() == TRANSFER_OUT_CONCEPT_ID) {
						oruFillerMapper.setEvent(Event.TRANSFER_OUT.getValue());
						fillers.add(oruFillerMapper.getOruFiller());
					} else if (obs.getValueCoded().getConceptId() == DECEASED_CONCEPT_ID) {
						oruFillerMapper.setEvent(Event.DECEASED.getValue());
						fillers.add(oruFillerMapper.getOruFiller());
					} else {
						break;
					}
					oruFillerMapper.mapObs(obs.getEncounter().getEncounterDatetime());
					break;
				case 1255:// Change in regimen
					oruFillerMapper.setEvent(Event.CHANGE_IN_REGIMEN.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					if (obs.getValueCoded().getConceptId() == YES_CONCEPT_ID) {
						oruFillerMapper.mapObs(YES_STRING);
					} else if(obs.getValueCoded().getConceptId() == NO_CONCEPT_ID){
						oruFillerMapper.mapObs(NO_STRING);
					}else{
						oruFillerMapper.mapObs(null);						
					}
					break;
				case 1113:// TB Treatment
					oruFillerMapper.setEvent(Event.TB_TREATMENT.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;
				case 5599:// Birth
					oruFillerMapper.setEvent(Event.BIRTH.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;
				case 162227:// Art Eligibility
					oruFillerMapper.setEvent(Event.ART_ELIGIBILITY.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					oruFillerMapper.mapObs(null);
					break;
				case 1659: // TB Diagnosis
					if (obs.getValueCoded().getConceptId() == DISEASED_DIAGNOSED_CONCEPT_ID) {
						oruFillerMapper.setEvent(Event.TB_DIAGNOSIS.getValue());
						fillers.add(oruFillerMapper.getOruFiller());
						oruFillerMapper.mapObs(obs.getEncounter().getEncounterDatetime());
					}
					break;
				default:

					break;
				}
			}
			try {
				EventsHl7Service eventsHl7Service = new EventsHl7Service(personMapper.getOecPerson(), fillers, appProperties);

				if (newEncounter) {
					eventsHl7Service.doWork(Triggers.R01.getValue());
				} else {
					eventsHl7Service.doWork(Triggers.R01.getValue());
				}
			} catch (Exception ex) {
				log.debug(ex);
				System.out.println("Unable to send HL7 message: " + ex.getMessage());
			}

			return o;
		}
	}
}
