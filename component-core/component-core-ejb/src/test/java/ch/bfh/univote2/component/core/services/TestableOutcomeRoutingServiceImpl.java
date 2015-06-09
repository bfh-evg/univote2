/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.services;

import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class TestableOutcomeRoutingServiceImpl extends OutcomeRoutingServiceImpl {

	@Override
	public void init() {
	}

	public void runInit() {
		super.init();
	}

	@Override
	public Map<String, String> getRouting() {
		return super.getRouting();
	}

}
