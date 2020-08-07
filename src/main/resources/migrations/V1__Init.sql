CREATE TABLE `code` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(32) NOT NULL,
    `role` INT NOT NULL,
    `used` BIT NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `school_class` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `grade` INT NOT NULL,
  `suffix` VARCHAR(3) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code_id` INT,
  `first_name` VARCHAR(100),
  `last_name` VARCHAR(100),
  `school_class_id` INT,
  `gender` TINYINT,
  `email` VARCHAR(100),
  `password` VARCHAR(255),
  `password_forgot_code` VARCHAR(32),
  `storage` INT,
  `banned` BIT,
  `manage_posts` BIT,
  `create_groups` BIT,
  `rubrik_erstellen` BIT,
  `manage_plans` BIT,
  `last_online` datetime,
  PRIMARY KEY (`id`),
  CONSTRAINT fk_user_code FOREIGN KEY (`code_id`) REFERENCES `code` (`id`) ON DELETE CASCADE,
  CONSTRAINT fk_user_class FOREIGN KEY (`school_class_id`) REFERENCES `school_class` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- groups

CREATE TABLE `group` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `leader_id` INT NOT NULL,
    `accepted` BIT NOT NULL,
    `chat` BIT NOT NULL,
    `lehrerchat` BIT NOT NULL,
    `generated` BIT NOT NULL,
    `parent_id` INT,
    `show_board_first` BIT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_group_parent FOREIGN KEY (`parent_id`) REFERENCES `group` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_group_user FOREIGN KEY (`leader_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `message` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `group_id` INT NOT NULL,
    `text` TEXT NOT NULL,
    `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_message_user FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_message_group FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_group` (
    `group_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `teacher` BIT NOT NULL,
    `accepted` BIT NOT NULL,
    `id` INT NOT NULL AUTO_INCREMENT,
    `chat` BIT NOT NULL DEFAULT 1,
    `write_board` BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_usergroup_group FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_usergroup_user FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- cloud

CREATE TABLE `folder` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` TEXT NOT NULL,
    `user_id` INT,
    `group_id` INT,
    `klasse_id` INT,
    `lehrerchat` BIT,
    `parent_id` INT,
    `locked` BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_folder_class FOREIGN KEY (`klasse_id`) REFERENCES `school_class` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_folder_group FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_folder_user FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_folder_parent FOREIGN KEY (`parent_id`) REFERENCES `folder` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `file` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT,
    `path` VARCHAR(1024),
    `group_id` INT,
    `klasse_id` INT,
    `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `size` INT,
    `mime` VARCHAR(255),
    `public` BIT NOT NULL,
    `lehrerchat` BIT,
    `folder_id` INT,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_file_user FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_file_group FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_file_class FOREIGN KEY (`klasse_id`) REFERENCES `school_class` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_file_folder FOREIGN KEY (`folder_id`) REFERENCES `folder` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- group board

CREATE TABLE `group_board` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `board_index` INT NOT NULL,
    `last_update` DATETIME NOT NULL,
    `group_id` INT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_groupboard_group FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `board_area` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `board_id` INT NOT NULL,
    `x` INT NOT NULL,
    `y` INT NOT NULL,
    `width` INT NOT NULL,
    `height` INT NOT NULL,
    `text` VARCHAR(255),
    `file_id` INT,
    `type` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_boardarea_board FOREIGN KEY (`board_id`) REFERENCES `group_board` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_boardarea_file FOREIGN KEY (`file_id`) REFERENCES `file` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- cms

CREATE TABLE `menu` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(32) NOT NULL,
    `parent_id` INT,
    `priority` INT NOT NULL,
    `link` TEXT,
    `rubrik_leiter` INT,
    `datei_id` INT,
    `password` CHAR(32),
    `approved` BIT,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_menu_file FOREIGN KEY (`datei_id`) REFERENCES `file` (`id`) ON DELETE SET NULL,
    CONSTRAINT fk_menu_user FOREIGN KEY (`rubrik_leiter`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT fk_menu_parent FOREIGN KEY (`parent_id`) REFERENCES `menu` (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `post` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT,
    `menu_id` INT,
    `title` VARCHAR(128) NOT NULL,
    `content` TEXT,
    `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `user_update_id` INT,
    `password` CHAR(32),
    `approved` BIT NOT NULL,
    `preview` INT NOT NULL,
    `layout_post` INT NOT NULL DEFAULT 0,
    `layout_preview` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_post_user FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_post_user_update FOREIGN KEY (`user_update_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_post_menu FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_menu` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `menu_id` INT,
    `user_id` INT NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_usermenu_user FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_usermenu_menu FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `file_post` (
    `file_id` INT NOT NULL,
    `post_id` INT NOT NULL,
    `id` INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`id`),
    CONSTRAINT fk_filepost_file FOREIGN KEY (`file_id`) REFERENCES `file` (`id`) ON DELETE CASCADE,
    CONSTRAINT fk_filepost_post FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- stats

CREATE TABLE `stats_day` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `date` DATE NOT NULL,
    `count` INT NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stats_hour` (
    `idx` INT NOT NULL,
    `hour` TINYINT NOT NULL,
    PRIMARY KEY (`idx`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `stats_cloud` (
    `timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `size` INT NOT NULL,
    `id` INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- other

CREATE TABLE `blackboard` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `duration` INT NOT NULL,
    `last_update` DATETIME NOT NULL,
    `order` INT NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    `visible` BIT NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `slider` (
    `index` INT NOT NULL,
    `path` VARCHAR(1024) NOT NULL,
    `title` VARCHAR(256),
    `sub_title` VARCHAR(256),
    `direction` VARCHAR(64) NOT NULL,
    `id` INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `log` (
    `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `type` INT NOT NULL,
    `info` TEXT NOT NULL,
    `id` INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `static` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `path` VARCHAR(1024) NOT NULL,
    `mode` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `config` (
    `key` VARCHAR(64),
    `value` TEXT,
    `id` INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;