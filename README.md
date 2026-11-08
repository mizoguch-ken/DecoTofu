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
* **Window Settings (`properties.xml`)**: Persists `STAGE_MAXIMIZED`, `STAGE_WIDTH`, and `STAGE_HEIGHT`. If `properties.xml` contains a corrupted value, it is reported to the console and replaced with its default value (`640.0` × `480.0`).
  * **ウィンドウ設定 (`properties.xml`)**: `STAGE_MAXIMIZED`、`STAGE_WIDTH`、`STAGE_HEIGHT` を保存します。破損した値がある場合はコンソールに報告され、既定値（`640.0` × `480.0`）が使用されます。
* **Application Directory Resolution**: Uses the current working directory if it contains `properties.xml` or `web`; otherwise resolves to the directory containing `DecoTofu.jar` (or its parent directory).
  * **アプリケーションディレクトリの解決**: カレントディレクトリに `properties.xml` または `web` が存在する場合はカレントディレクトリを使用し、存在しない場合は `DecoTofu.jar` の配置ディレクトリ（またはその親ディレクトリ）を使用します。
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
| `src/main/resources/fxml` | JavaFX FXML layout definitions (`DesignWeb.fxml`, `DesignLadders.fxml`)<br>JavaFX 画面定義 (FXML) |
| `src/main/resources/icons` | Application window icons (`icon_032.png` – `icon_512.png`)<br>ウィンドウアイコン |
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
* **JavaScript Binding**: The return value of `functionName()` becomes a property on the page's `window` object (`window.<functionName>`), exposing all `public` methods of the plug-in class to JavaScript.
  * **JavaScript バインディング**: `functionName()` の戻り値がページの `window` オブジェクトのプロパティ（`window.<functionName>`）として登録され、プラグインの `public` メソッドが JavaScript から呼び出せるようになります。
* **Lifecycle**: When the page finishes loading (`Worker.State.SUCCEEDED`), all plug-in objects are bound to `window`, and `setup()` in the page's JavaScript is automatically called if defined.
  * **ライフサイクル**: ページの読み込みが完了（`Worker.State.SUCCEEDED`）すると、すべてのプラグインが `window` に登録され、ページ内に `setup()` 関数が定義されていれば自動的に呼び出されます。
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

* **Editing & Persistence**: Ladders are edited in the Ladder Window and saved as JSON files.
  * **編集と保存**: ラダーはラダーウィンドウで編集し、JSON ファイルとして保存します。
* **Menus**:
  * `File`: `New`, `Open`, `Save`, `Save As..`, `Export as PDF`, `Difference`, `Recent Files`
  * `Edit`: `Undo`, `Redo`, `Cut`, `Copy`, `Paste`, `Select`, `Tab`
  * `View`: `Tab Select`, `Find Blocks`
  * `Tools`: `Connect`, `Run`, `Stop`
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
  * When loading a ladder containing `SCR` (JavaScript execution) blocks, DecoTofu prompts the user for confirmation before allowing script execution. If refused—or if loaded headlessly without user confirmation—the scripts are stripped automatically.
  * `SCR`（スクリプト）ブロックを含むラダーを読み込む際、実行前にユーザーへ確認ダイアログを表示します。拒否された場合やユーザー確認が行えない状態（スクリプトからの自動読み込み等）では、スクリプトは自動的に除去されます。
* **Ladder Settings (`ladder/properties.xml`)**: Stores `HISTORY_GENERATION`, `IDEAL_CYCLE_TIME`, and `LADDER_SCRIPT_ENABLE`.
  * **ラダー設定 (`ladder/properties.xml`)**: `HISTORY_GENERATION`、`IDEAL_CYCLE_TIME`、`LADDER_SCRIPT_ENABLE` を保存します。

---

## Built-in JavaScript API / 組み込み JS 関数

DecoTofu registers three built-in objects (`stage`, `ladders`, `soem`) on the `window` object when page loading succeeds:

* **Path Resolution**: Relative file paths passed to `ladders.fileOpen(...)` or `soem.load(...)` are resolved against the current working directory first, falling back to the application directory if not found.
  * **ファイルパスの解決**: 関数に渡された相対パスはまずカレントディレクトリ基準で検索され、存在しない場合はアプリケーションディレクトリ基準で解決されます。
* **Callback Validation**: Notification callback names passed to `soem.po2so(...)` or `soem.setNotify*(...)` are validated against `^[A-Za-z_$][A-Za-z0-9_$]*(\.[A-Za-z_$][A-Za-z0-9_$]*)*\z` to prevent script injection.
  * **コールバック関数名の検証**: 通知用に渡す関数名は正規表現で検証され、不正なスクリプト文字列を含む名前は拒否されます。

### `stage` (Main Window / メインウィンドウ制御)

```text
void    stage.showLicenses()
void    stage.showVersion()
Boolean stage.isShowing()
void    stage.show()
void    stage.hide()
Boolean stage.isFocused()
void    stage.requestFocus()
String  stage.getTitle()
void    stage.setTitle(String title)
Double  stage.getWidth()
void    stage.setWidth(double value)
Double  stage.getMinWidth()
void    stage.setMinWidth(double value)
Double  stage.getMaxWidth()
void    stage.setMaxWidth(double value)
Double  stage.getHeight()
void    stage.setHeight(double value)
Double  stage.getMinHeight()
void    stage.setMinHeight(double value)
Double  stage.getMaxHeight()
void    stage.setMaxHeight(double value)
Double  stage.getOpacity()
void    stage.setOpacity(double value)
Boolean stage.isAlwaysOnTop()
void    stage.setAlwaysOnTop(boolean state)
Boolean stage.isMaximized()
void    stage.setMaximized(boolean state)
Double  stage.getX()
void    stage.setX(double value)
Double  stage.getY()
void    stage.setY(double value)
void    stage.toBack()
void    stage.toFront()
```

### `ladders` (Ladder Window & Engine / ラダー制御)

```text
Boolean ladders.isShowing()
void    ladders.show()
void    ladders.hide()
Boolean ladders.isFocused()
void    ladders.requestFocus()
String  ladders.getTitle()
Double  ladders.getWidth()
void    ladders.setWidth(double value)
Double  ladders.getMinWidth()
void    ladders.setMinWidth(double value)
Double  ladders.getMaxWidth()
void    ladders.setMaxWidth(double value)
Double  ladders.getHeight()
void    ladders.setHeight(double value)
Double  ladders.getMinHeight()
void    ladders.setMinHeight(double value)
Double  ladders.getMaxHeight()
void    ladders.setMaxHeight(double value)
Double  ladders.getOpacity()
void    ladders.setOpacity(double value)
Boolean ladders.isAlwaysOnTop()
void    ladders.setAlwaysOnTop(boolean state)
Boolean ladders.isMaximized()
void    ladders.setMaximized(boolean state)
Double  ladders.getX()
void    ladders.setX(double value)
Double  ladders.getY()
void    ladders.setY(double value)
Integer ladders.getHistoryGeneration()
void    ladders.setHistoryGeneration(int historyGeneration)
Long    ladders.getIdealCycleTime()
void    ladders.setIdealCycleTime(long idealCycleTime)
void    ladders.registerWeb()
void    ladders.unregisterWeb()
Boolean ladders.registerSoemIn(String address, int slave, long bitsOffset, long bitsMask)
Boolean ladders.registerSoemOut(String address, int slave, long bitsOffset, long bitsMask)
void    ladders.unregisterSoem()
Double  ladders.getValue(String address)
void    ladders.setValue(String address, double value)
Boolean ladders.connect()
Boolean ladders.run()
void    ladders.stop()
Boolean ladders.fileNew()
Boolean ladders.fileOpen(String path)
Boolean ladders.fileSave()
String  ladders.getFileName()
```

### `soem` (EtherCAT Master / EtherCAT マスタ制御)

```text
Soem    soem.getSoem()
Boolean soem.load(String libraryPath)
Boolean soem.init(int size)
Boolean soem.po2so(int slave, long eep_man, long eep_id, String func)
Boolean soem.start(String ifname, long cycletime)
Boolean soem.start_redundant(String ifname, String ifname2, long cycletime)
void    soem.setNotifyErrorSafeOpError(String func)
void    soem.setNotifyErrorLost(String func)
void    soem.setNotifyWarningSafeOp(String func)
void    soem.setNotifyMessageReconfigured(String func)
void    soem.setNotifyMessageRecovered(String func)
void    soem.setNotifyMessageFound(String func)
void    soem.setNotifyMessageAllSlavesResumedOperational(String func)
Integer soem.slavecount()
Integer soem.state(int slave)
Boolean soem.islost(int slave)
Boolean soem.docheckstate()
Byte[]  soem.sdoRead(int slave, int index, int subIndex, int byteSize)
Integer soem.sdoWrite(int slave, int index, int subIndex, int byteSize, Object value)
Long    soem.in(int slave, long bitsOffset, long bitsMask)
Long    soem.outs(int slave, long bitsOffset, long bitsMask)
Long    soem.out(int slave, long bitsOffset, long bitsMask, long value)
String  soem.find_adapters()
String  soem.slaveinfo(boolean printSDO, boolean printMAP)
```

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
