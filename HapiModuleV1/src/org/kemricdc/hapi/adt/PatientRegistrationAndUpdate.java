/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kemricdc.hapi.adt;

import org.kemricdc.entities.Person;
import org.kemricdc.hapi.PersonFactory;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.examples.ExampleReceiverApplication;
import ca.uhn.hl7v2.hoh.api.DecodeException;
import ca.uhn.hl7v2.hoh.api.EncodeException;
import ca.uhn.hl7v2.hoh.api.IReceivable;
import ca.uhn.hl7v2.hoh.api.ISendable;
import ca.uhn.hl7v2.hoh.api.MessageMetadataKeys;
import ca.uhn.hl7v2.hoh.hapi.api.MessageSendable;
import ca.uhn.hl7v2.hoh.hapi.client.HohClientSimple;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.*;
import ca.uhn.hl7v2.model.v24.segment.MSH;
import ca.uhn.hl7v2.model.v24.segment.PID;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kemricdc.entities.Address;
import org.kemricdc.entities.Location;
import org.kemricdc.entities.PatientSource;
import org.kemricdc.entities.PersonIdentifier;
import org.kemricdc.hapi.SendHL7String;

/**
 *
 * @author Stanslaus Odhiambo
 */
public class PatientRegistrationAndUpdate {

    PersonFactory factory;
    Person person;
    HapiContext context = new DefaultHapiContext();
    Properties properties = new Properties();

    public PatientRegistrationAndUpdate(Person person, Set<Address> addresses, Location location,
            PatientSource patientSource) {

        factory = new PersonFactory(person, addresses, location, patientSource);
        this.person = factory.buildPerson();

    }

    public void processRegistrationOrUpdate(String triggerEvent) {
        try {
            properties.load(this.getClass().getResourceAsStream("/site.properties"));
            String s = generateHL7String(triggerEvent);
            // sendMessage(adt);
            
            new SendHL7String().sendStringMessage(s);

        } catch (HL7Exception | IOException ex) {
            Logger.getLogger(PatientRegistrationAndUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private ADT_A01 generateHL7(String triggerEvent) throws HL7Exception, IOException {

        Set<PersonIdentifier> identifiers = new HashSet<>();
        PersonIdentifier pi = new PersonIdentifier();
        pi.setIdentifier("1234567");
        identifiers.add(pi);
        person.setPersonIdentifiers(identifiers);
        person.setBirthdate(new Date());

        ADT_A01 adt_a01 = new ADT_A01();
        adt_a01.initQuickstart("ADT", "A04", "T");

        // Populate the MSH Segment
        MSH mshSegment = adt_a01.getMSH();
        mshSegment.getSendingApplication().getNamespaceID().setValue(properties.getProperty("application_name"));
        mshSegment.getSendingFacility().getNamespaceID().setValue(properties.getProperty("facility_name"));
        mshSegment.getSequenceNumber().setValue(properties.getProperty("sequence_number"));

        // Populate the PID Segment
        PID pid = adt_a01.getPID();
        pid.getPatientName(0).getFamilyName().getSurname().setValue(person.getLastName());
        pid.getPatientName(0).getGivenName().setValue(person.getFirstName());
        pid.getPatientIdentifierList(0).getID().setValue(((PersonIdentifier) person.getPersonIdentifiers().iterator().next()).getIdentifier());
        pid.getAdministrativeSex().setValue(person.getSex());
        pid.getDateTimeOfBirth().getTimeOfAnEvent().setValue(person.getBirthdate());
        pid.getMaritalStatus().getText().setValue(person.getMaritalStatusType().getValue());

        /*
         * Populate more fields and Segments
         */
        //  encoding the message and look at the output
        Parser parser = context.getPipeParser();
        String encodedMessage = parser.encode(adt_a01);
        System.out.println("\nPrinting ER7 Encoded Message:");
        System.out.println("\n" + encodedMessage);

//        // Next, let's use the XML parser to encode as XML
//        parser = context.getXMLParser();
//        encodedMessage = parser.encode(adt_a01);
//        System.out.println("Printing XML Encoded Message:");
//        System.out.println(encodedMessage);
        return adt_a01;
    }

    private String generateHL7String(String triggerEvent) throws HL7Exception, IOException {

        ADT_A01 adt_a01 = new ADT_A01();
        adt_a01.initQuickstart("ADT", triggerEvent, "T");
        

        // Populate the MSH Segment
        MSH mshSegment = adt_a01.getMSH();
        mshSegment.getSendingApplication().getNamespaceID().setValue(properties.getProperty("sending_application"));
        mshSegment.getSendingFacility().getNamespaceID().setValue(properties.getProperty("sending_facility"));
        mshSegment.getSequenceNumber().setValue(properties.getProperty("sequence_number"));
        mshSegment.getMessageType().getMessageStructure().setValue("ADT_"+triggerEvent);

        // Populate the PID Segment
        PID pid = adt_a01.getPID();
        pid.getPatientName(0).getFamilyName().getSurname().setValue(person.getLastName());
        pid.getPatientName(0).getGivenName().setValue(person.getFirstName());
        int count = 0;
        Set<PersonIdentifier> identifiers = person.getPersonIdentifiers();

        for (PersonIdentifier personIdentifier : identifiers) {
            pid.getPatientIdentifierList(count).getID().setValue(personIdentifier.getIdentifier());
            pid.getPatientIdentifierList(count).getIdentifierTypeCode().setValue(personIdentifier.getIdentifierType().getValue());
            count++;
        }
        pid.getAdministrativeSex().setValue(person.getSex());
        pid.getDateTimeOfBirth().getTimeOfAnEvent().setValue(person.getBirthdate());
        pid.getMaritalStatus().getCe1_Identifier().setValue(person.getMaritalStatusType().getValue());
//        pid.getMaritalStatus().getText().setValue(person.getMaritalStatusType().getValue());

        /*
         * Populate more fields and Segments
         */
        //  encoding the message and look at the output
        //First, lets use the XML Parser to encode XML
        Parser parser = context.getXMLParser();
        String encodedMessage = parser.encode(adt_a01);
        System.out.println("\nPrinting XML Encoded Message:");
        System.out.println("\n" + encodedMessage);

        // Next, let's use the ER7 parser to encode as ER7
        parser = context.getPipeParser();
        encodedMessage = parser.encode(adt_a01);
        System.out.println("\nPrinting ER7 Encoded Message:");
        System.out.println("\n" + encodedMessage);

        return encodedMessage;
    }

    /**
     * Sends an ADT_A01 message via http to the receiving application
     * @param adt The message to be sent out
     */
    public void sendMessage(ADT_A01 adt) {
        String host = properties.getProperty("host");
        int port = Integer.parseInt(properties.getProperty("port"));
        String uri = properties.getProperty("uri");

        // Create a parser
        Parser parser = PipeParser.getInstanceWithNoValidation();

        // Create a client
        HohClientSimple client = new HohClientSimple(host, port, uri, parser);
        // The MessageSendable provides the message to send 

        try {
            ISendable sendable = new MessageSendable(adt);
            // sendAndReceive actually sends the message
            IReceivable<Message> receivable = client.sendAndReceiveMessage(sendable);

            // receivavle.getRawMessage() provides the response
            Message message = receivable.getMessage();
            System.out.println("\nResponse was:\n" + message.encode());

            // IReceivable also stores metadata about the message
            String remoteHostIp = (String) receivable.getMetadata().get(MessageMetadataKeys.REMOTE_HOST_ADDRESS);
            System.out.println("\nFrom:\n" + remoteHostIp);

            /*
             * Note that the client may be reused as many times as you like,
             * by calling sendAndReceiveMessage repeatedly
             */
        } catch (DecodeException | EncodeException | HL7Exception | IOException e) {
            // Thrown if the response can't be read
            Logger.getLogger(PatientRegistrationAndUpdate.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    /**
     * Method to sent the "Stringified" message of any type to the host and port 
     * specified in the properties file
     * @param s  HL7 string version to be sent
     */
    

    public void sendOverTCPs(ADT_A01 adt_a01) {
        String host = properties.getProperty("host");
        int port = Integer.parseInt(properties.getProperty("port"));
        boolean useTLS = Boolean.parseBoolean(properties.getProperty("useTLS"));
        HapiContext context = new DefaultHapiContext();
        HL7Service server = context.newServer(port, useTLS);

        /*
         * The server may have any number of "application" objects registered to
         * handle messages. We are going to create an application to listen to
         * ADT^A01 messages.
         * 
         * You might want to look at the source of ExampleReceiverApplication
         * (it's a nested class below) to see how it works.
         */
        ReceivingApplication handler = new ExampleReceiverApplication();
        server.registerApplication("ADT", "A01", handler);

        /*
         * We are going to register the same application to handle ADT^A02
         * messages. Of course, we coud just as easily have specified a different
         * handler.
         */
        server.registerApplication("ADT", "A02", handler);
        try {
            // Start the server listening for messages
            server.startAndWait();
        } catch (InterruptedException ex) {
            Logger.getLogger(PatientRegistrationAndUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
