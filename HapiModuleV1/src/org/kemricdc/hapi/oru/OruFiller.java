/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kemricdc.hapi.oru;

import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.NM;
import ca.uhn.hl7v2.model.v25.datatype.SI;
import java.util.Date;

/**
 *
 * @author Stanslaus Odhiambo
 */
public class OruFiller {

    

    public OruFiller() {
        this.segmentId = "OBX";
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public SI getSetId() {
        return setId;
    }

    public void setSetId(SI setId) {
        this.setId = setId;
    }


    public String getObservationSubId() {
        return observationSubId;
    }

    public void setObservationSubId(String observationSubId) {
        this.observationSubId = observationSubId;
    }

    public String getObservationValue() {
        return observationValue;
    }

    public void setObservationValue(String observationValue) {
        this.observationValue = observationValue;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getReferencesRange() {
        return referencesRange;
    }

    public void setReferencesRange(String referencesRange) {
        this.referencesRange = referencesRange;
    }

    public String getAbnormalFlags() {
        return abnormalFlags;
    }

    public void setAbnormalFlags(String abnormalFlags) {
        this.abnormalFlags = abnormalFlags;
    }

    public String getProbability() {
        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }

    public String getNatureOfAbnormalTest() {
        return natureOfAbnormalTest;
    }

    public void setNatureOfAbnormalTest(String natureOfAbnormalTest) {
        this.natureOfAbnormalTest = natureOfAbnormalTest;
    }

    /**
     * This method returns the result status of the value we are sending through
     * Possible values include: F - Final results; N - Not asked; used to
     * affirmatively document that the observation identified in OBX was not
     * sought when the universal service ID in OBR-4 implies that it would be
     * sought; P - Preliminary results; S - Partial results; X - Results cannot
     * be obtained for this observation.
     *
     * @return - THE STATUS
     */
    public String getResultStatus() {
        return resultStatus;
    }

    /**
     * This method sets the result status of the value we are sending through
     * Possible values include: F - Final results; N - Not asked; used to
     * affirmatively document that the observation identified in OBX was not
     * sought when the universal service ID in OBR-4 implies that it would be
     * sought; P - Preliminary results; S - Partial results; X - Results cannot
     * be obtained for this observation.
     *
     * @param resultStatus The value to be set
     */
    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public Date getDateOfLastNormalValue() {
        return dateOfLastNormalValue;
    }

    public void setDateOfLastNormalValue(Date dateOfLastNormalValue) {
        this.dateOfLastNormalValue = dateOfLastNormalValue;
    }

    public String getUserDefinedAccessChecks() {
        return userDefinedAccessChecks;
    }

    public void setUserDefinedAccessChecks(String userDefinedAccessChecks) {
        this.userDefinedAccessChecks = userDefinedAccessChecks;
    }

    public Date getDateTimeOfObservation() {
        return dateTimeOfObservation;
    }

    public void setDateTimeOfObservation(Date dateTimeOfObservation) {
        this.dateTimeOfObservation = dateTimeOfObservation;
    }

    public String getProducerId() {
        return producerId;
    }

    public void setProducerId(String producerId) {
        this.producerId = producerId;
    }

    public String getResponsibleObserver() {
        return responsibleObserverId;
    }

    public void setResponsibleObserver(String responsibleObserver) {
        this.responsibleObserverId = responsibleObserver;
    }

    public String getObservationMethod() {
        return observationMethod;
    }

    public void setObservationMethod(String observationMethod) {
        this.observationMethod = observationMethod;
    }

    public String getObservationIdentifier() {
        return observationIdentifier;
    }

    public void setObservationIdentifier(String observationIdentifier) {
        this.observationIdentifier = observationIdentifier;
    }

    public String getObservationIdentifierText() {
        return observationIdentifierText;
    }

    public void setObservationIdentifierText(String observationIdentifierText) {
        this.observationIdentifierText = observationIdentifierText;
    }

    public String getCodingSystem() {
        return codingSystem;

    }

    public void setCodingSystem(String codingSystem) {
        this.codingSystem = codingSystem;
    }


    public String getResponsibleObserverId() {
        return responsibleObserverId;
    }

    public void setResponsibleObserverId(String responsibleObserverId) {
        this.responsibleObserverId = responsibleObserverId;
    }

    public String getResponsibleObserverGivenName() {
        return responsibleObserverGivenName;
    }

    public void setResponsibleObserverGivenName(String responsibleObserverGivenName) {
        this.responsibleObserverGivenName = responsibleObserverGivenName;
    }
    
    

    public enum ValueType {

        CE,
        TX,
        ST,
        NM

    }

    private String segmentId;
    private SI setId;
    private ValueType valueType;
    private String observationSubId;
    private String observationValue;
    private String units;
    private String referencesRange;
    private String abnormalFlags;
    private String probability;
    private String natureOfAbnormalTest;
    private String resultStatus;
    private Date dateOfLastNormalValue;
    private String userDefinedAccessChecks;
    private Date dateTimeOfObservation;
    private String producerId;
    private String responsibleObserverId;
    private String responsibleObserverGivenName;
    
    private String observationMethod;

    //New Fields
    private String observationIdentifier;
    private String observationIdentifierText;
    private String codingSystem;
}
