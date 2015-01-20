package org.openmrs.module.hivcasebasedsurveillance.advice;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.hivcasebasedsurveillance.mappers.PersonMapper;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.kemricdc.constants.IdentifierTypeName;
import org.kemricdc.entities.Person;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.hapi.adt.PatientRegistrationAndUpdate;

public class PatientAroundAdvisor extends StaticMethodMatcherPointcutAdvisor
		implements Advisor {

	private static final long serialVersionUID = -6843825997193392439L;

	private Log log = LogFactory.getLog(this.getClass());

	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		if (method.getName().equals("savePatient")) {
			return true;
		}
		return false;
	}

	@Override
	public Advice getAdvice() {
		return new PatientAroundAdvice();
	}

	private class PatientAroundAdvice implements MethodInterceptor {

		public Object invoke(MethodInvocation invocation) throws Throwable {

			Object args[] = invocation.getArguments();
			Patient patient = (Patient) args[0];

			if (patient.getPatientId() == null) {
				System.out.println("New Patient");
			} else {
				System.out.println("Existing Patient");
			}

			Object o = invocation.proceed();

			Person person = new Person();

			PersonMapper personMapper = new PersonMapper(patient, person);
			personMapper.mapPatient();

			PatientRegistrationAndUpdate pru = new PatientRegistrationAndUpdate(
					personMapper.getOecPerson(), null, null, null);

			try {
				pru.processRegistrationOrUpdate("A04");
			} catch (Exception Ex) {
				System.out.println("Unable to generate HL7 message. Error: "
						+ Ex.getMessage());
			}

			return o;
		}
	}
}
