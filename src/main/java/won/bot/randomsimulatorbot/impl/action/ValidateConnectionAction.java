package won.bot.randomsimulatorbot.impl.action;

import java.time.Duration;

import org.springframework.util.StopWatch;

import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.behaviour.CrawlConnectionDataBehaviour;
import won.bot.framework.eventbot.event.BaseAtomAndConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.crawlconnection.CrawlConnectionCommandEvent;
import won.bot.framework.eventbot.event.impl.crawlconnection.CrawlConnectionCommandFailureEvent;
import won.bot.framework.eventbot.event.impl.crawlconnection.CrawlConnectionCommandSuccessEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.WonMessageReceivedOnConnectionEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.WonMessageSentOnConnectionEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.agreement.AgreementProtocolState;
import won.protocol.model.Connection;
import won.protocol.util.WonConversationUtils;
import won.protocol.validation.WonConnectionValidator;

public class ValidateConnectionAction extends BaseEventBotAction {
    public ValidateConnectionAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        Connection con = null;
        if (event instanceof WonMessageReceivedOnConnectionEvent) {
            con = ((WonMessageReceivedOnConnectionEvent) event).getCon();
        } else if (event instanceof WonMessageSentOnConnectionEvent) {
            con = ((WonMessageSentOnConnectionEvent) event).getCon();
        } else if (event instanceof BaseAtomAndConnectionSpecificEvent) {
            con = ((BaseAtomAndConnectionSpecificEvent) event).getCon();
        }
        if (con == null) {
            return;
        }
        // initiate crawl behaviour
        CrawlConnectionCommandEvent command = new CrawlConnectionCommandEvent(con.getAtomURI(), con.getConnectionURI());
        CrawlConnectionDataBehaviour crawlConnectionDataBehaviour = new CrawlConnectionDataBehaviour(
                        getEventListenerContext(), command,
                        Duration.ofSeconds(60));
        final StopWatch crawlStopWatch = new StopWatch();
        crawlStopWatch.start("crawl");
        crawlConnectionDataBehaviour.onResult(new BaseEventBotAction(getEventListenerContext()) {
            @Override
            protected void doRun(Event event, EventListener executingListener) throws Exception {
                StringBuilder out = new StringBuilder();
                if (event instanceof CrawlConnectionCommandSuccessEvent) {
                    CrawlConnectionCommandSuccessEvent successEvent = (CrawlConnectionCommandSuccessEvent) event;
                    try {
                        crawlStopWatch.stop();
                        out.append("+---- Validation Result ----" + "\n");
                        out.append("| Crawling took " + crawlStopWatch.getTotalTimeSeconds() + " seconds" + "\n");
                        out.append("| Validating data of connection " + command.getConnectionURI() + "\n");
                        // TODO: use one validator for all invocations
                        WonConnectionValidator validator = new WonConnectionValidator();
                        StringBuilder message = new StringBuilder();
                        boolean valid = validator.validate(successEvent.getCrawledData(), message);
                        String successMessage = "Connection " + command.getConnectionURI() + " is valid: " + valid + " "
                                        + message.toString();
                        out.append("| " + successMessage + "\n");
                        // now validate again, but with the won-conversation tools, which are more
                        // thorough
                        out.append("| Checking with WonConversationUtils.getAgreementProtocolState() ..." + "\n");
                        AgreementProtocolState aps = WonConversationUtils.getAgreementProtocolState(
                                        command.getConnectionURI(), getEventListenerContext().getLinkedDataSource());
                        aps.getAgreements();
                        out.append(
                                        "| Checking with WonConversationUtils.getAgreementProtocolState() did not throw an Exception"
                                                        + "\n");
                        out.append("| done validating " + command.getConnectionURI() + "\n");
                        out.append("+---------------------------" + "\n");
                    } catch (Exception e) {
                        out.append("| Caught exception during validation: " + e + "\n");
                        out.append("+---------------------------" + "\n");
                    } finally {
                        System.out.println(out.toString());
                    }
                } else if (event instanceof CrawlConnectionCommandFailureEvent) {
                    CrawlConnectionCommandFailureEvent failureEvent = (CrawlConnectionCommandFailureEvent) event;
                    out.append("+---- Validation Result ----" + "\n");
                    out.append("| Cannot validate connection " + command.getConnectionURI()
                                    + " - error while crawling: " + failureEvent.getMessage() + "\n");
                    out.append("+---------------------------" + "\n");
                    System.out.println(out.toString());
                }
            }
        });
        crawlConnectionDataBehaviour.activate();
    }
}
