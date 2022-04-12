package com.codef.gunfunmvc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.activation.FileTypeMap;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.codef.gunfunmvc.models.entities.CarrySession;
import com.codef.gunfunmvc.models.entities.CleaningSession;
import com.codef.gunfunmvc.models.entities.Registry;
import com.codef.gunfunmvc.models.entities.ShootingSession;
import com.codef.gunfunmvc.models.entities.TriviaQuestionTemplate;
import com.codef.gunfunmvc.models.entities.TriviaRound;
import com.codef.gunfunmvc.models.entities.TriviaRoundQuestion;
import com.codef.gunfunmvc.models.entities.ValidCaliber;
import com.codef.gunfunmvc.other.Utils;
import com.codef.gunfunmvc.other.UtilsBuildConvert;
import com.codef.gunfunmvc.repos.CarrySessionRepo;
import com.codef.gunfunmvc.repos.CleaningSessionRepo;
import com.codef.gunfunmvc.repos.RegistryRepo;
import com.codef.gunfunmvc.repos.ShootingSessionRepo;
import com.codef.gunfunmvc.repos.TriviaQuestionTemplateRepo;
import com.codef.gunfunmvc.repos.TriviaRoundQuestionRepo;
import com.codef.gunfunmvc.repos.TriviaRoundRepo;
import com.codef.gunfunmvc.repos.ValidCaliberRepo;

@Controller
public class AppController {

	@Autowired
	private RegistryRepo gunRegistryRepo;

	@Autowired
	private CleaningSessionRepo gunCleaningSessionsRepo;

	@Autowired
	private ShootingSessionRepo gunShootingSessionsRepo;

	@Autowired
	private CarrySessionRepo gunCarrySessionsRepo;

	@Autowired
	private TriviaRoundRepo gunTriviaRoundsRepo;

	@Autowired
	private TriviaRoundQuestionRepo gunTriviaGameRepo;

	@Autowired
	private TriviaQuestionTemplateRepo gunTriviaTemplateQuestionsRepo;

	@Autowired
	private ValidCaliberRepo validCaliberRepo;

	@Autowired
	@Qualifier("jdbcMaster")
	private JdbcTemplate jdbcTemplateOne;

	@Autowired
	private Environment env;

	/*
	 * Main App Windows
	 */

	@GetMapping("/")
	public String indexLaunch(Model model) throws SQLException, IOException {

		try {

			Connection conn = jdbcTemplateOne.getDataSource().getConnection();
			if (getBuildSampleAssets()) {
				UtilsBuildConvert.checkDataFoldersBuilt(getGunFunAppLocation());
			}
			processCleaningReport(conn);
			conn.close();

		} catch (SQLException e) {
			// Database cannot be connected to, most likely BitLocker
			e.printStackTrace();
			return "index_nc";
		}
		
		
		
		
		
		return "index";
	}

	@GetMapping("/frame_navigation")
	public String frameNavigation(Model model) {
		return "frame_navigation";
	}

	@GetMapping("/frame_main")
	public String frameMain(@RequestParam(name = "showStats", required = false) boolean showStats, Model model)
			throws SQLException, IOException {

		if (showStats) {

			Connection conn = jdbcTemplateOne.getDataSource().getConnection();

			String totalGuns = Utils.getStringValueFromTable(conn, "SELECT count(*) as TOTAL_COUNT FROM registry",
					"TOTAL_COUNT");

			if (Long.parseLong(totalGuns) > 0) {

				String maxCost = Utils.getStringValueFromTable(conn,
						"SELECT MAX(purchase_cost) as MAX_COST FROM registry", "MAX_COST");
				model.addAttribute("maxCost", String.format("%,.2f", Double.parseDouble(maxCost)));

				String totalCost = Utils.getStringValueFromTable(conn,
						"SELECT SUM(purchase_cost) as TOTAL_COST FROM registry", "TOTAL_COST");
				model.addAttribute("totalCost", String.format("%,.2f", Double.parseDouble(totalCost)));

				String totalMarketCost = Utils.getStringValueFromTable(conn,
						"SELECT SUM(market_cost) as MARKET_COST FROM registry", "MARKET_COST");
				model.addAttribute("totalMarketCost", String.format("%,.2f", Double.parseDouble(totalMarketCost)));

				model.addAttribute("totalDifferenceCost",
						String.format("%,.2f", Double.parseDouble(totalMarketCost) - Double.parseDouble(totalCost)));

				String avgCost = Utils.getStringValueFromTable(conn,
						"SELECT avg(purchase_cost) as AVG_COST FROM registry", "AVG_COST");
				model.addAttribute("avgCost", String.format("%,.2f", Double.parseDouble(avgCost)));

				String minCost = Utils.getStringValueFromTable(conn,
						"SELECT MIN(purchase_cost) as MIN_COST FROM registry", "MIN_COST");
				model.addAttribute("minCost", String.format("%,.2f", Double.parseDouble(minCost)));

				String maxBarrel = Utils.getStringValueFromTable(conn,
						"SELECT MAX(barrel_length) as MAX_BL FROM registry", "MAX_BL");
				model.addAttribute("maxBarrel", String.format("%,.2f", Double.parseDouble(maxBarrel)));

				String avgBarrel = Utils.getStringValueFromTable(conn,
						"SELECT avg(barrel_length) as AVG_BL FROM registry", "AVG_BL");
				model.addAttribute("avgBarrel", String.format("%,.2f", Double.parseDouble(avgBarrel)));

				String minBarrel = Utils.getStringValueFromTable(conn,
						"SELECT MIN(barrel_length) as MIN_BL FROM registry", "MIN_BL");
				model.addAttribute("minBarrel", String.format("%,.2f", Double.parseDouble(minBarrel)));

				String sql = "SELECT MAKE, count(*) as TOTAL_COUNT FROM registry GROUP BY MAKE ORDER BY MAKE";
				model.addAttribute("manufacturers", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));

				sql = "SELECT CALIBER, count(*) as TOTAL_COUNT FROM registry GROUP BY CALIBER ORDER BY CALIBER";
				model.addAttribute("calibers", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));

				sql = "SELECT FRAME_MATERIAL, count(*) as TOTAL_COUNT FROM registry GROUP BY FRAME_MATERIAL ORDER BY FRAME_MATERIAL";
				model.addAttribute("frameMaterials",
						Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));

			}

			model.addAttribute("totalGuns", Long.parseLong(totalGuns));

			conn.close();

		} else {
			model.addAttribute("totalGuns", Long.parseLong("0"));
		}

		return "frame_main";
	}

	@GetMapping("/getImage")
	public ResponseEntity<byte[]> getImage(@RequestParam(name = "imageName", required = true) String imageName)
			throws IOException {
		File img = new File(getGunFunAppPhotoLocation() + imageName);
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img)))
				.body(Files.readAllBytes(img.toPath()));
	}

	@GetMapping("/getPdf")
	public ResponseEntity<byte[]> getPdf(@RequestParam(name = "pdfName", required = true) String pdfName)
			throws IOException {

		File pdf = null;
		byte[] pdfFileBytes = null;

		try {
			pdf = new File(getGunFunAppManualLocation() + pdfName);
			pdfFileBytes = Files.readAllBytes(pdf.toPath());
		} catch (Exception e) {
			pdf = new File(getGunFunAppManualLocation() + "_NOT_FOUND.pdf");
			pdfFileBytes = Files.readAllBytes(pdf.toPath());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/pdf"));
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
		headers.add("content-disposition", "inline;filename=" + pdfName);

		return ResponseEntity.ok().headers(headers)
				.contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(pdf)))
				.body(pdfFileBytes);

	}

	@GetMapping("/getImageForQuestion")
	public ResponseEntity<byte[]> getImageForQuestion(
			@RequestParam(name = "questionPk", required = true) String questionPk,
			@RequestParam(name = "size", required = true) String size) throws IOException, SQLException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		String nickname = Utils.getStringValueFromTable(conn,
				"SELECT NICKNAME FROM TRIVIA_ROUND_QUESTIONS WHERE QUESTION_PK = " + questionPk, "NICKNAME");
		conn.close();

		File img = new File(getGunFunAppPhotoLocation() + "/" + size + "/" + nickname + ".jpg");
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf(FileTypeMap.getDefaultFileTypeMap().getContentType(img)))
				.body(Files.readAllBytes(img.toPath()));

	}

	/*
	 * Reports
	 */

	@GetMapping("/report/shot")
	public String reportShot(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Shot Report");
		String sql = "SELECT NICKNAME, CALIBER, MAX(DATE_FIRED) AS LAST_DATE_FIRED, sum(NO_OF_ROUNDS) AS TOTAL_ROUNDS_FIRED "
				+ "FROM shooting_sessions group by NICKNAME, CALIBER order by NICKNAME, CALIBER";
		model.addAttribute("report", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));
		sql = "SELECT SUM(NO_OF_ROUNDS) AS REPORT_TOTAL FROM shooting_sessions";
		model.addAttribute("reportTotal", Utils.getStringValueFromTable(conn, sql, "REPORT_TOTAL"));
		conn.close();
		return "report_shot";

	}

	@GetMapping("/report/shotbycaliber")
	public String reportShotByCaliber(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Shot Report by Caliber");
		String sql = "SELECT CALIBER, SUM(NO_OF_ROUNDS) AS TOTAL_ROUNDS_FIRED FROM shooting_sessions "
				+ "GROUP BY CALIBER ORDER BY CALIBER;";
		model.addAttribute("report", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));
		sql = "SELECT SUM(NO_OF_ROUNDS) AS REPORT_TOTAL FROM shooting_sessions";
		model.addAttribute("reportTotal", Utils.getStringValueFromTable(conn, sql, "REPORT_TOTAL"));

		conn.close();
		return "report_shot_by_caliber";

	}

	@GetMapping("/report/carry")
	public String reportCarry(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		model.addAttribute("reportTitle", "Carry Report");
		String sql = "SELECT carry_sessions.NICKNAME, CALIBER, COUNT(*) AS TOTAL_TIMES_CARRIED, MAX(DATE_CARRIED) "
				+ "AS LAST_DATE_CARRIED FROM carry_sessions INNER JOIN registry ON registry.NICKNAME = "
				+ "carry_sessions.NICKNAME GROUP BY  carry_sessions.NICKNAME, CALIBER ORDER BY "
				+ "TOTAL_TIMES_CARRIED DESC;";
		model.addAttribute("report", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));
		sql = "SELECT SUM(NO_OF_ROUNDS) AS REPORT_TOTAL FROM cleaning_reports";
		model.addAttribute("reportTotal", Utils.getStringValueFromTable(conn, sql, "REPORT_TOTAL"));

		conn.close();
		return "report_carry";

	}

	@GetMapping("/report/cleaning")
	public String reportCleaning(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		model.addAttribute("reportTitle", "Cleaning Report");
		String sql = "SELECT NICKNAME, CALIBER, SUM(NO_OF_ROUNDS) as TOTAL_ROUNDS_FIRED, MAX(DATE_FIRED) AS LAST_DATE_FIRED "
				+ "FROM cleaning_reports WHERE CALIBER != '' group by NICKNAME, CALIBER order by NICKNAME ;";
		model.addAttribute("report", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));

		conn.close();
		return "report_cleaning";

	}

	@GetMapping("/report/market_report")
	public String marketReport(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Market Value Report");
		String sql = "SELECT GUN_PK, NICKNAME, MAKE, MODEL, PURCHASE_COST, MARKET_COST, MARKET_COST_DATE, "
				+ "MARKET_URL, (MARKET_COST - PURCHASE_COST) AS CURRENT_VALUE_CHANGE FROM registry order by NICKNAME";
		model.addAttribute("report", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));

		String sumPC = Utils.getStringValueFromTable(conn, "SELECT SUM(purchase_cost) as SUM_PC FROM registry",
				"SUM_PC");
		model.addAttribute("SUM_PC", String.format("%,.0f", Double.parseDouble(sumPC)));

		String sumMK = Utils.getStringValueFromTable(conn, "SELECT SUM(market_cost) as SUM_MK FROM registry", "SUM_MK");
		model.addAttribute("SUM_MK", String.format("%,.0f", Double.parseDouble(sumMK)));

		String sumDiff = Utils.getStringValueFromTable(conn,
				"SELECT (SUM(market_cost) - SUM(purchase_cost)) as SUM_DIFF FROM registry", "SUM_DIFF");
		model.addAttribute("SUM_DIFF", String.format("%,.0f", Double.parseDouble(sumDiff)));

		conn.close();

		return "report_market";
	}

	/*
	 * Logs
	 */

	@GetMapping("/log/shot")
	public String logShot(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Shot Log");
		String sql = "SELECT SHOOT_PK, NICKNAME, CALIBER, NO_OF_ROUNDS, DATE_FIRED FROM shooting_sessions ORDER by DATE_FIRED DESC, NICKNAME";
		model.addAttribute("report",
				Utils.makeSQLAsArrayListHashMap(conn, sql, "DATE_FIRED", null, null, "NO_OF_ROUNDS"));
		model.addAttribute("allCaliberSet", getAllCaliberValues());
		conn.close();

		return "log_shot";
	}

	@PostMapping("/log/shot")
	public String logShotDelete(HttpServletRequest request, Model model) {

		boolean editPerformed = false;
		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			if (keyStr.startsWith("GUN_PK_ROUNDS_")) {
				String shootPkToEdit = keyStr.replaceAll("GUN_PK_ROUNDS_", "");

				if (Long.parseLong(request.getParameter("GUN_PK_ROUNDS_" + shootPkToEdit)) > 0) {
					ShootingSession gunShootingSession = gunShootingSessionsRepo.findById(Long.parseLong(shootPkToEdit))
							.get();
					gunShootingSession
							.setNoOfRounds(Long.parseLong(request.getParameter("GUN_PK_ROUNDS_" + shootPkToEdit)));
					gunShootingSession
							.setDateFired(Utils.parseDate(request.getParameter("GUN_PK_DATE_FIRED_" + shootPkToEdit)));
					if (request.getParameter("GUN_PK_CALIBER_" + shootPkToEdit).equals("")) {
						// look up default caliber by nickname
						Registry gunRegistryEntry = gunRegistryRepo.findByNickname(gunShootingSession.getNickname())
								.get();
						gunShootingSession.setCaliber(gunRegistryEntry.getCaliber());
					} else {
						gunShootingSession.setCaliber(request.getParameter("GUN_PK_CALIBER_" + shootPkToEdit));
					}
					gunShootingSessionsRepo.save(gunShootingSession);
					editPerformed = true;
				}

			}
		}

		boolean deletesPerformed = false;
		if (request.getParameter("password").equals(getDeleteMasterPassword())) {

			requestParameterMap = request.getParameterMap();
			for (Object key : requestParameterMap.keySet()) {
				String keyStr = (String) key;
				if (keyStr.startsWith("DELETE_SHOOT_PK_")) {
					String shootPkToDelete = keyStr.replaceAll("DELETE_SHOOT_PK_", "");
					gunShootingSessionsRepo.deleteById(Long.parseLong(shootPkToDelete));
					deletesPerformed = true;
				}
			}
		}

		if (editPerformed) {
			model.addAttribute("MESSAGE", "Edits to shot log have been made.");
		}

		if (deletesPerformed) {
			model.addAttribute("MESSAGE_TWO", "Entries removed from shot log.");
		}

		return "frame_main";
	}

	@GetMapping("/log/shot_add")
	public String logShotAdd(Model model) throws SQLException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Add Shot Entries");
		model.addAttribute("allGunNamesSet", getAllGunNicknameValues());
		model.addAttribute("allCaliberSet", getAllCaliberValues());
		model.addAttribute("initialBlankEntries", Long.valueOf(10));
		model.addAttribute("todaysDate", new Date(System.currentTimeMillis()));
		conn.close();

		return "log_shot_add";
	}

	@PostMapping("/log/shot_add")
	public String logShotAddFinal(HttpServletRequest request, Model model) {

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			String[] valueArray = (String[]) requestParameterMap.get(keyStr);
			String possibleNicknameValue = valueArray[0];

			if (keyStr.startsWith("NICKNAME_") && !possibleNicknameValue.equals("")) {

				String formFieldKey = keyStr.replaceAll("NICKNAME_", "");
				String dateParameterName = "DATE_SHOT_" + formFieldKey;
				String noOfRoundsParameterName = "NO_OF_ROUNDS_" + formFieldKey;
				String caliberParameterName = "CALIBER_" + formFieldKey;

				ShootingSession shootingSession = new ShootingSession();
				shootingSession.setNickname(possibleNicknameValue);
				shootingSession.setNoOfRounds(Long.parseLong(request.getParameter(noOfRoundsParameterName)));
				shootingSession.setDateFired(Utils.parseDate(request.getParameter(dateParameterName)));

				if (request.getParameter(caliberParameterName).equals("")) {
					// look up default caliber by nickname
					Registry gunRegistryEntry = gunRegistryRepo.findByNickname(possibleNicknameValue).get();
					shootingSession.setCaliber(gunRegistryEntry.getCaliber());
				} else {
					shootingSession.setCaliber(request.getParameter(caliberParameterName));
				}

				gunShootingSessionsRepo.save(shootingSession);
			}
		}

		model.addAttribute("MESSAGE", "Entries added to shot log.");

		return "frame_main";
	}

	@GetMapping("/log/carry")
	public String logCarry(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Carry Log");
		String sql = "SELECT CARRY_PK, NICKNAME, DATE_CARRIED, DAY_OF_WEEK FROM carry_sessions ORDER by DATE_CARRIED DESC, NICKNAME";
		model.addAttribute("report",
				Utils.makeSQLAsArrayListHashMap(conn, sql, "DATE_CARRIED", "DAY_OF_WEEK", "Sunday", null));
		conn.close();

		return "log_carry";
	}

	@PostMapping("/log/carry")
	public String logCarryDelete(HttpServletRequest request, Model model) throws SQLException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		if (request.getParameter("password").equals(getDeleteMasterPassword())) {

			Map<String, String[]> requestParameterMap = request.getParameterMap();
			for (Object key : requestParameterMap.keySet()) {
				String keyStr = (String) key;
				if (keyStr.startsWith("DELETE_CARRY_PK_")) {
					String carryPkToDelete = keyStr.replaceAll("DELETE_CARRY_PK_", "");
					gunCarrySessionsRepo.deleteById(Long.parseLong(carryPkToDelete));
					String dateParameterName = "DELETE_DATE_CARRIED_" + carryPkToDelete;
					java.sql.Date dateCarried = Utils.parseDate(request.getParameter(dateParameterName));
					boolean needBlanklines = needBlankLines(conn, "", dateCarried);
					if (needBlanklines) {
						gunCarrySessionsRepo.save(new CarrySession("", dateCarried, Utils.getDayFromDate(dateCarried)));
					}
				}
			}

			model.addAttribute("MESSAGE", "Entries removed from carry log.");

		}

		conn.close();
		return "frame_main";
	}

	@GetMapping("/log/carry_add")
	public String logCarryAdd(HttpServletRequest request, Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		model.addAttribute("reportTitle", "Add Carry Entries");
		model.addAttribute("allGunNamesSet", getAllGunNicknameValues());

		int daysToDisplay = 15;
		model.addAttribute("initialBlankEntries", Long.valueOf(daysToDisplay));

		long currentDateInMillis = System.currentTimeMillis();

		ArrayList<Date> weekDates = new ArrayList<Date>();
		ArrayList<String> dayDates = new ArrayList<String>();

		long sevenDaysAgoInMillis = currentDateInMillis - (86400000 * 7);
		weekDates.add(new Date(sevenDaysAgoInMillis));
		dayDates.add(Utils.getDayFromDate(new Date(sevenDaysAgoInMillis)));
		for (int i = 0; i < daysToDisplay; i++) {
			weekDates.add(new Date(sevenDaysAgoInMillis + (86400000 * i)));
			dayDates.add(Utils.getDayFromDate(new Date(sevenDaysAgoInMillis + (86400000 * i))));
		}

		model.addAttribute("weekDates", weekDates);
		model.addAttribute("dayDates", dayDates);
		model.addAttribute("shutDownAfter", Boolean.valueOf(request.getParameter("SHUTDOWN_AFTER")));
		conn.close();

		return "log_carry_add";
	}

	@PostMapping("/log/carry_add")
	public String logCarryAddFinal(HttpServletRequest request, Model model) throws SQLException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			String[] valueArray = (String[]) requestParameterMap.get(keyStr);
			String possibleNicknameValue = valueArray[0];

			if (keyStr.startsWith("NICKNAME_")) {
				String formFieldKey = keyStr.replaceAll("NICKNAME_", "");
				String dateParameterName = "DATE_CARRIED_" + formFieldKey;
				java.sql.Date dateCarried = Utils.parseDate(request.getParameter(dateParameterName));
				boolean needBlanklines = needBlankLines(conn, possibleNicknameValue, dateCarried);
				if (possibleNicknameValue.equals("")) {
					// empty entry
					if (needBlanklines) {
						gunCarrySessionsRepo.save(new CarrySession("", dateCarried, Utils.getDayFromDate(dateCarried)));
					}
				} else {
					if (needBlanklines) {
						// make sure there's no duplicate for that day
						gunCarrySessionsRepo.save(new CarrySession(possibleNicknameValue, dateCarried,
								Utils.getDayFromDate(dateCarried)));
					}
				}
			}
		}

		conn.close();

		model.addAttribute("MESSAGE", "Entries added to carry log.");

		boolean addAndShutDown = Boolean.valueOf(request.getParameter("SHUTDOWN_AFTER"));
		if (addAndShutDown) {
			try {
				System.out.println("Shutting down the PC after 5 seconds.");
				String commandArray[] = new String[] { "cmd.exe", "/c", "shutdown /s /t 5" };
				Runtime.getRuntime().exec(commandArray);
			} catch (IOException e) {
				System.out.println("Exception: " + e);
			}
		}

		return "frame_main";
	}

	@GetMapping("/log/cleaning")
	public String logCleaning(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Cleaning Log");
		String sql = "SELECT CLEAN_PK, NICKNAME, DATE_CLEANED FROM cleaning_sessions ORDER by DATE_CLEANED DESC, NICKNAME";
		model.addAttribute("report", Utils.makeSQLAsArrayListHashMap(conn, sql, "DATE_CLEANED", null, null, null));
		conn.close();

		return "log_cleaning";
	}

	@PostMapping("/log/cleaning")
	public String logCleaningDelete(HttpServletRequest request, Model model) {

		if (request.getParameter("password").equals(getDeleteMasterPassword())) {

			Map<String, String[]> requestParameterMap = request.getParameterMap();
			for (Object key : requestParameterMap.keySet()) {
				String keyStr = (String) key;
				if (keyStr.startsWith("DELETE_CLEAN_PK_")) {
					String cleanPkToDelete = keyStr.replaceAll("DELETE_CLEAN_PK_", "");
					gunCleaningSessionsRepo.deleteById(Long.parseLong(cleanPkToDelete));
				}
			}

			model.addAttribute("MESSAGE", "Entries removed from cleaning log.");

		}
		return "frame_main";
	}

	@GetMapping("/log/cleaning_add")
	public String logCleaningAdd(HttpServletRequest request, Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		TreeSet<String> gunsToCleanSet = new TreeSet<String>();

		String sql = "SELECT NICKNAME AS DISPLAY_VALUE FROM cleaning_reports order by DISPLAY_VALUE;";
		gunsToCleanSet = Utils.makeFirstSQLColumnTreeSet(conn, sql);
		model.addAttribute("initialBlankEntries", Long.valueOf(gunsToCleanSet.size()));
		model.addAttribute("reportTitle", "Add Cleaning Entries (Dirty)");

		if (request.getParameter("dirtyOnly").equals("false")) {
			sql = "SELECT DISTINCT NICKNAME AS DISPLAY_VALUE FROM registry order by DISPLAY_VALUE;";
			gunsToCleanSet = Utils.makeFirstSQLColumnTreeSet(conn, sql);
			model.addAttribute("reportTitle", "Add Cleaning Entries (All)");
		}

		model.addAttribute("allGunNamesSet", gunsToCleanSet);
		model.addAttribute("todaysDate", new Date(System.currentTimeMillis()));

		conn.close();

		return "log_cleaning_add";
	}

	@PostMapping("/log/cleaning_add")
	public String logCleaningAddFinal(HttpServletRequest request, Model model) throws SQLException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			String[] valueArray = (String[]) requestParameterMap.get(keyStr);
			String possibleNicknameValue = valueArray[0];

			if (keyStr.startsWith("NICKNAME_") && !possibleNicknameValue.equals("")) {
				String formFieldKey = keyStr.replaceAll("NICKNAME_", "");
				String dateParameterName = "DATE_CLEANED_" + formFieldKey;
				gunCleaningSessionsRepo.save(new CleaningSession(possibleNicknameValue,
						Utils.parseDate(request.getParameter(dateParameterName))));
			}
		}

		model.addAttribute("MESSAGE", "Entries added to cleaning log.");

		processCleaningReport(conn);
		conn.close();

		return "frame_main";
	}

	/*
	 * Gun Registry
	 */

	@GetMapping("/registry")
	public String registry(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		String sql = "SELECT GUN_PK, NICKNAME, MAKE, MODEL, CALIBER, GUN_IS_DIRTY FROM registry order by NICKNAME";

		ArrayList<HashMap<String, String>> gunSearch = Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null,
				null);
		ArrayList<ArrayList<HashMap<String, String>>> allGuns = getGunsInTwoColumn(gunSearch);

		String totalGuns = Utils.getStringValueFromTable(conn, "SELECT count(*) as TOTAL_COUNT FROM registry",
				"TOTAL_COUNT");

		model.addAttribute("gunsFound", Integer.valueOf(totalGuns));
		model.addAttribute("report", allGuns);
		model.addAttribute("reportTitle", "All Registry Entries");
		model.addAttribute("searchStats", totalGuns + " Entries Found");

		model.addAttribute("todaysDate", new Date(System.currentTimeMillis()));
		conn.close();

		return "registry_all";
	}

	@GetMapping("/registry_search")
	public String registrySearch(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Search Registry Entries");
		model.addAttribute("allMakesSet", getAllMakesValues());
		model.addAttribute("allCaliberSet", getAllCaliberValues());
		conn.close();

		return "registry_search";
	}

	@PostMapping("/registry_search")
	public String registrySearchFinal(HttpServletRequest request, Model model) throws SQLException, IOException {

		ArrayList<String> subQueries = new ArrayList<String>();
		TreeMap<Integer, String> orderBys = new TreeMap<Integer, String>();

		String gunNickNameValue = request.getParameter("Nickname");
		String gunNickNameOrAnd = request.getParameter("NicknameRadio");
		String gunNickNameOrder = request.getParameter("NicknameOrder");
		if (!gunNickNameValue.equals("")) {
			subQueries.add("(LOWER(NICKNAME) like '%" + gunNickNameValue.toLowerCase() + "%') " + gunNickNameOrAnd);
		}
		if (gunNickNameOrder != null && !gunNickNameOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunNickNameOrder), "NICKNAME");
		}

		String gunMakeValue = request.getParameter("MakeDrop");
		String gunMakeOrAnd = request.getParameter("MakeRadio");
		String gunMakeOrder = request.getParameter("MakeOrder");
		if (!gunMakeValue.equals("")) {
			subQueries.add("(MAKE = '" + gunMakeValue + "') " + gunMakeOrAnd);
		}
		if (gunMakeOrder != null && !gunMakeOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunMakeOrder), "MAKE");
		}

		String gunModelValue = request.getParameter("Model");
		String gunModelOrAnd = request.getParameter("ModelRadio");
		String gunModelOrder = request.getParameter("ModelOrder");
		if (!gunModelValue.equals("")) {
			subQueries.add("(LOWER(MODEL) like '%" + gunModelValue.toLowerCase() + "%') " + gunModelOrAnd);
		}
		if (gunModelOrder != null && !gunModelOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunModelOrder), "MODEL");
		}

		String gunCaliberValue = request.getParameter("CaliberDrop");
		String gunCaliberOrAnd = request.getParameter("CaliberRadio");
		String gunCaliberOrder = request.getParameter("CaliberOrder");
		if (!gunCaliberValue.equals("")) {
			subQueries.add("(CALIBER = '" + gunCaliberValue + "') " + gunCaliberOrAnd);
		}
		if (gunCaliberOrder != null && !gunCaliberOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunCaliberOrder), "CALIBER");
		}

		String gunSerialValue = request.getParameter("SerialNo");
		String gunSerialOrAnd = request.getParameter("SerialRadio");
		String gunSerialOrder = request.getParameter("SerialOrder");
		if (!gunSerialValue.equals("")) {
			subQueries.add("(LOWER(SERIAL) like '%" + gunSerialValue.toLowerCase() + "%') " + gunSerialOrAnd);
		}
		if (gunSerialOrder != null && !gunSerialOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunSerialOrder), "SERIAL");
		}

		String gunBarrelLengthMinValue = request.getParameter("BarrelLengthMin");
		String gunBarrelLengthMaxValue = request.getParameter("BarrelLengthMax");
		String gunBarrelLengthOrAnd = request.getParameter("BarrelLengthRadio");
		String gunBarrelLengthOrder = request.getParameter("BarrelLengthOrder");
		if ((gunBarrelLengthMinValue != null && gunBarrelLengthMaxValue != null)
				&& (!gunBarrelLengthMinValue.equals("") && !gunBarrelLengthMaxValue.equals(""))) {
			subQueries.add("(BARREL_LENGTH BETWEEN '" + gunBarrelLengthMinValue + "' AND '" + gunBarrelLengthMaxValue
					+ "') " + gunBarrelLengthOrAnd);
		}
		if (gunBarrelLengthOrder != null && !gunBarrelLengthOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunBarrelLengthOrder), "BARREL_LENGTH");
		}

		String gunPurchaseCostMinValue = request.getParameter("PurchaseCostMin");
		String gunPurchaseCostMaxValue = request.getParameter("PurchaseCostMax");
		String gunPurchaseCostOrAnd = request.getParameter("PurchaseCostRadio");
		String gunPurchaseCostOrder = request.getParameter("PurchaseCostOrder");
		if ((gunPurchaseCostMinValue != null && gunPurchaseCostMaxValue != null)
				&& (!gunPurchaseCostMinValue.equals("") && !gunPurchaseCostMaxValue.equals(""))) {
			subQueries.add("(PURCHASE_COST BETWEEN '" + gunPurchaseCostMinValue + "' AND '" + gunPurchaseCostMaxValue
					+ "') " + gunPurchaseCostOrAnd);
		}
		if (gunPurchaseCostOrder != null && !gunPurchaseCostOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunPurchaseCostOrder), "PURCHASE_COST");
		}

		String gunPurchaseDateMinValue = request.getParameter("PurchaseDateMin");
		String gunPurchaseDateMaxValue = request.getParameter("PurchaseDateMax");
		String gunPurchaseDateOrAnd = request.getParameter("PurchaseDateRadio");
		String gunPurchaseDateOrder = request.getParameter("PurchaseDateOrder");
		if ((gunPurchaseDateMinValue != null && gunPurchaseDateMaxValue != null)
				&& (!gunPurchaseDateMinValue.equals("") && !gunPurchaseDateMaxValue.equals(""))) {
			subQueries.add("(PURCHASE_DATE BETWEEN '" + gunPurchaseDateMinValue + "' AND '" + gunPurchaseDateMaxValue
					+ "') " + gunPurchaseDateOrAnd);
		}
		if (gunPurchaseDateOrder != null && !gunPurchaseDateOrder.equals("")) {
			orderBys.put(Integer.valueOf(gunPurchaseDateOrder), "PURCHASE_DATE");
		}

		String dirtyClause = "";
		if (request.getParameter("ShowDirtyOnly") != null) {
			dirtyClause = " AND GUN_IS_DIRTY = true";
		}

		String whereClause = " 1=1 ";
		if (subQueries.size() > 0) {
			whereClause = subQueries.toString().replaceAll(",", "").replaceAll("\\[", "").replaceAll("\\]", "");
			whereClause = whereClause.substring(0, whereClause.length() - 3);
		}

		String orderByClause = "ORDER BY NICKNAME";
		if (orderBys.size() > 0) {
			orderByClause = "ORDER BY " + orderBys.values().toString().replaceAll("\\[", "").replaceAll("\\]", "");
		}

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		String sql = "SELECT *, (MARKET_COST - PURCHASE_COST) AS CURRENT_VALUE_CHANGE FROM registry WHERE "
				+ whereClause + " " + dirtyClause + " " + orderByClause;
		ArrayList<HashMap<String, String>> gunSearch = Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null,
				null);

		String whereClauseDirtyClause = whereClause + " " + dirtyClause;
		whereClauseDirtyClause = whereClauseDirtyClause.replaceAll("\s+", "\s").trim().replaceAll("1=1 AND ", "");

		model.addAttribute("searchStats", gunSearch.size() + " Entries Found: " + whereClauseDirtyClause);
		model.addAttribute("gunsFound", Integer.valueOf(gunSearch.size()));

		ArrayList<ArrayList<HashMap<String, String>>> allGuns = getGunsInTwoColumn(gunSearch);
		model.addAttribute("report", allGuns);

		model.addAttribute("reportTitle", "Search Entries");
		model.addAttribute("todaysDate", new Date(System.currentTimeMillis()));
		conn.close();

		return "registry_all";
	}

	@GetMapping("/registry_edit")
	public String registryEdit(@RequestParam(name = "gun_pk", required = false) Long gunPk, Model model) {

		Registry registryEdit = gunRegistryRepo.findById(gunPk).get();
		model.addAttribute("gunRegistry", registryEdit);
		model.addAttribute("reportTitle", "Edit Entry");

		return "registry_edit";
	}

	@PostMapping("/registry_edit")
	public String registryEditFinal(HttpServletRequest request, @RequestParam("gunPhoto") MultipartFile multipartFile,
			@ModelAttribute Registry gunRegistry, Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		if (request.getParameter("password").equals(getDeleteMasterPassword())) {

			long gunPkToDelete = gunRegistry.getGunPk();
			String nickname = gunRegistry.getNickname();

			try {
				Utils.deleteFile(getGunFunAppPhotoLocation() + "large\\" + nickname + ".jpg");
			} catch (IOException e) {
			}

			try {
				Utils.deleteFile(getGunFunAppPhotoLocation() + "medium\\" + nickname + ".jpg");
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Utils.deleteFile(getGunFunAppPhotoLocation() + "small\\" + nickname + ".jpg");
			} catch (IOException e) {
			}

			Utils.executeSQL(conn, "DELETE FROM cleaning_reports where NICKNAME='" + nickname + "'");
			Utils.executeSQL(conn, "DELETE FROM carry_sessions where NICKNAME='" + nickname + "'");
			Utils.executeSQL(conn, "DELETE FROM cleaning_sessions where NICKNAME='" + nickname + "'");
			Utils.executeSQL(conn, "DELETE FROM shooting_sessions where NICKNAME='" + nickname + "'");
			Utils.executeSQL(conn, "DELETE FROM trivia_round_questions where NICKNAME='" + nickname + "'");
			Utils.executeSQL(conn, "DELETE FROM trivia_question_templates where NICKNAME='" + nickname + "'");
			Utils.executeSQL(conn, "DELETE FROM trivia_question_templates_custom where NICKNAME='" + nickname + "'");

			gunRegistryRepo.deleteById(gunPkToDelete);

			model.addAttribute("MESSAGE", "'" + gunRegistry.getNickname() + "' removed from Registry.");

		} else {

			String priorGunNickname = request.getParameter("PRIOR_NICKNAME");
			String currentGunNickname = gunRegistry.getNickname();

			String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
			if (!fileName.equals("")) {
				Utils.saveFile(getGunFunAppPhotoLocation() + "large\\", priorGunNickname + ".jpg", multipartFile);

				// Rescale the original image
				BufferedImage imageIn = ImageIO
						.read(new File(getGunFunAppPhotoLocation() + "large\\" + priorGunNickname + ".jpg"));
				ImageIO.write(Utils.resizeImage(imageIn, 550, 412), "jpeg",
						new File(getGunFunAppPhotoLocation() + "medium\\" + priorGunNickname + ".jpg"));
				ImageIO.write(Utils.resizeImage(imageIn, 350, 260), "jpeg",
						new File(getGunFunAppPhotoLocation() + "small\\" + priorGunNickname + ".jpg"));
			}

			if (!priorGunNickname.equals(currentGunNickname)) {

				// change anything with this nickname with the new value
				// Copy image for the gun
				Utils.copyFile(getGunFunAppPhotoLocation() + "large\\" + priorGunNickname + ".jpg",
						getGunFunAppPhotoLocation() + "large\\" + currentGunNickname + ".jpg");
				Utils.copyFile(getGunFunAppPhotoLocation() + "medium\\" + priorGunNickname + ".jpg",
						getGunFunAppPhotoLocation() + "medium\\" + currentGunNickname + ".jpg");
				Utils.copyFile(getGunFunAppPhotoLocation() + "small\\" + priorGunNickname + ".jpg",
						getGunFunAppPhotoLocation() + "small\\" + currentGunNickname + ".jpg");

				// Delete the old one
				Utils.deleteFile(getGunFunAppPhotoLocation() + "large\\" + priorGunNickname + ".jpg");
				Utils.deleteFile(getGunFunAppPhotoLocation() + "medium\\" + priorGunNickname + ".jpg");
				Utils.deleteFile(getGunFunAppPhotoLocation() + "small\\" + priorGunNickname + ".jpg");

				Utils.executeSQL(conn, "UPDATE cleaning_reports SET NICKNAME = '" + currentGunNickname
						+ "' where NICKNAME='" + priorGunNickname + "'");
				Utils.executeSQL(conn, "UPDATE carry_sessions SET NICKNAME = '" + currentGunNickname
						+ "' where NICKNAME='" + priorGunNickname + "'");
				Utils.executeSQL(conn, "UPDATE cleaning_sessions SET NICKNAME = '" + currentGunNickname
						+ "' where NICKNAME='" + priorGunNickname + "'");
				Utils.executeSQL(conn, "UPDATE shooting_sessions SET NICKNAME = '" + currentGunNickname
						+ "' where NICKNAME='" + priorGunNickname + "'");
				Utils.executeSQL(conn, "UPDATE trivia_round_questions SET NICKNAME = '" + currentGunNickname
						+ "' where NICKNAME='" + priorGunNickname + "'");
				Utils.executeSQL(conn, "UPDATE trivia_question_templates SET NICKNAME = '" + currentGunNickname
						+ "' where NICKNAME='" + priorGunNickname + "'");
				Utils.executeSQL(conn, "UPDATE trivia_question_templates_custom SET NICKNAME = '" + currentGunNickname
						+ "' where NICKNAME='" + priorGunNickname + "'");

			}

			gunRegistryRepo.save(gunRegistry);
			conn.close();

			model.addAttribute("MESSAGE", "'" + gunRegistry.getNickname() + "' edited in Registry.");

		}

		return "frame_main";
	}

	@GetMapping("/registry_add")
	public String registryAdd(Model model) throws SQLException {
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("gunRegistry", new Registry());
		model.addAttribute("allCaliberSet", getAllCaliberValues());
		model.addAttribute("reportTitle", "Add Registry Entry");
		conn.close();
		return "registry_add";
	}

	@PostMapping("/registry_add")
	public String registryAddToDb(@RequestParam("gunPhoto") MultipartFile multipartFile,
			@ModelAttribute Registry gunRegistry, Model model) throws IOException {

		gunRegistry.setMarketCostDate(gunRegistry.getPurchaseDate());
		gunRegistry.setMarketCost(gunRegistry.getPurchaseCost());
		gunRegistry.setMarketUrl("Original Purchase");
		gunRegistryRepo.save(gunRegistry);

		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

		if (fileName.equals("")) {
			Utils.copyFile(getGunFunAppPhotoLocation() + "\\_NEW.jpg",
					getGunFunAppPhotoLocation() + "large\\" + gunRegistry.getNickname() + ".jpg");
			Utils.copyFile(getGunFunAppPhotoLocation() + "\\_NEW.jpg",
					getGunFunAppPhotoLocation() + "medium\\" + gunRegistry.getNickname() + ".jpg");
			Utils.copyFile(getGunFunAppPhotoLocation() + "\\_NEW.jpg",
					getGunFunAppPhotoLocation() + "small\\" + gunRegistry.getNickname() + ".jpg");
		} else {
			Utils.saveFile(getGunFunAppPhotoLocation() + "large\\", gunRegistry.getNickname() + ".jpg", multipartFile);

			// Rescale the original image
			BufferedImage imageIn = ImageIO
					.read(new File(getGunFunAppPhotoLocation() + "large\\" + gunRegistry.getNickname() + ".jpg"));
			ImageIO.write(Utils.resizeImage(imageIn, 550, 412), "jpeg",
					new File(getGunFunAppPhotoLocation() + "medium\\" + gunRegistry.getNickname() + ".jpg"));
			ImageIO.write(Utils.resizeImage(imageIn, 350, 260), "jpeg",
					new File(getGunFunAppPhotoLocation() + "small\\" + gunRegistry.getNickname() + ".jpg"));

		}

		// make some quiz questions
		List<Registry> singleGunList = new ArrayList<Registry>();
		singleGunList.add(gunRegistry);
		addStandardQuestionsForGuns(singleGunList);

		// Add a cleaning record, I should be cleaning it before ever firing it.
		gunCleaningSessionsRepo
				.save(new CleaningSession(gunRegistry.getNickname(), new Date(System.currentTimeMillis())));

		model.addAttribute("MESSAGE", "'" + gunRegistry.getNickname() + "' added to Registry.");

		return "frame_main";
	}

	/*
	 * Market Functions
	 */

	@GetMapping("/market/market_update")
	public String marketUpdate(Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Update Market Values");
		String sql = "SELECT GUN_PK, NICKNAME, MAKE, MODEL FROM registry order by NICKNAME";
		model.addAttribute("report", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));
		conn.close();

		return "market_update";
	}

	@PostMapping("/market/market_update")
	public String marketUpdateFinal(HttpServletRequest request, Model model) throws SQLException, IOException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			if (keyStr.startsWith("MARKET_COST_")) {
				String gunPkToUpdate = keyStr.replaceAll("MARKET_COST_", "");
				String gunCostToUpdate = request.getParameter("MARKET_COST_" + gunPkToUpdate).replace(",", "");

				if (!gunCostToUpdate.equals("")) {

					String gunUrlToUpdate = request.getParameter("MARKET_URL_" + gunPkToUpdate);

					Utils.executeSQL(conn,
							"UPDATE registry SET MARKET_COST = '" + Double.parseDouble(gunCostToUpdate)
									+ "', MARKET_URL = '" + gunUrlToUpdate + "', MARKET_COST_DATE = '"
									+ new Date(System.currentTimeMillis()) + "' WHERE GUN_PK = " + gunPkToUpdate);

				}

			}

		}

		model.addAttribute("MESSAGE", "Market values updated.");

		conn.close();

		return "frame_main";
	}

	@GetMapping("/function/rebuild_questions")
	public String functionRebuildQuestions(Model model) throws ClassNotFoundException, SQLException {

		model.addAttribute("MESSAGE", rebuildQuestions());
		return "frame_main";

	}

	/*
	 * Utility Functions
	 */

	@PostMapping("/range/packsheet")
	public String rangePackSheetFinal(HttpServletRequest request, Model model) throws SQLException, IOException {

		ArrayList<Long> gunsToPackList = new ArrayList<Long>();

		Map<String, String[]> requestParameterMap = request.getParameterMap();
		for (Object key : requestParameterMap.keySet()) {
			String keyStr = (String) key;
			if (keyStr.startsWith("GUN_PK_") && !keyStr.startsWith("GUN_PK_NICKNAME_")) {
				String gunPkToPack = keyStr.replaceAll("GUN_PK_", "");
				gunsToPackList.add(Long.parseLong(gunPkToPack));

				// lets make 0 entries on shot log to be edited
				ShootingSession shootingSession = new ShootingSession();
				shootingSession.setNickname(request.getParameter("GUN_PK_NICKNAME_" + gunPkToPack));
				shootingSession.setNoOfRounds(Long.parseLong("0"));
				shootingSession.setCaliber("");
				shootingSession.setDateFired(Utils.parseDate(request.getParameter("rangeDate")));
				gunShootingSessionsRepo.save(shootingSession);

			}
		}

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();
		model.addAttribute("reportTitle", "Pack List");
		String sql = "SELECT GUN_PK, NICKNAME, MAKE, MODEL, CALIBER FROM registry WHERE GUN_PK IN "
				+ gunsToPackList.toString().replace('[', '(').replace(']', ')') + " ORDER by CALIBER, NICKNAME";
		model.addAttribute("report", Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));
		conn.close();

		return "report_packsheet";
	}

	@GetMapping("/function/backup")
	public String functionBackup(HttpServletRequest request, Model model)
			throws ClassNotFoundException, SQLException, IOException {

		ArrayList<String> backedUpItems = new ArrayList<String>();

		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("DATA")) {
			backedUpItems.add("Data");

			Connection conn = jdbcTemplateOne.getDataSource().getConnection();

			String backupFolderLocation = getGunFunAppLocation() + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " DATA TAB";

			File oDirectory = new File(backupFolderLocation);
			if (!oDirectory.exists()) {
				oDirectory.mkdirs();
			}

			String tableName = "carry_sessions";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "cleaning_reports";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "cleaning_sessions";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "registry";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "roles";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "shooting_sessions";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "users";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "valid_calibers";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "trivia_question_templates";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "trivia_question_templates_custom";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "trivia_rounds";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			tableName = "trivia_round_questions";
			Utils.exportSQLAsTabDelimitedDataFile(conn, "SELECT * FROM " + tableName,
					backupFolderLocation + "\\" + tableName + ".tab", true);

			conn.close();
		}

		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("IMAGES")) {
			backedUpItems.add("Images");

			String backupFolderLocation = getGunFunAppLocation() + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " IMAGES.zip";

			File oDirectory = new File(backupFolderLocation);
			if (oDirectory.exists()) {
				oDirectory.delete();
			}

			Utils.zipDirectory(getGunFunAppPhotoLocation(), backupFolderLocation);

		}

		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("MANUALS")) {
			backedUpItems.add("Manuals");

			String backupFolderLocation = getGunFunAppLocation() + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " MANUALS.zip";

			File oDirectory = new File(backupFolderLocation);
			if (oDirectory.exists()) {
				oDirectory.delete();
			}

			Utils.zipDirectory(getGunFunAppManualLocation(), backupFolderLocation);

		}

		if (request.getParameter("what").equals("ALL") || request.getParameter("what").equals("PROPERTIES")) {
			backedUpItems.add("Properties");

			Path resourceDirectory = Paths.get("src", "main", "resources");
			String pathToResources = resourceDirectory.toFile().getAbsolutePath() + "\\application.properties";
			String backupFolderLocation = getGunFunAppLocation() + "\\_backup\\"
					+ new Date(System.currentTimeMillis()).toString() + " application.properties";

			File oDirectory = new File(backupFolderLocation);
			if (oDirectory.exists()) {
				oDirectory.delete();
			}

			Utils.copyFile(pathToResources, backupFolderLocation);
		}

		String backedUpItemsDisplay = backedUpItems.toString().replaceAll("\\[", "").replaceAll("\\]", "");
		model.addAttribute("MESSAGE", "Items backed up: " + backedUpItemsDisplay);

		return "frame_main";

	}

	/*
	 * Game Functions
	 */

	@GetMapping("/quiz/start")
	public String quizStart(HttpServletRequest request, Model model) throws SQLException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		model.addAttribute("reportTitle", "Start Quiz");
		model.addAttribute("getAllGunNames", getAllGunNicknames());
		model.addAttribute("getAllMakes", getAllMakes());
		model.addAttribute("getAllModels", getAllModels());
		model.addAttribute("getAllCalibers", getAllCalibers());

		conn.close();

		return "quiz_start";
	}

	@PostMapping("/quiz/question")
	public String quizQuestion(HttpServletRequest request,
			@ModelAttribute TriviaRoundQuestion gunTriviaRoundsQuestionsArg, Model model)
			throws SQLException, IOException {

		Long roundPk = null;
		Long questionPk = null;

		String roundPkAsString = request.getParameter("roundPk");
		if (roundPkAsString == null) {
			// new game
			String contestantName = request.getParameter("contestantName");
			long noOfQuestions = Long.parseLong(request.getParameter("noOfQuestions"));
			roundPk = createNewRound(contestantName, noOfQuestions);
		} else {
			// existing question
			roundPk = gunTriviaRoundsQuestionsArg.getRoundPk();
			questionPk = gunTriviaRoundsQuestionsArg.getQuestionPk();

			TriviaRoundQuestion gunTriviaRoundsQuestions = gunTriviaGameRepo.findById(questionPk).get();
			gunTriviaRoundsQuestions.setQuestionIsAnswered(true);
			gunTriviaRoundsQuestions.setUserResponse(gunTriviaRoundsQuestionsArg.getUserResponse());
			if (gunTriviaRoundsQuestions.getCorrectResponse().equals(gunTriviaRoundsQuestionsArg.getUserResponse())) {
				gunTriviaRoundsQuestions.setQuestionIsCorrect(true);
			} else {
				gunTriviaRoundsQuestions.setQuestionIsCorrect(false);
			}
			gunTriviaGameRepo.save(gunTriviaRoundsQuestions);
		}

		model.addAttribute("getAllGunNames", request.getParameter("getAllGunNames"));
		model.addAttribute("getAllMakes", request.getParameter("getAllMakes"));
		model.addAttribute("getAllModels", request.getParameter("getAllModels"));
		model.addAttribute("getAllCalibers", request.getParameter("getAllCalibers"));

		List<TriviaRoundQuestion> gunTriviaRoundsQuestions = gunTriviaGameRepo.findByRoundPk(roundPk);
		if (gunTriviaRoundsQuestions.size() > 0) {

			TriviaRoundQuestion question = gunTriviaRoundsQuestions.get(0);

			model.addAttribute("gunTriviaRoundsQuestions", question);

			if (question.getQuestionType().equals("MULTIPLE_CHOICE")) {
				String responses = question.getQuestionResponses();

				switch (responses) {
				case "ALL_GUNNAMES":
					responses = request.getParameter("getAllGunNames");
					break;
				case "ALL_MAKES":
					responses = request.getParameter("getAllMakes");
					break;
				case "ALL_MODELS":
					responses = request.getParameter("getAllModels");
					break;
				case "ALL_CALIBERS":
					responses = request.getParameter("getAllCalibers");
					break;
				}

				model.addAttribute("dropDownAnswers", Arrays.asList(responses.split("\\|")));
			}

			Connection conn = jdbcTemplateOne.getDataSource().getConnection();
			String totalQuestions = Utils.getStringValueFromTable(conn,
					"SELECT COUNT(QUESTION_PK) as TOTAL_COUNT FROM trivia_round_questions WHERE ROUND_PK = " + roundPk,
					"TOTAL_COUNT");
			String totalQuestionsAnswered = Utils.getStringValueFromTable(conn,
					"SELECT COUNT(QUESTION_PK) as TOTAL_COUNT_ANSWERED FROM trivia_round_questions WHERE QUESTION_IS_ANSWERED = true AND ROUND_PK = "
							+ roundPk,
					"TOTAL_COUNT_ANSWERED");
			model.addAttribute("reportTitle",
					"Quiz - Question " + (Long.valueOf(totalQuestionsAnswered) + 1) + " of " + totalQuestions);
			conn.close();

			return "quiz_question";

		} else {

			Connection conn = jdbcTemplateOne.getDataSource().getConnection();

			String totalQuestions = Utils.getStringValueFromTable(conn,
					"SELECT COUNT(QUESTION_PK) as TOTAL_COUNT FROM trivia_round_questions WHERE ROUND_PK = " + roundPk,
					"TOTAL_COUNT");
			String totalQuestionsAnsweredCorrectly = Utils.getStringValueFromTable(conn,
					"SELECT COUNT(QUESTION_PK) as TOTAL_COUNT_ANSWERED_CORRECTLY FROM trivia_round_questions WHERE QUESTION_IS_CORRECT = true AND ROUND_PK = "
							+ roundPk,
					"TOTAL_COUNT_ANSWERED_CORRECTLY");

			Double score = Double.parseDouble(totalQuestionsAnsweredCorrectly) * 100
					/ Double.parseDouble(totalQuestions);

			TriviaRound gunTriviaRounds = gunTriviaRoundsRepo.findById(roundPk).get();
			gunTriviaRounds.setRoundNoOfQuestionsCorrect(Long.parseLong(totalQuestionsAnsweredCorrectly));
			gunTriviaRounds.setRoundScore(BigDecimal.valueOf(score));
			gunTriviaRoundsRepo.save(gunTriviaRounds);

			String sql = "SELECT QUESTION_PK, QUESTION_IS_CORRECT, QUESTION, CORRECT_RESPONSE, USER_RESPONSE, IMAGE_LOCATION, NICKNAME "
					+ "FROM trivia_round_questions WHERE ROUND_PK = " + roundPk + " ORDER by QUESTION_PK";
			model.addAttribute("reviewAllQuestions",
					Utils.makeSQLAsArrayListHashMap(conn, sql, null, null, null, null));

			conn.close();

			model.addAttribute("reportTitle", "Quiz Complete!");
			model.addAttribute("score", "Your score is " + String.format("%,.2f", score) + "%");

			return "quiz_complete";

		}

	}

	/*
	 * Private Functions
	 */

	private String getAllGunNicknames() {

		TreeSet<String> allGunNickNamesSet = getAllGunNicknameValues();
		String allGunNames = allGunNickNamesSet.toString();
		allGunNames = allGunNames.substring(1, allGunNames.length() - 1).replace(", ", "|");
		return allGunNames;

	}

	private TreeSet<String> getAllGunNicknameValues() {

		TreeSet<String> allGunNames = new TreeSet<String>();

		List<Registry> gunRegistryEntries = (List<Registry>) gunRegistryRepo.findAll();
		for (Registry gunRegistry : gunRegistryEntries) {
			allGunNames.add(gunRegistry.getNickname());
		}

		return allGunNames;

	}

	private String getAllMakes() {

		TreeSet<String> allMakesSet = getAllMakesValues();

		String allMakes = allMakesSet.toString();
		allMakes = allMakes.substring(1, allMakes.length() - 1).replace(", ", "|");

		return allMakes;
	}

	private TreeSet<String> getAllMakesValues() {

		TreeSet<String> allMakesSet = new TreeSet<String>();

		List<Registry> gunRegistryEntries = (List<Registry>) gunRegistryRepo.findAll();
		for (Registry gunRegistry : gunRegistryEntries) {
			allMakesSet.add(gunRegistry.getMake());
		}

		return allMakesSet;

	}

	private String getAllModels() {

		TreeSet<String> allModelSet = getAllModelsValues();

		String allModels = allModelSet.toString();
		allModels = allModels.substring(1, allModels.length() - 1).replace(", ", "|");

		return allModels;
	}

	private TreeSet<String> getAllModelsValues() {

		TreeSet<String> allModelSet = new TreeSet<String>();

		List<Registry> gunRegistryEntries = (List<Registry>) gunRegistryRepo.findAll();
		for (Registry gunRegistry : gunRegistryEntries) {
			allModelSet.add(gunRegistry.getModel());
		}

		return allModelSet;
	}

	private String getAllCalibers() throws SQLException {

		TreeSet<String> allCaliberSet = getAllCaliberValues();

		String allCalibers = allCaliberSet.toString();
		allCalibers = allCalibers.substring(1, allCalibers.length() - 1).replace(", ", "|");

		return allCalibers;
	}

	private TreeSet<String> getAllCaliberValues() throws SQLException {

		TreeSet<String> allModelSet = new TreeSet<String>();

		List<ValidCaliber> validCalibers = (List<ValidCaliber>) validCaliberRepo.findAll();
		for (ValidCaliber singleValidCaliber : validCalibers) {
			allModelSet.add(singleValidCaliber.getCaliber());
		}

		return allModelSet;

	}

	private void processCleaningReport(Connection conn) throws SQLException {

		jdbcTemplateOne.execute("TRUNCATE TABLE cleaning_reports");

		String sql = "SELECT NICKNAME, MAX(date_cleaned) as last_date_cleaned FROM cleaning_sessions group by NICKNAME order by NICKNAME";
		Statement statement = conn.createStatement();
		ResultSet result = statement.executeQuery(sql);

		while (result.next()) {
			String nickname = result.getString("NICKNAME");
			Date lastCleanedDate = result.getDate("last_date_cleaned");
			String sqlSelect = "SELECT NICKNAME, CALIBER, NO_OF_ROUNDS, DATE_FIRED FROM shooting_sessions WHERE NICKNAME = '"
					+ nickname + "' AND DATE_FIRED > '" + lastCleanedDate + "' ORDER BY DATE_FIRED";
			String sqlTwo = "INSERT INTO cleaning_reports (NICKNAME, CALIBER, NO_OF_ROUNDS, DATE_FIRED) " + sqlSelect;
			jdbcTemplateOne.execute(sqlTwo);
		}

		jdbcTemplateOne.execute("UPDATE registry SET gun_is_dirty = false");
		jdbcTemplateOne.execute(
				"UPDATE registry SET gun_is_dirty = true WHERE NICKNAME IN (SELECT NICKNAME FROM cleaning_reports)");

	}

	private ArrayList<ArrayList<HashMap<String, String>>> getGunsInTwoColumn(
			ArrayList<HashMap<String, String>> gunsFound) {

		ArrayList<ArrayList<HashMap<String, String>>> twoColumnGuns = new ArrayList<ArrayList<HashMap<String, String>>>();

		if (gunsFound.size() % 2 != 0) {
			HashMap<String, String> blankEntry = new HashMap<String, String>();
			blankEntry.put("GUN_PK", "");
			gunsFound.add(blankEntry);
		}

		ArrayList<HashMap<String, String>> insertLine = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < gunsFound.size(); i++) {
			if (i % 2 == 0) {
				insertLine.add(gunsFound.get(i));
			} else {
				insertLine.add(gunsFound.get(i));
				twoColumnGuns.add(insertLine);
				insertLine = new ArrayList<HashMap<String, String>>();
			}
		}
		return twoColumnGuns;
	}

	private String rebuildQuestions() throws SQLException {

		jdbcTemplateOne.execute("TRUNCATE TABLE trivia_question_templates");
		if (env.getProperty("spring.datasource.driverClassName").equals("com.mysql.cj.jdbc.Driver")) {
			jdbcTemplateOne.execute("ALTER TABLE trivia_question_templates AUTO_INCREMENT = 1");

		} else {
			jdbcTemplateOne.execute("ALTER TABLE trivia_question_templates ALTER COLUMN TRIVIA_PK RESTART WITH 1");
		}

		jdbcTemplateOne.execute("TRUNCATE TABLE trivia_rounds");
		jdbcTemplateOne.execute("TRUNCATE TABLE trivia_round_questions");

		if (env.getProperty("spring.datasource.driverClassName").equals("com.mysql.cj.jdbc.Driver")) {
			jdbcTemplateOne.execute("ALTER TABLE trivia_rounds AUTO_INCREMENT = 1");
			jdbcTemplateOne.execute("ALTER TABLE trivia_round_questions AUTO_INCREMENT = 1");
		} else {
			jdbcTemplateOne.execute("ALTER TABLE trivia_rounds ALTER COLUMN ROUND_PK RESTART WITH 1");
			jdbcTemplateOne.execute("ALTER TABLE trivia_round_questions ALTER COLUMN QUESTION_PK RESTART WITH 1");
		}

		// put standard questions
		List<Registry> gunRegistryEntries = (List<Registry>) gunRegistryRepo.findAll();
		addStandardQuestionsForGuns(gunRegistryEntries);

		// put custom questions
		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		if (env.getProperty("spring.datasource.driverClassName").equals("com.mysql.cj.jdbc.Driver")) {
			Utils.executeSQL(conn, "INSERT INTO trivia_question_templates "
					+ "(QUESTION_TYPE, QUESTION, QUESTION_RESPONSES, CORRECT_RESPONSE, IMAGE_LOCATION, NICKNAME) VALUES "
					+ " (SELECT QUESTION_TYPE, QUESTION, QUESTION_RESPONSES, CORRECT_RESPONSE, IMAGE_LOCATION, NICKNAME FROM "
					+ " trivia_question_templates_custom) ");

		} else {
			Utils.executeSQL(conn, "INSERT INTO trivia_question_templates "
					+ "(QUESTION_TYPE, QUESTION, QUESTION_RESPONSES, CORRECT_RESPONSE, IMAGE_LOCATION, NICKNAME) "
					+ " (SELECT QUESTION_TYPE, QUESTION, QUESTION_RESPONSES, CORRECT_RESPONSE, IMAGE_LOCATION, NICKNAME FROM "
					+ " trivia_question_templates_custom) ");
		}

		conn.close();

		return "Question rounds, scores and questions have been rebuilt.";
	}

	private Long createNewRound(String username, long no_of_questions) throws SQLException {

		Connection conn = jdbcTemplateOne.getDataSource().getConnection();

		TriviaRound gunTriviaRounds = new TriviaRound();
		gunTriviaRounds.setRoundUser(username);
		gunTriviaRounds.setRoundNoOfQuestions(no_of_questions);
		gunTriviaRounds.setRoundPlayedDate(new Date(System.currentTimeMillis()));
		gunTriviaRoundsRepo.save(gunTriviaRounds);

		Long roundPk = gunTriviaRounds.getRoundPk();
		Long totalQuestionsAvailable = Utils.getRowsCountInDataTable(conn, "trivia_question_templates");

		HashSet<Integer> randomIds = new HashSet<Integer>();
		Random random = new Random();
		while (true) {
			int nextRandom = random.nextInt(totalQuestionsAvailable.intValue() + 1);
			if (nextRandom != 0) {
				randomIds.add(nextRandom);
			}
			if (randomIds.size() == no_of_questions)
				break;
		}

		for (int singleId : randomIds) {
			try {
				TriviaQuestionTemplate gunTriviaTemplateQuestions = gunTriviaTemplateQuestionsRepo
						.findById(Long.valueOf(singleId)).get();

				TriviaRoundQuestion gunTriviaRoundsQuestions = new TriviaRoundQuestion();
				gunTriviaRoundsQuestions.setRoundPk(roundPk);
				gunTriviaRoundsQuestions.setQuestionType(gunTriviaTemplateQuestions.getQuestionType());
				gunTriviaRoundsQuestions.setQuestion(gunTriviaTemplateQuestions.getQuestion());
				gunTriviaRoundsQuestions.setQuestionResponses(gunTriviaTemplateQuestions.getQuestionResponses());
				gunTriviaRoundsQuestions.setCorrectResponse(gunTriviaTemplateQuestions.getCorrectResponse());
				gunTriviaRoundsQuestions.setImageLocation(gunTriviaTemplateQuestions.getImageLocation());
				gunTriviaRoundsQuestions.setNickname(gunTriviaTemplateQuestions.getNickname());
				gunTriviaRoundsQuestions.setQuestionIsAnswered(false);
				gunTriviaGameRepo.save(gunTriviaRoundsQuestions);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return roundPk;

	}

	private void addStandardQuestionsForGuns(List<Registry> gunRegistryEntries) {

		for (Registry gunRegistry : gunRegistryEntries) {

			String gunName = gunRegistry.getNickname();

			TriviaQuestionTemplate gunTriviaTemplateQuestions = new TriviaQuestionTemplate();
			gunTriviaTemplateQuestions.setQuestionType("MULTIPLE_CHOICE");
			gunTriviaTemplateQuestions.setQuestion("What is this gun's nickname?");
			gunTriviaTemplateQuestions.setQuestionResponses("ALL_GUNNAMES");
			gunTriviaTemplateQuestions.setCorrectResponse(gunName);
			gunTriviaTemplateQuestions.setImageLocation("REGISTRY");
			gunTriviaTemplateQuestions.setNickname(gunName);
			gunTriviaTemplateQuestionsRepo.save(gunTriviaTemplateQuestions);

			gunTriviaTemplateQuestions = new TriviaQuestionTemplate();
			gunTriviaTemplateQuestions.setQuestionType("MULTIPLE_CHOICE");
			gunTriviaTemplateQuestions.setQuestion("What is this gun's manufacturer?");
			gunTriviaTemplateQuestions.setQuestionResponses("ALL_MAKES");
			gunTriviaTemplateQuestions.setCorrectResponse(gunRegistry.getMake());
			gunTriviaTemplateQuestions.setImageLocation("REGISTRY");
			gunTriviaTemplateQuestions.setNickname(gunName);
			gunTriviaTemplateQuestionsRepo.save(gunTriviaTemplateQuestions);

			gunTriviaTemplateQuestions = new TriviaQuestionTemplate();
			gunTriviaTemplateQuestions.setQuestionType("MULTIPLE_CHOICE");
			gunTriviaTemplateQuestions.setQuestion("What is this gun's model?");
			gunTriviaTemplateQuestions.setQuestionResponses("ALL_MODELS");
			gunTriviaTemplateQuestions.setCorrectResponse(gunRegistry.getModel());
			gunTriviaTemplateQuestions.setImageLocation("REGISTRY");
			gunTriviaTemplateQuestions.setNickname(gunName);
			gunTriviaTemplateQuestionsRepo.save(gunTriviaTemplateQuestions);

			gunTriviaTemplateQuestions = new TriviaQuestionTemplate();
			gunTriviaTemplateQuestions.setQuestionType("MULTIPLE_CHOICE");
			gunTriviaTemplateQuestions.setQuestion("What is this gun's caliber?");
			gunTriviaTemplateQuestions.setQuestionResponses("ALL_CALIBERS");
			gunTriviaTemplateQuestions.setCorrectResponse(gunRegistry.getCaliber());
			gunTriviaTemplateQuestions.setImageLocation("REGISTRY");
			gunTriviaTemplateQuestions.setNickname(gunName);
			gunTriviaTemplateQuestionsRepo.save(gunTriviaTemplateQuestions);

		}

	}

	private boolean needBlankLines(Connection conn, String nickname, java.sql.Date dateCarried) throws SQLException {

		if (nickname.equals("")) {
			// empty entry, check and see if empty entry exists before adding one
			String totalEmptyLines = Utils.getStringValueFromTable(conn,
					"SELECT count(*) as TOTAL_COUNT FROM carry_sessions WHERE date_carried = '" + dateCarried + "'",
					"TOTAL_COUNT");
			if (Long.parseLong(totalEmptyLines) > 0) {
				return false;
			} else {
				return true;
			}
		} else {

			String totalEmptyLines = Utils.getStringValueFromTable(conn,
					"SELECT count(*) as TOTAL_COUNT FROM carry_sessions WHERE NICKNAME = '" + nickname
							+ "' AND date_carried = '" + dateCarried + "'",
					"TOTAL_COUNT");
			if (Long.parseLong(totalEmptyLines) > 0) {
				return false;
			} else {
				String sql = "DELETE FROM carry_sessions WHERE NICKNAME = '' AND date_carried = '" + dateCarried + "'";
				conn.createStatement().execute(sql);
				return true;
			}
		}

	}

	public String getPreferenceStringValue(Connection conn, String prefName) throws SQLException {
		return Utils.getStringValueFromTable(conn, "SELECT PREFERENCE_VALUE FROM PREFERENCES WHERE PREFERENCE_KEY = '"
				+ prefName + "' AND PREFERENCE_TYPE = 'String'", "PREFERENCE_VALUE");
	}

	public boolean getPreferenceBooleanValue(Connection conn, String prefName) throws SQLException {
		return Boolean.parseBoolean(
				Utils.getStringValueFromTable(conn, "SELECT PREFERENCE_VALUE FROM PREFERENCES WHERE PREFERENCE_KEY = '"
						+ prefName + "' AND PREFERENCE_TYPE = 'Boolean'", "PREFERENCE_VALUE"));
	}

	public long getPreferenceLongValue(Connection conn, String prefName) throws NumberFormatException, SQLException {
		return Long.parseLong(
				Utils.getStringValueFromTable(conn, "SELECT PREFERENCE_VALUE FROM PREFERENCES WHERE PREFERENCE_KEY = '"
						+ prefName + "' AND PREFERENCE_TYPE = 'Long'", "PREFERENCE_VALUE"));
	}

	public double getPreferenceDoubleValue(Connection conn, String prefName)
			throws NumberFormatException, SQLException {
		return Double.parseDouble(
				Utils.getStringValueFromTable(conn, "SELECT PREFERENCE_VALUE FROM PREFERENCES WHERE PREFERENCE_KEY = '"
						+ prefName + "' AND PREFERENCE_TYPE = 'Double'", "PREFERENCE_VALUE"));
	}

	public boolean getBuildSampleAssets() {
		return Boolean.valueOf(env.getProperty("BUILD_SAMPLE_ASSETS"));
	}

	public String getDeleteMasterPassword() {
		return env.getProperty("DELETE_MASTER_PASSWORD");
	}

	public String getGunFunAppLocation() {
		return env.getProperty("GUNFUN_APP_FOLDER");
	}

	public String getGunFunAppPhotoLocation() {
		return env.getProperty("GUNFUN_APP_FOLDER") + "\\_images\\";
	}

	public String getGunFunAppManualLocation() {
		return env.getProperty("GUNFUN_APP_FOLDER") + "\\_manuals\\";
	}

}
