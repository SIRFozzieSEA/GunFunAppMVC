package com.codef.gunfunmvc.other;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

import com.codef.gunfunmvc.models.entities.CarrySession;
import com.codef.gunfunmvc.models.entities.CleaningSession;
import com.codef.gunfunmvc.models.entities.Registry;
import com.codef.gunfunmvc.models.entities.ShootingSession;
import com.codef.gunfunmvc.repos.CarrySessionRepo;
import com.codef.gunfunmvc.repos.CleaningSessionRepo;
import com.codef.gunfunmvc.repos.RegistryRepo;
import com.codef.gunfunmvc.repos.ShootingSessionRepo;

public class UtilsBuildConvert {

	/*
	 * 
	 * 
	 * // This is really only used to converting over data from old schema
	 * UtilsBuildConvert.checkDataFoldersBuilt(getGunFunAppLocation());
	 * UtilsBuildConvert.convertOldData(conn, connSlave, gunRegistryRepo,
	 * gunCarrySessionsRepo, gunCleaningSessionsRepo, gunShootingSessionsRepo);
	 * 
	 * 
	 */

	public static void addTriDates(Connection connMaster) throws SQLException {

		String getLastSql = "SELECT NICKNAME, MAX(DATE_CARRIED) AS MAX_VALUE FROM CARRY_SESSIONS WHERE NICKNAME != '' GROUP BY NICKNAME";
		Statement statement = connMaster.createStatement();
		ResultSet resultset = statement.executeQuery(getLastSql);
		while (resultset.next()) {
			connMaster.createStatement().execute("UPDATE REGISTRY SET last_carried_date = '" + resultset.getDate("MAX_VALUE")
					+ "' WHERE NICKNAME = '" + resultset.getString("NICKNAME") + "'");
		}

		getLastSql = "SELECT NICKNAME, MAX(DATE_CLEANED) AS MAX_VALUE FROM CLEANING_SESSIONS WHERE NICKNAME != '' GROUP BY NICKNAME";
		statement = connMaster.createStatement();
		resultset = statement.executeQuery(getLastSql);
		while (resultset.next()) {
			connMaster.createStatement().execute("UPDATE REGISTRY SET last_cleaned_date = '" + resultset.getDate("MAX_VALUE")
					+ "' WHERE NICKNAME = '" + resultset.getString("NICKNAME") + "'");
		}

		getLastSql = "SELECT NICKNAME, MAX(DATE_FIRED) AS MAX_VALUE FROM SHOOTING_SESSIONS WHERE NICKNAME != '' GROUP BY NICKNAME";
		statement = connMaster.createStatement();
		resultset = statement.executeQuery(getLastSql);
		while (resultset.next()) {
			connMaster.createStatement().execute("UPDATE REGISTRY SET last_fired_date = '" + resultset.getDate("MAX_VALUE")
					+ "' WHERE NICKNAME = '" + resultset.getString("NICKNAME") + "'");
		}

	}

	public static void convertOldData(Connection connMaster, Connection connSlave, RegistryRepo gunRegistryRepo,
			CarrySessionRepo gunCarrySessionsRepo, CleaningSessionRepo gunCleaningSessionsRepo,
			ShootingSessionRepo gunShootingSessionsRepo) throws SQLException {

		// GUN_REGISTRY
		connMaster.createStatement().execute("TRUNCATE TABLE REGISTRY ");
		Statement statement = connSlave.createStatement();
		ResultSet resultset = statement.executeQuery("SELECT * FROM GUN_REGISTRY ORDER BY PURCHASE_DATE");
		while (resultset.next()) {
			Registry newObj = new Registry();
			newObj.setSerial(resultset.getString("SERIAL"));
			newObj.setNickname(resultset.getString("GUN_NICKNAME"));
			newObj.setMake(resultset.getString("MAKE"));
			newObj.setModel(resultset.getString("MODEL"));
			newObj.setCaliber(resultset.getString("CALIBER"));
			newObj.setBarrelLength(resultset.getBigDecimal("BARREL_LENGTH"));
			newObj.setFrameMaterial(resultset.getString("FRAME_MATERIAL"));
			newObj.setSightedDate(resultset.getString("SIGHTED_DATE"));
			newObj.setPurchaseDate(resultset.getDate("PURCHASE_DATE"));
			newObj.setPurchaseCost(resultset.getBigDecimal("PURCHASE_COST"));
			newObj.setMarketCostDate(resultset.getDate("MARKET_COST_DATE"));
			newObj.setMarketCost(resultset.getBigDecimal("MARKET_COST"));
			newObj.setMarketUrl(resultset.getString("MARKET_URL"));
			newObj.setGunIsDirty(resultset.getBoolean("GUN_IS_DIRTY"));
			gunRegistryRepo.save(newObj);
		}

		// CARRY_SESSIONS
		connMaster.createStatement().execute("TRUNCATE TABLE CARRY_SESSIONS  ");
		statement = connSlave.createStatement();
		resultset = statement.executeQuery("SELECT * FROM GUN_SESSIONS_CARRY ORDER BY DATE_CARRIED, CARRY_PK");
		while (resultset.next()) {
			CarrySession newObj = new CarrySession();
			newObj.setNickname(resultset.getString("GUN_NICKNAME"));
			newObj.setDateCarried(resultset.getDate("DATE_CARRIED"));
			newObj.setDayOfWeek(resultset.getString("DAY_OF_WEEK"));
			gunCarrySessionsRepo.save(newObj);
		}

		// CLEANING_SESSIONS
		connMaster.createStatement().execute("TRUNCATE TABLE CLEANING_SESSIONS ");
		statement = connSlave.createStatement();
		resultset = statement.executeQuery("SELECT * FROM GUN_SESSIONS_CLEANING ORDER BY DATE_CLEANED");
		while (resultset.next()) {
			CleaningSession newObj = new CleaningSession();
			newObj.setNickname(resultset.getString("GUN_NICKNAME"));
			newObj.setDateCleaned(resultset.getDate("DATE_CLEANED"));
			gunCleaningSessionsRepo.save(newObj);
		}

		// SHOOTING_SESSIONS
		connMaster.createStatement().execute("TRUNCATE TABLE SHOOTING_SESSIONS ");
		statement = connSlave.createStatement();
		resultset = statement.executeQuery("SELECT * FROM GUN_SESSIONS_SHOOTING");
		while (resultset.next()) {
			ShootingSession newObj = new ShootingSession();
			newObj.setNickname(resultset.getString("GUN_NICKNAME"));
			newObj.setCaliber(resultset.getString("CALIBER"));
			newObj.setNoOfRounds(resultset.getInt("NO_OF_ROUNDS"));
			newObj.setDateFired(resultset.getDate("DATE_FIRED"));
			gunShootingSessionsRepo.save(newObj);
		}

		// CLEANING_REPORTS
		connMaster.createStatement().execute("TRUNCATE TABLE CLEANING_REPORTS ");

	}

	public static void checkDataFoldersBuilt(String pathToAppFolder) throws IOException {

		Path sampleAssetsPath = Paths.get("src", "main", "resources", "sample_app_assets");
		String pathToSampleResources = sampleAssetsPath.toFile().getAbsolutePath();

		checkBackupScriptBuilt(pathToSampleResources, pathToAppFolder);
		makeFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_images\\");
		makeFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_images\\large\\");
		makeFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_images\\medium\\");
		makeFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_images\\small\\");
		makeFolderWithAssets(pathToSampleResources, pathToAppFolder, "\\_manuals\\");

	}

	public static void checkBackupScriptBuilt(String pathToSampleResources, String pathToAppFolder) throws IOException {

		String backupScript = "\\_data\\BackupH2Data.bat";
		String sourceScriptPath = pathToSampleResources + backupScript;
		String targetScriptPath = pathToAppFolder + backupScript;
		File oDirectory = new File(targetScriptPath);
		if (!oDirectory.exists()) {
			Utils.copyFile(sourceScriptPath, targetScriptPath);
		}

	}

	public static void makeFolderWithAssets(String pathToSampleResources, String pathToAppFolder, String resourceFolder)
			throws IOException {

		ArrayList<String> standards = new ArrayList<String>(Arrays.asList("_NEW", "_NOT_FOUND"));
		ArrayList<String> nickNames = new ArrayList<String>(Arrays.asList("Alex", "Teresa", "Valerie", "Harriet"));
		String extension = resourceFolder.contains("manual") ? ".pdf" : ".jpg";

		boolean directoryMade = false;
		String appFolder = pathToAppFolder + resourceFolder;
		File oDirectory = new File(appFolder);
		if (!oDirectory.exists()) {
			oDirectory.mkdirs();
			directoryMade = true;
		}

		if (directoryMade) {
			if (resourceFolder.equals("\\_images\\")) {
				for (String singleStandards : standards) {
					Utils.copyFile(pathToSampleResources + resourceFolder + singleStandards + extension,
							appFolder + "\\" + singleStandards + extension);
				}
			} else {
				for (String singleNickname : nickNames) {
					Utils.copyFile(pathToSampleResources + resourceFolder + singleNickname + extension,
							appFolder + "\\" + singleNickname + extension);
				}
				if (extension.equals(".pdf")) {
					Utils.copyFile(pathToSampleResources + resourceFolder + "_NOT_FOUND" + extension,
							appFolder + "\\" + "_NOT_FOUND" + extension);
				}
			}

		}

	}

}
