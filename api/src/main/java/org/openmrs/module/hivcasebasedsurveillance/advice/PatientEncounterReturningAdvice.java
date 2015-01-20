package org.openmrs.module.hivcasebasedsurveillance.advice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kemricdc.constants.Event;
import org.kemricdc.hapi.SendHL7String;
import org.kemricdc.hapi.oru.OruFiller;
import org.kemricdc.hapi.oru.ProcessTransactions;
import org.kemricdc.entities.Person;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.module.hivcasebasedsurveillance.mappers.OruFillerMapper;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PersonMapper;
import org.springframework.aop.AfterReturningAdvice;

public class PatientEncounterReturningAdvice implements AfterReturningAdvice {
	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public void afterReturning(Object returnValue, Method method,
			Object[] args, Object target) throws Throwable {
		if (method.getName().equals("saveEncounter")) {
			log.info("Method " + method.getName());
			Encounter SavedEncounter = (Encounter) returnValue;
			Patient omrsPatient = SavedEncounter.getPatient();
			Person oecPerson = new Person();

			PersonMapper personMapper = new PersonMapper(omrsPatient, oecPerson);
			personMapper.mapPatient();

			List<OruFiller> fillers = new ArrayList<OruFiller>();

			for (Obs obs : SavedEncounter.getObs()) {
				OruFiller oruFiller = new OruFiller();
				OruFillerMapper oruFillerMapper = new OruFillerMapper(obs,
						oruFiller);
				switch (obs.getConcept().getConceptId()) {
				case 160554:// HIV Diagnosis
					oruFillerMapper.setEvent(Event.HIV_DIAGNOSIS.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					break;
				case 160555: // HIV Care Initiation
					oruFillerMapper.setEvent(Event.HIV_CARE_INITIATION
							.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
					break;
				case 1088: // ART Start Date
					// oruFillerMapper.setEvent(Event.HIV_CARE_INITIATION.getValue());
					// fillers.add(oruFillerMapper.getOruFiller());
					break;
				case 160563:// Transfer in
					oruFillerMapper.setEvent(Event.TRANSFER_IN.getValue());
					fillers.add(oruFillerMapper.getOruFiller());
				default:

					break;
				}
			}

			try {
				ProcessTransactions bxSegment = new ProcessTransactions(
						personMapper.getOecPerson(), fillers);
				String bxString = bxSegment.generateORU();
				new SendHL7String().sendStringMessage(bxString);
			} catch (Exception ex) {
				System.out.println("Unable to send HL7 message: "
						+ ex.getMessage());
			}

		}
	}

}
