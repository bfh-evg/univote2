/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.example;

import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.services.OutcomeRoutingService;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class OutcomeRoutingServiceImpl implements OutcomeRoutingService {

	private final Map<String, String> routingMap;

	public OutcomeRoutingServiceImpl() {
		this.routingMap = new HashMap<>();
		this.routingMap.put("InitInput", "initActionInput");
	}

	@Override
	public String getRoutingForUserInput(String userInputName) throws UnivoteException {
		if (routingMap.containsKey(userInputName)) {
			return routingMap.get(userInputName);
		}
		throw new UnivoteException("Undefined userInputName " + userInputName);
	}

}
