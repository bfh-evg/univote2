/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.bfh.univote.voteclient.parameters;

/**
 *
 * @author Philémon von Bergen &lt;philemon.vonbergen@bfh.ch&gt;
 */
public class ParametersNotFoundException extends Exception {
    
    public ParametersNotFoundException(String message){
        super(message);
    }
}
