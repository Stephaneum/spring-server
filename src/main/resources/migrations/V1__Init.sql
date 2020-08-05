CREATE TABLE code (
    id INT NOT NULL AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL,
    role INT NOT NULL,
    used BIT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE school_class (
  id INT NOT NULL AUTO_INCREMENT,
  grade INT NOT NULL,
  suffix VARCHAR(3) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user (
  id INT NOT NULL AUTO_INCREMENT,
  code_id INT,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  school_class_id INT,
  gender TINYINT,
  email VARCHAR(100),
  password VARCHAR(255),
  password_forgot_code VARCHAR(32),
  storage INT,
  banned BIT,
  manage_posts BIT,
  create_groups BIT,
  rubrik_erstellen BIT,
  manage_plans BIT,
  last_online datetime,
  PRIMARY KEY (id),
  CONSTRAINT fk_user_code FOREIGN KEY (code_id) REFERENCES code (id) ON DELETE CASCADE,
  CONSTRAINT fk_user_class FOREIGN KEY (school_class_id) REFERENCES school_class (id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- groups

CREATE TABLE projekt (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100),
    leiter_nutzer_id INT,
    akzeptiert BIT,
    chat BIT,
    lehrerchat BIT,
    `generated` BIT NOT NULL,
    parent_id INT,
    show_board_first BIT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_group_parent FOREIGN KEY (parent_id) REFERENCES projekt (id) ON DELETE CASCADE,
    CONSTRAINT fk_group_user FOREIGN KEY (leiter_nutzer_id) REFERENCES user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE nachricht (
    id INT NOT NULL AUTO_INCREMENT,
    nutzer_id INT,
    string TEXT,
    projekt_id INT,
    lehrerchat BIT,
    datum DATETIME DEFAULT CURRENT_TIMESTAMP,
    school_class_id INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_message_class FOREIGN KEY (school_class_id) REFERENCES school_class (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_user FOREIGN KEY (nutzer_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_group FOREIGN KEY (projekt_id) REFERENCES projekt (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE nutzer_projekt (
    projekt_id INT,
    nutzer_id INT,
    betreuer BIT,
    akzeptiert BIT,
    id INT NOT NULL AUTO_INCREMENT,
    chat BIT NOT NULL DEFAULT 1,
    write_board BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_usergroup_group FOREIGN KEY (projekt_id) REFERENCES projekt (id) ON DELETE CASCADE,
    CONSTRAINT fk_usergroup_user FOREIGN KEY (nutzer_id) REFERENCES user (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- cloud

CREATE TABLE ordner (
    id INT NOT NULL AUTO_INCREMENT,
    name TEXT,
    eigentum INT,
    projekt_id INT,
    klasse_id INT,
    lehrerchat BIT,
    parent INT,
    locked BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_folder_class FOREIGN KEY (klasse_id) REFERENCES school_class (id) ON DELETE CASCADE,
    CONSTRAINT fk_folder_group FOREIGN KEY (projekt_id) REFERENCES projekt (id) ON DELETE CASCADE,
    CONSTRAINT fk_folder_user FOREIGN KEY (eigentum) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_folder_parent FOREIGN KEY (parent) REFERENCES ordner (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE datei (
    id INT NOT NULL AUTO_INCREMENT,
    nutzer_id INT,
    pfad VARCHAR(1024),
    projekt_id INT,
    klasse_id INT,
    datum DATETIME DEFAULT CURRENT_TIMESTAMP,
    size INT,
    mime_type VARCHAR(255),
    public BIT,
    lehrerchat BIT,
    ordner_id INT,
    PRIMARY KEY (id),
    CONSTRAINT fk_file_user FOREIGN KEY (nutzer_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_file_group FOREIGN KEY (projekt_id) REFERENCES projekt (id) ON DELETE CASCADE,
    CONSTRAINT fk_file_class FOREIGN KEY (klasse_id) REFERENCES school_class (id) ON DELETE CASCADE,
    CONSTRAINT fk_file_folder FOREIGN KEY (ordner_id) REFERENCES ordner (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- group board

CREATE TABLE group_board (
    id INT NOT NULL AUTO_INCREMENT,
    board_index INT NOT NULL,
    last_update DATETIME NOT NULL,
    group_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_groupboard_group FOREIGN KEY (group_id) REFERENCES projekt (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE board_area (
    id int(11) NOT NULL AUTO_INCREMENT,
    board_id int(11) NOT NULL,
    x int(11) NOT NULL,
    y int(11) NOT NULL,
    width int(11) NOT NULL,
    height int(11) NOT NULL,
    text varchar(255),
    file_id int(11),
    type varchar(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_boardarea_board FOREIGN KEY (board_id) REFERENCES group_board (id) ON DELETE CASCADE,
    CONSTRAINT fk_boardarea_file FOREIGN KEY (file_id) REFERENCES datei (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- cms

CREATE TABLE gruppe (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(32),
    grp_id INT,
    priory INT,
    link TEXT,
    rubrik_leiter INT,
    datei_id INT,
    passwort CHAR(32),
    genehmigt BIT,
    PRIMARY KEY (id),
    CONSTRAINT fk_menu_file FOREIGN KEY (datei_id) REFERENCES datei (id) ON DELETE SET NULL,
    CONSTRAINT fk_menu_user FOREIGN KEY (rubrik_leiter) REFERENCES user (id) ON DELETE SET NULL,
    CONSTRAINT fk_menu_parent FOREIGN KEY (grp_id) REFERENCES gruppe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE beitrag (
    id INT NOT NULL AUTO_INCREMENT,
    nutzer_id INT,
    grp_id INT,
    titel VARCHAR(128),
    text TEXT NOT NULL,
    datum DATETIME DEFAULT CURRENT_TIMESTAMP,
    nutzer_id_update INT,
    passwort CHAR(32),
    genehmigt BIT,
    vorschau INT,
    show_autor BIT,
    layout_beitrag INT DEFAULT 0,
    layout_vorschau INT DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_user FOREIGN KEY (nutzer_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_user2 FOREIGN KEY (nutzer_id_update) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_menu FOREIGN KEY (grp_id) REFERENCES gruppe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_menu (
    id INT NOT NULL AUTO_INCREMENT,
    menu_id INT,
    user_id INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_usermenu_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    CONSTRAINT fk_usermenu_menu FOREIGN KEY (menu_id) REFERENCES gruppe (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE datei_beitrag (
    datei_id int(11),
    beitrag_id int(11),
    id int(11) NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id),
    CONSTRAINT fk_filepost_file FOREIGN KEY (datei_id) REFERENCES datei (id) ON DELETE CASCADE,
    CONSTRAINT fk_filepost_post FOREIGN KEY (beitrag_id) REFERENCES beitrag (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- stats

CREATE TABLE stats_tage (
    id INT NOT NULL AUTO_INCREMENT,
    datum DATE,
    anzahl INT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stats_stunden (
    idx INT NOT NULL,
    uhrzeit TINYINT,
    PRIMARY KEY (idx)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stats_cloud (
    datum DATETIME DEFAULT CURRENT_TIMESTAMP,
    size INT,
    id INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- other

CREATE TABLE blackboard (
    id INT NOT NULL AUTO_INCREMENT,
    duration INT NOT NULL,
    last_update DATETIME NOT NULL,
    `order` INT NOT NULL,
    type VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    visible BIT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE slider (
    idx INT,
    path VARCHAR(1024),
    title VARCHAR(256),
    sub VARCHAR(256),
    direction VARCHAR(64),
    id INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE log (
    datum DATETIME DEFAULT CURRENT_TIMESTAMP,
    typ INT,
    ereignis TEXT,
    id INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE static (
    id INT NOT NULL AUTO_INCREMENT,
    mode VARCHAR(255) NOT NULL,
    path VARCHAR(1024) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE konfig (
    variable VARCHAR(64),
    wert TEXT,
    id INT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;