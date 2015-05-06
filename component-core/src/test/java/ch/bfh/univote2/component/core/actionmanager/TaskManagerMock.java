/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote2.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.component.core.actionmanager;

import static ch.bfh.unicrypt.helper.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.data.Task;
import ch.bfh.univote2.component.core.manager.TaskManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class TaskManagerMock implements TaskManager {

    Map<String, List<Task>> tasks = new HashMap<>();

    @Override
    public String addTask(Task task) {
        List<Task> t = new ArrayList<>();
        t.add(task);
        this.tasks.put(task.getTenant(), t);
        FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
        return fixedStringSet.getRandomElement().getValue();
    }

    @Override
    public List<Task> getTasks(String tenant) {
        return this.tasks.get(tenant);
    }

}
