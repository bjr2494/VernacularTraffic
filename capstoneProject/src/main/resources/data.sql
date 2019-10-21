INSERT INTO PROFILE
(id, first_name, last_name, age, email_address, has_Made_Posts, has_Made_One_Post, has_Made_Post_Comments, 
 has_Made_One_Post_Comment, has_Made_Transcriptions, has_Made_One_Transcription, has_Made_Transcription_Comments, 
 has_Made_One_Transcription_Comment, one_Language, preferred_Language, num_Posts, num_Post_Comments, num_Transcriptions, 
 	num_Transcription_Comments)
VALUES
(1, 'boudica', 'iceni', 23, 'boudicaiceni@hotmail.com', true, false, false, false, true, false, true, true, false,
	'ENGLISH', 2, 0, 2, 1),
(2, 'hasdrubal', 'benes', 24, 'hasben@gmail.com', true, false, false, true, false, false, false, false, false, 
	'ENGLISH', 2, 0, 0, 0),
(3, 'yanis', 'caron', 27, 'dcerv@yahoo.com', true, false, false, false, false, false, false, false, true, 
	'ENGLISH', 2, 0, 0, 0),
(4, 'chantal', 'lune', 26, 'chantealalune@gmail.com', true, false, true, true, false, false, false, false, true, 
	'FRENCH', 2, 1, 0, 0),
(5, 'agnes', 'winthrop', 22, 'agwin@hotmail.com', true, false, false, false, false, false, false, false, true,
	'FRENCH', 2, 1, 0, 0),
(6, 'adasse', 'dimanche', 25, 'addo@gmail.com', true, false, false, false, false, false, true, true, true, 
	'FRENCH', 2, 0, 0, 1),
(7, 'vincent', 'drapeaux', 24, 'lesdrapeauxdevincent@gmail.com', true, false, false, false, false, false, false, false, false, 
	'FRENCH', 2, 1, 0, 0),
(8, 'alfred', 'wessex', 22, 'wessexoressex@yahoo.com', true, false, false, false, true, true, true, true, true, 
	'ENGLISH', 2, 0, 1, 1),
(9, 'bonjour', 'monde', 26, 'bonjourMonde@gmail.com', true, true, false, false, false, false, false, false, true,
	'FRENCH', 1, 0, 0, 0),
(10, 'tenth', 'user', 29, 'tenthUser@gmail.com', false, false, false, false, false, false, false, false, true, 'ENGLISH', 
	0, 0, 0, 0);

--INSERT INTO PROFILE_PREFERRED_LANGUAGE
--(profile_id, preferred_language)
--VALUES 
--(1, 'ENGLISH'),
--(2, 'ENGLISH'),
--(3, 'ENGLISH'),
--(4, 'FRENCH'),
--(5, 'ENGLISH'),
--(6, 'FRENCH'),
--(7, 'FRENCH'),
--(8, 'ENGLISH'),
--(9, 'FRENCH');

--
--(1, 'boudica', 'queen', '', '2018-04-19', '', '[English, French]'),
--(2, 'hasdrubal', 'theFairest', '', '2018-05-22', '', 'English, French'),
--(3, 'yanis', 'unCaron', '', '2018-06-18', '', 'English'), 
--(4, 'chantal', 'chanteuse', '2018-07-22', '', 'French'),
--(5, 'agnes', 'uniglot', '2018-08-15', '', 'English'),
--(6, 'adassa', 'addasseTrois', '2018-09-04', '', 'French'),
--(7, 'vincent', 'callebaut', '2018-10-17', '', 'French, English'),
--(8, 'alfred', 'greatWessex', '2018-11-25', '', 'English');
--(9, 'hello', 'leMonde', '2018-11-26, '', 'French, English');
--(10, 'tenthUser', 'tenthUser', '2019-10-17', '', 'english');

INSERT INTO USER
(id, username, password, salt, join_date, profile_id)
VALUES
(1, 'boudica234', 'P21JR6AZbMMkcRMFvfL2iJ4utkf4sCME9oUPXY3gmY0=', 'JdD8bg==', '2018-04-19', 1),
(2, 'hasdrubal543', 'theFairest', '', '2018-05-22', 2),
(3, '$yanis$', 'unCaron', '', '2018-06-18', 3),
(4, 'chantallll', 'chanteuse', '', '2018-07-22', 4),
(5, 'agneau', 'uniglot', '', '2018-08-15', 5),
(6, 'adasse3', 'addasseTrois', '', '2018-09-04', 6),
(7, 'vincent45', 'jWBvcmm1eyJsiJrsYF4pW7o2/tyakGs2h1FA7BH5si4=', 'av++Kg==', '2018-10-17', 7),
(8, 'alfredG', 'greatWessex', '', '2018-11-25', 8),
(9, 'bonjourMonde', 'iP//gBu6VSwFDfOKQ2IR1tW4J2UtOIfM7O8Kc45W9LE=', 'Ur1NAg==', '2018-11-26', 9), 
(10, 'tenthUser', '/YP0WTdoOP1hpODiD/jvNDdXXa9xbPOkLkDlFkzm8as=', 'MwmpjQ==', '2019-10-17', 10);

INSERT INTO POST 
(id, post_Date, post_Time, author_id, profile_id, content, name, has_Comments, has_Transcription, editable_By_Author,
has_One_Comment, deletable_By_Author, commentable_By_Author, post_Language, num_Post_Comments) 
VALUES
(1, '2018-04-19', '23:30:49', 1, 1, 'It is not that the girl is unfit for everything, it is that she is not of this world.',
	'Unfit Girl', true, false, false, true, false, true, 'ENGLISH', 1),
(2, '2018-05-22', '15:45:16', 2, 2, 'To be happy, we must not be too concerned with others.', 'Concerned', false, true, 
	false, false, true, false, 'ENGLISH', 0),
(3, '2018-06-18', '10:00:03', 3, 3, 'Tree of Hope, Stand Firm', 'Tree', false, true, false, false, true,
	false, 'ENGLISH', 0),
(4, '2018-07-22', '04:25:38', 4, 4, 'On ne change pas une équipe qui gagne', 'Une Équipe', false, false, true, false,
	true, false, 'FRENCH', 1),
(5, '2018-08-15', '23:16:24', 5, 5, 'A body sees noon from his own door', 'Own Door', false, false, false, true, false, true, 
	'ENGLISH', 0), 
(6, '2018-09-04', '17:45:00', 6, 6, 'La vraie amitié résiste au temps, à la distance et au silence', 'L''Amitié', false, false,
	true, false, true, false, 'FRENCH', 0),
(7, '2018-10-17', '14:58:35', 7, 7, 'Imaginer c''est choisir.', 'La Choix de La Vie', false, false, true, false, true, false, 
	'FRENCH', 0),
(8, '2018-11-25', '12:23:21', 8, 8, 'The Beautiful is always strange', 'Beautiful', false, true, false, false, true, false, 
	'ENGLISH', 0),
(9, '2018-12-21', '07:32:19', 1, 1, 'Tout ce qui peut être imaginé est vrai', 'Vrai', false, false, true, false, true, false, 
	'FRENCH', 0),
(10, '2019-01-01', '07:02:35', 2, 2, 'Les livres sont des amis froids et sûrs.', 'Les Vrais Amis', true, false, false, true,
	false, true, 'FRENCH', 1),
(11, '2019-02-13', '21:43:34', 3, 3, 'Where one door closes itself, another opens itself', 'Opening', false, false, true, false,
	true, false, 'ENGLISH', 0),
(12, '2019-03-16', '14:07:02', 4, 4, 'Écrire, c''est une façon de parler sans être interrompu.', 'Écrire des Mots', false,
	false, true, false, true, false, 'FRENCH', 0),
(13, '2019-04-19', '11:30:04', 5, 5, 
	'Summer afternoon, summer afternoon; to me those have always been the two most beautiful words in the English language',
	'Beautiful Words', false, false, true, false, true, false, 'ENGLISH', 0),
(14, '2019-05-06', '20:25:08', 6, 6, 'La plus grande richesse est de vivre content avec peu,', 'La Richesse', false, false,
	true, false, true, false, 'FRENCH', 0),
(15, '2019-06-11', '08:16:25', 7, 7, 'La langue n''a pas d''os, mais elle coupe le plus épais.', 'La Langue', false, false,
	true, false, true, false, 'FRENCH', 0),
(16, '2019-07-23', '10:30:36', 8, 8, 'If you judge people, you have no time to love them.', 'Judgment', false, false, true,
	false, true, false, 'ENGLISH', 0),
(17, '2019-08-23', '10:30:36', 9, 9, 'abcdéfghijklmnop', 'abcdéfgh', false, false, true, false, true, false, 
	'FRENCH', 0);

--INSERT INTO POST_POST_LANGUAGE
--(post_id, post_language)
--VALUES
--(1, 'ENGLISH'),
--(2, 'ENGLISH'), 
--(3, 'ENGLISH'),
--(4, 'FRENCH'),
--(5, 'ENGLISH'),
--(6, 'FRENCH'),
--(7, 'FRENCH'),
--(8, 'ENGLISH'),
--(9, 'FRENCH'),
--(10, 'FRENCH'),
--(11, 'ENGLISH'),
--(12, 'FRENCH'),
--(13, 'ENGLISH'),
--(14, 'FRENCH'),
--(15, 'FRENCH'),
--(16, 'ENGLISH'),
--(17, 'FRENCH');

INSERT INTO PROFILE_LANGUAGES
(profile_id, languages)
VALUES
(1, 'ENGLISH'),
(1, 'FRENCH'),
(2, 'ENGLISH'),
(2, 'FRENCH'),
(3, 'ENGLISH'),
(4, 'FRENCH'),
(5, 'ENGLISH'),
(6, 'FRENCH'),
(7, 'FRENCH'),
(7, 'ENGLISH'),
(8, 'ENGLISH'),
(9, 'FRENCH'), 
(10, 'ENGLISH');

INSERT INTO TRANSCRIPTION
(id, name, transcription_Date, transcription_Time, post_id, author_id, profile_id, content, has_Comments, editable_By_Author,
	has_One_Comment, deletable_By_Author, commentable_By_Author, transcription_Language, num_Transcription_Comments)
VALUES
(1, 'Transcription of ''concerned''', '2018-12-25', '06:30:03', 2, 8, 8,
	'tu bi ˈhæpi, wi mʌst nɑt bi tu kənˈsɜrnd wɪð ˈʌðərz', true, false, true, false, true, 'ENGLISH', 1),
(2, 'Transcription of ''Beautiful''', '2019-01-07', '05:43:43', 8, 1, 1,
	'THIS IS NOT REALLY A TRANSCRIPTION', true, false, false, false, true, 'ENGLISH', 2),
(3, 'Transcription of ''Tree''', '2019-01-01', '05:30:30', 3, 1, 1, 'me tree yeah me tree', false, true, false, true, false,
		'ENGLISH', 0);

INSERT INTO POST_COMMENT
(id, post_id, comment_Date, comment_Time, author_id, profile_id, content, deletable_By_Author, post_Comment_Language)
VALUES
(1, 4, '2018-08-17', '14:23:13', 4, 4, 'Est-ce que ca devrait etre ''d''equipe'' plutot qu''une equipe?', true, 'FRENCH'),
(2, 10, '2019-01-02', '23:32:03', 5, 5, 'C''est un commentaire de post', true, 'FRENCH'),
(3, 1, '2019-01-04', '06:34:35', 7, 7, 'Is she unfit?', true, 'ENGLISH');

INSERT INTO TRANSCRIPTION_COMMENT
(id, transcription_id, comment_Date, comment_Time, author_id, profile_id, content, deletable_By_Author, 
	transcription_Comment_Language)
VALUES
(1, 2, '2019-01-08', '14:32:51', 8, 8, 'What was she thinking with that transcription?', false, 'ENGLISH'),
(2, 1, '2019-02-06', '08:25:58', 6, 6, 'I love this transcription', true, 'ENGLISH'),
(3, 2, '2019-01-09', '16:02:35', 1, 1, 'Obviously t''is not a real transcription, Alfred the Okay', true, 'ENGLISH');
