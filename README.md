# DecoTofu

## Overview / 概要

* **DecoTofu** is an integrated industrial control, automation, and HMI platform built on Java 21 and JavaFX (`WebViewer`, Ladder Logic Engine & Editor, and EtherCAT Master via SOEM).
* **DecoTofu** は、Java 21 と JavaFX 上に構築された統合制御・オートメーション・HMI プラットフォームです（`WebViewer`、独自のラダーロジックエンジンおよびエディタ、SOEM ネイティブ共有ライブラリによる EtherCAT マスタを統合しています）。

---

## Features / 特徴

* **WebViewer HMI & Control**: Renders `web/index.html` as the application's interactive user interface and exposes native/Java functionality to the page's JavaScript environment.
  * **WebViewer による表示と制御**: `web/index.html` をアプリケーションのユーザーインターフェースとして表示し、ページの JavaScript 環境へネイティブおよび Java の機能を公開します。
* **Extensible Plug-in Architecture**: Loads modular plug-in JARs from `web/plugins` (`WebPlugins`) and binds each plug-in's public methods to `window.<functionName>` in JavaScript.
  * **拡張可能なプラグイン機構**: `web/plugins` ディレクトリ内のプラグイン JAR（`WebPlugins`）を読み込み、各プラグインの public メソッドを JavaScript の `window.<functionName>` にバインドします。
* **Ladder Logic Editor & Cyclic Execution**: Features a built-in visual ladder editor (`JSON` storage, PDF export, diff viewer, undo/redo history, search) and a deterministic cyclic execution engine that links directly with WebViewer JavaScript and EtherCAT I/O.
  * **ラダーロジックエディタとサイクル実行**: ラダープログラムの編集（JSON 保存、PDF エクスポート、差分比較、Undo/Redo 履歴、ブロック検索）と、WebViewer（JavaScript）および EtherCAT I/O と連携するサイクル実行エンジンを備えています。
* **EtherCAT Master (SOEM)**: Binds the native `DecoTofu-SOEM` shared library via `jnr-ffi` for cyclic process data exchange (`LRW` / `LRWDC`), SDO read/write, redundant ports, Distributed Clocks (DC) synchronization, and slave state monitoring.
  * **EtherCAT マスタ (SOEM)**: `jnr-ffi` を通じてネイティブ共有ライブラリ `DecoTofu-SOEM` をバインドし、周期プロセスデータ通信、SDO 読み書き、冗長ポート通信、DC（Distributed Clocks）同期、スレーブ状態監視を行います。

---

## Requirements / 要件

* **Java**: JDK 21 or later that includes the JavaFX modules (`javafx.web`, `javafx.fxml`, `javafx.swing`, `javafx.controls`, `javafx.graphics`, `javafx.base`, and `jdk.jsobject`).
  * **Java**: JavaFX モジュール（`javafx.web`, `javafx.fxml`, `javafx.swing`, `javafx.controls`, `javafx.graphics`, `javafx.base`）および `jdk.jsobject` を含む JDK 21 以上。
* **Build Tool**: Apache Maven 3.8 or later.
  * **ビルドツール**: Apache Maven 3.8 以上。
* **EtherCAT Native Library (Optional / 任意)**: To use the `soem` EtherCAT master functions, build the shared library from [DecoTofu-SOEM](https://github.com/mizoguch-ken/DecoTofu-SOEM) using CMake 3.28 or later, Visual Studio (on Windows) or GCC/Clang (on Linux), and Npcap installed in **WinPcap API-compatible Mode** (on Windows).
  * **EtherCAT ネイティブライブラリ（任意）**: `soem` の EtherCAT マスタ機能を使用する場合は、[DecoTofu-SOEM](https://github.com/mizoguch-ken/DecoTofu-SOEM) を CMake 3.28 以上、Visual Studio（Windows）または GCC/Clang（Linux）、および **WinPcap API 互換モードでインストールした Npcap**（Windows）を用いてビルドしてください。

---

## Build & Test / ビルドとテスト

### Build Executable JAR / 実行可能 JAR のビルド

```bash
mvn package
```

* Creates the shaded executable JAR with all Maven dependencies at `target/DecoTofu.jar`.
  * すべての Maven 依存ライブラリを含む実行可能 JAR が `target/DecoTofu.jar` として生成されます。
* Also generates `target/DecoTofu-2026.09.12-bin.jar` and `dependency-reduced-pom.xml` (not tracked in Git).
  * `target/DecoTofu-2026.09.12-bin.jar` および `dependency-reduced-pom.xml`（Git 管理外）も同時に生成されます。

### Run Unit Tests / ユニットテストの実行

```bash
mvn test
```

* Runs unit tests on the classpath (`useModulePath=false`).
  * クラスパス上でユニットテストを実行します。

---

## Run & Startup Behavior / 起動と動作仕様

```bash
java -jar target/DecoTofu.jar
```

* **Initial HTML Page (`web/index.html`)**: Loaded automatically at startup. If `web/index.html` does not exist, DecoTofu creates a default HTML5 template with `<title>DecoTofu</title>` and an empty `<body>`.
  * **初期 HTML ページ (`web/index.html`)**: 起動時に自動で読み込まれます。存在しない場合は、`<title>DecoTofu</title>` と空の `<body>` を持つテンプレートから自動生成されます。
* **Window Title**: Synchronized with the `<title>` of the loaded HTML page.
  * **ウィンドウタイトル**: 読み込まれた HTML ページの `<title>` がウィンドウタイトルになります。
* **Window Settings (`properties.xml`)**: Persists `STAGE_MAXIMIZED`, `STAGE_WIDTH`, `STAGE_HEIGHT`, and `LANGUAGE` (`auto` / `en` / `ja`). If `properties.xml` contains a corrupted value, it is reported to the console and replaced with its default value (`640.0` × `480.0`, `LANGUAGE` `auto`).
  * **ウィンドウ設定 (`properties.xml`)**: `STAGE_MAXIMIZED`、`STAGE_WIDTH`、`STAGE_HEIGHT`、および `LANGUAGE`（`auto` / `en` / `ja`）を保存します。破損した値がある場合はコンソールに報告され、既定値（`640.0` × `480.0`、`LANGUAGE` は `auto`）が使用されます。
* **Multilingual Support (`en` / `ja`)**: UI menus, dialogs, ladder command catalog, diff viewer, console logs, and `WebPlugins` messages support English and Japanese, automatically following the OS display locale (`auto`) or switching live via the Ladder Editor `View > Language` menu and `stage.setLanguage(...)` (propagated via system property `decotofu.language` and `WebViewerPlugin.onLanguageChanged(Locale)`).
  * **多言語対応 (`en` / `ja`)**: 各種メニュー、ダイアログ、ラダーコマンド一覧、差分比較画面、コンソールログ、および `WebPlugins` プラグインのメッセージが英語・日本語に対応しています。初期設定は OS の表示ロケール（`auto`）に従い、ラダーエディタの「表示 > 言語」メニューおよび `stage.setLanguage(...)` から実行時に動的に切り替えられます（システムプロパティ `decotofu.language` および `WebViewerPlugin.onLanguageChanged(Locale)` 経由でプラグインへ即時反映されます）。
* **Application Directory Resolution**: Uses the current working directory if it contains `properties.xml` or `web`; otherwise resolves from the directory containing `DecoTofu.jar` (or its classes directory, searching up to 3 parent directories), falling back to `java.class.path` (up to 3 parents) and finally `user.dir`.
  * **アプリケーションディレクトリの解決**: カレントディレクトリに `properties.xml` または `web` が存在する場合はカレントディレクトリを使用し、存在しない場合は `DecoTofu.jar`（またはクラスディレクトリ）から最大 3 階層親までのディレクトリを検索し、見つからない場合は `java.class.path`（最大 3 階層親まで）、最終的に `user.dir` を使用します。
* **Console Logging (`log/`)**: If a `log` directory exists in the application directory, console output and stack traces are written to `log/Console_yyyyMMddHHmmss.txt`.
  * **コンソールログ出力 (`log/`)**: アプリケーションディレクトリに `log` フォルダーが存在する場合、コンソール出力は `log/Console_yyyyMMddHHmmss.txt` に書き出されます。
* **Releases**: Pre-built JARs are available at [DecoTofu Releases](https://github.com/mizoguch-ken/DecoTofu/releases).
  * **リリース**: ビルド済み JAR は [DecoTofu Releases](https://github.com/mizoguch-ken/DecoTofu/releases) から入手できます。

---

## Project Structure / プロジェクト構成

| Path / パス | Description / 説明 |
| :--- | :--- |
| `src/main/java/ken/mizoguch/decotofu` | Application entry point (`DecoTofu`), path resolution (`PathResolver`), shutdown hooks, and native library loader (`JavaLibrary`)<br>アプリケーションエントリポイント、パス解決、シャットダウンフック、ネイティブライブラリローダー |
| `src/main/java/ken/mizoguch/webviewer` | `WebViewer`, `StageSettingsPlugin` (`stage`), and `WebViewerPlugin` interface (`ken.mizoguch.webviewer.plugin`)<br>`WebViewer`、`StageSettingsPlugin` (`stage`)、およびプラグインインターフェース |
| `src/main/java/ken/mizoguch/ladders` | Visual ladder editor, cyclic ladder engine (`Ladders`), and `LaddersPlugin` (`ladders`)<br>ラダーエディタ、ラダーサイクルエンジン、および `LaddersPlugin` (`ladders`) |
| `src/main/java/ken/mizoguch/soem` | SOEM (EtherCAT) JNR-FFI binding (`Soem`, `SoemLibrary`, `SoemEtherCAT`) and `SoemPlugin` (`soem`)<br>SOEM (EtherCAT) の `jnr-ffi` バインディングおよび `SoemPlugin` (`soem`) |
| `src/main/java/ken/mizoguch/console` | Console output and log writer (`Console`)<br>コンソール出力・ログ書き出し |
| `src/main/java/module-info.java` | Java module descriptor (`module DecoTofu`)<br>モジュール定義 |
| `src/main/resources/fxml` | JavaFX FXML layout definitions (`DesignWeb.fxml`, `DesignLadders.fxml`, `DesignLaddersDifference.fxml`)<br>JavaFX 画面定義 (FXML) |
| `src/main/resources/icons` | Application window icons (`icon_032.png` – `icon_512.png` loaded at startup; `icon_016.png` also present)<br>ウィンドウアイコン（起動時は `icon_032.png` – `icon_512.png` を読み込み、`icon_016.png` も格納） |
| `src/main/resources/font` | Fonts (`Myrica`, `MyricaM`) used when exporting ladders as PDF<br>ラダーの PDF エクスポート用フォント |
| `src/main/resources/LICENSES` | Third-party license texts displayed by `stage.showLicenses()`<br>`stage.showLicenses()` で表示される第三者ライセンス全文 |
| `src/test/java` | JUnit 5 test suite<br>JUnit 5 ユニットテスト |
| `web/` | Default web assets, including the bundled Ace Editor (`web/index.html` kitchen sink demo)<br>`WebViewer` が読み込む Web アセットおよび同梱の Ace エディタ |
| `web/plugins/` | Directory for optional external `WebViewer` plug-in JARs built from `WebPlugins`<br>起動時に読み込まれる外部プラグイン JAR の格納ディレクトリ |
| `properties.xml` | Main window configuration file<br>メインウィンドウ設定ファイル |
| `ladder/properties.xml` | Ladder editor & cycle configuration file<br>ラダーエディタ・サイクル設定ファイル |

---

## Dependencies / 依存ライブラリ

```xml
<dependencies>
    <dependency>
        <groupId>io.github.java-diff-utils</groupId>
        <artifactId>java-diff-utils</artifactId>
        <version>4.17</version>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.14.0</version>
    </dependency>
    <dependency>
        <groupId>com.github.jnr</groupId>
        <artifactId>jnr-ffi</artifactId>
        <version>2.3.3</version>
    </dependency>
    <dependency>
        <groupId>org.apache.pdfbox</groupId>
        <artifactId>pdfbox</artifactId>
        <version>3.0.8</version>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.14.4</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

> [!NOTE]
> JavaFX modules are required in `module-info.java` and resolved from the JDK at compile and run time rather than bundled as Maven dependencies.
> JavaFX モジュールは `module-info.java` で要求され、Maven 依存ではなく JDK から直接提供されます。

---

## Plug-ins / プラグイン機構

* **Loading**: Any JAR placed in `web/plugins/` whose `META-INF/MANIFEST.MF` `Main-Class` implements `ken.mizoguch.webviewer.plugin.WebViewerPlugin` is loaded at startup.
  * **読み込み**: `web/plugins/` ディレクトリに配置された JAR のうち、`MANIFEST.MF` の `Main-Class` が `ken.mizoguch.webviewer.plugin.WebViewerPlugin` を実装しているものが起動時に読み込まれます。
* **JavaScript Binding**: The return value of `functionName()` becomes a property on the page's `window` object (`window.<functionName>`) when that property is `"undefined"` (pre-existing JavaScript globals are preserved), exposing all `public` methods of the plug-in class to JavaScript.
  * **JavaScript バインディング**: `functionName()` の戻り値がページの `window` オブジェクト上にまだ未定義（`"undefined"`）である場合にのみ `window.<functionName>` として登録され（既存の JS グローバル変数は上書きされません）、プラグインの `public` メソッドが JavaScript から呼び出せるようになります。
* **Lifecycle**: In the main `WebViewer`, `plugin.initialize(this)` runs once at startup (before external `web/plugins/*.jar` plug-ins, after built-in `stage`, `soem`, and `ladders`), `plugin.state(newValue)` is called on every `WebEngine` state change, and when the page finishes loading (`Worker.State.SUCCEEDED`), plug-ins are bound to `window` and `setup()` in the page's JavaScript is automatically called if defined. Popup windows reuse the parent's plug-in instances without re-calling `initialize()`, `state()`, or `close()`. On application shutdown, `plugin.close()` is called on all owned plug-ins (a JavaScript `close()` function is not automatically called).
  * **ライフサイクル**: メインの `WebViewer` では起動時に `plugin.initialize(this)` が 1 度呼ばれ（組み込みの `stage`・`soem`・`ladders` の後、`web/plugins/*.jar` が初期化されます）、状態遷移ごとに `plugin.state(newValue)` が通知されます。ページ読み込み完了（`Worker.State.SUCCEEDED`）時に各プラグインが `window` にバインドされ、ページ側の `setup()` 関数が定義されていれば自動実行されます。ポップアップウィンドウは親のプラグインインスタンスを再利用し、`initialize()`・`state()`・`close()` は再呼び出ししません。アプリ終了時にはホストが各プラグインの Java `close()` メソッドを呼び出します（JS 側の `close()` 関数は自動呼び出しされません）。
* **Fault Isolation**: A JAR whose `Main-Class` does not implement `WebViewerPlugin` is skipped; if a plug-in fails to load, the error is logged and remaining plug-ins continue loading.
  * **障害分離**: `Main-Class` が `WebViewerPlugin` を実装していない JAR は無視され、特定のプラグインの読み込みに失敗してもエラーを報告した上で他のプラグインの読み込みを継続します。
* **Official Plug-ins ([WebPlugins](https://github.com/mizoguch-ken/WebPlugins))**:
  * `CapAiClient.jar` (`capaiclient`), `CapAiClient2.jar` (`capaiclient2`), `CapAiClient3.jar` (`capaiclient3`), `CapAiServer.jar` (`capaiserver`)
  * `Db.jar` (`db`), `Dbg.jar` (`dbg`), `File.jar` (`file`), `Ftp.jar` (`ftp`), `Jasper.jar` (`jasper`)
  * `Llm.jar` (`llm`), `OnlineArt.jar` (`onlineArt`), `Printer.jar` (`printer`), `Serial.jar` (`serial`), `WebCam.jar` (`webcam`)
  * Each official plug-in provides `<plugin>.licenses()` to display the third-party licenses stored in its `licenses.txt`.
  * 各公式プラグインは `<plugin>.licenses()` メソッドを備えており、内包するライブラリのライセンス（`licenses.txt`）を表示できます。

### Custom Plug-in Example / カスタムプラグイン実装例

```java
package example;

import java.nio.file.Path;
import java.util.List;
import javafx.concurrent.Worker;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import ken.mizoguch.webviewer.plugin.WebViewerPlugin;

public class MyPlugin implements WebViewerPlugin {

    private WebViewerPlugin webViewer_;

    @Override
    public void initialize(WebViewerPlugin webViewer) {
        webViewer_ = webViewer;
    }

    @Override
    public String functionName() {
        return "myplugin";
    }

    @Override
    public void state(Worker.State state) {
    }

    @Override
    public void close() {
    }

    @Override
    public Stage stage() {
        return webViewer_.stage();
    }

    @Override
    public List<Image> icons() {
        return webViewer_.icons();
    }

    @Override
    public WebEngine webEngine() {
        return webViewer_.webEngine();
    }

    @Override
    public Path webPath() {
        return webViewer_.webPath();
    }

    @Override
    public void writeStackTrace(String name, Throwable throwable) {
        webViewer_.writeStackTrace(name, throwable);
    }

    @Override
    public void write(String name, String msg, boolean err) {
        webViewer_.write(name, msg, err);
    }

    public String hello() {
        return "hello";
    }
}
```

* When packaged in `web/plugins/MyPlugin.jar`, the page can call `myplugin.hello()`. (When building inside the `WebPlugins` repository, you can also extend `PluginSupport` from `PluginKit` to eliminate the delegation boilerplate.)
  * `web/plugins/MyPlugin.jar` として配置すると、ページの JavaScript から `myplugin.hello()` を呼び出せます（`WebPlugins` リポジトリの `PluginKit` を使用する場合は `PluginSupport` を継承することで委譲コードを省略できます）。

---

## Ladder Logic / ラダーロジック

* **Editing & Persistence**: Ladders are edited in the Ladder Window and saved as JSON files. Typing a command character (such as `L`, `O`, `;`, `/`, `@`, `%`, `=`, `<`, `>`, `&`, `|`, `^`) directly on a selected grid cell opens the inline command input immediately (and `Enter` / `F2` edits an existing block, or pick from the `Command` menu).
  * **編集と保存**: ラダーはラダーウィンドウで編集し、JSON ファイルとして保存します。セルを選択した状態でコマンド文字（`L`、`O`、`;`、`/`、`@`、`%`、`=`、`<`、`>`、`&`、`|`、`^` など）を直接入力するとインラインのコマンド入力欄が開きスムーズに入力できます（既存ブロックの編集は `Enter` / `F2`、`Command` メニューからの挿入にも対応）。
* **Menus**:
  * `File`: `New`, `Open...`, `Open Recent` (`Clear Recent`), `Save`, `Save As...`, `Export as PDF...`, `Difference...`, `Close`
  * `Edit`: `Undo`, `Redo`, `Cut`, `Copy`, `Paste`, `Delete`, `Edit Block...`, `Invert Block`, `Cycle Edge`, `Wire` (`Left`, `Right`, `Up`, `Down`), `Insert Row`, `Delete Row`, `Find Usages`, `Find Address...`
  * `Command`: `Contact`, `Comparison`, `Output`, `Timer / Counter`, `Arithmetic / Data`, `Bitwise`, `;` (Block Comment), `SCR` (Script)
  * `Tab`: `New Tab`, `Close Tab`, `Rename Tab...`, `Move Tab Left`, `Move Tab Right`, `Previous Tab`, `Next Tab`
  * `View`: `I/O Panel`, `Status Bar`, `Expand All I/O`, `Collapse All I/O`, `Clear Find Results`
  * `Tools`: `Connect`, `Run`, `Stop`, `Toggle Block Value`, `Ideal Cycle Time...`, `Undo History Limit...`, `Enable Scripts (SCR)`
* **Supported Ladder Instructions (Blocks)**:
  * Contacts / Loads: `LD`, `/LD`, `@LD`, `/@LD`, `%LD`, `/%LD`
  * Coils / Outputs: `OUT`, `/OUT`, `@OUT`, `/@OUT`, `%OUT`, `/%OUT`, `SET`, `RES`
  * Comparisons & Bitwise: `=`, `<>`, `<`, `<=`, `>`, `>=`, `&`, `|`, `^`
  * Logic Operations: `AND`, `OR`, `XOR`, `NOT`
  * Arithmetic & Data: `ADD`, `SUB`, `MUL`, `DIV`, `AVE`, `SFL`, `SFR`, `SIG`, `RND`, `MOV`
  * Timers & Counters: `TIM`, `/TIM`, `CNT`, `/CNT`
  * Script & Comment: `SCR`, Comment (`;`)
* **Addressing**: An address that does **not** start with `.` is global across ladders; an address that starts with `.` is local to its ladder tab.
  * **アドレス指定**: `.` で始まらないアドレスはグローバルアドレス、`.` で始まるアドレスはそのラダータブ固有のローカルアドレスとして扱われます。
* **Execution & Error Handling**:
  * Connect a ladder with `ladders.connect()` and start cyclic execution with `ladders.run()`.
  * Runtime errors in the ladder cycle are reported to the console; if errors occur repeatedly, the cycle stops automatically for safety.
  * Unregistered I/O addresses are reported once and ignored during the cycle.
  * `ladders.connect()` でラダーを接続し、`ladders.run()` でサイクル実行を開始します。サイクル中のエラーはコンソールに報告され、エラーが連続した場合は安全のためサイクルが自動停止します。未登録の I/O は初回のみ報告され、サイクルでは無視されます。
* **`SCR` Block Security Confirmation**:
  * When `ladders.isScriptEnable()` is `true` and a ladder containing `SCR` (JavaScript execution) blocks is opened on the JavaFX Application Thread, DecoTofu prompts the user for confirmation before keeping script blocks. If `ladders.isScriptEnable()` is `false` or the file is opened off the FX thread without UI confirmation, scripts are stripped **silently without prompting**; if the user declines the dialog, scripts are also stripped. Each `LadderIo` `SCR` buffer is limited to `4096` characters.
  * `ladders.isScriptEnable()` が `true` の状態で JavaFX アプリケーションスレッド上から `SCR`（スクリプト）ブロックを含むラダーを開くと、実行前にユーザーへ確認ダイアログが表示されます。`isScriptEnable()` が `false` の場合や FX スレッド以外から開かれた場合は確認なしでスクリプトが**自動的に除去**され、ダイアログで拒否した場合も除去されます。`SCR` スクリプト長の上限は `4096` 文字です。
* **Ladder Settings (`ladder/properties.xml`)**: Stores `HISTORY_GENERATION`, `IDEAL_CYCLE_TIME`, `LADDER_SCRIPT_ENABLE`, window and splitter geometry (`STAGE_MAXIMIZED`, `STAGE_WIDTH`, `STAGE_HEIGHT`, `PANE_ROOT_WIDTH`, `PANE_ROOT_HEIGHT`, `SPLIT_DIVIDER_IO_LADDER`, `SPLIT_DIVIDER_IO_IO`), `WORK_FILE_PATH`, and `MENU_RECENT_FILE_1`–`MENU_RECENT_FILE_10`.
  * **ラダー設定 (`ladder/properties.xml`)**: `HISTORY_GENERATION`、`IDEAL_CYCLE_TIME`、`LADDER_SCRIPT_ENABLE`、ウィンドウ・スプリッタ寸法（`STAGE_*`、`PANE_ROOT_*`、`SPLIT_DIVIDER_*`）、`WORK_FILE_PATH`、`MENU_RECENT_FILE_1`〜`MENU_RECENT_FILE_10` を保存します。

---

## Built-in JavaScript API / 組み込み JS 関数

When page loading reaches `Worker.State.SUCCEEDED`, DecoTofu binds three built-in Java objects—`stage`, `ladders`, and `soem`—to `window.<name>` (provided that `window.<name>` is `"undefined"`) and then calls `setup()` if defined. External plug-ins from `web/plugins/*.jar` are initialized right after the built-in plug-ins.

ページの読み込みが `Worker.State.SUCCEEDED` に達すると、DecoTofu は 3 つの組み込みオブジェクト（`stage`・`ladders`・`soem`）を `window.<name>` が未定義（`"undefined"`）の場合に限り登録し、`setup()` が定義されていれば自動実行します。`web/plugins/*.jar` の外部プラグインは組み込みプラグインの後に初期化されます。

* **Path Resolution / ファイルパスの解決**: Relative file paths passed to `ladders.fileOpen(...)` or `soem.load(...)` are resolved via `PathResolver`: if empty or invalid syntax, resolution fails; absolute paths are used as-is; otherwise the current working directory is checked if the file already exists there, falling back to the application directory if it exists there, and finally defaulting to the working directory.
  * `ladders.fileOpen(...)` / `soem.load(...)` に渡された相対パスは、絶対パスでない限りまずカレントディレクトリ（存在する場合）を調べ、次にアプリケーションディレクトリ（存在する場合）を調べ、どちらも存在しない場合はカレントディレクトリ基準になります。
* **Callback Name Validation / コールバック関数名の検証**: Callback names passed to `ladders.setOnTraceEvent(...)`, `soem.po2so(...)`, and `soem.setNotify*(...)` are validated against `^[A-Za-z_$][A-Za-z0-9_$]*(\.[A-Za-z_$][A-Za-z0-9_$]*)*\z` to prevent script injection. Invalid strings are logged and rejected/ignored.
  * `ladders.setOnTraceEvent(...)`、`soem.po2so(...)`、`soem.setNotify*(...)` に渡すコールバック関数名は正規表現で検証され、不正なスクリプト文字列は拒否・無視されます。
* **Inherited `WebViewerPlugin` Methods / 継承されるホスト共通メソッド**: Because JavaFX `WebView` exposes all `public` methods on bound Java objects, the 10 `WebViewerPlugin` methods (`initialize`, `functionName`, `state`, `close`, `stage`, `icons`, `webEngine`, `webPath`, `writeStackTrace`, `write`) are also callable on `stage`, `ladders`, and `soem` (plus 7 `SoemPluginListener` internal callback methods on `soem`). Usually JavaScript application code only needs the API methods documented below.
  * JavaFX `WebView` の仕様上、`WebViewerPlugin` の 10 個の `public` メソッド（`soem` ではさらに `SoemPluginListener` の 7 メソッド）もオブジェクト上に公開されていますが、通常は下記の API メソッドを使用してください。

---

### `window.stage` (`StageSettingsPlugin` — Main Window / メインウィンドウ制御)

Controls the main DecoTofu application `Stage` and displays version / license dialogs. When the underlying stage has not been initialized (`stage_ == null`), getters return `null` and setters/actions (except `showLicenses`, `showVersion`, and `getVersion`) are silent no-ops.

メインのアプリケーションウィンドウを制御し、バージョンやライセンスダイアログを表示します。未初期化時（`stage_ == null`）はゲッターが `null` を返し、`showLicenses` / `showVersion` / `getVersion` 以外のセッター・操作は何も行いません。

#### Usage Example / 使用例

```javascript
function setup() {
    stage.setTitle("HMI Console v" + stage.getVersion());
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    stage.setWidth(1280);
    stage.setHeight(800);
    stage.setOpacity(1.0);
    stage.show();
    stage.toFront();
    stage.requestFocus();
}
```

#### Function Reference / 関数リファレンス (41 API methods)

| Signature / シグネチャ | Description / 説明 |
| :--- | :--- |
| `String stage.getVersion()` | Returns DecoTofu `Implementation-Version` from the JAR manifest, or `"unknown"` when running unpacked classes outside a packaged JAR.<br>JAR マニフェストのバージョン文字列を返します（未パッケージ実行時は `"unknown"`）。 |
| `void stage.showVersion()` | Opens a non-blocking `INFORMATION` dialog titled `"VERSION"` displaying `"DecoTofu"` / `"VERSION <version>"`.<br>バージョン情報ダイアログを非ブロッキングで表示します。 |
| `void stage.showLicenses()` | Opens a non-blocking resizable dialog displaying third-party license texts from classpath resource `LICENSES` (UTF-8).<br>第三者ライセンス全文（`LICENSES`）を表示するダイアログを開きます。 |
| `String stage.getLanguage()` / `String stage.getResolvedLanguage()` / `Boolean stage.setLanguage(String lang)` | Gets or sets the configured language (`"auto"` / `"en"` / `"ja"`), or queries the active resolved language (`"en"` / `"ja"`). Switches DecoTofu UI, console logs, and plugins live.<br>設定言語（`"auto"` / `"en"` / `"ja"`）および実際に適用されている解決済み言語（`"en"` / `"ja"`）を取得・変更します。変更は画面・ログ・プラグインへ即時に反映されます。 |
| `Boolean stage.isShowing()` / `void stage.show()` / `void stage.hide()` | Queries, shows, or hides the main window (`null` if uninitialized).<br>メインウィンドウの表示状態取得・表示・非表示を行います。 |
| `Boolean stage.isFocused()` / `void stage.requestFocus()` | Queries focus state or requests window focus (`null` if uninitialized).<br>フォーカス状態の取得およびフォーカス要求を行います。 |
| `String stage.getTitle()` / `void stage.setTitle(String title)` | Gets or sets the main window title (`null` if uninitialized).<br>メインウィンドウのタイトルを取得・設定します。 |
| `Double stage.getWidth()` / `void stage.setWidth(double value)` | Gets or sets current window width in pixels.<br>ウィンドウ幅（px）を取得・設定します。 |
| `Double stage.getMinWidth()` / `void stage.setMinWidth(double value)` | Gets or sets minimum window width.<br>最小ウィンドウ幅を取得・設定します。 |
| `Double stage.getMaxWidth()` / `void stage.setMaxWidth(double value)` | Gets or sets maximum window width.<br>最大ウィンドウ幅を取得・設定します。 |
| `Double stage.getHeight()` / `void stage.setHeight(double value)` | Gets or sets current window height in pixels.<br>ウィンドウ高さ（px）を取得・設定します。 |
| `Double stage.getMinHeight()` / `void stage.setMinHeight(double value)` | Gets or sets minimum window height.<br>最小ウィンドウ高さを取得・設定します。 |
| `Double stage.getMaxHeight()` / `void stage.setMaxHeight(double value)` | Gets or sets maximum window height.<br>最大ウィンドウ高さを取得・設定します。 |
| `Double stage.getOpacity()` / `void stage.setOpacity(double value)` | Gets or sets window opacity (`0.0` transparent – `1.0` opaque).<br>ウィンドウの不透明度（`0.0`〜`1.0`）を取得・設定します。 |
| `Boolean stage.isAlwaysOnTop()` / `void stage.setAlwaysOnTop(boolean state)` | Gets or sets always-on-top mode.<br>最前面固定の取得・設定を行います。 |
| `Boolean stage.isMaximized()` / `void stage.setMaximized(boolean state)` | Gets or sets maximized state.<br>最大化状態の取得・設定を行います。 |
| `Boolean stage.isFullScreen()` / `void stage.setFullScreen(boolean state)` | Gets or sets full-screen mode.<br>フルスクリーン状態の取得・設定を行います。 |
| `Boolean stage.isIconified()` / `void stage.setIconified(boolean state)` | Gets or sets minimized (iconified) state.<br>最小化状態の取得・設定を行います。 |
| `Double stage.getX()` / `void stage.setX(double value)` | Gets or sets window X coordinate on screen.<br>ウィンドウの X 座標を取得・設定します。 |
| `Double stage.getY()` / `void stage.setY(double value)` | Gets or sets window Y coordinate on screen.<br>ウィンドウの Y 座標を取得・設定します。 |
| `void stage.toBack()` / `void stage.toFront()` | Sends window to back or brings it to front.<br>ウィンドウを最背面または最前面へ移動します。 |

---

### `window.ladders` (`LaddersPlugin` — Ladder Window, PLC Engine, Telemetry & Safe Actuation / ラダー制御)

Controls the Ladder Editor window, cyclic PLC execution, WebView (`SCR`) and EtherCAT (`soem`) I/O bindings, structural inspection, live I/O snapshots, fail-closed safe actuation, and event tracing.

ラダーエディタ画面、周期実行エンジン、WebView (`SCR`) / EtherCAT (`soem`) 連携、ラダー構造・I/O スナップショット取得、境界値保護付きの安全書き込み、トレース記録とフィードバックを制御します。

#### Recommended Startup Order & Usage Examples / 推奨起動順序と使用例

1. Open or edit the ladder (`fileOpen` — note that `fileOpen` and `fileNew` invoke `allClear()`, clearing prior SOEM mappings, cycle count, and trace history).
2. Register WebView for `SCR` blocks (`registerWeb`) and register SOEM bit mappings (`registerSoemIn`, `registerSoemOut`).
3. Validate and bind the program (`connect()`), then start cyclic execution (`run()`).

推奨順序: `fileOpen`（`fileOpen` / `fileNew` は既存の SOEM 連携やトレース履歴をクリアするため必ず最初に実行）→ `registerWeb` / `registerSoemIn` / `registerSoemOut` → `connect()` → `run()`。

```javascript
// Example 1: Open ladder, bind SOEM I/O and WebView, start cyclic execution
function setup() {
    ladders.setIdealCycleTime(10 * 1000 * 1000); // 10 ms in nanoseconds
    if (ladders.fileOpen("ladder/machine.json")) {
        ladders.registerWeb();
        ladders.registerSoemIn("X0", 1, 0, 0x01);
        ladders.registerSoemOut("Y0", 1, 0, 0x01);
        if (ladders.connect()) {
            ladders.run();
        }
    }
    ladders.setValue("M0", 1); // Takes effect on the NEXT ladder cycle
}
```

```javascript
// Example 2: Telemetry, safe actuation bounds, and trace events
window.onLadderEvent = function (eventJson) {
    var ev = JSON.parse(eventJson); // Single JSON string argument
    console.log("[TRACE]", ev.kind, ev.seq, ev.cycle);
};

function startMonitoring() {
    ladders.setOnTraceEvent("onLadderEvent");
    ladders.startTrace(JSON.stringify({ maxEntries: 512, epsilon: 0.01 }));

    // Allowlist addresses before calling applySafeAction
    ladders.setWritableBounds(JSON.stringify({
        "D0": { min: 0.0, max: 100.0 },
        "M10": { allow: true, min: 0.0, max: 1.0 }
    }));

    var res = JSON.parse(ladders.applySafeAction(JSON.stringify({
        inferenceId: "inf-001",
        prompt: "Ramp heater setpoint",
        writes: { "D0": 45.0, "M10": 1.0 }
    })));
    if (!res.applied) {
        console.warn("Rejected:", res.reason);
    }

    var obs = JSON.parse(ladders.drainObservations(50));
    ladders.recordFeedback(JSON.stringify({
        inferenceId: "inf-001",
        reward: 1.0,
        note: "Setpoint stable"
    }));
}
```

#### Function Reference / 関数リファレンス (82 API methods)

##### A. Ladder Editor Window Control / ラダーウィンドウ制御 (31 methods)
Delegates to the Ladder `Stage` with the same `null`/no-op behavior as `stage` when uninitialized. Unlike `window.stage`, `isFullScreen`, `setFullScreen`, `isIconified`, and `setIconified` do **not** exist on `window.ladders`.

| Method / メソッド | Description / 説明 |
| :--- | :--- |
| `Boolean ladders.isShowing()`, `void ladders.show()`, `void ladders.hide()` | Queries, shows, or hides the ladder window.<br>ラダーウィンドウの表示状態取得・表示・非表示。 |
| `Boolean ladders.isFocused()`, `void ladders.requestFocus()`, `void ladders.toBack()`, `void ladders.toFront()` | Queries/requests focus and changes Z-order.<br>フォーカス制御および最背面・最前面移動。 |
| `String ladders.getTitle()`, `void ladders.setTitle(String title)` | Gets or sets ladder window title.<br>ウィンドウタイトルの取得・設定。 |
| `Double ladders.getWidth()`, `setWidth(double)`, `getMinWidth()`, `setMinWidth(double)`, `getMaxWidth()`, `setMaxWidth(double)` | Gets or sets window width and width bounds (px).<br>幅および最小・最大幅の取得・設定。 |
| `Double ladders.getHeight()`, `setHeight(double)`, `getMinHeight()`, `setMinHeight(double)`, `getMaxHeight()`, `setMaxHeight(double)` | Gets or sets window height and height bounds (px).<br>高さおよび最小・最大高さの取得・設定。 |
| `Double ladders.getOpacity()`, `void ladders.setOpacity(double value)` | Gets or sets window opacity.<br>ウィンドウ不透明度の取得・設定。 |
| `Boolean ladders.isAlwaysOnTop()`, `void ladders.setAlwaysOnTop(boolean state)` | Gets or sets always-on-top mode.<br>最前面固定の取得・設定。 |
| `Boolean ladders.isMaximized()`, `void ladders.setMaximized(boolean state)` | Gets or sets maximized state.<br>最大化状態の取得・設定。 |
| `Double ladders.getX()`, `setX(double)`, `getY()`, `setY(double)` | Gets or sets screen X/Y coordinates.<br>画面 X/Y 座標の取得・設定。 |

##### B. Settings, Cycle Metrics & Status / 設定・サイクル計測・状態 (11 methods)

| Signature / シグネチャ | Return & Behavior / 戻り値・仕様 |
| :--- | :--- |
| `Integer ladders.getHistoryGeneration()` | Returns undo/redo history limit (`-1` default = unlimited). Persisted in `ladder/properties.xml`.<br>Undo/Redo 履歴世代数（既定 `-1` = 無制限）を返します。 |
| `void ladders.setHistoryGeneration(int historyGeneration)` | Sets undo/redo history generation limit.<br>Undo/Redo 履歴世代数を設定します。 |
| `Long ladders.getIdealCycleTime()` | Returns target cycle period in **nanoseconds** (`0` = no inter-cycle sleep).<br>目標サイクル周期を **ナノ秒 (ns)** で返します（`0` = 待機なし）。 |
| `void ladders.setIdealCycleTime(long idealCycleTime)` | Sets target cycle period in **nanoseconds** and updates the UI label asynchronously.<br>目標サイクル周期（ns）を設定します。 |
| `Long ladders.getCycleTime()` | Returns last measured cycle duration in **nanoseconds** (`0` before running).<br>直近のサイクル実行時間（ns）を返します。 |
| `Long ladders.getMinCycleTime()` | Returns minimum measured cycle duration in **nanoseconds** (`0` when reset).<br>最小サイクル実行時間（ns）を返します。 |
| `Long ladders.getMaxCycleTime()` | Returns maximum measured cycle duration in **nanoseconds** (`0` when reset).<br>最大サイクル実行時間（ns）を返します。 |
| `Boolean ladders.isRunning()` | Returns `true` if cyclic ladder execution is active.<br>サイクル実行中なら `true` を返します。 |
| `Boolean ladders.isChanged()` | Returns `true` if the ladder has unsaved edits.<br>未保存の変更があれば `true` を返します。 |
| `Boolean ladders.isScriptEnable()` | Returns whether `SCR` blocks may be kept when loading files (default `true`).<br>ファイル読込時に `SCR` ブロックを許可するかどうかを返します。 |
| `void ladders.setScriptEnable(boolean enable)` | Enables/disables `SCR` block retention on `fileOpen`. If `false`, scripts are silently stripped without prompting.<br>`SCR` 許可状態を設定します（`false` の場合は確認なしでスクリプトを除去）。 |

##### C. WebView / SOEM Registration & Direct I/O / Web・SOEM 連携と直接 I/O (8 methods)

| Signature / シグネチャ | Return & Behavior / 戻り値・仕様 |
| :--- | :--- |
| `void ladders.registerWeb()` | Attaches the `WebEngine` and `Worker.State` to all `LadderIo` entries so `SCR` blocks can execute JS. Kept synchronized on page loads and re-applied by `connect()`.<br>`SCR` ブロックから JS を実行できるよう `WebEngine` を登録します。 |
| `void ladders.unregisterWeb()` | Detaches the `WebEngine` and resets `LadderIo` web states to `READY`.<br>`WebEngine` の連携を解除します。 |
| `Boolean ladders.registerSoemIn(String address, int slave, long bitsOffset, long bitsMask)` | Maps EtherCAT slave input bits to a **global** ladder address (trimmed, non-empty, cannot start with `"."` or contain spaces). Creates global I/O + editor tree row if absent; replaces any prior mapping for `address`. Returns `false` if invalid or SOEM handle is null.<br>SOEM 入力ビットをグローバルアドレスへ割り当てます（`fileOpen`/`fileNew` で解除されるため読込後に呼び出してください）。 |
| `Boolean ladders.registerSoemOut(String address, int slave, long bitsOffset, long bitsMask)` | Maps a **global** ladder address to EtherCAT slave output bits (same validation and replacement rules).<br>グローバルアドレスを SOEM 出力ビットへ割り当てます。 |
| `void ladders.unregisterSoem()` | Clears all registered SOEM input/output mappings and clears the SOEM reference.<br>すべての SOEM I/O 割り当てを解除します。 |
| `Double ladders.getValue(String address)` | Reads the current live value of a **global** address, or `null` if `address == null`, starts with `"."`, or is unregistered.<br>グローバルアドレスの現在値を返します（未登録・`.` 始まり・`null` の場合は `null`）。 |
| `void ladders.setValue(String address, double value)` | Writes a value to a **global** address (silently ignores `null` or `.`-prefixed addresses). Creates the I/O entry if missing, updates the editor table immediately, and stages the write in `scriptIoMap_` — **live `ioMap_` (`getValue`/`snapshot`) updates on the next ladder cycle**.<br>グローバルアドレスへ値を書き込みます。エディタ表は即座に更新されますが、実際の実行データへの反映は**次サイクル開始時**になります。 |
| `String ladders.getComment(String address)` | Returns the comment configured for a global address, or `null` if none. Passes `address` to a `ConcurrentHashMap`, so passing Java `null` throws `NullPointerException`.<br>グローバルアドレスのコメントを返します（`null` を渡すと例外が発生します）。 |

##### D. Execution Lifecycle & File Operations / 実行制御・ファイル操作 (10 methods)

| Signature / シグネチャ | Return & Behavior / 戻り値・仕様 |
| :--- | :--- |
| `Boolean ladders.connect()` | Validates and compiles all editor tabs, snapshots compiled ladders into the runtime engine, links I/O maps, and re-applies `registerWeb()`. Returns `true` on success, `false` on compile/connection error.<br>全タブのラダーを検証・コンパイルし、実行用マップを接続します（`run()` の前提条件）。 |
| `Boolean ladders.run()` | Starts cyclic execution (`LadderCycleTask`). Requires `!isRunning() && connect()` succeeded and non-empty I/O (`false` otherwise).<br>サイクル実行を開始します（事前に `connect()` が必要）。 |
| `void ladders.stop()` | Stops cyclic execution. Returns immediately without blocking when called on the FX thread (JS); awaits task termination up to timeout when called from non-FX threads.<br>サイクル実行を停止します（FX スレッド上では非ブロッキング）。 |
| `Boolean ladders.fileNew()` | Prompts to save unsaved changes; on confirm/skip, calls `allClear()` (**stops cycle and clears SOEM mappings, cycleCount, and trace**) and creates default tab `"Ladders"`. Returns `false` if cancelled/failed.<br>変更確認後に全クリア（SOEM 連携・トレースも消去）して新規作成します。 |
| `Boolean ladders.fileOpen(String path)` | Resolves `path` via `PathResolver`, prompts for unsaved changes, loads JSON, applies `SCR` security rules (silent strip if `!isScriptEnable()` or off FX thread; dialog if enabled on FX thread), calls `allClear()`, and restores previous state if loading fails.<br>ラダー JSON ファイルを開きます（`SCR` 確認後に全クリアして読込）。 |
| `Boolean ladders.fileSave()` | Saves to the current regular file path, or opens the Save As chooser. Returns `true` if saved.<br>現在のファイルへ上書き保存（未設定時は保存ダイアログ）します。 |
| `Boolean ladders.fileSaveAs()` | Opens native Save FileChooser (`*.json`) and saves. Returns `true` if saved.<br>別名で JSON 保存ダイアログを開きます。 |
| `Boolean ladders.exportPdf()` | Opens native Save FileChooser (`*.pdf`) and exports ladder diagrams as PDF. Returns `true` if exported.<br>PDF エクスポートダイアログを開いて出力します。 |
| `String ladders.getFileName()` | Returns current filename (e.g. `"machine.json"`), or `null` if unset or a directory.<br>現在のファイル名（未保存時は `null`）を返します。 |
| `String ladders.getFilePath()` | Returns current full path string (`<appDir>/ladder` initially at startup before any file is opened), or `null` if unset.<br>現在のファイルパス文字列を返します。 |

##### E. Telemetry, Structure, Batch/Safe Actuation, Trace & Feedback (22 methods)
Methods that parse JSON or validate arguments log `IllegalArgumentException | IllegalStateException` messages to the console and return Java `null` on malformed input/empty I/O (except `applySafeAction`, which returns a structured rejection JSON object for safety/validation rule rejections, and `setOnTraceEvent`, which returns `Boolean`).

| Signature / シグネチャ | Return & Behavior / 戻り値・仕様 |
| :--- | :--- |
| `String ladders.snapshot()` / `snapshot(String filterJson)` | Returns JSON snapshot of cycle metrics and global I/O states (or `null` if `filterJson` is invalid). See schema #1.<br>サイクル統計とグローバル I/O スナップショットを JSON で返します。 |
| `String ladders.addresses()` / `addresses(boolean includeLocal)` | Returns JSON array of registered addresses, comments, and values. `includeLocal` defaults to `false` (global only).<br>登録済みアドレス一覧を JSON 配列で返します（既定はグローバルのみ）。 |
| `String ladders.structure()` / `structure(boolean includeScripts)` | Returns non-empty ladder blocks across connected tabs (falls back to editor tabs on FX thread if not yet connected). `includeScripts` defaults to `false`.<br>ラダー構成（空・接続線除く）を JSON で返します。 |
| `String ladders.setValues(String valuesJson)` | Atomically validates and writes multiple global values via `setValue`. **Bypasses `setWritableBounds`** and emits no trace event. Returns `{"written":N}` or `null`.<br>複数アドレスへ一括書き込みします（境界値チェックなし・トレース記録なし）。 |
| `String ladders.setWritableBounds(String boundsJson)` | Configures, updates, or clears (`{}`) the allowlist bounds enforced by `applySafeAction`. Returns updated bounds map JSON, or `null` on invalid JSON/bounds.<br>`applySafeAction` 用の書き込み許可範囲を設定・解除します。 |
| `String ladders.getWritableBounds()` | Returns current allowlist bounds JSON object (sorted keys, `"{}"` if empty).<br>現在設定されている許可境界マップ JSON を返します。 |
| `String ladders.applySafeAction(String actionJson)` | Atomically validates writes against allowlist `writableBounds` (fail-closed if unconfigured), rejects local (`.`) or non-finite values, writes via `setValue`, records/dispatches `"action"` or `"safety_reject"` trace event, and returns result JSON (or `null` if I/O empty / non-object JSON).<br>許可範囲内のみを安全に一括書き込みし、`action`/`safety_reject` トレースを記録します。 |
| `String ladders.startTrace()` / `startTrace(String optionsJson)` | Enables delta-transition recording in the cycle loop with optional JSON config and returns trace status JSON (`null` on invalid options).<br>I/O 変化トレース記録を開始します。 |
| `void ladders.stopTrace()` | Disables `transition` recording while keeping buffered events and cursor. Note: `action`, `safety_reject`, `feedback`, and cycle `error` events still record even when stopped.<br>`transition` の自動記録を停止します（バッファや cursor は維持）。 |
| `void ladders.clearTrace()` | Clears trace buffer, baseline values, and causal action state; advances drain cursor past cleared events.<br>トレースバッファと前回値・因果追跡状態をクリアします。 |
| `String ladders.trace()` / `trace(int limit)` | Non-destructive read of buffered trace events (`limit <= 0`: all; `limit > 0`: newest `limit` events).<br>蓄積されたトレースイベントを非破壊で取得します。 |
| `String ladders.drainTrace()` / `drainTrace(int limit)` | Reads only undrained events (`seq > cursor`) and advances the shared cursor (`limit <= 0`: all; `limit > 0`: oldest `limit` undrained).<br>未読イベントのみを取得し、読み取りカーソルを進めます。 |
| `String ladders.drainObservations()` / `drainObservations(int limit)` | Same shared cursor as `drainTrace`; returns JSON array of formatted human-readable observation strings.<br>未読イベントを整形済み観測テキスト配列として取得し、カーソルを進めます。 |
| `String ladders.recordFeedback(String feedbackJson)` | Appends a `"feedback"` event to the trace buffer, fires `onTraceEvent`, and returns the event JSON string (or `null` if non-object / non-finite `reward`).<br>`feedback` イベントを記録して JSON を返します。 |
| `Boolean ladders.setOnTraceEvent(String callbackName)` | Registers JS callback function for push notifications of all trace events. `null` or blank clears and returns `true`; invalid regex returns `false`.<br>トレースイベント発生時の JS コールバック関数名を設定（`null`/空で解除）します。 |

#### Exact JSON Schemas for `ladders` / JSON 形式

All JSON strings returned by `ladders` use `Gson` with HTML escaping disabled.

##### 1. `ladders.snapshot([filterJson])`
* **Input `filterJson`**: `null` / blank / JSON `null` (all global addresses), a JSON array of global addresses `["X0", "Y0"]`, or `{"addresses": ["X0", "Y0"]}` (omitted or `null` `"addresses"` returns all).
* **Output**:
```json
{
  "running": true,
  "cycleCount": 123,
  "cycleTimeNs": 150000,
  "minCycleTimeNs": 90000,
  "maxCycleTimeNs": 220000,
  "timestampMs": 1700000000000,
  "io": [
    {
      "address": "X0",
      "comment": "Start",
      "value": 1.0,
      "ld": true,
      "rising": false,
      "falling": false
    }
  ]
}
```
(`io` includes global addresses sorted naturally; `comment` defaults to `""`; non-finite `value` serializes as JSON `null`.)

##### 2. `ladders.addresses([includeLocal])`
Sorted by `tabIndex` ascending then natural `address` order (`tabIndex == 0` is global; local entries appear only when `includeLocal == true`):
```json
[
  { "scope": "global", "tabIndex": 0, "address": "X0", "comment": "Start", "value": 1.0 },
  { "scope": "local",  "tabIndex": 1, "address": ".T0", "comment": "LocalTimer", "value": 42.0 }
]
```

##### 3. `ladders.structure([includeScripts])`
Omits `EMPTY`, `CONTENTS`, and `CONNECT_LINE` blocks; sorted by `row` then `col`. Optional keys (`address`, `comment`, `functions`, `script`) are omitted when empty/disabled:
```json
{
  "tabs": 1,
  "ladders": [
    {
      "index": 1,
      "name": "Main",
      "columns": 10,
      "rows": 10,
      "blocks": [
        {
          "col": 1,
          "row": 2,
          "block": "LOAD",
          "command": "LD",
          "address": "X0",
          "comment": "Start",
          "blockValue": 0.0,
          "functions": [
            { "address": "D0", "value": 10.0, "number": false }
          ],
          "script": "ladders.setValue('Y0', 1);"
        }
      ]
    }
  ]
}
```

##### 4. `ladders.setValues(valuesJson)`
* **Input**: Non-empty JSON object mapping global addresses to finite numbers: `{"X0": 1.0, "D0": 25.5}`.
* **Output**: `{"written": 2}` (or `null` if I/O empty, invalid JSON, local address `.`, or non-finite value).

##### 5. `ladders.setWritableBounds(boundsJson)` & `ladders.getWritableBounds()`
* **Input `boundsJson`**: `{}` clears all bounds; non-empty object merges/removes per global address (`min <= max`, both finite; `null` or `{"allow": false}` deletes that key):
```json
{
  "Y0": { "min": 0.0, "max": 10.0 },
  "D0": { "allow": true, "min": -5.0, "max": 5.0 },
  "OLD1": null,
  "OLD2": { "allow": false }
}
```
* **Output** (sorted keys):
```json
{
  "D0": { "min": -5.0, "max": 5.0 },
  "Y0": { "min": 0.0, "max": 10.0 }
}
```

##### 6. `ladders.applySafeAction(actionJson)`
* **Input**: Either nested `{"inferenceId": "inf-1", "prompt": "...", "note": "...", "writes": {"Y0": 4.5, "D0": -2.0}}` OR flat `{"Y0": 4.5, "inferenceId": "inf-1"}`.
* **Accepted Output**: `{"applied": true, "rejected": false, "written": 2, "writes": {"D0": -2.0, "Y0": 4.5}, "inferenceId": "inf-1"}` (`inferenceId` omitted if not supplied as string).
* **Rejected Output** (zero writes applied): `{"applied": false, "rejected": true, "reason": "Value 9.0 for \"D0\" outside bounds [-5.0, 5.0]", "inferenceId": "inf-1"}`. Returns Java `null` only if I/O is empty or `actionJson` is not a JSON object.

##### 7. `ladders.startTrace([optionsJson])`
* **Input** (optional; `null`/blank uses defaults):
```json
{ "maxEntries": 512, "minIntervalMs": 0, "epsilon": 0.0, "addresses": ["X0", "Y0"], "reset": false }
```
Constraints: `maxEntries` `16..10000` (default `512`), `minIntervalMs` `0..60000` (default `0`), `epsilon` finite `>= 0.0` (default `0.0`), `addresses` global array (omitted/null = all global), `reset` boolean (`false`).
* **Output**: `{"tracing": true, "maxEntries": 512, "minIntervalMs": 0, "epsilon": 0.0, "buffered": 0, "addresses": ["X0", "Y0"]}` (`addresses` omitted if watching all).

##### 8. `ladders.recordFeedback(feedbackJson)`
* **Input** (all fields optional, must be JSON object):
```json
{ "inferenceId": "inf-1", "prompt": "...", "reward": 1.0, "note": "verified", "correction": "..." }
```
* **Output**: The recorded `"feedback"` event JSON string (see #9).

##### 9. Trace Event Objects (`trace`, `drainTrace`, `recordFeedback`, `setOnTraceEvent`)
Buffer holds up to `maxEntries` (FIFO eviction). Every event has `seq`, `cycle`, `timestampMs`, `kind`:
* **`transition`** (only while `startTrace` active; first cycle omits `previous`; attributes `causedBySeq`/`causedByInferenceId` if `cycle - lastActionCycle <= 3`):
  ```json
  { "seq": 2, "cycle": 10, "timestampMs": 1700000000000, "cycleTimeNs": 200000, "kind": "transition", "causedBySeq": 1, "causedByInferenceId": "inf-1", "changes": { "X0": { "previous": 0.0, "value": 1.0, "comment": "Start" } } }
  ```
* **`error`** (recorded on cycle exception even when tracing is off; if within 3 cycles of last action, includes `causedBySeq`, `causedByInferenceId`, and `inferenceId` then clears attribution):
  ```json
  { "seq": 3, "cycle": 11, "timestampMs": 1700000000000, "cycleTimeNs": 50000, "kind": "error", "note": "java.lang.ArithmeticException: ...", "reward": -1.0, "causedBySeq": 1, "causedByInferenceId": "inf-1", "inferenceId": "inf-1" }
  ```
* **`action`** (recorded on accepted `applySafeAction` even when tracing is off):
  ```json
  { "seq": 1, "cycle": 9, "timestampMs": 1700000000000, "kind": "action", "writes": { "Y0": 4.5 }, "inferenceId": "inf-1", "prompt": "...", "note": "..." }
  ```
* **`safety_reject`** (recorded on rejected `applySafeAction` even when tracing is off):
  ```json
  { "seq": 4, "cycle": 11, "timestampMs": 1700000000000, "kind": "safety_reject", "reward": -1.0, "inferenceId": "inf-2", "prompt": "...", "note": "<rejectReason>" }
  ```
* **`feedback`** (recorded by `recordFeedback` even when tracing is off):
  ```json
  { "seq": 5, "cycle": 11, "timestampMs": 1700000000000, "kind": "feedback", "inferenceId": "inf-1", "prompt": "...", "reward": 1.0, "note": "verified", "correction": "..." }
  ```

##### 10. `ladders.drainObservations([limit])`
Returns a JSON array of formatted summary lines sharing the drain cursor with `drainTrace`:
```json
[
  "[LADDER cycle=10 transition] X0(Start)=0->1 Y0(Motor)=0->2.5",
  "[LADDER cycle=11 safety_reject] # Value 9.0 for \"D0\" outside bounds [-5.0, 5.0]"
]
```

##### 11. `ladders.setOnTraceEvent(callbackName)` Callback Signature
Queued up to **64** pending events (oldest dropped on overflow) and invoked on the FX Application Thread once `Worker.State.SUCCEEDED`:
* **Invocation**: `<callbackName>(eventJsonString)` — receives **1 JSON string argument** (call `JSON.parse(arg)` in JavaScript).

#### Notes & Safety Traps / 注意点・安全仕様
* **Deferred Writes / 書き込みタイミングの遅延**: `setValue`, `setValues`, and `applySafeAction` stage writes into `scriptIoMap_`; live `ioMap_` values (`getValue`, `snapshot`) reflect them on the **next** ladder cycle (edge flags active for 1 cycle).
* **Fail-Closed Safety / フェイルクローズド**: `applySafeAction` refuses any address not explicitly allowlisted via `setWritableBounds`, rejects local (`.`-prefixed) addresses and non-finite numbers, and performs all-or-nothing validation.
* **Cycle Error Auto-Stop / 連続エラー時の自動停止**: The cyclic runner stops automatically after **100** consecutive cycle exceptions (`LADDER_CYCLE_ERROR_LIMIT`).

---

### `window.soem` (`SoemPlugin` — EtherCAT Master / EtherCAT マスタ制御)

Controls an EtherCAT master built on [DecoTofu-SOEM](https://github.com/mizoguch-ken/DecoTofu-SOEM) via JNR-FFI. Supports cyclic process data (`in`/`outs`/`out`), normal/CA SDO read/write, redundant network ports, Distributed Clocks (`dcsync0`/`dcsync01`), PRE_OP→SAFE_OP hooks (`po2so`), slave diagnostics (`slaveinfo`), and asynchronous health notifications.

`DecoTofu-SOEM` 共有ライブラリを用いた EtherCAT マスタ機能を提供します。プロセスデータ I/O、SDO 読み書き、冗長ポート、DC 同期、`po2so` フック、スレーブ詳細取得、異常通知コールバックをサポートします。

#### Strict Lifecycle Order & Usage Example / 厳守すべき呼び出し順序と使用例

1. **`soem.load(libraryPath)`** — Loads the native SOEM shared library once per JVM/plugin lifetime (`web/DecoTofu-SOEM.dll`, `.so`, or `.dylib`).
2. *(Optional)* **`soem.find_adapters()`** / **`soem.version()`** — Discover network interfaces.
3. **`soem.init(ioSize)`** — Allocates native `ecx_contextt`, `ecx_parcelt`, and `ioSize`-byte IO map.
4. *(Optional, **BEFORE `start`**)* **`soem.setOverlappedMode(...)`**, **`soem.setPackedMode(...)`**, **`soem.po2so(...)`** — Configure IO mapping mode and PRE_OP→SAFE_OP hooks.
5. **`soem.start(ifname, cycletimeNs)`** or **`soem.start_redundant(ifname, ifname2, cycletimeNs)`** — Brings slaves through `PRE_OP` → `SAFE_OP` → `OPERATIONAL` and starts monitor + cyclic threads.
6. **⚠️ `soem.setNotify*(...)` MUST be called AFTER `start()` / `start_redundant()` succeeds!** (`SoemEcatCheck` is instantiated inside `start()`; calling `setNotify*` before `start()` leaves notifications inactive.)
7. Operate: `dcsync0`/`dcsync01`, `enableCyclicMailbox`, `sdoRead*`, `sdoWrite*`, `in`, `outs`, `out`, `slaveinfo`, status/error checks.
8. **`soem.stop()`** — Stops threads, transitions slaves to `INIT`, and frees context/port/IOmap while keeping the shared library loaded so `init()` → `start()` can be run again.

> [!IMPORTANT]
> Call `soem.setNotify*(...)` **after** `soem.start(...)` or `soem.start_redundant(...)` succeeds. Calling `setNotify*` before `start()` will not arm notifications on the internal monitor instance created by `start()`.
>
> `soem.setNotify*(...)` は必ず `soem.start(...)` / `soem.start_redundant(...)` の成功**後**に呼び出してください。`start()` 前に呼んでも監視インスタンスが未生成のため通知は有効になりません。

```javascript
window.onSlavePo2So = function (slave) {
    console.log("Configuring slave in PRE_OP->SAFE_OP:", slave);
    // Optional DC sync inside po2so once mapped
    soem.dcsync0(slave, true, 1000000, 0); // 1 ms in ns
    return 1;
};

window.onSlaveLost = function (slave) {
    console.error("EtherCAT slave lost:", slave);
};

window.onAllResumed = function () {
    console.log("All EtherCAT slaves resumed OPERATIONAL");
};

function setup() {
    if (!soem.load("DecoTofu-SOEM.dll")) {
        console.error("Failed to load SOEM library");
        return;
    }
    var adapters = JSON.parse(soem.find_adapters());
    console.log("Adapters:", adapters);

    if (!soem.init(4096)) {
        return;
    }
    soem.setOverlappedMode(false);
    soem.po2so(1, 0x00000002, 0x03f03052, "onSlavePo2So");

    var ifname = Object.values(adapters)[0];
    if (soem.start(ifname, 1000000)) { // 1 ms cycle time in nanoseconds
        // Register notifications AFTER start() succeeds!
        soem.setNotifyErrorLost("onSlaveLost");
        soem.setNotifyMessageAllSlavesResumedOperational("onAllResumed");

        console.log("Slave info:", JSON.parse(soem.slaveinfo(false, true)));
        soem.out(1, 0, 0x01, 1);
    }
}
```

#### Function Reference / 関数リファレンス (43 API methods)

##### A. Library Loading, Initialization, Configuration & Lifecycle (18 methods)

| Signature / シグネチャ | Return & Behavior / 戻り値・仕様 |
| :--- | :--- |
| `Soem soem.getSoem()` | Returns the underlying Java `Soem` instance.<br>内部の `Soem` Java オブジェクトを返します。 |
| `void soem.setState(Worker.State state)` | Sets the internal `Worker.State` directly (mirrors host `state()` updates).<br>内部の `Worker.State` を直接設定します。 |
| `Boolean soem.load(String libraryPath)` | Resolves `libraryPath` via `PathResolver`, loads the shared library via JNR-FFI, and checks native ABI compatibility (`EC_MAXSLAVE=200`, `EC_MAXGROUP=2`). Can succeed only **once** per plugin instance (`false` if missing, incompatible, or already loaded).<br>共有ライブラリを 1 度だけロードし、ABI 互換性を検証します。 |
| `Boolean soem.isLoaded()` | Returns `true` if the native SOEM library is loaded.<br>ライブラリがロード済みなら `true` を返します。 |
| `Boolean soem.isRunning()` | Returns `true` if the cyclic thread exists and reached `OPERATIONAL`.<br>周期スレッドが稼働中（`OPERATIONAL` 到達済み）なら `true` を返します。 |
| `String soem.version()` | Returns `ec_version()` string, or `null` if unloaded or symbol absent in legacy builds.<br>ネイティブ SOEM バージョン文字列を返します。 |
| `Boolean soem.init(int size)` | Allocates native context, parcel, and `size`-byte IO map. Returns `null` if unloaded; `false` if `size <= 0`, already initialized, or allocation fails.<br>指定バイト数の IO マップとコンテキストを確保します。 |
| `Boolean soem.setOverlappedMode(boolean enable)` | Configures overlapped IO map mode. Must be called **after `init()` and before `start()`**. Returns `null` if unloaded, `false` if uninitialized/running.<br>Overlapped モードを設定します（`init()` 後・`start()` 前のみ有効）。 |
| `Boolean soem.isOverlappedMode()` | Returns current overlapped mode flag, or `null` if uninitialized.<br>Overlapped モード設定を返します。 |
| `Boolean soem.setPackedMode(boolean enable)` | Configures bit-packed PDO mapping mode. Must be called **after `init()` and before `start()`**. Returns `null` if unloaded, `false` if uninitialized/running.<br>Packed モードを設定します（`init()` 後・`start()` 前のみ有効）。 |
| `Boolean soem.isPackedMode()` | Returns current packed mode flag, or `null` if uninitialized.<br>Packed モード設定を返します。 |
| `Boolean soem.po2so(int slave, long eep_man, long eep_id, String func)` | Registers a PRE_OP→SAFE_OP JS hook `func(slave)` (500 ms FX-thread timeout, numeric return cast to `int`). During `start()`, verifies `sl.eep_man == eep_man && sl.eep_id == eep_id` (mismatch aborts `start()`). Must be called **after `init()` and before `start()`**; passing invalid JS name returns `false`.<br>指定スレーブの PRE_OP→SAFE_OP 遷移時コールバック `func(slave)` を登録します。 |
| `Boolean soem.start(String ifname, long cycletime)` | Opens `ifname`, configures slaves through `SAFE_OP`→`OPERATIONAL`, and starts monitor + cyclic threads with period `cycletime` (**nanoseconds**). Returns `null` if unloaded, `false` on error.<br>`cycletime`（ns）でシングルポート通信を開始します。 |
| `Boolean soem.start_redundant(String ifname, String ifname2, long cycletime)` | Same as `start`, using primary + redundant interfaces (`ifname`, `ifname2`).<br>二重化冗長ポートで通信を開始します。 |
| `Boolean soem.stop()` | Stops cyclic & monitor threads, transitions slaves to `INIT`, and frees context/IOmap while **keeping the native library loaded** (`init()` can be called again). Returns `null` if unloaded, `false` on thread shutdown timeout.<br>通信を停止しスレーブを `INIT` へ戻します（ライブラリ自体はロード済みのまま保持）。 |
| `Boolean soem.enableCyclicMailbox(int slave)` | Enables cyclic mailbox polling (`1 <= slave <= slavecount`). Returns `null` if unloaded, `false` if invalid/failed.<br>指定スレーブのサイクリックメールボックスを有効化します。 |
| `Boolean soem.dcsync0(int slave, boolean act, long cyclTime, int cyclShift)` | Configures Distributed Clocks `SYNC0` (`1 <= slave <= slavecount`, `0 <= cyclTime <= 0xFFFFFFFF` ns). Returns `null` if unloaded, `false` on invalid args.<br>DC `SYNC0` を設定します（`cyclTime` はナノ秒・uint32 範囲）。 |
| `Boolean soem.dcsync01(int slave, boolean act, long cyclTime0, long cyclTime1, int cyclShift)` | Configures Distributed Clocks `SYNC0`/`SYNC1` (`cyclTime0`/`cyclTime1` in uint32 ns).<br>DC `SYNC0` / `SYNC1` を設定します。 |

##### B. Status, Diagnostics & Error Queue (9 methods)

| Signature / シグネチャ | Return & Behavior / 戻り値・仕様 |
| :--- | :--- |
| `Integer soem.slavecount()` | Number of discovered slaves, or `null` if uninitialized.<br>検出されたスレーブ数を返します。 |
| `Integer soem.state(int slave)` | EtherCAT AL state for `slave` (`0` = group summary, `1..slavecount`), or `null` if out of range/uninitialized. Values: `0x00` NONE, `0x01` INIT, `0x02` PRE_OP, `0x03` BOOT, `0x04` SAFE_OP, `0x08` OPERATIONAL, `0x10` ERROR/ACK bit (e.g. `0x14` = SAFE_OP + ERROR).<br>スレーブの EtherCAT 状態コードを返します。 |
| `Boolean soem.islost(int slave)` | `true` if `slave` is marked lost, or `null` if invalid/uninitialized.<br>スレーブがロスト状態かどうかを返します。 |
| `Boolean soem.docheckstate()` | `true` if group 0 requires state checking, or `null` if uninitialized.<br>状態チェック要求フラグ（`docheckstate`）を返します。 |
| `Integer soem.wkc()` | Last cyclic Working Counter (`WKC`), or `null` if uninitialized.<br>直近の WKC 値を返します。 |
| `Integer soem.expectedWkc()` | Expected WKC (`(outputsWKC * 2) + inputsWKC`), or `null` if uninitialized.<br>期待 WKC 値を返します。 |
| `Long soem.dcTime()` | Master Distributed Clock time (`ns`), or `null` if uninitialized.<br>マスタの DC 時刻（ns）を返します。 |
| `Boolean soem.isError()` | Checks if EtherCAT errors exist in the error list (`null` if unloaded/uninitialized).<br>EtherCAT エラーが溜まっているか確認します。 |
| `String soem.popError()` | Pops and formats one error string (`ecx_elist2string`), or `null` if none / uninitialized.<br>エラーリストから 1 件取り出して文字列として返します。 |

##### C. Notification Callbacks / 通知コールバック (7 methods)
All 7 accept a JS callback name validated by regex (`null` disables). Callbacks dispatch asynchronously on the FX thread (`Worker.State.SUCCEEDED`) from the 10 ms `SoemEcatCheck` monitor — **must be registered AFTER `start()` / `start_redundant()`**.

| Method / メソッド | JS Callback Call / JS 側への通知 | Trigger Condition / 発生条件 |
| :--- | :--- | :--- |
| `void soem.setNotifyErrorSafeOpError(String func)` | `func(slave)` *(1 int arg)* | Slave enters `SAFE_OP + ERROR` (`0x14`); acknowledges with `SAFE_OP + ACK`. |
| `void soem.setNotifyWarningSafeOp(String func)` | `func(slave)` *(1 int arg)* | Slave enters `SAFE_OP` (`0x04`); transitions back to `OPERATIONAL`. |
| `void soem.setNotifyMessageReconfigured(String func)` | `func(slave)` *(1 int arg)* | Slave `> NONE` (not OP/SAFE_OP) successfully reconfigured. |
| `void soem.setNotifyErrorLost(String func)` | `func(slave)` *(1 int arg)* | Slave transitions to `NONE` (`0x00`); marks lost & zeros inputs. |
| `void soem.setNotifyMessageFound(String func)` | `func(slave)` *(1 int arg)* | Previously lost slave responds with state `!= NONE`. |
| `void soem.setNotifyMessageRecovered(String func)` | `func(slave)` *(1 int arg)* | Previously lost slave successfully recovered. |
| `void soem.setNotifyMessageAllSlavesResumedOperational(String func)` | `func()` *(0 args)* | Following a check cycle, all slaves in group 0 are `OPERATIONAL`. |

##### D. SDO & Process Data I/O (7 methods)

| Signature / シグネチャ | Return & Behavior / 戻り値・仕様 |
| :--- | :--- |
| `Byte[] soem.sdoRead(int slave, int index, int subIndex, int byteSize)` | Normal CoE SDO upload (`CA=FALSE`, timeout `700 ms`). Returns signed `Byte[actualSize]` (`-128..127`) when `wkc > 0 && 0 <= actualSize <= byteSize`, else `null`. (`0 <= slave <= slavecount`, `byteSize > 0`.)<br>通常 SDO 読み込み。符号付き `Byte[]` 配列（または `null`）を返します。 |
| `Byte[] soem.sdoReadCA(int slave, int index, int subIndex, int byteSize)` | Complete Access (`CA=TRUE`) SDO upload with the same parameters/return behavior.<br>Complete Access モードで SDO を読み込みます。 |
| `Integer soem.sdoWrite(int slave, int index, int subIndex, int byteSize, Object value)` | Normal CoE SDO download (`CA=FALSE`). Returns native **WKC `Integer`**, or `null` on invalid args/unloaded.<br>• `value` is **`Number`**: encoded little-endian; **`byteSize` MUST be `1`, `2`, `4`, or `8`** (other values return `null`).<br>• `value` is **JS Array / `Uint8Array` / Java `byte[]`**: written in byte order (`length > 0`; **`byteSize` is ignored**).<br>通常 SDO 書き込み。数値は `byteSize`（`1/2/4/8`）固定のリトルエンディアン、配列時は各要素を下位バイトとして書き込み（`byteSize` 無視）WKC を返します。 |
| `Integer soem.sdoWriteCA(int slave, int index, int subIndex, int byteSize, Object value)` | Complete Access (`CA=TRUE`) SDO download with the same rules.<br>Complete Access モードで SDO を書き込みます。 |
| `Long soem.in(int slave, long bitsOffset, long bitsMask)` | Reads up to 64 masked little-endian bits (clamped to slave `Ibits`) from `slave`'s **input** process data map. Returns value (`0L` if clamped mask is 0), or `null` on invalid slave/offset/`bitsMask == 0`.<br>スレーブの入力プロセスデータから指定ビット範囲を読み取ります。 |
| `Long soem.outs(int slave, long bitsOffset, long bitsMask)` | **Reads** up to 64 masked bits from `slave`'s **output** process data map (`Obits`). Same return/null rules as `in()`.<br>スレーブの出力プロセスデータの現在値を読み取ります。 |
| `Long soem.out(int slave, long bitsOffset, long bitsMask, long value)` | **Queues an asynchronous bit-masked write** to `slave`'s output process data map (applied by the native cyclic thread while idle, preserving neighboring bits/slaves). **Returns the byte offset** (`sl.outputs - sl0.outputs + bitsOffset/8`) in the group output map (`0L` if clamped mask is 0, `null` on invalid args) — **not** the written value.<br>出力プロセスデータへのビットマスク付き非同期書き込みを予約します。戻り値は書き込んだ値ではなく**出力マップ内のバイトオフセット**です。 |

##### E. Discovery & Introspection (2 methods)

| Signature / シグネチャ | Return & Behavior / 戻り値・仕様 |
| :--- | :--- |
| `String soem.find_adapters()` | Enumerates OS network interfaces (preserving order, capped at 1024) and returns JSON object mapping `description -> adapterName` (`"{}"` if none, Java `null` if library unloaded or symbol missing).<br>利用可能なネットワークアダプタ一覧を `{説明: アダプタ名}` の JSON で返します。 |
| `String soem.slaveinfo(boolean printSDO, boolean printMAP)` | Queries discovered slaves and PDO/CoE SDO dictionaries under mailbox lock, returning JSON string. **Returns Java `null` if unloaded, or JSON string `"null"` if loaded but `init()` was not called**.<br>スレーブ・PDO・SDO 構成を JSON 文字列で返します（ライブラリ未ロード時は `null`、未 `init()` 時は JSON 文字列 `"null"`）。 |

#### Exact JSON Schemas for `soem` / JSON 形式

##### 1. `soem.find_adapters()`
```json
{
  "Intel(R) Ethernet Connection": "\\Device\\NPF_{12345678-90AB-CDEF-1234-567890ABCDEF}",
  "eth0": "eth0"
}
```

##### 2. `soem.slaveinfo(printSDO, printMAP)`
Serialized with default `Gson` (all `null` fields are omitted):
```json
{
  "SlaveCount": 1,
  "ExpectedWKC": 3,
  "Slavelist": [
    {
      "Index": 1,
      "Name": "EL1008",
      "OutputBits": 0,
      "InputBits": 8,
      "State": 8,
      "ALStatusCode": 0,
      "ALStatusCodeString": "No error",
      "PropagationDelay": 0,
      "HasDC": 1,
      "ParentPort": 0,
      "ActivePorts": 1,
      "ConfigAddress": 4097,
      "EEP_MAN": 2,
      "EEP_ID": 66072658,
      "EEP_REV": 1048576,
      "SMlist": [ { "Index": 0, "StartAddress": 4096, "SMLength": 1, "SMFlags": 65536, "SMType": 4 } ],
      "FMMUlist": [ { "Index": 0, "LogStart": 0, "LogLength": 1, "LogStartbit": 0, "LogEndbit": 7, "PhysStart": 4096, "PhysStartBit": 0, "FMMUType": 1, "FMMUActive": 1 } ],
      "MailboxProtocols": 4,
      "RxPDOlist": [
        { "SyncM": 2, "Index": 2, "Name": "Outputs", "SIIlist": [ { "AbsOffset": 0, "AbsBit": 0, "ObjectIndex": 28672, "ObjectSubIndex": 1, "BitLength": 1, "ObjectDataType": "BOOLEAN", "Name": "Output" } ] }
      ],
      "TxPDOlist": [],
      "SDO": {
        "ODlistEntries": 1,
        "ODlist": [
          {
            "Index": 4096, "DataType": 7, "ObjectCode": 7, "DataTypeName": "VAR", "MaxSub": 0, "Name": "Device type",
            "OElist": [
              { "Index": 0, "DataType": 7, "BitLength": 32, "DataTypeName": "UNSIGNED32", "ObjectAccess": 7, "AccessType": "R_R_R_", "Name": "Device type", "SDO": "0x00001389 / 5001" }
            ]
          }
        ]
      }
    }
  ]
}
```
* **Conditional fields**: `EcatError` appears only when `ecaterror > 0`; `ParentPort` appears when `HasDC > 0`; `SDO` appears only when `printSDO == true` and CoE is supported; `RxPDOlist` / `TxPDOlist` appear only when `printMAP == true` (for CoE SDO-mapped slaves `PDO.Index` holds SyncManager index while `SyncM`/`Name` are omitted).

---

## Wiki & Examples / Wiki とサンプル

* **DecoTofu Wiki**: [https://github.com/mizoguch-ken/DecoTofu/wiki](https://github.com/mizoguch-ken/DecoTofu/wiki)
  * **Application**: [Build and Run](https://github.com/mizoguch-ken/DecoTofu/wiki/BuildAndRun), [WebViewer](https://github.com/mizoguch-ken/DecoTofu/wiki/WebViewer), [Script API](https://github.com/mizoguch-ken/DecoTofu/wiki/ScriptAPI), [Plug-ins](https://github.com/mizoguch-ken/DecoTofu/wiki/Plugins)
  * **Ladder Logic**: [Ladders](https://github.com/mizoguch-ken/DecoTofu/wiki/Ladders)
  * **EtherCAT**: [EtherCAT](https://github.com/mizoguch-ken/DecoTofu/wiki/EtherCAT) (see also [GcodeFX EtherCAT Wiki](https://github.com/mizoguch-ken/GcodeFX/wiki/EtherCAT) and [Example](https://github.com/mizoguch-ken/GcodeFX/wiki/EtherCAT#example))
  * **Plug-ins**: [Serial](https://github.com/mizoguch-ken/DecoTofu/wiki/Serial), [Serial Test](https://github.com/mizoguch-ken/DecoTofu/wiki/SerialTest), [OnlineArt](https://github.com/mizoguch-ken/DecoTofu/wiki/OnlineArt), [Db](https://github.com/mizoguch-ken/DecoTofu/wiki/Db), [File](https://github.com/mizoguch-ken/DecoTofu/wiki/File), [Ftp](https://github.com/mizoguch-ken/DecoTofu/wiki/Ftp), [Jasper](https://github.com/mizoguch-ken/DecoTofu/wiki/Jasper), [WebCam](https://github.com/mizoguch-ken/DecoTofu/wiki/WebCam), [Dbg](https://github.com/mizoguch-ken/DecoTofu/wiki/Dbg), [CapAi](https://github.com/mizoguch-ken/DecoTofu/wiki/CapAi), and `Llm` / `Printer` (documented in [WebPlugins](https://github.com/mizoguch-ken/WebPlugins)).

---

## Licenses / ライセンス

* **DecoTofu**: The license of DecoTofu itself is not declared; all rights and copyright to the DecoTofu source code are reserved by the author (`mizoguch-ken`).
  * **DecoTofu 本体**: DecoTofu 自体のライセンスは未宣言であり、DecoTofu のコードの著作権は作者（`mizoguch-ken`）に留保されます。
* **Third-Party Libraries**: Displayed via `stage.showLicenses()` (full texts in `src/main/resources/LICENSES`):
  * **第三者ライブラリ**: `stage.showLicenses()` で表示されます（全文は `src/main/resources/LICENSES` に収録）：

| Component / コンポーネント | License / ライセンス |
| :--- | :--- |
| **SOEM (Simple Open EtherCAT Master Library)** | GPL-2.0 with a special exception (an EtherCAT Master License from Beckhoff Automation GmbH is required for an EtherCAT network master) |
| **Java Diff Utils** | Apache-2.0 |
| **Gson** | Apache-2.0 |
| **JNR-FFI, JFFI** | Apache-2.0 or LGPL-3.0 or later |
| **JNR a64asm** | Apache-2.0 |
| **JNR x86asm** | MIT |
| **Apache PDFBox (including FontBox)** | Apache-2.0 |
| **Apache Commons Logging** | Apache-2.0 |
| **ASM (`asm`, `asm-analysis`, `asm-commons`, `asm-tree`, `asm-util`)** | BSD-3-Clause |
| **Myrica, MyricaM (PDF export fonts based on M+ FONTS)** | SIL Open Font License 1.1 and M+ FONTS notice |
| **Ace Editor (`web/`, ace-builds 1.44.0)** | BSD-3-Clause (`web/LICENSE`) |

* **JavaFX**: Provided by the JDK rather than bundled in this repository; subject to the license of the JDK's OpenJFX distribution (GPL-2.0 with Classpath Exception).
  * **JavaFX**: このプロジェクトには含まれず JDK 側から提供され、OpenJFX 自身のライセンス（Classpath Exception 付き GPL-2.0）に従います。
* **SOEM Shared Library**: Not distributed in this repository; built by the user from [DecoTofu-SOEM](https://github.com/mizoguch-ken/DecoTofu-SOEM).
  * **SOEM 共有ライブラリ**: このリポジトリには含まれず、ユーザーが [DecoTofu-SOEM](https://github.com/mizoguch-ken/DecoTofu-SOEM) からビルドして使用します。
