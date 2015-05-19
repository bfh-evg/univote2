/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.manager;

import static ch.bfh.unicrypt.helper.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.RunActionTask;
import ch.bfh.univote2.component.core.data.Task;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputTask;
import ch.bfh.univote2.component.core.services.InformationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Singleton;

@Singleton
public class TaskManagerImpl implements TaskManager {

    @EJB
    ActionManager actionManager;
    @EJB
    InformationService informationService;

    Map<String, Task> tasks = new HashMap<>();
    private static final Logger logger = Logger.getLogger(TaskManagerImpl.class.getName());

    @Override
    public List<Task> getTasks(String tenant) {
        List<Task> tenantTasks = new ArrayList<>();
        for (Task t : tasks.values()) {
            if (t.getTenant().equals(tenant)) {
                tenantTasks.add(t);
            }
        }
        return tenantTasks;
    }

    @Override
    public String addUserInputTask(UserInputTask userInputTask) {
        FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
        String notificationCode = fixedStringSet.getRandomElement().getValue();
        userInputTask.setNotificationCode(notificationCode);
        this.tasks.put(notificationCode, userInputTask);
        return notificationCode;
    }

    @Override
    public void addRunActionTask(RunActionTask runActionTask) {
        FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
        String notificationCode = fixedStringSet.getRandomElement().getValue();
        runActionTask.setNotificationCode(notificationCode);
        this.tasks.put(notificationCode, runActionTask);
    }

    @Override
    public void userInputReceived(String notificationCode, UserInput userInput) {
        if (this.tasks.containsKey(notificationCode)) {
            this.tasks.remove(notificationCode);
            this.actionManager.onUserInputNotification(notificationCode, userInput);
        }
    }

    @Override
    public void runAction(String notificationCode) {
        if (this.tasks.containsKey(notificationCode)) {
            Task task = this.tasks.remove(notificationCode);
            if (task instanceof RunActionTask) {
                RunActionTask rATask = (RunActionTask) task;
                try {
                    this.actionManager.runAction(rATask.getActionName(), rATask.getTenant(), rATask.getSection());
                } catch (UnivoteException ex) {
                    this.informationService.informTenant(rATask.getActionName(), rATask.getTenant(),
                            rATask.getSection(),
                            ex.getMessage());
                    logger.log(Level.INFO, ex.getMessage());
                }
            }
        }
    }

    @Override
    public void runAction(String actionName, String tenant, String section) {
        try {
            this.actionManager.runAction(actionName, tenant, section);
        } catch (UnivoteException ex) {
            this.informationService.informTenant(actionName, tenant, section, ex.getMessage());
            logger.log(Level.INFO, ex.getMessage());
        }
    }

}
