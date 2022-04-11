INSERT INTO USERS (USER_NAME, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, ACTIVE) VALUES (
	'steve', 'Steve', 'C', 'sirfozzie@github.com', 'password', 1);
	
INSERT INTO ROLES (ROLE_PK, ROLE) VALUES (1, 'ADMIN');

INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('12 Ga', '12 Ga');
INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('22 LR', '22 LR');
INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('223 Rem', '223 Rem');
INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('357 Magnum', '357 Magnum|38 Special');
INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('38 Special', '38 Special');
INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('380 Auto', '380 Auto');
INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('40 S&W', '40 S&W');
INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('45 Auto', '45 Auto');
INSERT INTO VALID_CALIBERS (CALIBER, SHOOTS_CALIBER) VALUES ('9mm', '9mm');

INSERT INTO trivia_question_templates_custom (question_type, question, question_responses, correct_response, image_location, nickname) 
	VALUES ('MULTIPLE_CHOICE', 'What was my first gun?', 'ALL_GUNNAMES', 'Eileen', 'REGISTRY', 'Eileen');
INSERT INTO trivia_question_templates_custom (question_type, question, question_responses, correct_response, image_location, nickname) 
	VALUES ('MULTIPLE_CHOICE', 'What caliber can also shoot a 38 special?', 'ALL_CALIBERS', '357 Magnum', '', '');
INSERT INTO trivia_question_templates_custom (question_type, question, question_responses, correct_response, image_location, nickname) 
	VALUES ('MULTIPLE_CHOICE', 'What gun brand does James Bond traditionally use?', 'ALL_MAKES', 'Walther', '', '');
INSERT INTO trivia_question_templates_custom (question_type, question, question_responses, correct_response, image_location, nickname) 
	VALUES ('MULTIPLE_CHOICE', 'What gun model does James Bond traditionally use?', 'PPK|PPP|PPS|PPQ', 'PPK', '', '');

/*
INSERT INTO REGISTRY (NICKNAME, MAKE, MODEL, SERIAL, CALIBER, BARREL_LENGTH, 
	FRAME_MATERIAL, PURCHASE_COST, PURCHASE_DATE, SIGHTED_DATE, MARKET_COST, MARKET_COST_DATE, MARKET_URL, GUN_IS_DIRTY) VALUES (
	'Teresa', 'Walther', 'PPK/S', 'SERIAL_01', '380 Auto', 3.0, 'Metal', '500.00', '2021-01-01', '2021-02-02', 600.00, 
	'2022-03-03', 'http://www.fozden.com', '0');
INSERT INTO REGISTRY (NICKNAME, MAKE, MODEL, SERIAL, CALIBER, BARREL_LENGTH, 
	FRAME_MATERIAL, PURCHASE_COST, PURCHASE_DATE, SIGHTED_DATE, MARKET_COST, MARKET_COST_DATE, MARKET_URL, GUN_IS_DIRTY) VALUES (
	'Alex', 'Beretta', 'APX', 'SERIAL_02', '9mm', 4.0, 'Composite', '600.00', '2000-01-01', '2000-02-02', 600.00, 
	'2001-03-03', 'http://www.fozden.com', '0');
INSERT INTO REGISTRY (NICKNAME, MAKE, MODEL, SERIAL, CALIBER, BARREL_LENGTH, 
	FRAME_MATERIAL, PURCHASE_COST, PURCHASE_DATE, SIGHTED_DATE, MARKET_COST, MARKET_COST_DATE, MARKET_URL, GUN_IS_DIRTY) VALUES (
	'Valerie', 'Heckler & Koch', 'VP9', 'SERIAL_03', '9mm', 4.0, 'Composite', '700.00', '2010-01-01', '2010-02-02', 800.00, 
	'2011-03-03', 'http://www.fozden.com', '0');
INSERT INTO REGISTRY (NICKNAME, MAKE, MODEL, SERIAL, CALIBER, BARREL_LENGTH, 
	FRAME_MATERIAL, PURCHASE_COST, PURCHASE_DATE, SIGHTED_DATE, MARKET_COST, MARKET_COST_DATE, MARKET_URL, GUN_IS_DIRTY) VALUES (
	'Harriet', 'Smith & Wesson', '686+', 'SERIAL_04', '357 Magnum', 4.0, 'Metal', '750.00', '2010-01-05', '2010-02-05', 850.00, 
	'2011-03-05', 'http://www.fozden.com', '0');
*/