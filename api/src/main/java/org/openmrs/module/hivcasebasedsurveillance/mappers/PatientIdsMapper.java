package org.openmrs.module.hivcasebasedsurveillance.mappers;

import java.util.HashSet;

import org.kemricdc.entities.IdentifierType;
import org.kemricdc.entities.PersonIdentifier;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;

public class PatientIdsMapper {
	private static final int NATIONAL_ID_NUMBER_ID = 5;
	private static final int PATIENT_CLINIC_NUMBER_ID = 4;
	private HashSet<PersonIdentifier> patientIds = new HashSet<PersonIdentifier>();
	private Patient patient;

	public PatientIdsMapper(Patient patient) {
		this.patient = patient;
	}

	public HashSet<PersonIdentifier> getPatientIds() {
		patientIds = getAssignedPatientIds();
		return patientIds;
	}

	public void setPatientIds(HashSet<PersonIdentifier> patientIds) {
		this.patientIds = patientIds;
	}

	private HashSet<PersonIdentifier> getAssignedPatientIds() {
		HashSet<PersonIdentifier> patientIds = new HashSet<PersonIdentifier>();

		PersonIdentifier p0 = new PersonIdentifier();
		p0.setIdentifierType(IdentifierType.PID);
		p0.setIdentifier(this.patient.getPatientId().toString());
		patientIds.add(p0);
		for (PatientIdentifierType pidType : Context.getPatientService().getPatientIdentifierTypes(null, null, null, null)) {
			PatientIdentifier patientIdentifier = this.patient.getPatientIdentifier(pidType);
			if (patientIdentifier != null) {
				String patientIdentifierValue = patientIdentifier.getIdentifier();
				switch (pidType.getId()) {
				case PATIENT_CLINIC_NUMBER_ID:// Patient Clinic Number
					if (patientIdentifierValue != null) {
						PersonIdentifier p = new PersonIdentifier();
						p.setIdentifierType(IdentifierType.CCC);
						p.setIdentifier(patientIdentifierValue);
						patientIds.add(p);
					}
					break;
				case NATIONAL_ID_NUMBER_ID: // National ID Number
					if (patientIdentifierValue != null) {
						PersonIdentifier p = new PersonIdentifier();
						p.setIdentifierType(IdentifierType.NATIONAL);
						p.setIdentifier(patientIdentifierValue);
						patientIds.add(p);
					}
					break;
				default:
					break;
				}
			}
		}

		return patientIds;
	}
}
