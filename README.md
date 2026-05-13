# DailySchedule

DailySchedule は Spring Boot を用いて開発したスケジュール管理Webアプリケーションです。

1日の予定を時間単位で管理できるように設計しており、  
実務を意識したレイヤードアーキテクチャ・例外処理・テスト構成を取り入れています。

---

# アプリ概要

スケジュールシートを作成し、  
その中に予定を登録して管理できます。

同一種別(TaskType)の予定同士は時間帯が重複しないように実装しており、  
Service層でバリデーションを行っています。

---

# 使用技術

| 技術 | 内容 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 |
| ORM | Spring Data JPA |
| Database | PostgreSQL |
| Template Engine | Thymeleaf |
| Migration | Flyway |
| Test | JUnit5 / Mockito / Testcontainers |

---

# 主な機能

- スケジュールシート作成
- slug形式URL
- 予定登録
- 同一種別の時間重複チェック
- DTOによるViewModel分離
- GlobalExceptionHandlerによる例外処理
- FlywayによるDBマイグレーション
- PostgreSQL + Testcontainers による統合テスト

---

# URL設計

| URL | 説明 |
|---|---|
| `/schedule-sheets` | シート一覧 |
| `/schedule-sheets/{slug}` | シート詳細 |

例:

```text
/schedule-sheets/my-study-plan
```

---

# アーキテクチャ

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
Database
```

---

# パッケージ構成

```text
src/main/java/com/example/demo
├─ advice
├─ controller
├─ dto
├─ entity
├─ repository
├─ service
├─ web
```

---

# Entity設計

## ScheduleSheet

スケジュール全体を管理するシート。

| カラム | 内容 |
|---|---|
| id | UUID |
| title | シート名 |
| slug | URL識別子 |
| createdAt | 作成日時 |

---

## ScheduleEntry

個別の予定データ。

| カラム | 内容 |
|---|---|
| id | UUID |
| type | TaskType |
| startMin | 開始時刻 |
| endMin | 終了時刻 |
| memo | メモ |
| createdAt | 作成日時 |

---

# TaskType

- WORK
- HOBBY
- BREAK
- HOUSEWORK
- LIFE
- FREE

---

# 時間重複チェック

同一TaskType内で時間帯が重複しないようにしています。

判定条件:

```text
new.start < existing.end
AND
new.end > existing.start
```

Repository:

```java
existsBySheet_SlugAndTypeAndStartMinLessThanAndEndMinGreaterThan(
    String slug,
    TaskType type,
    int endMin,
    int startMin
)
```

---

# 工夫した点

## 時間を int(分) で管理

`LocalTime` ではなく `0〜1440` の整数で管理しています。

理由:

- 24:00 を扱いやすい
- 重複判定が容易
- バリデーションが単純化できる

---

## DTO分離

EntityをViewへ直接渡さず、

- ScheduleSheetDto
- ScheduleEntryDto
- ScheduleDetailDto

を用いてViewModelを分離しています。

---

## GlobalExceptionHandler

`@ControllerAdvice` を使用して例外を集中管理しています。

主な例外:

- ScheduleConflictException
- ScheduleSheetNotFoundException
- ScheduleEntryNotFoundException
- IllegalArgumentException

エラー時はリダイレクトせず、
同一画面でエラーメッセージを表示する構成にしています。

---

# テスト

以下の構成でテストを実装しています。

```text
src/test/java
├─ controller
├─ advice
├─ service
├─ repository
└─ integration
```

---

## Unit Test

- Controller Test
- Service Test
- GlobalExceptionHandler Test

---

## Repository Test

PostgreSQLを使用したRepositoryテスト。

---

## Integration Test

以下を使用:

- SpringBootTest
- Testcontainers
- Flyway

主な統合テスト:

```text
ScheduleFlowIT
ScheduleSheetServiceImplIT
ScheduleServiceImplIT
```

---

# DBマイグレーション

Flywayを使用してスキーマ管理を行っています。

```text
V1__create_tables.sql
V2__add_indexes.sql
```
---

# Author

GitHub  
https://github.com/SK-Shun
