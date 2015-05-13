/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.manager;

import static ch.bfh.unicrypt.helper.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.data.Task;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Singleton;

@Singleton
public class TaskManagerImpl implements TaskManager {

    Map<String, List<Task>> tasks = new HashMap<>();

    @Override
    public String addTask(Task task) {
        FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
        return fixedStringSet.getRandomElement().getValue();
    }

    @Override
    public List<Task> getTasks(String tenant) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
