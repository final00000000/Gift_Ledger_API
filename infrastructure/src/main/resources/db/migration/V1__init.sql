-- MySQL 版本的初始化脚本

CREATE TABLE users (
  id CHAR(36) PRIMARY KEY,
  username VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX ux_users_email ON users (email);

CREATE TABLE refresh_tokens (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  token_hash VARCHAR(255) NOT NULL,
  expires_at DATETIME NOT NULL,
  revoked_at DATETIME NULL,
  user_agent TEXT NULL,
  ip VARCHAR(45) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX ux_refresh_tokens_token_hash ON refresh_tokens (token_hash);
CREATE INDEX ix_refresh_tokens_user_id ON refresh_tokens (user_id, expires_at DESC);

CREATE TABLE reminder_settings (
  user_id CHAR(36) PRIMARY KEY,
  due_days INT NOT NULL DEFAULT 180,
  warn_days INT NOT NULL DEFAULT 90,
  max_remind_count INT NOT NULL DEFAULT 3,
  template TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_reminder_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_due_days CHECK (due_days > 0),
  CONSTRAINT chk_warn_days CHECK (warn_days >= 0),
  CONSTRAINT chk_max_remind_count CHECK (max_remind_count > 0),
  CONSTRAINT chk_warn_le_due CHECK (warn_days <= due_days)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE guests (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  relationship VARCHAR(255) NOT NULL,
  phone VARCHAR(50) NULL,
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_guests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_guests_user_id_name ON guests (user_id, name);

CREATE TABLE event_books (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(100) NOT NULL,
  event_date DATE NOT NULL,
  lunar_date VARCHAR(50) NULL,
  note TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_event_books_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_event_books_user_id_event_date ON event_books (user_id, event_date DESC);

CREATE TABLE gifts (
  id CHAR(36) PRIMARY KEY,
  user_id CHAR(36) NOT NULL,
  guest_id CHAR(36) NOT NULL,
  is_received BOOLEAN NOT NULL,
  amount DECIMAL(12, 2) NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  event_book_id CHAR(36) NULL,
  occurred_at DATE NOT NULL,
  note TEXT NULL,
  related_gift_id CHAR(36) NULL,
  is_returned BOOLEAN NOT NULL DEFAULT FALSE,
  reminded_count INT NOT NULL DEFAULT 0,
  last_reminded_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_gifts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_gifts_guest FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE,
  CONSTRAINT fk_gifts_event_book FOREIGN KEY (event_book_id) REFERENCES event_books(id) ON DELETE CASCADE,
  CONSTRAINT fk_gifts_related_gift FOREIGN KEY (related_gift_id) REFERENCES gifts(id) ON DELETE SET NULL,
  CONSTRAINT chk_amount CHECK (amount > 0),
  CONSTRAINT chk_reminded_count CHECK (reminded_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX ix_gifts_user_id_occurred_at ON gifts (user_id, occurred_at DESC);
CREATE INDEX ix_gifts_user_id_guest_id ON gifts (user_id, guest_id, occurred_at DESC);
CREATE INDEX ix_gifts_user_id_event_book_id ON gifts (user_id, event_book_id, occurred_at DESC);
CREATE INDEX ix_gifts_user_id_pending ON gifts (user_id, is_returned, is_received, occurred_at DESC);
