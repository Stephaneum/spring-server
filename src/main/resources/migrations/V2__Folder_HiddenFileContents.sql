ALTER TABLE `folder`
    ADD `hidden_file_contents` BIT NOT NULL DEFAULT 0 AFTER `locked`;