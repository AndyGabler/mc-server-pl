package com.gabler.huntersmc.scheduled;

import com.gabler.huntersmc.context.glory.GloryData;
import com.gabler.huntersmc.context.territory.TerritoryData;
import com.gabler.huntersmc.context.territory.model.Territory;
import com.gabler.huntersmc.util.TransactionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

public class TerritoryHoldingRewardRunnable implements Runnable {

    private static final String DATE_FORMAT = "yyyy-MM-dd HHmmss";
    private static final String LAST_RUN_FILE_NAME = "plugins/HuntersMC/lastTerritoryHoldingRunDate.txt";

    private final JavaPlugin plugin;
    private final TerritoryData territoryData;
    private final GloryData gloryData;

    public TerritoryHoldingRewardRunnable(JavaPlugin aPlugin, TerritoryData aTerritoryData, GloryData aGloryData) {
        plugin = aPlugin;
        territoryData = aTerritoryData;
        gloryData = aGloryData;
    }

    @Override
    public void run() {
        final String logHeader = TransactionUtil.rollupTransactionIdLogHeader();
        plugin.getLogger().info(logHeader + "Running territory holdings reward runnable. Checking timestamp.");
        Date lastRunDate;
        try {
            lastRunDate = getLastRunDateFromFile();
        } catch (Exception exception) {
            plugin.getLogger().severe(logHeader + "Failed to read last run file from \"" + LAST_RUN_FILE_NAME + "\".");
            exception.printStackTrace();
            return;
        }

        Date timeStampOfThisRun = new Date();

        if (lastRunDate != null) {
            final Calendar calendar = new GregorianCalendar();
            calendar.setTime(lastRunDate);
            calendar.add(Calendar.HOUR, 24);
            timeStampOfThisRun = calendar.getTime();

            // If time stamp of this run, is after the current date, stop
            if (timeStampOfThisRun.after(new Date())) {
                plugin.getLogger().info(logHeader + "Determined it has not yet been 24 hours since last run at " + lastRunDate + ".");
                return;
            }
        }

        plugin.getLogger().info(logHeader + "It has been over 24 hours since last run at " + lastRunDate + ".");
        try {
            doRun(logHeader);
        } catch (Exception exception) {
            return;
        }

        plugin.getLogger().info(logHeader + "Run complete. Marking last run date as " + timeStampOfThisRun + ".");
        try {
            final FileWriter writer = new FileWriter(LAST_RUN_FILE_NAME);
            writer.write(new SimpleDateFormat(DATE_FORMAT).format(timeStampOfThisRun));
            writer.flush();
            writer.close();
        } catch (IOException exception) {
            plugin.getLogger().severe(logHeader + "Failed to save last run time stamp to \"" + LAST_RUN_FILE_NAME + "\".");
            exception.printStackTrace();
        }
    }

    private Date getLastRunDateFromFile() throws FileNotFoundException, ParseException {
        final File file = new File(LAST_RUN_FILE_NAME);
        if (file.exists()) {
            return new SimpleDateFormat(DATE_FORMAT).parse(new Scanner(file).nextLine());
        }

        return null;
    }

    private void doRun(String logHeader) throws IOException {
        Bukkit.broadcastMessage(ChatColor.COLOR_CHAR + "b[System] Rewarding players for their held territories.");

        final List<Territory> territories = territoryData.allTerritories();
        territories.forEach(territory -> {
            final int territoriesOwned = territory.getClaims().size();
            plugin.getLogger().info(logHeader + "Applying reward to player with UUID " + territory.getOwnerUuid() + " for owning " + territoriesOwned + " territories.");
            gloryData.applyGloryEvent(
                territory.getOwnerUuid(), "Territory Holding Daily Reward", "glory-config.events.territories-holding", territoriesOwned
            );
        });

        try {
            gloryData.save();
            Bukkit.broadcastMessage(ChatColor.COLOR_CHAR + "b[System] Players have received daily reward for territories held!");
        } catch (Exception exception) {
            Bukkit.broadcastMessage(ChatColor.COLOR_CHAR + "4[System] Error saving Glory Data. Report time stamp to admin: " + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()));
            plugin.getLogger().severe("Failed to save glory data after applying glory for territories held.");
            exception.printStackTrace();
            throw new RuntimeException(exception);
        }
    }
}
