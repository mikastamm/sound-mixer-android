package mikastamm.com.soundmixer.Helpers;

public class AutoResetEvent
{
    private final Object _monitor = new Object();
    private volatile boolean _isOpen = false;

    public AutoResetEvent(boolean open)
    {
        _isOpen = open;
    }

    public void waitOne()
    {
        synchronized (_monitor) {
            while (!_isOpen) {
                try {
                    _monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            _isOpen = false;
        }
    }

    public void waitOne(long timeout)
    {
        synchronized (_monitor) {
            long t = System.currentTimeMillis();
            while (!_isOpen) {
                try {
                    _monitor.wait(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Check for timeout
                if (System.currentTimeMillis() - t >= timeout)
                    break;
            }
            _isOpen = false;
        }
    }

    public void set()
    {
        synchronized (_monitor) {
            _isOpen = true;
            _monitor.notify();
        }
    }

    public void reset()
    {
        _isOpen = false;
    }
}