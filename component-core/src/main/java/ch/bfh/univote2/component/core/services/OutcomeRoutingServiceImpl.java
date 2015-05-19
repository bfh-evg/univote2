/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.services;

import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;

@Singleton
@DependsOn("ConfigurationManagerImpl")
public class OutcomeRoutingServiceImpl implements OutcomeRoutingService {

    private static final String CONFIGURATION_NAME = "outcome-routing";

    private final Map<String, String> routing = new HashMap<>();

    /**
     * ConfigurationHelper. Gives access to configurations stored in the JNDI.
     */
    @EJB
    private ConfigurationManager configurationManager;

    @PostConstruct
    public void init() {
        Properties config = this.configurationManager.getConfiguration(CONFIGURATION_NAME);
        for (String userInputname : config.stringPropertyNames()) {
            this.routing.put(userInputname, config.getProperty(userInputname));
        }
    }

    @Override
    public String getRoutingForUserInput(String userInputName) throws UnivoteException {
        if (this.routing.containsKey(userInputName)) {
            return this.routing.get(userInputName);
        }
        throw new UnivoteException("Unknown user input name: " + userInputName);
    }
    // Methods for testing

    protected Map<String, String> getRouting() {
        return this.routing;
    }

}
