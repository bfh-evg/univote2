/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.services;

import ch.bfh.univote2.component.core.UnivoteException;
import javax.ejb.Local;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface OutcomeRoutingService {

	public String getRoutingForUserInput(String userInputName) throws UnivoteException;
}
