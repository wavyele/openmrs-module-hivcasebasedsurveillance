package org.openmrs.module.hivcasebasedsurveillance.mappers;

import java.util.HashSet;
import java.util.Set;

import org.kemricdc.constants.IdentifierTypeName;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;

public class PersonMapper {

	public static final Integer CCC_NUMBER_ID = 6;
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
		oecPerson.setBirthdate(omrsPatient.getBirthdate());
		oecPerson.setSex(omrsPatient.getGender());

		PatientIdentifier pid = omrsPatient.getPatientIdentifier(Context
				.getPatientService().getPatientIdentifierType(CCC_NUMBER_ID));
		if (pid != null) {
			Set<PersonIdentifier> patientIds = new HashSet<PersonIdentifier>();

			PersonIdentifier p0 = new PersonIdentifier();
			p0.setIdentifierType(IdentifierTypeName.CCC_NUMBER);
			p0.setIdentifier(pid.getIdentifier());
			patientIds.add(p0);
			oecPerson.setPersonIdentifiers(patientIds);
		} else {
			System.out.println("Patient identifier(CCC Number) not found");
		}

	}
}
