-- project3 procedure

DROP TABLE IF EXISTS warning_message;
CREATE TABLE warning_message(
	warning INT
);
INSERT INTO warning_message VALUES(0);

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
	SELECT uoscode,semester,year,deptid,uosname,credits
    FROM transcript NATURAL JOIN unitofstudy
    WHERE studid = in_studid AND year = in_year AND semester = in_semester;
END//

DROP PROCEDURE IF EXISTS list_transcript//
CREATE PROCEDURE list_transcript(IN id INT(11))
BEGIN
	SELECT uoscode,semester,year,grade
    FROM transcript 
    WHERE studid = id;
END//

DROP PROCEDURE IF EXISTS list_course_detail//
CREATE PROCEDURE list_course_detail(IN stuId INT(11), IN classNum char(8))
BEGIN
	SELECT UoSCode, UoSName, Name as lecturer, Enrollment, MaxEnrollment, Year, Semester, Grade
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

DROP PROCEDURE IF EXISTS print_enroll_candidate_courses//
CREATE PROCEDURE print_enroll_candidate_courses(IN in_studid INT(11), IN in_semester CHAR(2),IN in_year INT(11),IN in_next_semester CHAR(2),IN in_next_year INT(11))
BEGIN
	DROP TABLE IF EXISTS enroll_candidate_courses;
    CREATE TABLE enroll_candidate_courses AS
	SELECT uoscode,semester,year,deptid,uosname,credits
	FROM uosoffering natural join unitofstudy
	WHERE (enrollment < maxenrollment) 
		AND ((semester = in_semester AND year = in_year) OR (semester = in_next_semester AND year = in_next_year))
		AND NOT EXISTS(SELECT * FROM transcript WHERE in_studid = studid AND uosoffering.uoscode = uoscode);
	SELECT * FROM enroll_candidate_courses;
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
CREATE PROCEDURE enroll(IN in_studid INT(11), IN in_uoscode CHAR(8))
BEGIN
	SELECT semester,year into @Q,@Y FROM enroll_candidate_courses WHERE uoscode = in_uoscode;
	INSERT INTO transcript(studid,uoscode,semester,year,grade)
    VALUES(in_studid,in_uoscode,@Q,@Y,NULL);
	# update enroll number
	UPDATE uosoffering
	SET enrollment = enrollment + 1
	WHERE uoscode = in_uoscode AND semester = @Q AND year = @Y;
END//

DROP PROCEDURE IF EXISTS update_profile//
CREATE PROCEDURE update_profile(IN studid INT(11), IN userpassword VARCHAR(10), IN useraddress VARCHAR(50))
BEGIN
	UPDATE student
    SET Address = useraddress, Password = userpassword
    WHERE Id = studid;
END//

DROP PROCEDURE IF EXISTS print_withdraw_candidate_courses//
CREATE PROCEDURE print_withdraw_candidate_courses(IN in_studid INT(11))
BEGIN
	DROP TABLE IF EXISTS withdraw_candidate_courses;
    CREATE TABLE withdraw_candidate_courses AS
	SELECT uoscode,semester,year,deptid,uosname,credits
	FROM transcript natural join unitofstudy
	WHERE grade IS NULL AND in_studid = studid;
    SELECT * FROM withdraw_candidate_courses;
END//


DROP PROCEDURE IF EXISTS withdraw//
CREATE PROCEDURE withdraw(IN in_studid INT(11), IN in_uoscode CHAR(8))
BEGIN
	SELECT semester,year into @Q,@Y FROM withdraw_candidate_courses WHERE uoscode = in_uoscode;
	
	# update enroll number
	UPDATE uosoffering
	SET enrollment = enrollment - 1
	WHERE uoscode = in_uoscode AND semester = @Q AND year = @Y;
    
	DELETE FROM transcript
    WHERE studid = in_studid AND uoscode = in_uoscode;
END//

DROP TRIGGER IF EXISTS withdraw//
CREATE TRIGGER withdraw AFTER UPDATE ON uosoffering
FOR EACH ROW
	BEGIN
        IF NEW.enrollment < OLD.enrollment AND NEW.enrollment * 2 < NEW.maxenrollment
        THEN 
			CALL set_warning_message(1);
		END IF;
	END//
    
DROP PROCEDURE IF EXISTS set_warning_message//
CREATE PROCEDURE set_warning_message(IN warning_value INT)
BEGIN
	# update enroll number
	UPDATE warning_message
	SET warning = warning_value;
END//

DROP PROCEDURE IF EXISTS check_warning_message//
CREATE PROCEDURE check_warning_message()
BEGIN
	# update enroll number
	SELECT warning FROM warning_message;
END//

delimiter ;
