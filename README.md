# <u>DecoTofu</u>

### <u>Overview</u>

### <u>概要</u>

 * tools for action
 * 行動するためのツール
### <u>Description</u>

### <u>説明</u>

 * Display and control by WebViewer
 * WebViewerを使った表示とコントロール
 * Plug-in function to WebViewer
 * WebViewerへのプラグイン機能
 * Unique ladder logic
 * 独特なラダーロジック
 * EtherCAT master (SOEM) by a native shared library
 * ネイティブ共有ライブラリによるEtherCATマスタ(SOEM)
 * The HTML page web/index.html is the user interface of the application
 * アプリケーションのユーザーインターフェースはHTMLページ web/index.html
### <u>Requirement</u>

### <u>要件</u>

 * Java 21 or later JDK which includes JavaFX (javafx.web, javafx.fxml, javafx.swing, javafx.controls)
 * JavaFX(javafx.web, javafx.fxml, javafx.swing, javafx.controls)を含むJava 21以上のJDK
 * Maven 3 or later
 * Maven 3 以上
 * CMake 3.28 or later, Visual Studio (Windows) and Npcap installed in WinPcap API-compatible Mode, to build the SOEM shared library of https://github.com/mizoguch-ken/DecoTofu-SOEM
 * SOEM共有ライブラリ( https://github.com/mizoguch-ken/DecoTofu-SOEM )の作成にはCMake 3.28以上、Visual Studio(Windows)、WinPcap API互換モードでのNpcapのインストールが必要
### <u>Build</u>

### <u>ビルド</u>

    mvn package

 * the executable jar with the dependencies is created as target/DecoTofu.jar
 * 依存ライブラリを含む実行可能jarが target/DecoTofu.jar として作成される
 * the jar with the classifier bin and the dependency reduced pom are created too, the dependency reduced pom is not a part of the repository
 * classifierがbinのjarと依存を削減したpomも作成されますが、依存を削減したpomはリポジトリに含まれません

    mvn test

 * the unit tests are run on the classpath
 * ユニットテストはクラスパス上で実行される
### <u>Run</u>

### <u>起動</u>

    java -jar target/DecoTofu.jar

 * web/index.html is loaded at startup, and it is created from a template when it does not exist
 * 起動時に web/index.html が読み込まれ、存在しない場合はテンプレートから作成される
 * the page created from the template keeps the title DecoTofu and an empty body
 * テンプレートから作成されるページはタイトルがDecoTofuで本文は空です
 * the window title is the title of the loaded page
 * ウィンドウのタイトルは読み込まれたページのタイトル
 * properties.xml keeps the window settings : STAGE_MAXIMIZED, STAGE_WIDTH, STAGE_HEIGHT
 * properties.xml にはウィンドウの設定 STAGE_MAXIMIZED, STAGE_WIDTH, STAGE_HEIGHT が保存される
 * a damaged value of properties.xml is reported and its default value is used
 * properties.xml の値が壊れている場合は報告され、既定値が使われる
 * the directory of the application is the current directory when it holds properties.xml or web, else it is the directory of the jar or of its parents
 * アプリケーションのディレクトリは properties.xml か web を置く現在のディレクトリで、無い場合はjarかその親のディレクトリ
 * if a log directory exists next to the jar, the console output is written to log/Console_yyyyMMddHHmmss.txt
 * jarと同じ場所に log ディレクトリがある場合、コンソール出力は log/Console_yyyyMMddHHmmss.txt に書き出される
 * the released jar is available at https://github.com/mizoguch-ken/DecoTofu/releases
 * リリースされたjarは https://github.com/mizoguch-ken/DecoTofu/releases からダウンロードできる
### <u>Dependency</u>

### <u>依存</u>

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

 * JavaFX is required by module-info.java, but it is not declared here, so it must be provided by the JDK
 * JavaFXはmodule-info.javaで必要とされるがここでは宣言されていないため、JDK側で提供される必要がある
### <u>Project</u>

### <u>プロジェクト構成</u>

    src/main/java/ken/mizoguch/decotofu  : application entry and native library loader
    src/main/java/ken/mizoguch/webviewer : WebViewer and the plug-ins of the WebViewer
    src/main/java/ken/mizoguch/ladders   : ladder editor and the ladder cycle
    src/main/java/ken/mizoguch/soem      : SOEM(EtherCAT) binding by jnr-ffi
    src/main/java/ken/mizoguch/console   : console output
    src/main/java/module-info.java       : module definition
    src/main/resources/fxml              : user interface definitions
    src/main/resources/icons             : window icons
    src/main/resources/font              : fonts used by the PDF export
    src/main/resources/LICENSES          : licenses shown by stage.showLicenses()
    src/test/java                        : unit tests
    web                                  : the Ace editor and the page used by the WebViewer
    web/index.html                       : the page loaded at startup, the kitchen sink page of the Ace editor here
    web/plugins                          : plug-in jar files loaded at startup, built in the WebPlugins repository
    properties.xml                       : window settings
    ladder/properties.xml                : ladder editor settings

### <u>Plugin</u>

### <u>プラグイン</u>

 * a plug-in is the jar in the web/plugins directory whose Main-Class implements WebViewerPlugin
 * プラグインは web/plugins ディレクトリに置く、Main-ClassがWebViewerPluginを実装するjar
 * the value of functionName() is set as a member of the window object, so it is the name of the JS functions
 * functionName()の値がwindowオブジェクトのメンバーとして設定され、JS関数の名前になる
 * the web/plugins directory is optional, the plug-ins are loaded when it exists
 * web/plugins ディレクトリは任意で、存在するときプラグインが読み込まれる
 * when the page loading is succeeded, setup() of the page is called if it is defined
 * ページの読み込みが成功したとき、定義されていればページのsetup()が呼び出される
 * the plug-ins are built in https://github.com/mizoguch-ken/WebPlugins : its jar keeps its module name without a version (Serial.jar, Db.jar, Dbg.jar, File.jar, Ftp.jar, Jasper.jar, OnlineArt.jar, WebCam.jar, CapAiClient.jar, CapAiClient2.jar, CapAiClient3.jar, CapAiServer.jar)
 * プラグインは https://github.com/mizoguch-ken/WebPlugins で作成され、jarはバージョンを含まないモジュール名です(Serial.jar, Db.jar, Dbg.jar, File.jar, Ftp.jar, Jasper.jar, OnlineArt.jar, WebCam.jar, CapAiClient.jar, CapAiClient2.jar, CapAiClient3.jar, CapAiServer.jar)
 * the page asks a plug-in for the licenses of the libraries it carries : plug-in.licenses() is a public method of the plug-in, its text is kept in the licenses.txt of the package of the plug-in
 * ページはプラグインが含むライブラリのライセンスを要求します : plug-in.licenses()はプラグインのpublicメソッドで、その文はプラグインのパッケージのlicenses.txtに保持されます
 * a jar whose Main-Class does not implement WebViewerPlugin is not used, a plug-in which can not be loaded is reported and the other plug-ins are still loaded
 * Main-ClassがWebViewerPluginを実装しないjarは使われず、読み込めないプラグインは報告されますが、他のプラグインは読み込まれます
 * the plug-ins and their functions are described in the wiki : https://github.com/mizoguch-ken/DecoTofu/wiki/Plugins
 * プラグインとその関数はwikiのPlug-insページに説明があります

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

 * the JS code of the page calls the public methods of the plug-in : myplugin.hello()
 * ページのJSコードはプラグインのpublicメソッドを呼び出す : myplugin.hello()
### <u>Ladder</u>

### <u>ラダー</u>

 * a ladder is edited in the ladder window and it is saved as a JSON file
 * ラダーはラダーウィンドウで編集され、JSONファイルとして保存される
 * File(New, Open, Save, Save As.., Export as PDF, Difference, Recent Files), Edit(Undo, Redo, Cut, Copy, Paste, Select, Tab), View(Tab Select, Find Blocks), Tools(Connect, Run, Stop)
 * ファイル(New, Open, Save, Save As.., Export as PDF, Difference, Recent Files)、編集(Undo, Redo, Cut, Copy, Paste, Select, Tab)、表示(Tab Select, Find Blocks)、ツール(Connect, Run, Stop)
 * blocks : LD /LD @LD /@LD %LD /%LD, OUT /OUT @OUT /@OUT %OUT /%OUT, = <> < <= > >= & | ^, SET RES AND OR XOR NOT ADD SUB MUL DIV AVE SFL SFR SIG RND TIM /TIM CNT /CNT MOV SCR, comment(;)
 * ブロック : LD /LD @LD /@LD %LD /%LD、OUT /OUT @OUT /@OUT %OUT /%OUT、= <> < <= > >= & | ^、SET RES AND OR XOR NOT ADD SUB MUL DIV AVE SFL SFR SIG RND TIM /TIM CNT /CNT MOV SCR、コメント(;)
 * an address which does not start with "." is global, an address which starts with "." is local to the ladder
 * "."で始まらないアドレスはグローバル、"."で始まるアドレスはそのラダーのローカル
 * the ladder is connected by ladders.connect() and it cycles by ladders.run()
 * ラダーはladders.connect()で接続され、ladders.run()でサイクル実行される
 * an error of the ladder cycle is reported and the cycle is stopped when the error is repeated
 * ラダーサイクルのエラーは報告され、エラーが続く場合はサイクルが停止する
 * an io which is not registered is reported only once and its block is ignored by the cycle
 * 登録されていないI/Oは一度だけ報告され、そのブロックはサイクルが無視する
 * the SCR block script of a loaded ladder is run after its confirmation, and it is removed when the confirmation is refused
 * 読み込んだラダーのSCRブロックのスクリプトは確認後に実行され、確認が拒否された場合は除去される
 * the scripts are removed when they can not be confirmed, as when a page loads the ladder without the user
 * ユーザーの確認ができない場合(ページによる読み込みなど)もスクリプトは除去される
 * ladder/properties.xml keeps the ladder settings : HISTORY_GENERATION, IDEAL_CYCLE_TIME, LADDER_SCRIPT_ENABLE
 * ladder/properties.xml にはラダーの設定 HISTORY_GENERATION, IDEAL_CYCLE_TIME, LADDER_SCRIPT_ENABLE が保存される

### <u>JS Functions</u>

### <u>JS関数</u>

 * the functions are added to the window object after the page loading is succeeded
 * 関数はページの読み込み成功後にwindowオブジェクトに追加される
 * a file path given to a function is used from the current directory, and it is taken from the directory of the application when the file is not in the current directory
 * 関数に渡すファイルパスは現在のディレクトリ基準で、そこにファイルが無い場合はアプリケーションのディレクトリから使われる
 * a name given for a notification is a javascript function name only : a name which carries an other script is refused
 * 通知に渡す名前はjavascriptの関数名だけで、他のスクリプトを含む名前は拒否される

[stage]

* void stage.showLicenses()
* void stage.showVersion()
* Boolean stage.isShowing()
* void stage.show()
* void stage.hide()
* Boolean stage.isFocused()
* void stage.requestFocus()
* String stage.getTitle()
* void stage.setTitle(String title)
* Double stage.getWidth()
* void stage.setWidth(double value)
* Double stage.getMinWidth()
* void stage.setMinWidth(double value)
* Double stage.getMaxWidth()
* void stage.setMaxWidth(double value)
* Double stage.getHeight()
* void stage.setHeight(double value)
* Double stage.getMinHeight()
* void stage.setMinHeight(double value)
* Double stage.getMaxHeight()
* void stage.setMaxHeight(double value)
* Double stage.getOpacity()
* void stage.setOpacity(double value)
* Boolean stage.isAlwaysOnTop()
* void stage.setAlwaysOnTop(boolean state)
* Boolean stage.isMaximized()
* void stage.setMaximized(boolean state)
* Double stage.getX()
* void stage.setX(double value)
* Double stage.getY()
* void stage.setY(double value)
* void stage.toBack()
* void stage.toFront()

[ladders]

* Boolean ladders.isShowing()
* void ladders.show()
* void ladders.hide()
* Boolean ladders.isFocused()
* void ladders.requestFocus()
* String ladders.getTitle()
* Double ladders.getWidth()
* void ladders.setWidth(double value)
* Double ladders.getMinWidth()
* void ladders.setMinWidth(double value)
* Double ladders.getMaxWidth()
* void ladders.setMaxWidth(double value)
* Double ladders.getHeight()
* void ladders.setHeight(double value)
* Double ladders.getMinHeight()
* void ladders.setMinHeight(double value)
* Double ladders.getMaxHeight()
* void ladders.setMaxHeight(double value)
* Double ladders.getOpacity()
* void ladders.setOpacity(double value)
* Boolean ladders.isAlwaysOnTop()
* void ladders.setAlwaysOnTop(boolean state)
* Boolean ladders.isMaximized()
* void ladders.setMaximized(boolean state)
* Double ladders.getX()
* void ladders.setX(double value)
* Double ladders.getY()
* void ladders.setY(double value)
* Integer ladders.getHistoryGeneration()
* void ladders.setHistoryGeneration(int historyGeneration)
* Long ladders.getIdealCycleTime()
* void ladders.setIdealCycleTime(long idealCycleTime)
* void ladders.registerWeb()
* void ladders.unregisterWeb()
* Boolean ladders.registerSoemIn(String address, int slave, long bitsOffset, long bitsMask)
* Boolean ladders.registerSoemOut(String address, int slave, long bitsOffset, long bitsMask)
* void ladders.unregisterSoem()
* Double ladders.getValue(String address)
* void ladders.setValue(String address, double value)
* Boolean ladders.connect()
* Boolean ladders.run()
* void ladders.stop()
* Boolean ladders.fileNew()
* Boolean ladders.fileOpen(String path)
* Boolean ladders.fileSave()
* String ladders.getFileName()

[soem]

* Soem soem.getSoem()
* Boolean soem.load(String libraryPath)
* Boolean soem.init(int size)
* Boolean soem.po2so(int slave, long eep_man, long eep_id, String func)
* Boolean soem.start(String ifname, long cycletime)
* Boolean soem.start_redundant(String ifname, String ifname2, long cycletime)
* void soem.setNotifyErrorSafeOpError(String func)
* void soem.setNotifyErrorLost(String func)
* void soem.setNotifyWarningSafeOp(String func)
* void soem.setNotifyMessageReconfigured(String func)
* void soem.setNotifyMessageRecovered(String func)
* void soem.setNotifyMessageFound(String func)
* void soem.setNotifyMessageAllSlavesResumedOperational(String func)
* Integer soem.slavecount()
* Integer soem.state(int slave)
* Boolean soem.islost(int slave)
* Boolean soem.docheckstate()
* Byte[] soem.sdoRead(int slave, int index, int subIndex, int byteSize)
* Integer soem.sdoWrite(int slave, int index, int subIndex, int byteSize, Object value)
* Long soem.in(int slave, long bitsOffset, long bitsMask)
* Long soem.outs(int slave, long bitsOffset, long bitsMask)
* Long soem.out(int slave, long bitsOffset, long bitsMask, long value)
* String soem.find_adapters()
* String soem.slaveinfo(boolean printSDO, boolean printMAP)
### <u>Wiki</u>

### <u>Wiki</u>

 * the wiki pages describe the features and the plug-ins : https://github.com/mizoguch-ken/DecoTofu/wiki
 * wikiのページが機能とプラグインを説明しています : https://github.com/mizoguch-ken/DecoTofu/wiki
 * application : [Build and Run](https://github.com/mizoguch-ken/DecoTofu/wiki/BuildAndRun), [WebViewer](https://github.com/mizoguch-ken/DecoTofu/wiki/WebViewer), [Script API](https://github.com/mizoguch-ken/DecoTofu/wiki/ScriptAPI), [Plug-ins](https://github.com/mizoguch-ken/DecoTofu/wiki/Plugins)
 * アプリケーション : Build and Run、WebViewer、Script API、Plug-ins
 * ladder : [Ladders](https://github.com/mizoguch-ken/DecoTofu/wiki/Ladders)
 * ラダー : Ladders
 * EtherCAT : [EtherCAT](https://github.com/mizoguch-ken/DecoTofu/wiki/EtherCAT)
 * EtherCAT : EtherCAT
 * plug-ins : [Serial](https://github.com/mizoguch-ken/DecoTofu/wiki/Serial), [Serial Test](https://github.com/mizoguch-ken/DecoTofu/wiki/SerialTest), [OnlineArt](https://github.com/mizoguch-ken/DecoTofu/wiki/OnlineArt), [Db](https://github.com/mizoguch-ken/DecoTofu/wiki/Db), [File](https://github.com/mizoguch-ken/DecoTofu/wiki/File), [Ftp](https://github.com/mizoguch-ken/DecoTofu/wiki/Ftp), [Jasper](https://github.com/mizoguch-ken/DecoTofu/wiki/Jasper), [WebCam](https://github.com/mizoguch-ken/DecoTofu/wiki/WebCam), [Dbg](https://github.com/mizoguch-ken/DecoTofu/wiki/Dbg), [CapAi](https://github.com/mizoguch-ken/DecoTofu/wiki/CapAi)
 * プラグイン : Serial、Serial Test、OnlineArt、Db、File、Ftp、Jasper、WebCam、Dbg、CapAi
### <u>Configuration</u>

### <u>設定</u>

- [EtherCAT](https://github.com/mizoguch-ken/DecoTofu/wiki/EtherCAT)
  - the shared library of SOEM is built by the user in https://github.com/mizoguch-ken/DecoTofu-SOEM
- [EtherCAT of GcodeFX](https://github.com/mizoguch-ken/GcodeFX/wiki/EtherCAT)
  - the configuration of the SOEM shared library of the other application of the same binding
  - [Example](https://github.com/mizoguch-ken/GcodeFX/wiki/EtherCAT#example)
### <u>Example</u>

### <u>例</u>

- [Ladders](https://github.com/mizoguch-ken/DecoTofu/wiki/Ladders)
- [Serial Test](https://github.com/mizoguch-ken/DecoTofu/wiki/SerialTest)

 * the index page of the web directory of this repository is the kitchen sink page of the Ace editor, the page of the Serial Test example is written in its wiki page
 * このリポジトリのwebディレクトリのindexページはAceエディタのkitchen sinkページで、Serial Testの例のページはそのwikiのページに書かれています

### <u>Licenses</u>

### <u>ライセンス</u>

 * the license of DecoTofu itself is not declared, so the copyright of the DecoTofu code is reserved by the author
 * DecoTofu 自体のライセンスは未宣言で、DecoTofu のコードの著作権は作者に留保されます
 * only the licenses of the third parties are listed here, the full texts are in src/main/resources/LICENSES which stage.showLicenses() displays
 * ここでは第三者のライセンスのみを掲載しており、全文は stage.showLicenses() が表示する src/main/resources/LICENSES にあります

    SOEM (Simple Open EtherCAT Master Library)                        GPL-2.0 with a special exception, and the EtherCAT Master License of Beckhoff Automation GmbH is required for an EtherCAT network master
    Java Diff Utils                                                   Apache-2.0
    Gson                                                              Apache-2.0
    Jnr Ffi, Jffi                                                     Apache-2.0 or LGPL-3.0 or later
    Jnr a64asm                                                        Apache-2.0
    Jnr X86asm                                                        MIT
    Apache PDFBox (FontBox included)                                  Apache-2.0
    Apache Commons Logging                                            Apache-2.0
    ASM, ASM Analysis, ASM Commons, ASM Tree, ASM Util                BSD-3-Clause
    Myrica, MyricaM (the fonts used by the PDF export, based on M+ FONTS)   SIL Open Font License 1.1, and the M+ FONTS notice

 * the web directory contains the Ace editor (ace-builds 1.44.0), it is BSD-3-Clause and the text is web/LICENSE
 * web ディレクトリには Ace エディタ(ace-builds 1.44.0)が含まれており、BSD-3-Clause で、その文は web/LICENSE です
 * JavaFX is not a part of this project, it is provided by the JDK and the terms of JavaFX itself apply (OpenJFX : GPL-2.0 with the Classpath Exception)
 * JavaFXはこのプロジェクトの一部ではなくJDKが提供するもので、JavaFX自身の条件に従います(OpenJFX : Classpath Exception付きGPL-2.0)
 * the SOEM shared library is not a part of this project either, it must be built by the user from the SOEM source : https://github.com/mizoguch-ken/DecoTofu-SOEM
 * SOEM共有ライブラリもこのプロジェクトには含まれておらず、ユーザーがSOEMのソースから作成します : https://github.com/mizoguch-ken/DecoTofu-SOEM