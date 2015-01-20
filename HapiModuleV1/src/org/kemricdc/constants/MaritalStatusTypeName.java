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
public enum MaritalStatusTypeName {
    
    SINGLE("SINGLE"),
    MONOGAMOUS_MARRIED("MONOGAMOUS_MARRIED"),
    POLYGAMOUS_MARRIED("POLYGAMOUS_MARRIED"),
    DIVORCED("DIVORCED"),
    SEPARATED("SEPARATED"),
    WIDOWED("WIDOWED"),
    COHABITING("COHABITING"),
    MISSING("MISSING");
    private final String value;
    
    private MaritalStatusTypeName(String value){
        this.value=value;        
    }
    public String getValue(){
        return value;
        
    }
    
}
