
CREATE DATABASE oldmsi CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE USER 'oldmsi'@'localhost' IDENTIFIED BY 'oldmsi';
GRANT ALL PRIVILEGES ON *.* TO 'oldmsi'@'localhost' WITH GRANT OPTION;
CREATE USER 'oldmsi'@'%' IDENTIFIED BY 'oldmsi';
GRANT ALL PRIVILEGES ON *.* TO 'oldmsi'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

CREATE TABLE `tweet` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `messageId` int(11) NOT NULL,
  `twitterId` binary(64) NOT NULL,
  `text` varchar(200) NOT NULL,
  `validFrom` datetime NOT NULL,
  `validTo` datetime DEFAULT NULL,

  PRIMARY KEY (`id`)
);
