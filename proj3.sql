-- project3 procedure

delimiter //

DROP PROCEDURE IF EXISTS login//
CREATE PROCEDURE login(IN username VARCHAR(20), IN userpassword VARCHAR(10))
BEGIN
	IF EXISTS (SELECT * FROM student WHERE username = Name AND userpassword = password) 
    THEN
        SELECT * FROM student WHERE username = Name AND userpassword = password;
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
        SET @c1 = 0;
        SELECT @c1:=@c1 +1 AS c1 ,prerequoscode FROM requires WHERE NEW.uoscode = uoscode AND CURDATE() >= enforcedsince;
        
	END//

delimiter ;
