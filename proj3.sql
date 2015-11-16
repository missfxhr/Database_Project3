-- project3 procedure

delimiter //

DROP PROCEDURE IF EXISTS login//
CREATE PROCEDURE login(IN studid INT(11), IN userpassword VARCHAR(10))
BEGIN
	IF EXISTS (SELECT * FROM student WHERE studid = id AND userpassword = password)
	THEN
		SELECT * FROM student WHERE studid = id AND userpassword = password;
	END IF;
END//

DROP PROCEDURE IF EXISTS list_current_courses//
CREATE PROCEDURE list_current_courses(IN id INT(11))
BEGIN
	IF MONTH(CURDATE()) >= 9 ||  MONTH(CURDATE()) <= 2
    THEN
		SET @Q = 'Q1';
	ELSE
		SET @Q = 'Q2';
	END IF;
    IF MONTH(CURDATE()) <= 2
    THEN
		SET @Y = YEAR(CURDATE()) - 1;
	ELSE
		SET @Y = YEAR(CURDATE());
	END IF;
	SELECT * 
    FROM transcript NATURAL JOIN unitofstudy
    WHERE studid = id AND year = @Y AND semester = @Q;
END//

DROP PROCEDURE IF EXISTS list_transcript//
CREATE PROCEDURE list_transcript(IN id INT(11))
BEGIN
	SELECT * 
    FROM transcript 
    WHERE studid = id;
END//

DROP PROCEDURE IF EXISTS list_course_detail//
CREATE PROCEDURE list_course_detail(IN stuId INT(11), IN classNum char(8))
BEGIN
	SELECT StudId, UoSCode, UoSName, Name as lecturer, Enrollment, MaxEnrollment, Year, Semester, Grade
	FROM
	(SELECT UoSCode, UoSName
	FROM unitofstudy) helper1
	NATURAL JOIN
	(SELECT *
	FROM
	(SELECT *
	FROM
	(SELECT *
	FROM transcript
	WHERE studid = stuId and UoSCode = classNum) trans NATURAL JOIN uosoffering) trans_usoffering
	NATURAL JOIN 
	(SELECT Id as InstructorId, Name FROM faculty) faculty_info) helper2;
END//

DROP TRIGGER IF EXISTS enroll//
CREATE TRIGGER enroll BEFORE INSERT ON transcript
FOR EACH ROW
	BEGIN
		# course existence
        IF NOT EXISTS (SELECT * FROM uosoffering
					WHERE uoscode = NEW.uoscode  
                    AND semester = NEW.semester 
                    AND year = NEW.year)
		THEN 
			SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Invalid Course';
		END IF;
		
        # max enrollment limitation
        IF NOT EXISTS (SELECT * FROM uosoffering
					WHERE uoscode = NEW.uoscode  
                    AND semester = NEW.semester 
                    AND year = NEW.year
                    AND enrollment < maxenrollment)
			THEN 
				SIGNAL SQLSTATE '45002' SET MESSAGE_TEXT = 'Max Enrollment Reached';
		END IF;
        
		# time requirement
        IF MONTH(CURDATE()) >= 9 ||  MONTH(CURDATE()) <= 2
		THEN
			SET @Q = 'Q1';
		ELSE
			SET @Q = 'Q2';
		END IF;
		IF MONTH(CURDATE()) <= 2
		THEN
			SET @Y = YEAR(CURDATE()) - 1;
		ELSE
			SET @Y = YEAR(CURDATE());
		END IF;
        IF NOT ((NEW.year = @Y AND NEW.semester = @Q) 
			OR (@Q = 'Q1' AND NEW.semester = 'Q2' AND NEW.year = @Y + 1) 
            OR (@Q = 'Q2' AND NEW.semester = 'Q1' AND NEW.year = @Y))
		THEN
			SIGNAL SQLSTATE '45003' SET MESSAGE_TEXT = 'Invalid Enrollment Period';
		END IF;
        
        # pre-requisites limitation
        IF EXISTS
        (
			SELECT grade
			FROM requires LEFT JOIN transcript
			ON requires.prerequoscode = transcript.uoscode
			WHERE NEW.studid = transcript.studid AND requires.uoscode = NEW.uoscode AND (grade IS NULL OR grade = 'F')
        )
        THEN
			SIGNAL SQLSTATE '45004' SET MESSAGE_TEXT = 'Unsatisfied Pre-requisites';
		END IF;
        
        # update enroll number
        UPDATE uosoffering
        SET enrollment = enrollment + 1
        WHERE uoscode = NEW.uoscode  AND semester = NEW.semester AND year = NEW.year;
        
        SET NEW.grade = NULL;
	END//

DROP PROCEDURE IF EXISTS print_prerequisites//
CREATE PROCEDURE print_prerequisites(IN in_studid INT(11), IN in_uoscode CHAR(8))
BEGIN
	SELECT transcript.uoscode as uoscode, transcript.semester as semester, transcript.year as year, transcript.grade as grade
	FROM requires LEFT JOIN transcript
	ON requires.prerequoscode = transcript.uoscode
	WHERE in_studid = transcript.studid AND requires.uoscode = in_uoscode AND (grade IS NULL OR grade = 'F');
END//

DROP PROCEDURE IF EXISTS enroll//
CREATE PROCEDURE enroll(IN in_studid INT(11), IN in_uoscode CHAR(8), IN in_semester CHAR(2), IN in_year INT(11))
BEGIN
	INSERT INTO transcript
    VALUES(in_studid,in_uoscode,in_semester,in_year,NULL);
END//

delimiter ;
