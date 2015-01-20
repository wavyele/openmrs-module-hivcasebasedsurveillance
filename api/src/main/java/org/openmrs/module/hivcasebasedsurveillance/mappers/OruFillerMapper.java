package org.openmrs.module.hivcasebasedsurveillance.mappers;

import org.kemricdc.hapi.oru.OruFiller;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;

public class OruFillerMapper {
	private Obs obs;
	private OruFiller oruFiller;
	private String event;
	public static final String CIEL_CONCEPT_DICTIONARY = "MVP/CIEL";

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

	public void mapObs() throws Exception {
		oruFiller.setObservationIdentifierText(this.getEvent());
		oruFiller.setCodingSystem(CIEL_CONCEPT_DICTIONARY);
		oruFiller.setDateTimeOfObservation(obs.getObsDatetime());

		ConceptDatatype conceptDataType = this.obs.getConcept().getDatatype();
		if (conceptDataType.isCoded()) {
			oruFiller.setObservationValue(obs.getValueCoded()
					.getDisplayString());
		} else if (conceptDataType.isNumeric()) {
			oruFiller.setObservationValue(obs.getValueNumeric().toString());
		} else if (conceptDataType.isDateTime()) {
			oruFiller.setObservationValue(obs.getValueDatetime().toString());
		} else if (conceptDataType.isDate()) {
			oruFiller.setObservationValue(obs.getValueDate().toString());
		} else if (conceptDataType.isText()) {
			oruFiller.setObservationValue(obs.getValueText());
		} else {
			throw new Exception("Unsupported Concept Datatype");
		}

	}

}
