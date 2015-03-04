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
package ch.bfh.univote2.component.core.manager;

import ch.bfh.univote2.component.core.helper.InitialisationHelper;
import ch.bfh.uniboard.data.PostDTO;
import static ch.bfh.unicrypt.helper.Alphabet.UPPER_CASE;
import ch.bfh.unicrypt.math.algebra.general.classes.FixedStringSet;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.data.ActionContext;
import ch.bfh.univote2.component.core.data.BoardNotificationData;
import ch.bfh.univote2.component.core.data.NotificationData;
import ch.bfh.univote2.component.core.data.NotificationDataAccessor;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.TimerPreconditionQuery;
import ch.bfh.univote2.component.core.data.TimerNotificationData;
import ch.bfh.univote2.component.core.data.UserInput;
import ch.bfh.univote2.component.core.data.UserInputPreconditionQuery;
import ch.bfh.univote2.component.core.helper.RegistrationHelper;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@Startup
public class NotificationManagerImpl implements NotificationManager {

    static final Logger logger = Logger.getLogger(NotificationManagerImpl.class.getName());
    private static final String CONFIGURATION_NAME = "action-list";
    private final NotificationDataAccessor notificationDataAccessor;

    /**
     * Session context. Used to locate the notifiable actions over the JNDI.
     */
    @Resource(name = "sessionContext")
    private SessionContext sctx;

    /**
     * RegistrationHelper. Implements the ws-client for the registration on uniboard
     */
    @EJB
    RegistrationHelper registrationHelper;

    /**
     * ConfigurationHelper. Gives access to configurations stored in the JNDI.
     */
    @EJB
    ConfigurationManager configurationManager;

    /**
     * TenantManager. Manages all available tenants on this component.
     */
    @EJB
    TenantManager tenantManager;
    /**
     * UserInputManager. Responsible to manage GUI parts of this component.
     */
    @EJB
    TaskManager userTaskManager;
    /**
     * InitialisationHelper. Provides the component specific initial action and query for new sections.
     */
    @EJB
    InitialisationHelper initialisationHelper;

    /**
     * TimerService used to create java-ee timers
     */
    @Resource
    private TimerService timerService;

    public NotificationManagerImpl() {
        this.notificationDataAccessor = new NotificationDataAccessor();
    }

    @PostConstruct
    public void init() {
        //Load action graph from the configuration
        //TODO
        //Get list of tennant
        for (String tenant : this.tenantManager.getAllTentants()) {
            for (String section : this.initialisationHelper.getSections(tenant)) {
                //TODO
            }
            //Register this tenant for new sections
            //TODO

        }
    }

    @PreDestroy
    public void cleanUp() {
        for (String notificationCode : this.notificationDataAccessor.getAllNotificationCodes()) {
            try {
                //TODO
                this.registrationHelper.unregister("", notificationCode);
            } catch (UnivoteException ex) {
                this.log(ex);
            }
        }
    }

    @Override
    public void onBoardNotification(String notificationCode, PostDTO post) {

        if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
            logger.log(Level.INFO, "Received unknown notification code for board notification. {0}", notificationCode);
            this.registrationHelper.unregisterUnknownNotification(notificationCode);
            return;
        }
        NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
        ActionContext actionContext = nData.getActionContext();
        NotifiableAction action;
        try {
            action = this.getAction(actionContext.getAction());
        } catch (UnivoteException ex) {
            this.log(ex);
            return;
        }
        action.notifyAction(actionContext, post);
    }

    @Override
    public void onUserInputNotification(String notificationCode, UserInput userInput) {
        if (!this.notificationDataAccessor.containsNotificationCode(notificationCode)) {
            logger.log(Level.INFO, "Received unknown notification code for user input. {0}", notificationCode);
            return;
        }
        NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
        ActionContext actionContext = nData.getActionContext();
        NotifiableAction action;
        try {
            action = this.getAction(actionContext.getAction());
        } catch (UnivoteException ex) {
            this.log(ex);
            return;
        }
        action.notifyAction(actionContext, userInput);
    }

    @Timeout
    @Override
    public void onTimerNotification(Timer timer) {
        String notificationCode = (String) timer.getInfo();
        NotificationData nData = this.notificationDataAccessor.findByNotificationCode(notificationCode);
        ActionContext actionContext = nData.getActionContext();
        NotifiableAction action;
        try {
            action = this.getAction(actionContext.getAction());
        } catch (UnivoteException ex) {
            this.log(ex);
            return;
        }
        action.notifyAction(actionContext, timer);
    }

    //TODO asynchron
    @Override
    public void runFinished(ActionContext actionContext) {
        //Check if its the initialisation action
        if (actionContext.getAction().equals(this.initialisationHelper.getInitialistionAction())) {
            this.runFinishedInitialisation(actionContext);
            return;
        }

        this.unregisterAction(actionContext);

        //check if there is a next process
        //TODO
    }

    protected void runFinishedInitialisation(ActionContext actionContext) {
        //TODO
    }

    //TODO asynchron
    @Override
    public void runFailed(ActionContext actionContext) {
        //TODO What to do?
        //Register for rerun
    }

    //TODO asynchron
    @Override
    public void runPartlyFinished(ActionContext actionContext) {
        //TODO What to do?
    }

    protected NotifiableAction getAction(String actionName) throws UnivoteException {
        try {
            return (NotifiableAction) sctx.lookup("java:app" + actionName
                    + "!ch.bfh.univote.component.core.action.NotifiableAction");
        } catch (Exception ex) {
            throw new UnivoteException("Could not find action with name " + actionName, ex);
        }
    }

    protected void runAction(ActionContext actionContext) throws UnivoteException {
        NotifiableAction action = this.getAction(actionContext.getAction());
        action.run(actionContext);
    }

    protected void registerAction(ActionContext actionContext) throws UnivoteException {

        List<PreconditionQuery> conditions = actionContext.getPreconditionQueries();
        for (PreconditionQuery cond : conditions) {
            if (cond instanceof BoardPreconditionQuery) {
                BoardPreconditionQuery qNC = (BoardPreconditionQuery) cond;
                String newNotificationCode = this.registrationHelper.register(qNC.getBoard(), qNC.getQuery());
                this.notificationDataAccessor.add(new BoardNotificationData(qNC.getBoard(),
                        newNotificationCode, actionContext));
            } else if (cond instanceof UserInputPreconditionQuery) {
                UserInputPreconditionQuery uiNC = (UserInputPreconditionQuery) cond;
                String newNotificationCode = this.userTaskManager.addTask(uiNC.getUserInputRequest());
                if (newNotificationCode != null) {
                    this.notificationDataAccessor.add(
                            new NotificationData(newNotificationCode, actionContext));
                }
            } else if (cond instanceof TimerPreconditionQuery) {
                TimerPreconditionQuery tNC = (TimerPreconditionQuery) cond;
                //Get a notificationCode for a timer
                String newNotificationCode = this.createTimer(tNC);
                this.notificationDataAccessor.add(new TimerNotificationData(
                        newNotificationCode, actionContext));
            } else {
                throw new UnivoteException("Unsupported notification condition " + cond.getClass());
            }
        }
    }

    protected void unregisterAction(ActionContext actionContext) {

        //unregister current process
        for (NotificationData notificationData : this.notificationDataAccessor.findByActionContext(actionContext)) {
            //Remove from notificationDataAccessor
            this.notificationDataAccessor.removeByNotificationCode(notificationData.getNotifictionCode());
            try {
                //Has to act based on the type of the notification
                //BoardCondition: unregister on the corresponding uniboard
                if (notificationData instanceof BoardNotificationData) {
                    BoardNotificationData bnd = (BoardNotificationData) notificationData;
                    this.registrationHelper.unregister(bnd.getBoard(), bnd.getNotifictionCode());
                }
                //No action required for UserInputCondition
                //TimerCondition
                if (notificationData instanceof TimerNotificationData) {
                    this.cancelTimer(notificationData.getNotifictionCode());
                }
            } catch (UnivoteException ex) {
                this.log(ex);
            }
        }
    }

    protected String createTimer(TimerPreconditionQuery timerNotificationCondition) {
        FixedStringSet fixedStringSet = FixedStringSet.getInstance(UPPER_CASE, 20);
        String notificationCode = fixedStringSet.getRandomElement().getValue();
        this.timerService.createTimer(timerNotificationCondition.getDate(), notificationCode);
        logger.log(Level.FINE, "Created new timer notificationCode = {0} and Date {1}",
                new Object[]{notificationCode, timerNotificationCondition.getDate()});
        return notificationCode;
    }

    protected void cancelTimer(String notificationCode) {
        Collection<Timer> timers = timerService.getTimers();
        for (Timer t : timers) {
            String timerNC = (String) t.getInfo();
            if (notificationCode.equals(timerNC)) {
                t.cancel();
            }
        }
    }

    protected void log(Exception ex) {
        logger.log(Level.WARNING, ex.getMessage());
        if (ex.getCause() != null) {
            logger.log(Level.WARNING, ex.getCause().getMessage());
        }
    }

}
