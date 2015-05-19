/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.bfh.univote2.component.core.manager;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.UserInput;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Timer;

@Singleton
@LocalBean
public class ActionManagerMock implements ActionManager {

    private UserInput lastUserInput;
    private String lastNotificationCode;
    private String actionName;
    private String tenant;
    private String section;

    @Override
    public void onBoardNotification(String notificationCode, PostDTO post) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onTimerNotification(Timer timer) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onUserInputNotification(String notificationCode, UserInput userInput) {
        this.lastUserInput = userInput;
        this.lastNotificationCode = notificationCode;
    }

    public UserInput getLastUserInput() {
        return lastUserInput;
    }

    public String getLastNotificationCode() {
        return lastNotificationCode;
    }

    @Override
    public void runAction(String actionName, String tenant, String section) throws UnivoteException {
        this.actionName = actionName;
        this.tenant = tenant;
        this.section = section;
    }

    public String getActionName() {
        return actionName;
    }

    public String getTenant() {
        return tenant;
    }

    public String getSection() {
        return section;
    }

    @Override
    public void runFinished(ActionContext actionContext, ResultStatus resultStatus) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
