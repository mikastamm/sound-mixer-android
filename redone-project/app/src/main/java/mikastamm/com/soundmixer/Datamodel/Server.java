package mikastamm.com.soundmixer.Datamodel;

import java.io.Serializable;

import mikastamm.com.soundmixer.ServerState;

/**
 * Created by Mika on 27.03.2018.
 */

public class Server implements Serializable{
    public String name;
    public boolean hasPassword;
    public String ipAddress;
    public String savedPassword;
    public String id;
    public ServerState state = ServerState.available;

    public Server(boolean hasPassword, String name, String ipAddress, String savedPassword, String id)
    {
        this.name = name;
        this.ipAddress = ipAddress;
        this.savedPassword = savedPassword;
        this.hasPassword = hasPassword;
        this.id = id;
    }

    public Server(boolean hasPassword, String name, String ipAddress, String id)
    {
        this.name = name;
        this.ipAddress = ipAddress;
        this.savedPassword = "";
        this.hasPassword = hasPassword;
        this.id = id;
    }

    public Server() {

    }

    @Override
    public String toString(){
        return "\nName="+name+"\nhasPassword="+hasPassword+"\nipAddress="+ ipAddress +"\nsavedPassword="+ savedPassword +"\nid="+ id;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o.getClass().equals(Server.class))
        {
            return ((Server)o).id.equals(id);
        }
        else {
            return super.equals(o);
        }
    }

    //Returns an integer representation of the server id
    public int getIntegerServerId()
    {
        int intId = 0;
        for(int i=0;i< id.length();i++)
        {
            char c=id.charAt(i);
            intId += Character.getNumericValue(c);
        }
        return intId;
    }
}
