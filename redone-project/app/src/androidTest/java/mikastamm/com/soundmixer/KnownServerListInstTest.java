package mikastamm.com.soundmixer;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Mika on 27.03.2018.
 */
@RunWith(AndroidJUnit4.class)
public class KnownServerListInstTest {
    String[] rsaKeys = AndroidMockDataFactory.getRSAKeys(10);

    @Test
    public void test_addToKnown(){
        for(int i = 0; i < rsaKeys.length; i++)
        {
            MainActivity main = new MainActivity();
            KnownServerList.addToKnown(rsaKeys[i], main);
        }
    }
}
