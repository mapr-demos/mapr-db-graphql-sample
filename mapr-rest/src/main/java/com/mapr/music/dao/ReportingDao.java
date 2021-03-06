package com.mapr.music.dao;

import com.google.common.base.Stopwatch;
import com.mapr.music.model.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.mapr.music.util.MaprProperties.DRILL_DATA_SOURCE;

@Named("reportingDao")
public class ReportingDao {

    private static final Logger log = LoggerFactory.getLogger(ReportingDao.class);

    @Resource(lookup = DRILL_DATA_SOURCE)
    private DataSource ds;

    private Connection connection;

    /**
     * Return the most common area with artists.
     *
     * @param numberOfRows specifies number of 'area for artists' rows, which will be returned.
     * @return top area for artists.
     */
    public List<Pair> getTopAreaForArtists(int numberOfRows) {

        String sql = "SELECT `area` AS `area`, COUNT(1) AS `count` " +
                " FROM dfs.`/apps/artists`" +
                " GROUP BY `area` ORDER BY 2 DESC LIMIT " + numberOfRows;

        List<Pair> pairs = populatePaiFromSQL(sql);
        return pairs;
    }

    /**
     * Returns top languages for album.
     *
     * @param numberOfRows specifies number of 'languages for album' rows, which will be returned.
     * @return languages for album.
     */
    public List<Pair> getTopLanguagesForAlbum(int numberOfRows) {
        String sql = "SELECT l.`name` as `language`, COUNT(1) as `count` " +
                " FROM dfs.`/apps/albums` AS a " +
                " LEFT JOIN dfs.`/apps/languages` AS l ON l.`_id` = a.`language` " +
                " GROUP BY l.`name` ORDER BY 2 DESC LIMIT " + numberOfRows;
        List<Pair> pairs = populatePaiFromSQL(sql);
        return pairs;
    }

    /**
     * Returns number of albums per year.
     *
     * @param numberOfRows specifies number of 'number of albums per year' rows, which will be returned.
     * @return number of albums per year.
     */
    public List<Pair> getNumberOfAlbumsPerYear(int numberOfRows) {
        String sql = "SELECT EXTRACT(YEAR FROM released_date) AS `year`, COUNT(1) AS `count` " +
                " FROM (SELECT TO_DATE(released_date) AS `released_date`, `name`, `_id` FROM dfs.`/apps/albums` " +
                " WHERE released_date IS NOT NULL ORDER BY released_date DESC) " +
                " GROUP BY EXTRACT(YEAR from released_date) LIMIT " + numberOfRows;
        List<Pair> pairs = populatePaiFromSQL(sql);
        return pairs;
    }


    /**
     * Get the connection from the datasouce.
     *
     * @return the JDBC Connection
     * @throws SQLException
     */
    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = ds.getConnection();
        }
        return connection;
    }


    /**
     * Execute the SQL statement and return a list of K/V
     *
     * @param sql query.
     * @return
     */
    private List<Pair> populatePaiFromSQL(String sql) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Pair> pairs = new ArrayList<>();
        try {

            log.debug("Executing SQL :\n\t" + sql);

            Statement st = getConnection().createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String label = rs.getString(1);
                if (label == null || label.trim().isEmpty()) {
                    label = "Unknown";
                }
                pairs.add(new Pair(label, rs.getString(2)));
            }
            rs.close();
            st.close();
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: Manage exception
        }

        log.debug("Performing query: '{}' took: {}", sql, stopwatch);
        return pairs;
    }

}
