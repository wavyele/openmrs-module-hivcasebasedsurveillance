package org.openmrs.module.hivcasebasedsurveillance.mappers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.kemricdc.hapi.util.OruFiller;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;

public class OruFillerMapper {
	private Obs obs;
	private OruFiller oruFiller;
	private String event;
	public static final String CIEL_CONCEPT_DICTIONARY = "MVP/CIEL";
	private SimpleDateFormat sdo = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);

	public Obs getObs() {
		return obs;
	}

	public void setObs(Obs obs) {
		this.obs = obs;
	}

	public OruFiller getOruFiller() {
		return oruFiller;
	}

	public void setOruFiller(OruFiller oruFiller) {
		this.oruFiller = oruFiller;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public OruFillerMapper() {

	}

	public OruFillerMapper(Obs obs, OruFiller oruFiller) {
		this.setObs(obs);
		this.setOruFiller(oruFiller);
	}

	public OruFillerMapper(Obs obs, OruFiller oruFiller, String event) {
		this.setObs(obs);
		this.setOruFiller(oruFiller);
		this.setEvent(event);
	}

	public void mapObs(Object explicitValue) throws Exception {
		oruFiller.setObservationIdentifierText(this.getEvent());
		oruFiller.setDateTimeOfObservation(sdo.format(obs.getObsDatetime()));
		/*
		 * oruFiller.setCodingSystem(CIEL_CONCEPT_DICTIONARY);
		 * oruFiller.setObservationIdentifier
		 * (obs.getConcept().getConceptId().toString());
		 * oruFiller.setObservationIdentifierText
		 * (obs.getConcept().getPreferredName(Locale.ENGLISH).getName());
		 */

		if (explicitValue == null) {
			ConceptDatatype conceptDataType = this.obs.getConcept().getDatatype();
			if (conceptDataType.isCoded()) {
				oruFiller.setObservationValue(obs.getValueCoded().getDisplayString());
			} else if (conceptDataType.isNumeric()) {
				oruFiller.setObservationValue(obs.getValueNumeric().toString());
			} else if (conceptDataType.isDateTime()) {
				oruFiller.setObservationValue(sdo.format(obs.getValueDatetime()));
			} else if (conceptDataType.isDate()) {
				oruFiller.setObservationValue(sdo.format(obs.getValueDate()));
			} else if (conceptDataType.isText()) {
				oruFiller.setObservationValue(obs.getValueText());
			} else {
				throw new Exception(ConceptDatatype.TEXT + ": Unsupported Concept Datatype");
			}
		} else {
			if (explicitValue instanceof Date) {
				oruFiller.setObservationValue(sdo.format(explicitValue));
			} else {
				oruFiller.setObservationValue((String) explicitValue);
			}
		}
	}

}
