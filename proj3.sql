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
CREATE PROCEDURE list_current_courses(IN in_studid INT(11),IN in_semester CHAR(2),IN in_year INT(11))
BEGIN
	SELECT * 
    FROM transcript NATURAL JOIN unitofstudy
    WHERE studid = in_studid AND year = in_year AND semester = in_semester;
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
	END//

DROP PROCEDURE IF EXISTS print_candidate_courses//
CREATE PROCEDURE print_candidate_courses(IN in_studid INT(11), IN in_semester CHAR(2),IN in_year INT(11))
BEGIN
	SELECT uoscode, semester, year
	FROM uosoffering
	WHERE (enrollment < maxenrollment) 
		AND ((year = in_year AND semester = in_semester) 
			OR (in_semester = 'Q1' AND semester = 'Q2' AND year = in_year + 1) 
			OR (in_semester = 'Q2' AND semester = 'Q1' AND year = in_year));
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
    
	# update enroll number
	UPDATE uosoffering
	SET enrollment = enrollment + 1
	WHERE uoscode = in_uoscode AND semester = in_semester AND year = in_year;
END//

DROP PROCEDURE IF EXISTS update_profile//
CREATE PROCEDURE update_profile(IN studid INT(11), IN userpassword VARCHAR(10), IN useraddress VARCHAR(50))
BEGIN
	UPDATE student
    SET Address = useraddress, Password = userpassword
    WHERE Id = studid;
END//

delimiter ;
