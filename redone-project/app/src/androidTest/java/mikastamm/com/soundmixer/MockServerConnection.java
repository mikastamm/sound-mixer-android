package mikastamm.com.soundmixer;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import mikastamm.com.soundmixer.Datamodel.AudioSession;
import mikastamm.com.soundmixer.Datamodel.AudioSessionIcon;
import mikastamm.com.soundmixer.Helpers.Json;
import mikastamm.com.soundmixer.Networking.ServerConnection;

/**
 * Created by Mika on 02.04.2018.
 */

public class MockServerConnection implements ServerConnection {
    boolean connected = false;
    public List<AudioSession> sessions = new ArrayList<>();
    private ArrayDeque<ReadLineMessageType> nextMessageTypes = new ArrayDeque<>();
    public static int repopulateApplicationCount = 6;

    @Override
    public void writeLine(String line) {

    }

    public void addNextMessageType(ReadLineMessageType type)
    {
        nextMessageTypes.add(type);
    }

    @Override
    public String readLine() throws IOException {
        ReadLineMessageType messageType = null;

        if(!nextMessageTypes.isEmpty())
        messageType = nextMessageTypes.pop();


        if(messageType == ReadLineMessageType.REPOPULATE)
        {
            List<AudioSession> newSessions = AndroidMockDataFactory.getAudioSessions(repopulateApplicationCount);
            sessions.clear();
            sessions.addAll(newSessions);
            nextMessageTypes.push(ReadLineMessageType.IMGS);
            return "REP" + Json.serialize(newSessions);
        }
        else if(messageType == ReadLineMessageType.ADD)
        {
            AudioSession s = AndroidMockDataFactory.getAudioSessions(1).get(0);
            sessions.add(s);
            nextMessageTypes.push(ReadLineMessageType.IMG);
            return "ADD" + Json.serialize(s);
        }
        else if(messageType == ReadLineMessageType.REMOVE)
        {
            String json = Json.serialize(sessions.get(0));
            sessions.remove(0);
            return "DEL" + json;
        }
        else if(messageType == ReadLineMessageType.EDIT)
        {
            AudioSession s = AndroidMockDataFactory.getAudioSessions(1).get(0);
            s.id = sessions.get(0).id;
            sessions.get(0).updateValues(s);
            return "EDIT" + Json.serialize(sessions.get(0));
        }
        else if(messageType == ReadLineMessageType.IMG)
        {
            ImageEncoding encoding = ImageEncodingFactory.getStandardEncoding();
            AudioSessionIcon icon = new AudioSessionIcon();
            Bitmap bmp = AndroidMockDataFactory.getSessionIcons(1).get(0);
            sessions.get(sessions.size() - 1).icon = bmp;
            icon.id = sessions.get(sessions.size() - 1).id;
            icon.icon = encoding.encode(bmp);
            return "IMG" + Json.serialize(icon);
        }
        else if(messageType == ReadLineMessageType.IMGS)
        {
            ImageEncoding encoding = ImageEncodingFactory.getStandardEncoding();
            List<AudioSessionIcon> icons = new ArrayList<>();
            List<Bitmap> bitmaps = AndroidMockDataFactory.getSessionIcons(repopulateApplicationCount);
            List<String> encodedBitmaps = encoding.encode(bitmaps);

            int i = 0;
            //Set Icons owner application id to the last $repopulateApplicationCount sessions
            for(int j = sessions.size() - repopulateApplicationCount; j < sessions.size(); j++)
            {
                AudioSessionIcon icon = new AudioSessionIcon();
                sessions.get(j).icon = bitmaps.get(i);
                icon.id = sessions.get(j).id;
                icon.icon = encodedBitmaps.get(i);
                icons.add(icon);
                i++;
            }

            return "IMGS" + Json.serialize(icons);
        }
        return null;
    }

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public void dispose() {
        connected = false;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    public enum ReadLineMessageType{
        ADD,
        REMOVE,
        EDIT,
        REPOPULATE,
        IMG,
        IMGS
    }
}
