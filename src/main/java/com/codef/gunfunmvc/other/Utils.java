package com.codef.gunfunmvc.other;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.web.multipart.MultipartFile;

import com.codef.gunfunmvc.AppConfiguration;

public class Utils {

	public static java.sql.Date parseDate(String date) {
		try {
			return new Date(AppConfiguration.DATE_FORMAT.parse(date).getTime());
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static java.sql.Timestamp parseTimestamp(String timestamp) {
		try {
			return new Timestamp(AppConfiguration.DATE_TIME_FORMAT.parse(timestamp).getTime());
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static void saveFile(String uploadDir, String fileName, MultipartFile multipartFile) throws IOException {
		Path uploadPath = Paths.get(uploadDir);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		try (InputStream inputStream = multipartFile.getInputStream()) {
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ioe) {
			throw new IOException("Could not save image file: " + fileName, ioe);
		}
	}

	public static void copyFile(String sourceFile, String targetFile) throws IOException {
		Files.copy(new File(sourceFile).toPath(), new File(targetFile).toPath());
	}

	public static void deleteFile(String pathToFile) throws IOException {
		Path filePath = Paths.get(pathToFile);
		Files.delete(filePath);
	}

	public static void deleteFolder(String pathToFolder) {
		File file = new File(pathToFolder);
		deleteFolder(file);
	}

	private static void deleteFolder(File file) {
		for (File subFile : file.listFiles()) {
			if (subFile.isDirectory()) {
				deleteFolder(subFile);
			} else {
				subFile.delete();
			}
		}
		file.delete();
	}

	public static Connection getAccessConnection(String accessDbPath) throws ClassNotFoundException, SQLException {
		String dbURL = "jdbc:ucanaccess://" + accessDbPath;
		Connection o_connection_access = DriverManager.getConnection(dbURL);
		return o_connection_access;
	}

	public static void executeSQL(Connection conn, String sql) throws SQLException {
//		System.out.println("execute: " + sql);
		conn.createStatement().execute(sql);
	}

	public static ResultSet querySQL(Connection conn, String sql) throws SQLException {
//		System.out.println("  query: " + sql);
		return conn.createStatement().executeQuery(sql);
	}

	public static StringBuffer exportSQLAsTabDelimitedDataFile(Connection conn, String sql, String exportFilePath,
			boolean writeHeader) throws SQLException, IOException {

		StringBuffer oExportFileStringbuffer = new StringBuffer();

		ResultSet resultset = conn.createStatement().executeQuery(sql);
		ResultSetMetaData oRsmd = resultset.getMetaData();

		if (writeHeader) {
			for (int i = 0; i < oRsmd.getColumnCount(); i++) {
				oExportFileStringbuffer.append(oRsmd.getColumnName(i + 1) + "\t");
			}
			oExportFileStringbuffer = new StringBuffer(
					oExportFileStringbuffer.substring(0, oExportFileStringbuffer.length() - 1) + "\n");
		}

		while (resultset.next()) {
			StringBuffer oRowStringbuffer = new StringBuffer();
			for (int i = 0; i < oRsmd.getColumnCount(); i++) {
				String sValue = resultset.getString(i + 1);
				if (sValue == null || sValue.equals("null")) {
					sValue = "";
				}
				oRowStringbuffer.append(sValue + "\t");
			}
			oRowStringbuffer = new StringBuffer(oRowStringbuffer.substring(0, oRowStringbuffer.length() - 1) + "\n");
			oExportFileStringbuffer.append(oRowStringbuffer);

		}

		oExportFileStringbuffer = new StringBuffer(oExportFileStringbuffer.toString().trim());

		if (exportFilePath != null) {
			oExportFileStringbuffer = new StringBuffer(oExportFileStringbuffer.toString().replaceAll("`", "'"));
			writeStringToFile(oExportFileStringbuffer.toString(), exportFilePath);
			return null;
		} else {
			return oExportFileStringbuffer;
		}

	}

	public static String getDayFromDate(java.sql.Date dateCarried) {

		java.util.GregorianCalendar cal = new java.util.GregorianCalendar();
		cal.setTime(dateCarried);
		int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);

		switch (dayOfWeek) {
		case 1:
			return "Sunday";
		case 2:
			return "Monday";
		case 3:
			return "Tuesday";
		case 4:
			return "Wednesday";
		case 5:
			return "Thursday";
		case 6:
			return "Friday";
		case 7:
			return "Saturday";
		}
		return "";
	}

	public static TreeSet<String> makeFirstSQLColumnTreeSet(Connection conn, String sql)
			throws SQLException, IOException {
		TreeSet<String> returnSet = new TreeSet<String>();
		ResultSet resultset = conn.createStatement().executeQuery(sql);

		while (resultset.next()) {
			returnSet.add(resultset.getString("DISPLAY_VALUE"));
		}
		return returnSet;
	}

	public static ArrayList<HashMap<String, String>> makeSQLAsArrayListHashMap(Connection conn, String sql,
			String dateColumnForBlankRow, String dayColumnForBlankRow, String dayToTriggerBlank,
			String roundColumnForTotal) throws SQLException, IOException {

		boolean firstTimeLoop = true;
		String lastDateValue = "";
		int lastRoundValue = 0;

		ArrayList<String> columnList = new ArrayList<String>();
		ArrayList<HashMap<String, String>> returnList = new ArrayList<HashMap<String, String>>();

		ResultSet resultset = conn.createStatement().executeQuery(sql);
		ResultSetMetaData oRsmd = resultset.getMetaData();

		for (int i = 0; i < oRsmd.getColumnCount(); i++) {
			columnList.add(oRsmd.getColumnName(i + 1));
		}

		while (resultset.next()) {

			HashMap<String, String> rowRecord = new HashMap<String, String>();

			if (!firstTimeLoop && dateColumnForBlankRow != null) {

				String compareDateValue = resultset.getString(dateColumnForBlankRow);

				if (dayColumnForBlankRow != null) {

					String compareDayValue = resultset.getString(dayColumnForBlankRow);
					if (!compareDateValue.equals(lastDateValue) && compareDayValue.equals(dayToTriggerBlank)) {
						for (int i = 0; i < oRsmd.getColumnCount(); i++) {
							rowRecord.put(columnList.get(i), "");
						}
						returnList.add(rowRecord);
						rowRecord = new HashMap<String, String>();
					}

				} else {

					if (!compareDateValue.equals(lastDateValue)) {

						if (lastRoundValue == 0) {
							for (int i = 0; i < oRsmd.getColumnCount(); i++) {
								rowRecord.put(columnList.get(i), "");
							}
							rowRecord.put("SHOW_TOTAL", "YES");
							returnList.add(rowRecord);
						}

						rowRecord = new HashMap<String, String>();
						for (int i = 0; i < oRsmd.getColumnCount(); i++) {
							rowRecord.put(columnList.get(i), "");
						}
						rowRecord.put("SHOW_TOTAL", "NO");
						returnList.add(rowRecord);

						rowRecord = new HashMap<String, String>();
					}

				}

			}

			for (int i = 0; i < oRsmd.getColumnCount(); i++) {
				String sValue = resultset.getString(i + 1);
				if (sValue == null || sValue.equals("null")) {
					sValue = "";
				}

				if (dateColumnForBlankRow != null && columnList.get(i).equals(dateColumnForBlankRow)) {
					lastDateValue = sValue;
				}

				rowRecord.put(columnList.get(i).toUpperCase(), sValue);
			}

			returnList.add(rowRecord);

			firstTimeLoop = false;
			if (roundColumnForTotal != null) {
				lastRoundValue = resultset.getInt(roundColumnForTotal);
			}

		}

		return returnList;

	}

	public static StringBuffer makeSQLAsTabTable(Connection conn, String sql, boolean writeHeader)
			throws SQLException, IOException {

		StringBuffer oExportFileStringbuffer = new StringBuffer();

		oExportFileStringbuffer.append("<table>");

		ResultSet resultset = conn.createStatement().executeQuery(sql);
		ResultSetMetaData oRsmd = resultset.getMetaData();

		oExportFileStringbuffer.append("<tr>");
		if (writeHeader) {
			for (int i = 0; i < oRsmd.getColumnCount(); i++) {
				oExportFileStringbuffer.append("<td>" + oRsmd.getColumnName(i + 1) + "</td>");
			}
		}
		oExportFileStringbuffer.append("</tr>");

		while (resultset.next()) {

			oExportFileStringbuffer.append("<tr>");
			for (int i = 0; i < oRsmd.getColumnCount(); i++) {
				String sValue = resultset.getString(i + 1);
				if (sValue == null || sValue.equals("null")) {
					sValue = "";
				}
				oExportFileStringbuffer.append("<td>" + sValue + "</td>");
			}
			oExportFileStringbuffer.append("</tr>");
		}

		oExportFileStringbuffer.append("</table>");
		return oExportFileStringbuffer;

	}

	public static String getStringValueFromTable(Connection conn, String sql, String keyName) throws SQLException {
		ResultSet resultset = conn.createStatement().executeQuery(sql);
		if (resultset.next()) {
			return resultset.getString(keyName);
		} else {
			return "";
		}
	}

	public static synchronized void writeStringToFile(String stringToWrite, String filePath) throws IOException {
		Files.write(Paths.get(filePath), stringToWrite.getBytes());
	}

	public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight)
			throws IOException {
		BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = resizedImage.createGraphics();
		graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
		graphics2D.dispose();
		return resizedImage;
	}

	public static String readFile(String pathToFile) throws IOException {
		return new String(Files.readAllBytes(new File(pathToFile).toPath()));
	}

	public static void zipDirectory(String sourceDirectoryPath, String zipPath) throws IOException {
		Path zipFilePath = Files.createFile(Paths.get(zipPath));
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
			Path sourceDirPath = Paths.get(sourceDirectoryPath);
			Files.walk(sourceDirPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
				ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
				try {
					zipOutputStream.putNextEntry(zipEntry);
					zipOutputStream.write(Files.readAllBytes(path));
					zipOutputStream.closeEntry();
				} catch (Exception e) {
					System.err.println(e);
				}
			});
		}
	}

}