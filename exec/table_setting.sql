-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema project3
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `project3` DEFAULT CHARACTER SET utf8mb3 ;
USE `project3` ;

-- -----------------------------------------------------
-- Table `project3`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `project3`.`user` (
  `userSeq` INT NOT NULL AUTO_INCREMENT,
  `userId` VARCHAR(45) NOT NULL,
  `userPassword` VARCHAR(256) NOT NULL,
  `userNickname` VARCHAR(45) NOT NULL,
  `userProfile` VARCHAR(256) NULL DEFAULT NULL,
  `userGitUsername` VARCHAR(45) NULL DEFAULT NULL,
  `userGitToken` VARCHAR(100) NULL DEFAULT NULL,
  `userProvider` VARCHAR(50) NULL DEFAULT NULL,
  `userRefresh` VARCHAR(500) NULL DEFAULT NULL,
  PRIMARY KEY (`userSeq`),
  UNIQUE INDEX `userSeq_UNIQUE` (`userSeq` ASC) VISIBLE,
  UNIQUE INDEX `userUid_UNIQUE` (`userId` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 31
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `project3`.`forum`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `project3`.`forum` (
  `postSeq` INT NOT NULL AUTO_INCREMENT,
  `userSeq` INT NOT NULL,
  `postTitle` VARCHAR(45) NOT NULL,
  `postContent` VARCHAR(500) NOT NULL,
  `postCreatedAt` DATETIME NOT NULL,
  `postUpdatedAt` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`postSeq`),
  UNIQUE INDEX `postSeq_UNIQUE` (`postSeq` ASC) VISIBLE,
  INDEX `user_forum_fk_idx` (`userSeq` ASC) VISIBLE,
  CONSTRAINT `user_forum_fk`
    FOREIGN KEY (`userSeq`)
    REFERENCES `project3`.`user` (`userSeq`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `project3`.`team`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `project3`.`team` (
  `teamSeq` INT NOT NULL AUTO_INCREMENT,
  `teamLeaderSeq` INT NOT NULL,
  `teamName` VARCHAR(45) NOT NULL,
  `teamGit` VARCHAR(256) NULL DEFAULT NULL,
  `teamType` INT NULL DEFAULT NULL,
  PRIMARY KEY (`teamSeq`),
  INDEX `user_team_fk_idx` (`teamLeaderSeq` ASC) VISIBLE,
  CONSTRAINT `user_team_fk`
    FOREIGN KEY (`teamLeaderSeq`)
    REFERENCES `project3`.`user` (`userSeq`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 185
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `project3`.`member`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `project3`.`member` (
  `memberSeq` INT NOT NULL AUTO_INCREMENT,
  `userSeq` INT NOT NULL,
  `teamSeq` INT NOT NULL,
  `settings` TEXT NOT NULL,
  PRIMARY KEY (`memberSeq`),
  INDEX `user_member_fk_idx` (`userSeq` ASC) VISIBLE,
  INDEX `team_member_fk_idx` (`teamSeq` ASC) VISIBLE,
  CONSTRAINT `team_member_fk`
    FOREIGN KEY (`teamSeq`)
    REFERENCES `project3`.`team` (`teamSeq`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `user_member_fk`
    FOREIGN KEY (`userSeq`)
    REFERENCES `project3`.`user` (`userSeq`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 228
DEFAULT CHARACTER SET = utf8mb3;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
