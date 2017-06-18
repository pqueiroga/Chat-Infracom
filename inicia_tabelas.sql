USE `amizade`;
CREATE TABLE IF NOT EXISTS `users` (
  `username` VARCHAR(20) NOT NULL,
  `salt` VARCHAR(32) NOT NULL,
  `password` VARCHAR(128) NOT NULL,
  PRIMARY KEY(`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `relacao` (
  `username_one` VARCHAR(20) NOT NULL,
  `username_two` VARCHAR(20) NOT NULL,
  `status` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  `action_username` VARCHAR(20) NOT NULL,
  FOREIGN KEY (`username_one`) REFERENCES users(`username`),
  FOREIGN KEY (`username_two`) REFERENCES users(`username`),
  FOREIGN KEY (`action_username`) REFERENCES users(`username`),
  PRIMARY KEY(`username_one`, `username_two`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- SELECT * FROM `users`;
-- SELECT * FROM `relacao`;
