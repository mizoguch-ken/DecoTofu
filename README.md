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
### <u>Requirement</u>

### <u>要件</u>

 * Java 17 or later
 * Java 17 以上
### <u>Dependency</u>

### <u>依存</u>

    <dependency>
      <groupId>com.googlecode.java-diff-utils</groupId>
      <artifactId>diffutils</artifactId>
    </dependency>
    <dependency>
      <groupId>com.google.code.gson</groupId>
      <artifactId>gson</artifactId>
    </dependency>
    <dependency>
      <groupId>com.github.jnr</groupId>
      <artifactId>jnr-ffi</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.pdfbox</groupId>
      <artifactId>pdfbox</artifactId>
    </dependency>
### <u>JS Functions</u>

### <u>JS関数</u>

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
### <u>Configuration</u>

### <u>設定</u>

- [EtherCAT](https://github.com/mizoguch-ken/GcodeFX/wiki/EtherCAT)
  - [Example](https://github.com/mizoguch-ken/GcodeFX/wiki/EtherCAT#example)

### <u>Example</u>

### <u>例</u>

- [Ladders](https://github.com/mizoguch-ken/DecoTofu/wiki/Ladders)
- [Serial Test](https://github.com/mizoguch-ken/DecoTofu/wiki/SerialTest)

