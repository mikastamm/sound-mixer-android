package mikastamm.com.soundmixer.Datamodel;

import mikastamm.com.soundmixer.ServerState;

/**
 * Created by Mika on 27.03.2018.
 */

public class Server {
    public String name;
    public boolean hasPassword;
    public String IPAddress;
    public String savedPassword;
    public String id;
    public ServerState state = ServerState.available;

    public Server(boolean hasPassword, String name, String IPAddress, String savedPassword, String id)
    {
        this.name = name;
        this.IPAddress = IPAddress;
        this.savedPassword = savedPassword;
        this.hasPassword = hasPassword;
        this.id = id;
    }

    public Server(boolean hasPassword, String name, String IPAddress, String id)
    {
        this.name = name;
        this.IPAddress = IPAddress;
        this.savedPassword = "";
        this.hasPassword = hasPassword;
        this.id = id;
    }

    public Server() {

    }

    @Override
    public String toString(){
        return "\nName="+name+"\nhasPassword="+hasPassword+"\nIPAddress="+IPAddress+"\nsavedPassword="+ savedPassword +"\nid="+ id;
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
}
