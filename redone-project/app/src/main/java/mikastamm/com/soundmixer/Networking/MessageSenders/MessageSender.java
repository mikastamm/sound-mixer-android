package mikastamm.com.soundmixer.Networking.MessageSenders;

import java.util.LinkedList;
import java.util.Queue;

import mikastamm.com.soundmixer.Networking.Connection;

/**
 * Created by Mika on 03.04.2018.
 */

public abstract class MessageSender {
    abstract void send();

    private Queue<MessageSender> nextSenders = new LinkedList<>();

    protected void startNextMessageSender()
    {
        MessageSender sender = nextSenders.poll();
        if(sender != null)
        {
            sender.addSenders(nextSenders);
            sender.send();
        }
    }

    public void addSender(MessageSender sender){
        nextSenders.add(sender);
    }

    public void addSenders(Queue<MessageSender> collection)
    {
        nextSenders.addAll(collection);
    }
}
