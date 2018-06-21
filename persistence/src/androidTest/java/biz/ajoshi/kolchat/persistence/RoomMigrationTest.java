package biz.ajoshi.kolchat.persistence;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import biz.ajoshi.kolchat.persistence.chat.ChannelDao;

import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static biz.ajoshi.kolchat.persistence.KolDB.MIGRATION_4_5;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class RoomMigrationTest {

    @Rule
    public MigrationTestHelper testHelper =
            new MigrationTestHelper(
                    InstrumentationRegistry.getInstrumentation(),
                    KolDB.class.getCanonicalName(),
                    new FrameworkSQLiteOpenHelperFactory());

    @Test
    public void testUpgradeFrom4To5() throws IOException {

        testHelper.createDatabase(ChannelDao.CHANNEL_DB_NAME, 4);
        testHelper.runMigrationsAndValidate(ChannelDao.CHANNEL_DB_NAME, 5, true, MIGRATION_4_5);
    }

//    private KolDB getMigratedRoomDatabase() {
//        KolDB database = Room.databaseBuilder(InstrumentationRegistry.getTargetContext(),
//                                              KolDB.class, ChannelDao.CHANNEL_DB_NAME)
//                             .addMigrations(MIGRATION_4_5)
//                             .build();
//        // close the database and release any stream resources when the test finishes
//        testHelper.closeWhenFinished(database);
//        return database;
//    }
}
