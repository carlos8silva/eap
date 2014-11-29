
-- VIEW: eap_users_v

DROP VIEW eap_users_v;

CREATE VIEW eap_users_v AS 
SELECT u.user_id AS user_id, CASE WHEN u.time_disabled IS NOT NULL THEN u.old_username ELSE u.username END AS username, u.password AS password, u.security_question_1 AS security_question_1, u.security_question_2 AS security_question_2,
u.security_answer_1 AS security_answer_1, u.security_answer_2 AS security_answer_2, u.time_locked AS time_locked, u.password_change_time AS password_change_time,
u.session_timeout_time AS session_timeout_time, u.failed_login_attempts AS failed_login_attempts, u.help_on AS help_on, u.first_name AS first_name,
u.last_name AS last_name, u.middle_initial AS middle_initial, u.old_username AS old_username, u.time_disabled AS time_disabled, u.time_created AS time_created, u.time_updated AS time_updated,
CONCAT(u.last_name, ", ", u.first_name, (CASE WHEN ISNULL(u.middle_initial) then "" ELSE CONCAT(" ", u.middle_initial) END)) AS full_name,
CASE WHEN u.time_disabled IS NOT NULL THEN "Disabled" WHEN ISNULL(u.time_locked) THEN "Active" ELSE "Locked" END AS status
FROM eap_users u;

-- VIEW: eap_sysmsgs_v

DROP VIEW IF EXISTS eap_sysmsgs_v;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `eap_sysmsgs_v` AS 
SELECT
  `um`.`msg_id`        AS `msg_id`,
  `um`.`user_id`       AS `user_id`,
  `m`.`subject`        AS `subject`,
  `um`.`status`        AS `status_code`,
  `m`.`date_sent`      AS `date_sent`,
  `m`.`from_name`       AS `from_name`,
  `m`.`message`        AS `message`,
  (CASE WHEN (`um`.`status` = 'U') THEN 'Unread' ELSE (CASE WHEN (`um`.`status` = 'R') THEN 'Read' ELSE 'Unread' END) END) AS `status_text`,
  CONCAT(CHAR((64 + TRUNCATE((`um`.`msg_id` / 1000000),0))),RIGHT(`um`.`msg_id`,6)) AS `ref_num`,
  `m`.`to_roles`       AS `to_roles`,
  `m`.`to_permissions` AS `to_permissions`,
  `m`.`to_users`       AS `to_users`
FROM (`eap_user_sysmsgs` `um`
   JOIN `eap_sysmsgs` `m`
     ON ((`um`.`msg_id` = `m`.`msg_id`)));


-- VIEW: rit_items_v

DROP VIEW rit_items_v;

CREATE VIEW rit_items_v AS 
SELECT ri.item_id AS item_id, ri.time_created AS time_created, ri.time_updated AS time_updated, ri.title AS title, ri.description AS description, ri.date_due AS date_due,
ri.mitigating_actions AS mitigating_actions, ri.impact_description AS impact_description, ri.date_transformed AS date_transformed, ri.time_closed AS time_closed, ri.project_id AS project_id,
CASE WHEN ri.item_type = "R" THEN "Risk" WHEN ri.item_type = "I" THEN "Issue" END AS item_type,
CONCAT(u1.last_name, ", ", u1.first_name, (CASE WHEN ISNULL(u1.middle_initial) then "" ELSE CONCAT(" ", u1.middle_initial) END)) AS raised_by,
CONCAT(u2.last_name, ", ", u2.first_name, (CASE WHEN ISNULL(u2.middle_initial) then "" ELSE CONCAT(" ", u2.middle_initial) END)) AS assigned_to,
CASE WHEN ri.priority="C" THEN "Critical" WHEN ri.priority="H" THEN "High" WHEN ri.priority="M" THEN "Medium" WHEN ri.priority="L" THEN "Low" END AS priority,
CASE WHEN ri.likelyhood="V" THEN "Very High" WHEN ri.likelyhood="H" THEN "High" WHEN ri.likelyhood="M" THEN "Medium" WHEN ri.likelyhood="L" THEN "Low" END AS likelyhood,
CASE WHEN ISNULL(ri.time_closed) THEN "Open" ELSE "Closed" END AS status
FROM rit_items ri, eap_users u1, eap_users u2
WHERE u1.user_id = ri.raised_by AND
u2.user_id = ri.assigned_to


-- VIEW: rit_item_updates_v
DROP VIEW rit_item_updates_v;

CREATE VIEW rit_item_updates_v AS
SELECT riu.update_id AS update_id, riu.item_id AS item_id, riu.time_created AS time_created, riu.time_updated AS time_updated, riu.description AS description,
CONCAT(u.last_name, ", ", u.first_name, (CASE WHEN ISNULL(u.middle_initial) then "" ELSE CONCAT(" ", u.middle_initial) END)) AS updated_by
FROM rit_item_updates riu INNER JOIN eap_users u ON u.user_id = riu.updated_by

-- VIEW: eap_conversations
DROP VIEW eap_messages_v;

CREATE VIEW eap_messages_v AS
SELECT uc.conversation_id AS conversation_id, m.message_id AS message_id, uc.user_id AS user_id, c.subject AS subject, uc.time_read AS time_read, m.time_created AS time_sent, c.time_archived AS time_archived,
m.from_user AS from_user, CONCAT(u1.last_name, ", ", u1.first_name, (CASE WHEN ISNULL(u1.middle_initial) THEN "" ELSE CONCAT(" ", u1.middle_initial) END)) AS from_user_name,
m.to_users AS to_users, (CASE WHEN ISNULL(u2.username) THEN m.to_users ELSE (CONCAT(u2.last_name, ", ", u2.first_name, (CASE WHEN ISNULL(u2.middle_initial) THEN "" ELSE CONCAT(" ", u2.middle_initial) END))) END) AS to_users_name
FROM eap_user_conversations uc
INNER JOIN eap_messages m ON uc.conversation_id=m.conversation_id
INNER JOIN eap_conversations c ON uc.conversation_id=c.conversation_id
INNER JOIN eap_users u1 ON u1.username=m.from_user
LEFT JOIN eap_users u2 ON u2.username=m.to_users
