package org.openmrs.module.hivcasebasedsurveillance.advice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kemricdc.constants.Triggers;
import org.kemricdc.entities.AppProperties;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.hapi.EventsHl7Service;
import org.kemricdc.hapi.util.OruFiller;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivcasebasedsurveillance.constants.Event;
import org.openmrs.module.hivcasebasedsurveillance.mappers.OruFillerMapper;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PatientIdsMapper;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PersonMapper;
import org.openmrs.module.hivcasebasedsurveillance.utils.AppPropertiesLoader;
import org.springframework.aop.AfterReturningAdvice;

public class PatientOrderReturningAdvice implements AfterReturningAdvice {
	private static final int MIN_DRUGS_IN_A_REGIMEN = 3;
	private static final String CODING_SYSTEM_PROPERTY_NAME = "coding_system";
	private static final int CHANGE_REGIMEN_CONCEPT_ID = 1259;
	private static final int START_DRUGS_CONCEPT_ID = 1256;
	private static final int ARV_PLAN_CONCEPT_ID = 1255;
	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		if (method.getName().equals("saveOrder")) {
			Order order = (Order) returnValue;
			Patient omrsPatient = order.getPatient();
			org.openmrs.Person omrsPerson = Context.getPersonService().getPerson(omrsPatient.getPersonId());
			@SuppressWarnings("deprecation")
			List<DrugOrder> drugOrders = Context.getOrderService().getDrugOrdersByPatient(omrsPatient);
			if (drugOrders != null) {
				/*
				 * Check if the drug order fulfills the combination of 3 or more
				 * drugs per regimen
				 */
				if (drugOrders.size() >= MIN_DRUGS_IN_A_REGIMEN) {
					// Construct the regimen from drug orders
					String regimen = "";
					for (DrugOrder drugOrder : drugOrders) {
						regimen += drugOrder.getConcept().getShortNameInLocale(Locale.ENGLISH).getName() + "/";
					}
					// Truncate the trailing "/" from the regimen string
					regimen = regimen.substring(0, regimen.length() - 1);

					// check the ARV plan
					// 1255=> ARV Plan
					Concept arvPlanConcept = Context.getConceptService().getConcept(ARV_PLAN_CONCEPT_ID);

					// 1256 => start drugs
					Concept startDrugsConcept = Context.getConceptService().getConcept(START_DRUGS_CONCEPT_ID);
					// 1259 => Change Regimen
					Concept changeRegimenConcept = Context.getConceptService().getConcept(CHANGE_REGIMEN_CONCEPT_ID);

					List<Obs> lastArvPlanObsList = Context.getObsService().getObservations(Collections.singletonList(omrsPerson), null,
							Collections.singletonList(arvPlanConcept), null, null, null, null, 1, null, null, null, false);
					if (lastArvPlanObsList != null) {
						if (lastArvPlanObsList.size() > 0) {
							AppProperties appProperties = new AppPropertiesLoader(new AppProperties()).getAppProperties();
							Person oecPerson = new Person();
							List<OruFiller> fillers = new ArrayList<OruFiller>();
							OruFiller oruFiller = new OruFiller();
							Obs lastArvPlanObs = lastArvPlanObsList.get(0);
							OruFillerMapper oruFillerMapper = new OruFillerMapper(lastArvPlanObs, oruFiller);
							HashSet<PersonIdentifier> patientIds = new PatientIdsMapper(omrsPatient).getPatientIds();
							PersonMapper personMapper = new PersonMapper(omrsPatient, oecPerson);
							personMapper.mapPatient(patientIds);
							oruFiller.setCodingSystem((String) appProperties.getProperty(CODING_SYSTEM_PROPERTY_NAME));
							Boolean proceed = true;
							if (lastArvPlanObs.getValueCoded().equals( startDrugsConcept)) {
								oruFillerMapper.setEvent(Event.FIRST_LINE_REGIMEN.getValue());
								fillers.add(oruFillerMapper.getOruFiller());
								oruFillerMapper.mapObs(regimen);

							} else if (lastArvPlanObs.getValueCoded().equals(changeRegimenConcept)) {
								oruFillerMapper.setEvent(Event.SECOND_LINE_REGIMEN.getValue());
								fillers.add(oruFillerMapper.getOruFiller());
								oruFillerMapper.mapObs(regimen);
							} else {
								proceed = false;
							}
							if (proceed) {
								try {
									EventsHl7Service eventsHl7Service = new EventsHl7Service(personMapper.getOecPerson(), fillers,
											appProperties);
									eventsHl7Service.doWork(Triggers.R01.getValue());
								} catch (Exception ex) {
									log.debug(ex);
									System.out.println("Unable to send HL7 message: " + ex.getMessage());
								}
							}
						}
					}
				}
			}
		}
	}
}
