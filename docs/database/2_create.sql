-- Created by Redgate Data Modeler (https://datamodeler.redgate-platform.com)
-- Last modification date: 2026-09-21 12:42:39.07

-- Kõik tabelid luuakse testi_platvorm skeemi
SET search_path TO testi_platvorm;

-- tables
-- Table: ai_question

SET TIME ZONE 'Europe/Tallinn';

CREATE TABLE ai_question (
                             id serial  NOT NULL,
                             title varchar(100)  NOT NULL,
                             description varchar(1000)  NOT NULL,
                             competence_id int  NOT NULL,
                             competence_level_id int  NOT NULL,
                             question_type_id int  NOT NULL,
                             status char(1)  NOT NULL,
                             created_at timestamptz  NOT NULL,
                             created_by int  NOT NULL,
                             updated_at timestamptz  NOT NULL,
                             score int  NULL,
                             feedback varchar(255)  NULL,
                             is_good boolean  NULL,
                             CONSTRAINT ai_question_pk PRIMARY KEY (id)
);

-- Table: ai_question_answer
CREATE TABLE ai_question_answer (
                                    id serial  NOT NULL,
                                    ai_question_id int  NOT NULL,
                                    is_correct boolean  NULL,
                                    correct_position int  NULL,
                                    status char(1)  NOT NULL,
                                    answer_text varchar(255)  NOT NULL,
                                    ai_question_answer_id int  NULL,
                                    score int  NOT NULL,
                                    feedback int  NOT NULL,
                                    CONSTRAINT ai_question_answer_pk PRIMARY KEY (id)
);

-- Table: competence
CREATE TABLE competence (
                            id serial  NOT NULL,
                            name varchar(255)  NOT NULL,
                            short_description varchar(255)  NOT NULL,
                            description text  NOT NULL,
                            updated_at timestamptz  NOT NULL,
                            doc_url varchar(500)  NOT NULL,
                            doc_filename varchar(255)  NOT NULL,
                            status char(1)  NOT NULL,
                            created_by int  NOT NULL,
                            created_at timestamptz  NOT NULL,
                            CONSTRAINT competence_ak_1 UNIQUE (name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                            CONSTRAINT competence_pk PRIMARY KEY (id)
);

-- Table: competence_file
CREATE TABLE competence_file (
                                 id serial  NOT NULL,
                                 competence_id int  NOT NULL,
                                 file_name varchar(255)  NOT NULL,
                                 file_type varchar(50)  NOT NULL,
                                 file_data bytea  NOT NULL,
                                 CONSTRAINT competence_file_pk PRIMARY KEY (id)
);

-- Table: competence_level
CREATE TABLE competence_level (
                                  id serial  NOT NULL,
                                  competence_id int  NOT NULL,
                                  level_id int  NOT NULL,
                                  status char(1)  NOT NULL,
                                  CONSTRAINT competence_level_pk PRIMARY KEY (id)
);

-- Table: group
CREATE TABLE "group" (
                         id serial  NOT NULL,
                         name varchar(255)  NOT NULL,
                         status char(1)  NOT NULL,
                         created_at timestamptz  NOT NULL,
                         updated_at timestamptz  NOT NULL,
                         CONSTRAINT group_ak_1 UNIQUE (name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                         CONSTRAINT group_pk PRIMARY KEY (id)
);

-- Table: group_manager
CREATE TABLE group_manager (
                               id serial  NOT NULL,
                               group_id int  NOT NULL,
                               user_id int  NOT NULL,
                               status char(1)  NOT NULL,
                               CONSTRAINT group_manager_pk PRIMARY KEY (id)
);

-- Table: group_member
CREATE TABLE group_member (
                              id serial  NOT NULL,
                              group_id int  NOT NULL,
                              user_id int  NOT NULL,
                              added_by int  NOT NULL,
                              created_at timestamptz  NOT NULL,
                              updated_at timestamptz  NOT NULL,
                              CONSTRAINT group_member_pk PRIMARY KEY (id)
);

-- Table: invitation
CREATE TABLE invitation (
                            id serial  NOT NULL,
                            invited_by int  NOT NULL,
                            user_id int  NOT NULL,
                            token varchar(255)  NOT NULL,
                            created_at timestamptz  NOT NULL,
                            expires_at timestamptz  NOT NULL,
                            status char(1)  NOT NULL,
                            CONSTRAINT invitation_token UNIQUE (token) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                            CONSTRAINT invitation_pk PRIMARY KEY (id)
);

-- Table: level
CREATE TABLE "level" (
                         id serial  NOT NULL,
                         "level" int  NOT NULL,
                         name varchar(255)  NOT NULL,
                         description varchar(255)  NULL,
                         CONSTRAINT level_ak_1 UNIQUE (name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                         CONSTRAINT level_pk PRIMARY KEY (id)
);

-- Table: profile
CREATE TABLE profile (
                         id serial  NOT NULL,
                         user_id int  NOT NULL,
                         first_name varchar(255)  NOT NULL,
                         last_name varchar(255)  NOT NULL,
                         phone_number varchar(20)  NULL,
                         created_at timestamptz  NOT NULL,
                         updated_at timestamptz  NOT NULL,
                         CONSTRAINT profile_pk PRIMARY KEY (id)
);

-- Table: question
CREATE TABLE question (
                          id serial  NOT NULL,
                          competence_id int  NOT NULL,
                          competence_level_id int  NOT NULL,
                          title varchar(100)  NOT NULL,
                          description varchar(1000)  NOT NULL,
                          question_type_id int  NOT NULL,
                          score int  NOT NULL,
                          status char(1)  NOT NULL,
                          created_at timestamptz  NOT NULL,
                          created_by int  NOT NULL,
                          updated_at timestamptz  NOT NULL,
                          CONSTRAINT question_pk PRIMARY KEY (id)
);

-- Table: question_answer
CREATE TABLE question_answer (
                                 id serial  NOT NULL,
                                 question_id int  NOT NULL,
                                 answer_text varchar(255)  NOT NULL,
                                 correct_choice boolean  NULL,
                                 correct_position int  NULL,
                                 paired_answer_id int  NULL,
                                 status char(1)  NOT NULL,
                                 CONSTRAINT question_answer_pk PRIMARY KEY (id)
);

-- Table: question_type
CREATE TABLE question_type (
                               id serial  NOT NULL,
                               name varchar(20)  NOT NULL,
                               CONSTRAINT question_type_pk PRIMARY KEY (id)
);

-- Table: result
CREATE TABLE result (
                        id serial  NOT NULL,
                        user_test_id int  NOT NULL,
                        status char(1)  NOT NULL,
                        max_score int  NOT NULL,
                        score_total int  NOT NULL,
                        completed_at timestamptz  NOT NULL,
                        started_at timestamptz  NOT NULL,
                        total_questions int  NOT NULL,
                        questions_answered int  NOT NULL,
                        CONSTRAINT result_pk PRIMARY KEY (id)
);

-- Table: role
CREATE TABLE role (
                      id serial  NOT NULL,
                      name varchar(255)  NOT NULL,
                      CONSTRAINT role_ak_1 UNIQUE (name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                      CONSTRAINT role_pk PRIMARY KEY (id)
);

-- Table: test
CREATE TABLE test (
                      id serial  NOT NULL,
                      competence_id int  NOT NULL,
                      competence_level_id int  NOT NULL,
                      name varchar(255)  NOT NULL,
                      short_description varchar(150)  NOT NULL,
                      description text  NOT NULL,
                      is_timed boolean  NOT NULL,
                      timer_min int  NULL,
                      pass_percent decimal(5,2)  NOT NULL,
                      round_score_up boolean  NOT NULL,
                      status char(1)  NOT NULL,
                      created_by int  NOT NULL,
                      created_at timestamptz  NOT NULL,
                      updated_at timestamptz  NOT NULL,
                      CONSTRAINT test_pk PRIMARY KEY (id)
);

-- Table: test_question
CREATE TABLE test_question (
                               id serial  NOT NULL,
                               test_id int  NOT NULL,
                               question_id int  NOT NULL,
                               position int  NOT NULL,
                               added_by int  NOT NULL,
                               created_at timestamptz  NOT NULL,
                               updated_at timestamptz  NOT NULL,
                               CONSTRAINT test_question_pk PRIMARY KEY (id)
);

-- Table: test_question_answer
CREATE TABLE test_question_answer (
                                      id serial  NOT NULL,
                                      test_question_result_id int  NOT NULL,
                                      question_answer_id int  NOT NULL,
                                      correct_choice_user_answer boolean  NULL,
                                      correct_position_user_answer int  NULL,
                                      paired_answer_id_user_answer int  NULL,
                                      CONSTRAINT test_question_answer_pk PRIMARY KEY (id)
);

-- Table: test_question_result
CREATE TABLE test_question_result (
                                      id serial  NOT NULL,
                                      result_id int  NOT NULL,
                                      question_id int  NOT NULL,
                                      correct_answer boolean  NOT NULL,
                                      status varchar(2)  NOT NULL,
                                      sequence_number int  NOT NULL,
                                      CONSTRAINT test_question_result_pk PRIMARY KEY (id)
);

-- Table: user
CREATE TABLE "user" (
                        id serial  NOT NULL,
                        email varchar(255)  NOT NULL,
                        password_hash varchar(255)  NULL,
                        role_id int  NOT NULL,
                        status char(1)  NOT NULL,
                        created_at timestamptz  NOT NULL,
                        updated_at timestamptz  NOT NULL,
                        CONSTRAINT user_ak_1 UNIQUE (email) NOT DEFERRABLE  INITIALLY IMMEDIATE,
                        CONSTRAINT user_pk PRIMARY KEY (id)
);

-- Table: user_test
CREATE TABLE user_test (
                           id serial  NOT NULL,
                           test_id int  NOT NULL,
                           user_id int  NOT NULL,
                           opens_at timestamptz  NOT NULL,
                           closes_at timestamptz  NOT NULL,
                           status char(1)  NOT NULL,
                           group_id int  NOT NULL,
                           assigned_by int  NOT NULL,
                           created_at timestamptz  NOT NULL,
                           CONSTRAINT assignment_pk PRIMARY KEY (id)
);

-- foreign keys
-- Reference: ai_question_answer_ai_question (table: ai_question_answer)
ALTER TABLE ai_question_answer ADD CONSTRAINT ai_question_answer_ai_question
    FOREIGN KEY (ai_question_id)
        REFERENCES ai_question (id)
        ON DELETE  CASCADE
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: ai_question_answer_ai_question_answer (table: ai_question_answer)
ALTER TABLE ai_question_answer ADD CONSTRAINT ai_question_answer_ai_question_answer
    FOREIGN KEY (ai_question_answer_id)
        REFERENCES ai_question_answer (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: ai_question_competence (table: ai_question)
ALTER TABLE ai_question ADD CONSTRAINT ai_question_competence
    FOREIGN KEY (competence_id)
        REFERENCES competence (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: ai_question_competence_level (table: ai_question)
ALTER TABLE ai_question ADD CONSTRAINT ai_question_competence_level
    FOREIGN KEY (competence_level_id)
        REFERENCES competence_level (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: assigning_user (table: user_test)
ALTER TABLE user_test ADD CONSTRAINT assigning_user
    FOREIGN KEY (assigned_by)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: assignment_test (table: user_test)
ALTER TABLE user_test ADD CONSTRAINT assignment_test
    FOREIGN KEY (test_id)
        REFERENCES test (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: competence_file_competence (table: competence_file)
ALTER TABLE competence_file ADD CONSTRAINT competence_file_competence
    FOREIGN KEY (competence_id)
        REFERENCES competence (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: competence_level_competence (table: competence_level)
ALTER TABLE competence_level ADD CONSTRAINT competence_level_competence
    FOREIGN KEY (competence_id)
        REFERENCES competence (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: competence_level_level (table: competence_level)
ALTER TABLE competence_level ADD CONSTRAINT competence_level_level
    FOREIGN KEY (level_id)
        REFERENCES "level" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: competence_user (table: competence)
ALTER TABLE competence ADD CONSTRAINT competence_user
    FOREIGN KEY (created_by)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: group_manager_group (table: group_manager)
ALTER TABLE group_manager ADD CONSTRAINT group_manager_group
    FOREIGN KEY (group_id)
        REFERENCES "group" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: group_manager_user (table: group_manager)
ALTER TABLE group_manager ADD CONSTRAINT group_manager_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: group_member_group (table: group_member)
ALTER TABLE group_member ADD CONSTRAINT group_member_group
    FOREIGN KEY (group_id)
        REFERENCES "group" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: invited_by_user (table: invitation)
ALTER TABLE invitation ADD CONSTRAINT invited_by_user
    FOREIGN KEY (invited_by)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: invited_user (table: invitation)
ALTER TABLE invitation ADD CONSTRAINT invited_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: manager_user (table: group_member)
ALTER TABLE group_member ADD CONSTRAINT manager_user
    FOREIGN KEY (added_by)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: member_user (table: group_member)
ALTER TABLE group_member ADD CONSTRAINT member_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: profile_user (table: profile)
ALTER TABLE profile ADD CONSTRAINT profile_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: question_answer_question_answer (table: question_answer)
ALTER TABLE question_answer ADD CONSTRAINT question_answer_question_answer
    FOREIGN KEY (paired_answer_id)
        REFERENCES question_answer (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: question_competence (table: question)
ALTER TABLE question ADD CONSTRAINT question_competence
    FOREIGN KEY (competence_id)
        REFERENCES competence (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: question_competence_level (table: question)
ALTER TABLE question ADD CONSTRAINT question_competence_level
    FOREIGN KEY (competence_level_id)
        REFERENCES competence_level (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: question_id_solution (table: question_answer)
ALTER TABLE question_answer ADD CONSTRAINT question_id_solution
    FOREIGN KEY (question_id)
        REFERENCES question (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: question_question_type (table: question)
ALTER TABLE question ADD CONSTRAINT question_question_type
    FOREIGN KEY (question_type_id)
        REFERENCES question_type (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: question_user (table: question)
ALTER TABLE question ADD CONSTRAINT question_user
    FOREIGN KEY (created_by)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: result_user_test_id (table: result)
ALTER TABLE result ADD CONSTRAINT result_user_test_id
    FOREIGN KEY (user_test_id)
        REFERENCES user_test (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_competence (table: test)
ALTER TABLE test ADD CONSTRAINT test_competence
    FOREIGN KEY (competence_id)
        REFERENCES competence (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_competence_level (table: test)
ALTER TABLE test ADD CONSTRAINT test_competence_level
    FOREIGN KEY (competence_level_id)
        REFERENCES competence_level (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_question_answer_paired_question_answer (table: test_question_answer)
ALTER TABLE test_question_answer ADD CONSTRAINT test_question_answer_paired_question_answer
    FOREIGN KEY (paired_answer_id_user_answer)
        REFERENCES question_answer (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_question_answer_question_answer (table: test_question_answer)
ALTER TABLE test_question_answer ADD CONSTRAINT test_question_answer_question_answer
    FOREIGN KEY (question_answer_id)
        REFERENCES question_answer (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_question_answer_test_question_result (table: test_question_answer)
ALTER TABLE test_question_answer ADD CONSTRAINT test_question_answer_test_question_result
    FOREIGN KEY (test_question_result_id)
        REFERENCES test_question_result (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_question_question (table: test_question)
ALTER TABLE test_question ADD CONSTRAINT test_question_question
    FOREIGN KEY (question_id)
        REFERENCES question (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_question_result_question (table: test_question_result)
ALTER TABLE test_question_result ADD CONSTRAINT test_question_result_question
    FOREIGN KEY (question_id)
        REFERENCES question (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_question_result_result (table: test_question_result)
ALTER TABLE test_question_result ADD CONSTRAINT test_question_result_result
    FOREIGN KEY (result_id)
        REFERENCES result (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_question_test (table: test_question)
ALTER TABLE test_question ADD CONSTRAINT test_question_test
    FOREIGN KEY (test_id)
        REFERENCES test (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_question_user (table: test_question)
ALTER TABLE test_question ADD CONSTRAINT test_question_user
    FOREIGN KEY (added_by)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: test_user (table: test)
ALTER TABLE test ADD CONSTRAINT test_user
    FOREIGN KEY (created_by)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_role (table: user)
ALTER TABLE "user" ADD CONSTRAINT user_role
    FOREIGN KEY (role_id)
        REFERENCES role (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_test_group (table: user_test)
ALTER TABLE user_test ADD CONSTRAINT user_test_group
    FOREIGN KEY (group_id)
        REFERENCES "group" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- Reference: user_test_user (table: user_test)
ALTER TABLE user_test ADD CONSTRAINT user_test_user
    FOREIGN KEY (user_id)
        REFERENCES "user" (id)
        NOT DEFERRABLE
            INITIALLY IMMEDIATE
;

-- End of file.

