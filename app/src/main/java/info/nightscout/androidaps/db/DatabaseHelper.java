package info.nightscout.androidaps.db;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.nightscout.androidaps.Constants;
import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.data.GlucoseStatus;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static Logger log = LoggerFactory.getLogger(DatabaseHelper.class);

    public static final String DATABASE_NAME = "AndroidAPSDb";
    public static final String DATABASE_BGREADINGS = "BgReadings";
    public static final String DATABASE_TEMPBASALS = "TempBasals";
    public static final String DATABASE_TEMPTARGETS = "TempTargets";
    public static final String DATABASE_TREATMENTS = "Treatments";
    public static final String DATABASE_DANARHISTORY = "DanaRHistory";

    private static final int DATABASE_VERSION = 5;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        onCreate(getWritableDatabase(), getConnectionSource());
    }


    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            log.info("onCreate");
            TableUtils.createTableIfNotExists(connectionSource, TempBasal.class);
            TableUtils.createTableIfNotExists(connectionSource, TempTarget.class);
            TableUtils.createTableIfNotExists(connectionSource, Treatment.class);
            TableUtils.createTableIfNotExists(connectionSource, BgReading.class);
            TableUtils.createTableIfNotExists(connectionSource, DanaRHistoryRecord.class);
        } catch (SQLException e) {
            log.error("Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            log.info(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, TempBasal.class, true);
            TableUtils.dropTable(connectionSource, TempTarget.class, true);
            TableUtils.dropTable(connectionSource, Treatment.class, true);
            TableUtils.dropTable(connectionSource, BgReading.class, true);
            TableUtils.dropTable(connectionSource, DanaRHistoryRecord.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            log.error("Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
    }

    public void cleanUpDatabases() {
        // TODO: call it somewhere
        log.debug("Before BgReadings size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_BGREADINGS));
        getWritableDatabase().delete(DATABASE_BGREADINGS, "timeIndex" + " < '" + (new Date().getTime() - Constants.hoursToKeepInDatabase * 60 * 60 * 1000L) + "'", null);
        log.debug("After BgReadings size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_BGREADINGS));

        log.debug("Before TempBasals size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_TEMPBASALS));
        getWritableDatabase().delete(DATABASE_TEMPBASALS, "timeIndex" + " < '" + (new Date().getTime() - Constants.hoursToKeepInDatabase * 60 * 60 * 1000L) + "'", null);
        log.debug("After TempBasals size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_TEMPBASALS));

        log.debug("Before TempTargets size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_TEMPTARGETS));
        getWritableDatabase().delete(DATABASE_TEMPTARGETS, "timeIndex" + " < '" + (new Date().getTime() - Constants.hoursToKeepInDatabase * 60 * 60 * 1000L) + "'", null);
        log.debug("After TempTargets size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_TEMPTARGETS));

        log.debug("Before Treatments size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_TREATMENTS));
        getWritableDatabase().delete(DATABASE_TREATMENTS, "timeIndex" + " < '" + (new Date().getTime() - Constants.hoursToKeepInDatabase * 60 * 60 * 1000L) + "'", null);
        log.debug("After Treatments size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_TREATMENTS));

        log.debug("Before History size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_DANARHISTORY));
        getWritableDatabase().delete(DATABASE_DANARHISTORY, "recordDate" + " < '" + (new Date().getTime() - Constants.daysToKeepHistoryInDatabase * 24 * 60 * 60 * 1000L) + "'", null);
        log.debug("After History size: " + DatabaseUtils.queryNumEntries(getReadableDatabase(), DATABASE_DANARHISTORY));
    }

    public void resetDatabases() {
        try {
            TableUtils.dropTable(connectionSource, TempBasal.class, true);
            TableUtils.dropTable(connectionSource, TempTarget.class, true);
            TableUtils.dropTable(connectionSource, Treatment.class, true);
            TableUtils.dropTable(connectionSource, BgReading.class, true);
            TableUtils.dropTable(connectionSource, DanaRHistoryRecord.class, true);
            TableUtils.createTableIfNotExists(connectionSource, TempBasal.class);
            TableUtils.createTableIfNotExists(connectionSource, TempTarget.class);
            TableUtils.createTableIfNotExists(connectionSource, Treatment.class);
            TableUtils.createTableIfNotExists(connectionSource, BgReading.class);
            TableUtils.createTableIfNotExists(connectionSource, DanaRHistoryRecord.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetTreatments() {
        try {
            TableUtils.dropTable(connectionSource, Treatment.class, true);
            TableUtils.createTableIfNotExists(connectionSource, Treatment.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void resetTempTargets() {
        try {
            TableUtils.dropTable(connectionSource, TempTarget.class, true);
            TableUtils.createTableIfNotExists(connectionSource, TempTarget.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Dao<TempBasal, Long> getDaoTempBasals() throws SQLException {
        return getDao(TempBasal.class);
    }

    public Dao<TempTarget, Long> getDaoTempTargets() throws SQLException {
        return getDao(TempTarget.class);
    }

    public Dao<Treatment, Long> getDaoTreatments() throws SQLException {
        return getDao(Treatment.class);
    }

    public Dao<BgReading, Long> getDaoBgReadings() throws SQLException {
        return getDao(BgReading.class);
    }

    public Dao<DanaRHistoryRecord, String> getDaoDanaRHistory() throws SQLException {
        return getDao(DanaRHistoryRecord.class);
    }

    /*
     * Return last BgReading from database or null if db is empty
     */
    @Nullable
    public BgReading lastBg() {
        List<BgReading> bgList = null;

        try {
            Dao<BgReading, Long> daoBgReadings = MainApp.getDbHelper().getDaoBgReadings();
            QueryBuilder<BgReading, Long> queryBuilder = daoBgReadings.queryBuilder();
            queryBuilder.orderBy("timeIndex", false);
            queryBuilder.limit(1L);
            queryBuilder.where().gt("value", 38);
            PreparedQuery<BgReading> preparedQuery = queryBuilder.prepare();
            bgList = daoBgReadings.query(preparedQuery);

        } catch (SQLException e) {
            log.debug(e.getMessage(), e);
        }
        if (bgList != null && bgList.size() > 0)
            return bgList.get(0);
        else
            return null;
    }

    /*
     * Return bg reading if not old ( <9 min )
     * or null if older
     */
    @Nullable
    public BgReading actualBg() {
        BgReading lastBg = lastBg();

        if (lastBg == null)
            return null;

        if (lastBg.timeIndex > new Date().getTime() - 9 * 60 * 1000)
            return lastBg;

        return null;
    }

    public List<BgReading> getBgreadingsDataFromTime(long mills, boolean ascending) {
        try {
            Dao<BgReading, Long> daoBgreadings = getDaoBgReadings();
            List<BgReading> bgReadings;
            QueryBuilder<BgReading, Long> queryBuilder = daoBgreadings.queryBuilder();
            queryBuilder.orderBy("timeIndex", ascending);
            Where where = queryBuilder.where();
            where.ge("timeIndex", mills).and().gt("value", 38);
            PreparedQuery<BgReading> preparedQuery = queryBuilder.prepare();
            bgReadings = daoBgreadings.query(preparedQuery);
            return bgReadings;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<BgReading>();
    }

    public List<Treatment> getTreatmentDataFromTime(long mills, boolean ascending) {
        try {
            Dao<Treatment, Long> daoTreatments = getDaoTreatments();
            List<Treatment> treatments;
            QueryBuilder<Treatment, Long> queryBuilder = daoTreatments.queryBuilder();
            queryBuilder.orderBy("timeIndex", ascending);
            Where where = queryBuilder.where();
            where.ge("timeIndex", mills);
            PreparedQuery<Treatment> preparedQuery = queryBuilder.prepare();
            treatments = daoTreatments.query(preparedQuery);
            return treatments;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<Treatment>();
    }

    public List<TempTarget> getTemptargetsDataFromTime(long mills, boolean ascending) {
        try {
            Dao<TempTarget, Long> daoTempTargets = getDaoTempTargets();
            List<TempTarget> tempTargets;
            QueryBuilder<TempTarget, Long> queryBuilder = daoTempTargets.queryBuilder();
            queryBuilder.orderBy("timeIndex", ascending);
            Where where = queryBuilder.where();
            where.ge("timeIndex", mills);
            PreparedQuery<TempTarget> preparedQuery = queryBuilder.prepare();
            tempTargets = daoTempTargets.query(preparedQuery);
            return tempTargets;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<TempTarget>();
    }


    public List<TempBasal> getTempbasalsDataFromTime(long mills, boolean ascending, boolean isExtended) {
        try {
            Dao<TempBasal, Long> daoTempbasals = getDaoTempBasals();
            List<TempBasal> tempbasals;
            QueryBuilder<TempBasal, Long> queryBuilder = daoTempbasals.queryBuilder();
            queryBuilder.orderBy("timeIndex", ascending);
            Where where = queryBuilder.where();
            where.ge("timeIndex", mills).and().eq("isExtended", isExtended);
            PreparedQuery<TempBasal> preparedQuery = queryBuilder.prepare();
            tempbasals = daoTempbasals.query(preparedQuery);
            return tempbasals;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<TempBasal>();
    }

}
