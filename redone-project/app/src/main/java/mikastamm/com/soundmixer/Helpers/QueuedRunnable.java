package mikastamm.com.soundmixer.Helpers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Mika on 24.04.2018.
 */

public class QueuedRunnable implements Runnable{
    private Queue<QueuedRunnable> nextRunnables = new LinkedList<>();
    private Runnable innerRunnable;

    public QueuedRunnable(Runnable runnable)
    {
        innerRunnable = runnable;
    }

    @Override
    public void run() {
        innerRunnable.run();
        startNext();
    }

    public void runInNewThread(){
        new Thread(this).start();
    }

    private void startNext()
    {
        QueuedRunnable queuedRunnable = nextRunnables.poll();

        if(queuedRunnable != null)
        {
            queuedRunnable.addRunnables(nextRunnables);
            queuedRunnable.run();
        }
    }

    public void addRunnable(Runnable runnable){
        nextRunnables.add(new QueuedRunnable(runnable));
    }

    public void addRunnables(Collection<QueuedRunnable> collection)
    {
        nextRunnables.addAll(collection);
    }

}
