package mikastamm.com.soundmixer;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.Server;
import mikastamm.com.soundmixer.Networking.MessageHandlerFactory;
import mikastamm.com.soundmixer.Networking.MessageHandlers.ReceivedMessageHandler;

/**
 * Created by Mika on 02.04.2018.
 */

public class MessageHandlerTests {
    ClientAudioSessions audioSessions;
    MockServerConnection connection;
    Server server;

    private void setup()
    {
        server = AndroidMockDataFactory.getServer(1).get(0);
        ServerList.getInstance().addServer(server);
        audioSessions = ClientAudioSessionsManager.registerClientAudioSession(server.id);
        connection = new MockServerConnection();
    }

    private static void AssertAudioSessionListsEqual(List<AudioSession> listA, List<AudioSession> listB)
    {
        Assert.assertTrue(listA.size() == listB.size());
        for (int i = 0; i < listA.size(); i++) {
            AudioSession a = listA.get(i);
            AudioSession b = listB.get(i);

            Assert.assertTrue(a.id.equals(b.id));
            Assert.assertTrue(a.title.equals(b.title));
            Assert.assertTrue(a.volume == b.volume);
            Assert.assertTrue(a.mute == b.mute);

            //Compare Encoded Strings of Icon A and Icon B
            ImageEncoding enc = ImageEncodingFactory.getStandardEncoding();
            Assert.assertTrue(enc.encode(a.icon).equals(enc.encode(b.icon)));
        }
    }

    @Test
    public void test_Rep(){
        setup();

        connection.addNextMessageType(MockServerConnection.ReadLineMessageType.REPOPULATE);

        try {
            String msg;
            while ((msg = connection.readLine()) != null) {
                ReceivedMessageHandler handler = MessageHandlerFactory.getHandler(msg, server.id);
                handler.handleMessage();
            }

            List<AudioSession> clientSide = audioSessions.getAudioSessionListCopy();
            List<AudioSession> serverSide = connection.sessions;

            AssertAudioSessionListsEqual(clientSide, serverSide);
            Assert.assertTrue( clientSide.size() == MockServerConnection.repopulateApplicationCount);

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test_RepAndRemove(){
        setup();

        connection.addNextMessageType(MockServerConnection.ReadLineMessageType.REPOPULATE);
        for (int i = 0; i < MockServerConnection.repopulateApplicationCount; i++) {
            connection.addNextMessageType(MockServerConnection.ReadLineMessageType.REMOVE);
        }

        try {
            String msg;
            while ((msg = connection.readLine()) != null) {
                ReceivedMessageHandler handler = MessageHandlerFactory.getHandler(msg, server.id);
                handler.handleMessage();
            }
            
            Assert.assertTrue(audioSessions.getAudioSessionListCopy().size() == connection.sessions.size()
                    && audioSessions.getAudioSessionListCopy().size() == 0);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
