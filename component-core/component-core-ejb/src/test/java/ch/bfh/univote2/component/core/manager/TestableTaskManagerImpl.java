/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.manager;

import ch.bfh.univote2.component.core.data.Task;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

@Singleton
@LocalBean
public class TestableTaskManagerImpl extends TaskManagerImpl {

	@Override
	public Map<String, Task> getTasks() {
		return super.getTasks();
	}

}
