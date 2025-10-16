/* ==========================================================
   BAG2BAG — clean, consistent schema with seed data
   Charset: utf8mb4 / Collation: utf8mb4_general_ci
   MySQL 8.0+
   ========================================================== */

CREATE DATABASE IF NOT EXISTS db_school_trade
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
USE db_school_trade;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

/* -----------------------------
   Drop (safe order not required
   because FK checks are off)
   ----------------------------- */
DROP TABLE IF EXISTS sh_favorite;
DROP TABLE IF EXISTS sh_message;
DROP TABLE IF EXISTS sh_order;
DROP TABLE IF EXISTS sh_idle_item;
DROP TABLE IF EXISTS sh_user;
DROP TABLE IF EXISTS sh_admin;
DROP TABLE IF EXISTS sh_trade;

DROP TABLE IF EXISTS idle_item_sub_label;
DROP TABLE IF EXISTS idle_item_label;
DROP TABLE IF EXISTS idle_item_new;
DROP TABLE IF EXISTS idle_item_trade;
DROP TABLE IF EXISTS idle_item_status;

/* =============================
   Dictionary tables (seeded)
   ============================= */

-- 狀態字典（0=删除,1=已发布,2=已下架）
CREATE TABLE idle_item_status (
                                  id        TINYINT      NOT NULL PRIMARY KEY,
                                  code      VARCHAR(32)  NOT NULL UNIQUE,   -- 'deleted','published','off_shelf'
                                  label     VARCHAR(32)  NOT NULL,          -- 顯示文字
                                  is_active TINYINT(1)   NOT NULL DEFAULT 1,
                                  CHECK (is_active IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 交易方式字典（1=出售,2=交換）
CREATE TABLE idle_item_trade (
                                 id        TINYINT      NOT NULL PRIMARY KEY,
                                 code      VARCHAR(32)  NOT NULL UNIQUE,   -- 'sell','exchange'
                                 label     VARCHAR(32)  NOT NULL,
                                 is_active TINYINT(1)   NOT NULL DEFAULT 1,
                                 CHECK (is_active IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 新舊程度字典（1=全新,2=九成新,3=二手）
CREATE TABLE idle_item_new (
                               id        TINYINT      NOT NULL PRIMARY KEY,
                               code      VARCHAR(32)  NOT NULL UNIQUE,   -- 'brand_new','like_new','used'
                               label     VARCHAR(32)  NOT NULL,
                               is_active TINYINT(1)   NOT NULL DEFAULT 1,
                               CHECK (is_active IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 類別字典（與主表 idle_label=INT 對齊）
CREATE TABLE idle_item_label (
                                 id        INT          NOT NULL PRIMARY KEY,
                                 code      VARCHAR(32)  NOT NULL UNIQUE,   -- 'univ','tech','clothes','sports','living','other'
                                 label     VARCHAR(64)  NOT NULL,
                                 sort_no   INT          NOT NULL DEFAULT 0,
                                 is_active TINYINT(1)   NOT NULL DEFAULT 1,
                                 CHECK (is_active IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 子類別（如需要）
CREATE TABLE idle_item_sub_label (
                                     id        INT          NOT NULL PRIMARY KEY,
                                     parent_id INT          NOT NULL,          -- 指向 idle_item_label.id
                                     code      VARCHAR(32)  NOT NULL UNIQUE,
                                     label     VARCHAR(64)  NOT NULL,
                                     sort_no   INT          NOT NULL DEFAULT 0,
                                     is_active TINYINT(1)   NOT NULL DEFAULT 1,
                                     CHECK (is_active IN (0,1)),
                                     CONSTRAINT fk_sublabel_parent
                                         FOREIGN KEY (parent_id) REFERENCES idle_item_label(id)
                                             ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 初始化字典資料
INSERT INTO idle_item_status (id, code, label) VALUES
                                                   (0,'deleted','刪除'),
                                                   (1,'published','已發布'),
                                                   (2,'off_shelf','已下架');

INSERT INTO idle_item_trade (id, code, label) VALUES
                                                  (1,'sell','出售'),
                                                  (2,'exchange','交換');

INSERT INTO idle_item_new (id, code, label) VALUES
                                                (1,'brand_new','全新'),
                                                (2,'like_new','九成新'),
                                                (3,'used','二手');

INSERT INTO idle_item_label (id, code, label, sort_no, is_active) VALUES
                                                                      (1,'univ','University',10,1),
                                                                      (2,'tech','Tech',20,1),
                                                                      (3,'clothes','Clothes',30,1),
                                                                      (4,'sports','Sports',40,1),
                                                                      (5,'living','Living',50,1),
                                                                      (6,'other','Other',60,1);

/* =============================
   Business tables
   ============================= */

-- 管理員
CREATE TABLE sh_admin (
                          id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
                          account_number  VARCHAR(32)  NOT NULL UNIQUE COMMENT '管理员账号',
                          admin_password  VARCHAR(128) NOT NULL COMMENT '密码(哈希或明文佔位)',
                          admin_name      VARCHAR(32)  NOT NULL COMMENT '管理员名字'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 使用者
CREATE TABLE sh_user (
                         id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
                         account_number  VARCHAR(32)  NOT NULL UNIQUE COMMENT '账号（手机号）',
                         Email_number    VARCHAR(128) NOT NULL COMMENT '邮箱',
                         UPI             VARCHAR(32)  NOT NULL COMMENT '如：yyan799',
                         user_password   VARCHAR(128) NOT NULL COMMENT '登录密码(哈希或明文佔位)',
                         nickname        VARCHAR(64)  NOT NULL COMMENT '昵称',
                         avatar          VARCHAR(512) NOT NULL COMMENT '头像URL',
                         sign_in_time    DATETIME     NOT NULL COMMENT '注册时间',
                         user_status     TINYINT      NOT NULL DEFAULT 0 COMMENT '状态（1代表封禁）',
                         major           VARCHAR(256)          DEFAULT NULL COMMENT '科系',
                         National        VARCHAR(256)          DEFAULT NULL COMMENT '國家',
                         degree          VARCHAR(256)          DEFAULT NULL COMMENT '學位',
                         rating          DECIMAL(3,2)          DEFAULT 0 COMMENT '評分',
                         rating_count    Int          NOT NULL DEFAULT 0 COMMENT '評分次數'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 閒置物
CREATE TABLE sh_idle_item (
                              id                   BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
                              idle_name            VARCHAR(64)   NOT NULL COMMENT '闲置物名称',
                              idle_details         VARCHAR(2048) NOT NULL COMMENT '详情',
                              picture_list         VARCHAR(1024) NOT NULL COMMENT '图集(JSON数组)',
                              idle_price           DECIMAL(10,2) NOT NULL COMMENT '价格',
                              idle_original_price  DECIMAL(10,2) NOT NULL COMMENT '原始价格',
                              idle_place           VARCHAR(32)   NOT NULL COMMENT '发货地区',
                              idle_label           INT           NOT NULL COMMENT '分類(字典 idle_item_label.id)',
                              release_time         DATETIME      NOT NULL COMMENT '发布时间',
                              idle_status          TINYINT       NOT NULL COMMENT '狀態: 0=刪除,1=已發布,2=已下架',
                              idle_trade           TINYINT       NOT NULL COMMENT '交易: 1=出售,2=交換',
                              user_id              BIGINT        NOT NULL COMMENT '用户主键id',
                              idle_new             TINYINT       NOT NULL COMMENT '新舊: 1=全新,2=九成新,3=二手',
                              KEY idx_user_id     (user_id),
                              KEY idx_idle_status (idle_status),
                              KEY idx_idle_trade  (idle_trade),
                              KEY idx_idle_new    (idle_new),
                              KEY idx_idle_label  (idle_label),
                              CONSTRAINT fk_idle_status FOREIGN KEY (idle_status) REFERENCES idle_item_status(id)
                                  ON UPDATE RESTRICT ON DELETE RESTRICT,
                              CONSTRAINT fk_idle_trade  FOREIGN KEY (idle_trade)  REFERENCES idle_item_trade(id)
                                  ON UPDATE RESTRICT ON DELETE RESTRICT,
                              CONSTRAINT fk_idle_new    FOREIGN KEY (idle_new)    REFERENCES idle_item_new(id)
                                  ON UPDATE RESTRICT ON DELETE RESTRICT,
                              CONSTRAINT fk_idle_label  FOREIGN KEY (idle_label)  REFERENCES idle_item_label(id)
                                  ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 收藏
CREATE TABLE sh_favorite (
                             id           BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键id',
                             create_time  DATETIME NOT NULL COMMENT '加入收藏的时间',
                             user_id      BIGINT   NOT NULL COMMENT '用户主键id',
                             idle_id      BIGINT   NOT NULL COMMENT '闲置物主键id',
                             UNIQUE KEY user_idle_unique (user_id, idle_id),
                             CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES sh_user(id)
                                 ON UPDATE CASCADE ON DELETE CASCADE,
                             CONSTRAINT fk_fav_idle FOREIGN KEY (idle_id) REFERENCES sh_idle_item(id)
                                 ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 留言
CREATE TABLE sh_message (
                            id           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
                            user_id      BIGINT      NOT NULL COMMENT '用户主键id',
                            idle_id      BIGINT      NOT NULL COMMENT '闲置主键id',
                            content      VARCHAR(256) NOT NULL COMMENT '留言内容',
                            create_time  DATETIME    NOT NULL COMMENT '留言时间',
                            to_user      BIGINT      NOT NULL COMMENT '所回复的用户',
                            to_message   BIGINT               DEFAULT NULL COMMENT '所回复的留言',
                            KEY idx_msg_user (user_id),
                            KEY idx_msg_idle (idle_id),
                            KEY idx_msg_to_user (to_user),
                            CONSTRAINT fk_msg_user FOREIGN KEY (user_id) REFERENCES sh_user(id)
                                ON UPDATE CASCADE ON DELETE CASCADE,
                            CONSTRAINT fk_msg_idle FOREIGN KEY (idle_id) REFERENCES sh_idle_item(id)
                                ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 訂單（此處先不對 payment_status/order_status 建字典；可後續加）
CREATE TABLE sh_order (
                          id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
                          order_number   VARCHAR(32)  NOT NULL COMMENT '订单编号',
                          user_id        BIGINT       NOT NULL COMMENT '用户主键id',
                          idle_id        BIGINT       NOT NULL COMMENT '闲置物品主键id',
                          order_price    DECIMAL(10,2) NOT NULL COMMENT '订单总价',
                          payment_status TINYINT      NOT NULL COMMENT '支付状态(0=未支付,1=已支付等)',
                          payment_way    VARCHAR(16)           DEFAULT NULL COMMENT '支付方式',
                          create_time    DATETIME     NOT NULL COMMENT '创建时间',
                          payment_time   DATETIME              DEFAULT NULL COMMENT '支付时间',
                          order_status   TINYINT      NOT NULL COMMENT '订单状态(3=完成,4=取消等)',
                          is_deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除',
                          KEY idx_order_user (user_id),
                          KEY idx_order_idle (idle_id),
                          CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES sh_user(id)
                              ON UPDATE CASCADE ON DELETE CASCADE,
                          CONSTRAINT fk_order_idle FOREIGN KEY (idle_id) REFERENCES sh_idle_item(id)
                              ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 交易主表（最小可行）
CREATE TABLE sh_trade (
                                        id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                        item_id       BIGINT       NOT NULL,                 -- 對應 sh_idle_item.id
                                        buyer_id      BIGINT       NOT NULL,                 -- 發起方（買家）
                                        seller_id     BIGINT       NOT NULL,                 -- 買賣雙方不可相同
                                        meet_time     DATETIME     NOT NULL,                 -- 見面時間
                                        meet_place    VARCHAR(255) NOT NULL,                 -- 見面地點
                                        price_cents   INT          NOT NULL,                 -- 金額（分）避免小數誤差
                                        trade_item    VARCHAR(255) NOT NULL,

-- 狀態機：0=PROPOSED(提案)，1=AGREED(同意)，2=PENDING_BOTH_CONFIRM(等待雙方確認)，
--         3=COMPLETED(完成)，4=DECLINED(拒絕)，5=CANCELLED(取消)，6=EXPIRED(超時)
    status        TINYINT      NOT NULL,

    buyer_ok      TINYINT      NOT NULL DEFAULT 0,       -- 完成卡：買家是否已確認
    seller_ok     TINYINT      NOT NULL DEFAULT 0,       -- 完成卡：賣家是否已確認
    locked        TINYINT      NOT NULL DEFAULT 0,       -- 提案被賣家接受後鎖定（前端卡片置灰）
    version       INT          NOT NULL DEFAULT 0,       -- 樂觀鎖，用於避免重複點擊/競態

    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    KEY idx_trade_item   (item_id),
    KEY idx_trade_buyer  (buyer_id),
    KEY idx_trade_seller (seller_id),
    KEY idx_trade_status (status),

    CONSTRAINT fk_trade_item   FOREIGN KEY (item_id)   REFERENCES sh_idle_item(id)
                                                                  ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_trade_buyer  FOREIGN KEY (buyer_id)  REFERENCES sh_user(id)
                                                                  ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_trade_seller FOREIGN KEY (seller_id) REFERENCES sh_user(id)
                                                                  ON UPDATE CASCADE ON DELETE RESTRICT
    );




/* =============================
   Seed data
   ============================= */

-- 管理員
INSERT INTO sh_admin (id, account_number, admin_password, admin_name) VALUES
                                                                          (1,  '11',         '123123', '超级管理员'),
                                                                          (13, '1866666666', '123456', 'admin');

-- 使用者（補 id=11,12 以符合後續示例資料的外鍵）
INSERT INTO sh_user
(id, account_number, Email_number, UPI, user_password, nickname, avatar, sign_in_time, user_status, major, National, degree, rating)
VALUES
    (11, '13800000011', 'user11@example.com', 'yyan711', 'pass11', 'user11',
     'https://example.com/a11.png', '2024-01-01 10:00:00', 0, 'CS', 'NZ', 'BSc', NULL),
    (12, '13800000012', 'user12@example.com', 'yyan799', 'pass12', 'user12',
     'https://example.com/a12.png', '2024-01-02 11:00:00', 0, 'CS', 'NZ', 'BSc', NULL);

-- 閒置物（補示例 id，以便後續收藏/留言/訂單外鍵）
INSERT INTO sh_idle_item
(id, idle_name, idle_details, picture_list, idle_price, idle_original_price, idle_place,
 idle_label, release_time, idle_status, idle_trade, user_id, idle_new)
VALUES
    (105, 'Sample Item 105', 'details', '[]', 100.00, 150.00, 'Auckland', 2, '2024-01-03 09:00:00', 1, 1, 11, 3),
    (106, 'Sample Item 106', 'details', '[]', 3000.00, 3500.00, 'Auckland', 2, '2024-01-03 10:00:00', 1, 1, 12, 2),
    (107, 'Sample Item 107', 'details', '[]',  50.00,   80.00, 'Auckland', 1, '2024-01-04 12:00:00', 1, 2, 11, 3),
    (111, 'Sample Item 111', 'details', '[]', 599.00,  699.00, 'Auckland', 2, '2024-01-05 12:30:00', 1, 1, 12, 2),
    (112, 'Sample Item 112', 'details', '[]', 120.00,  200.00, 'Auckland', 5, '2024-01-05 14:20:00', 1, 1, 12, 3);

-- 收藏（來自你原始示例）
INSERT INTO sh_favorite (id, create_time, user_id, idle_id) VALUES
    (48, '2024-01-05 14:29:40', 12, 112);

-- 留言（來自你原始示例）
INSERT INTO sh_message (id, user_id, idle_id, content, create_time, to_user, to_message) VALUES
                                                                                             (48, 11, 112, '多少钱',            '2024-01-05 14:26:48', 11, NULL),
                                                                                             (49, 11, 111, '这个可以便宜点么？', '2024-01-05 14:27:03', 11, NULL),
                                                                                             (50, 11, 111, '可以',              '2024-01-05 14:27:19', 11, 49),
                                                                                             (51, 11, 107, '东西怎么买啊？人在哪？','2024-01-06 00:32:51', 11, NULL),
                                                                                             (52, 11, 105, '东西怎么买？',      '2024-01-06 00:33:54', 11, NULL);

-- 訂單（來自你原始示例）
INSERT INTO sh_order (id, order_number, user_id, idle_id, order_price, payment_status, payment_way, create_time, payment_time, order_status, is_deleted) VALUES
                                                                                                                                                             (85, '170446501212310003', 12, 106, 3000.00, 0, NULL,       '2024-01-05 14:30:12', NULL,                4, 0),
                                                                                                                                                             (86, '170446506640210004', 12, 111,  599.00, 1, '支付宝',    '2024-01-05 14:31:06', '2024-01-05 14:31:11', 3, 0);

SET FOREIGN_KEY_CHECKS = 1;
/* =============================
   Done.
   ============================= */
