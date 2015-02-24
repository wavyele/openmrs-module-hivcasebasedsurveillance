package org.openmrs.module.hivcasebasedsurveillance.mappers;

import java.util.ArrayList;
import java.util.Set;

import org.kemricdc.entities.MaritalStatus;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.entities.Sex;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class PersonMapper {

	private Patient omrsPatient;
	private Person oecPerson;

	public Patient getPatient() {
		return omrsPatient;
	}

	public void setPatient(Patient patient) {
		this.omrsPatient = patient;
	}

	public Person getOecPerson() {
		return oecPerson;
	}

	public void setOmrsPerson(Person person) {
		this.oecPerson = person;
	}

	public PersonMapper(Patient patient, Person person) {
		this.setPatient(patient);
		this.setOmrsPerson(person);
	}

	public PersonMapper() {

	}

	public void mapPatient() {

		oecPerson.setFirstName(omrsPatient.getGivenName());
		oecPerson.setMiddleName(omrsPatient.getMiddleName());
		oecPerson.setLastName(omrsPatient.getFamilyName());
		oecPerson.setDob(omrsPatient.getBirthdate());
		oecPerson.setSex(omrsPatient.getGender().equals("M") ? Sex.MALE : Sex.FEMALE);

		Concept maritalStatusConcept = Context.getConceptService().getConcept(1054);
		ArrayList<Obs> maritalStatusObs = (ArrayList<Obs>) Context.getObsService().getObservationsByPersonAndConcept(omrsPatient,
				maritalStatusConcept);
		if (maritalStatusObs == null) {
			oecPerson.setMaritalStatus(MaritalStatus.MISSING);
		} else {
			if (maritalStatusObs.size() == 0) {
				oecPerson.setMaritalStatus(MaritalStatus.MISSING);
			} else {
				Obs lastMaritalStatusRecorded = (Obs) maritalStatusObs.get(0);
				this.mapPatientMaritalStatus(lastMaritalStatusRecorded.getValueCoded().getConceptId());
			}
		}

	}

	public void mapPatient(Set<PersonIdentifier> patientIds) {
		this.mapPatient();
		oecPerson.setPersonIdentifiers(patientIds);
	}

	public void mapPatientMaritalStatus(int maritalStatus) {
		switch (maritalStatus) {
		case 1057:
			oecPerson.setMaritalStatus(MaritalStatus.SINGLE);
			break;
		case 5555:
			oecPerson.setMaritalStatus(MaritalStatus.MONOGAMOUS_MARRIED);
			break;
		case 159715:
			oecPerson.setMaritalStatus(MaritalStatus.POLYGAMOUS_MARRIED);
			break;
		case 1058:
			oecPerson.setMaritalStatus(MaritalStatus.DIVORCED);
			break;
		case 1056:
			oecPerson.setMaritalStatus(MaritalStatus.SEPARATED);
			break;
		case 1059:
			oecPerson.setMaritalStatus(MaritalStatus.WIDOWED);
			break;
		case 1060:
			oecPerson.setMaritalStatus(MaritalStatus.COHABITING);
			break;
		default:
			oecPerson.setMaritalStatus(MaritalStatus.MISSING);
		}

	}

}
