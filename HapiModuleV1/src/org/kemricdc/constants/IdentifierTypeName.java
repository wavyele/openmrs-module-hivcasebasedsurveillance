/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kemricdc.constants;

/**
 *
 * @author Stanslaus Odhiambo
 */
public enum IdentifierTypeName {
    
    

    CCC_NUMBER("CCC_NUMBER"),
    ANC_NUMBER("ANC_NUMBER"),
    PMTCT_NUMBER("PMTCT_NUMBER"),
    TB_NUMBER("TB_NUMBER"),
    NATIONAL_ID("NATIONAL_ID"),
    GBVRC_NUMBER("GBVRC_NUMBER"),
    HEI_NUMBER("HEI_NUMBER");
    
    
    
    private final String value;
    
    
    public String getValue(){
        return value;
    }
    private IdentifierTypeName(String value){
        this.value=value;
    }

}
