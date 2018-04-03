package mikastamm.com.soundmixer.Networking.MessageSenders;

/**
 * Created by Mika on 03.04.2018.
 */

public class CompositeMessageSender implements MessageSender {
    private MessageSender[] messageSenders;

    public CompositeMessageSender(MessageSender... messageSenders)
    {
        this.messageSenders = messageSenders;
    }

    @Override
    public void send() {
        for(MessageSender s : messageSenders)
        {
            s.send();
        }
    }
}
