/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import ken.mizoguch.console.Console;

/**
 *
 * @author mizoguch-ken
 */
public class Ladder {

    private final List<LadderGrid> ladderGrids_;
    private final List<LadderGrid> inGrids_;
    private final List<LadderGrid> outGrids_;
    private final Map<Integer, List<LadderGrid>> inConnectGrids_;
    private final Map<Integer, List<LadderGrid>> outConnectGrids_;

    private int[] connectNumbers_;
    private int[] runConnectNumbers_;
    /**
     * addresses whose io is not registered in the io map : every address is reported
     * only once, so that the cycle does not write a message at every cycle
     */
    private final Set<String> runIoErrorSet_;
    private int column_, row_;
    private int idx_;
    private String name_;
    private boolean isConnectFailed_;

    /**
     *
     * @param idx
     * @param name
     * @param gridColumn
     * @param gridRow
     */
    public Ladder(int idx, String name, int gridColumn, int gridRow) {
        ladderGrids_ = new ArrayList<>();
        inGrids_ = new ArrayList<>();
        outGrids_ = new ArrayList<>();
        inConnectGrids_ = new HashMap<>();
        outConnectGrids_ = new HashMap<>();
        runIoErrorSet_ = new HashSet<>();

        column_ = gridColumn;
        row_ = gridRow;
        connectNumbers_ = new int[column_ * row_];
        runConnectNumbers_ = new int[column_ * row_];

        idx_ = idx;
        name_ = name;
        isConnectFailed_ = false;
    }

    /**
     *
     * @return
     */
    public Ladder copy() {
        Ladder ladder = new Ladder(idx_, name_, column_, row_);
        int index, size;

        for (index = 0, size = ladderGrids_.size(); index < size; index++) {
            ladder.getLadderGrids().add(ladderGrids_.get(index).copy());
        }
        ladder.connectGrids();
        return ladder;
    }

    /**
     *
     * @param grid
     */
    public void removeLadderGrid(LadderGrid grid) {
        ladderGrids_.remove(grid);
    }

    /**
     *
     * @param grid
     */
    public void addLadderGrid(LadderGrid grid) {
        ladderGrids_.add(grid);
    }

    /**
     *
     * @return
     */
    public List<LadderGrid> getLadderGrids() {
        return ladderGrids_;
    }

    /**
     *
     * @return
     */
    public List<LadderGrid> getInGrids() {
        return inGrids_;
    }

    /**
     *
     * @param inGrids
     */
    public void setInGrids(List<LadderGrid> inGrids) {
        int index, size;

        inGrids_.clear();
        for (index = 0, size = inGrids.size(); index < size; index++) {
            inGrids_.add(inGrids.get(index));
        }
    }

    /**
     *
     * @return
     */
    public List<LadderGrid> getOutGrids() {
        return outGrids_;
    }

    /**
     *
     * @param OutGrids
     */
    public void setOutGrids(List<LadderGrid> OutGrids) {
        int index, size;

        outGrids_.clear();
        for (index = 0, size = OutGrids.size(); index < size; index++) {
            outGrids_.add(OutGrids.get(index));
        }
    }

    /**
     *
     * @return
     */
    public int getColumn() {
        return column_;
    }

    /**
     *
     * @param column
     */
    public void setColumn(int column) {
        if (column_ != column) {
            column_ = column;
            connectNumbers_ = new int[column_ * row_];
        }
    }

    /**
     *
     * @return
     */
    public int getRow() {
        return row_;
    }

    /**
     *
     * @param row
     */
    public void setRow(int row) {
        if (row_ != row) {
            row_ = row;
            connectNumbers_ = new int[column_ * row_];
        }
    }

    /**
     *
     * @return
     */
    public int getIdx() {
        return idx_;
    }

    /**
     *
     * @param idx
     */
    public void setIdx(int idx) {
        idx_ = idx;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name_;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        name_ = name;
    }

    /**
     *
     * @param address
     * @param comment
     */
    public void setComments(String address, String comment) {
        LadderGrid grid;
        int index, size;

        for (index = 0, size = ladderGrids_.size(); index < size; index++) {
            grid = ladderGrids_.get(index);
            if (address.equals(grid.getAddress())) {
                grid.setComment(comment);
            }
        }
    }

    /**
     *
     */
    public void connectGrids() {
        LadderGrid[] gridUps = new LadderGrid[column_];
        LadderGrid grid, gridLeft;
        int columnIndex, columnSize, rowIndex, rowSize;

        for (rowIndex = 1, rowSize = row_; rowIndex < rowSize; rowIndex++) {
            gridLeft = null;
            for (columnIndex = 1, columnSize = column_; columnIndex < columnSize; columnIndex++) {
                grid = findGrid(columnIndex, rowIndex);
                grid.setLeftLadderGrid(gridLeft);
                grid.setUpLadderGrid(gridUps[columnIndex]);
                if (gridLeft != null) {
                    gridLeft.setRightLadderGrid(grid);
                }
                if (gridUps[columnIndex] != null) {
                    gridUps[columnIndex].setDownLadderGrid(grid);
                }
                gridUps[columnIndex] = grid;
                gridLeft = grid;
            }
        }
    }

    /**
     *
     * @param ioMap
     * @param ioTreeTable
     * @return
     */
    public boolean connectLadder(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            TreeTableView<LadderTreeTableIo> ioTreeTable) {
        LadderGrid grid = findGrid(1, 1);
        int connectNumberLast;

        Arrays.fill(connectNumbers_, 0);
        inGrids_.clear();
        outGrids_.clear();
        inConnectGrids_.clear();
        outConnectGrids_.clear();
        runIoErrorSet_.clear();
        isConnectFailed_ = false;

        connectNumberLast = 1;
        while (grid != null) {
            if (grid.getBlock() != LadderBlock.EMPTY) {
                if (connectNumbers_[blockIndex(grid)] == 0) {
                    connectNumberLast = connectRoute(ioMap, ioTreeTable, grid, 1, connectNumberLast);
                }
            }
            grid = grid.getDownLadderGrid();
        }
        outGrids_.sort((LadderGrid t, LadderGrid t1) -> {
            return t.getRowIndex() - t1.getRowIndex();
        });

        runConnectNumbers_ = new int[column_ * row_];
        return !isConnectFailed_;
    }

    private int connectRoute(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            TreeTableView<LadderTreeTableIo> ioTreeTable, LadderGrid grid, int connectNumber, int connectNumberLast) {
        LadderGrid lgrid, ugrid, rgrid, dgrid;
        String address, functionAddress;
        int idx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
        int funcIdx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;

        if (grid != null) {
            lgrid = grid.getLeftLadderGrid();
            ugrid = grid.getUpLadderGrid();
            rgrid = grid.getRightLadderGrid();
            dgrid = grid.getDownLadderGrid();

            if (connectNumbers_[blockIndex(grid)] == 0) {
                switch (grid.getBlock()) {
                    case LOAD:
                    case LOAD_NOT:
                    case LOAD_RISING:
                    case LOAD_RISING_NOT:
                    case LOAD_FALLING:
                    case LOAD_FALLING_NOT:
                        address = grid.getAddress();
                        if (address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                            idx = idx_;
                        }
                        if (!LadderNumber.isNumber(address)) {
                            registerIo(ioMap, ioTreeTable, idx, address);
                            grid.setInConnectNumber(connectNumber);
                            grid.setOutConnectNumber(0);
                            inGrids_.add(grid);
                            inConnectGrids_.putIfAbsent(connectNumber, new ArrayList<>());
                            inConnectGrids_.get(connectNumber).add(grid);
                        } else {
                            writeNumericAddressError(grid);
                        }
                        break;
                    case COMPARISON_EQUAL:
                    case COMPARISON_NOT_EQUAL:
                    case COMPARISON_LESS:
                    case COMPARISON_LESS_EQUAL:
                    case COMPARISON_GREATER:
                    case COMPARISON_GREATER_EQUAL:
                    case COMPARISON_AND_BITS:
                    case COMPARISON_OR_BITS:
                    case COMPARISON_XOR_BITS:
                        address = grid.getAddress();
                        if (!LadderNumber.isNumber(address)) {
                            if (address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                idx = idx_;
                            }
                            registerIo(ioMap, ioTreeTable, idx, address);
                            if (!grid.getBlockFunctions()[0].isNumber()) {
                                functionAddress = grid.getBlockFunctions()[0].getAddress();
                                if (!LadderNumber.isNumber(functionAddress)) {
                                    if (functionAddress.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                        funcIdx = idx_;
                                    }
                                    registerIo(ioMap, ioTreeTable, funcIdx, functionAddress);
                                } else {
                                    writeNumericAddressError(grid);
                                }
                            }
                            grid.setInConnectNumber(connectNumber);
                            grid.setOutConnectNumber(0);
                            inGrids_.add(grid);
                            inConnectGrids_.putIfAbsent(connectNumber, new ArrayList<>());
                            inConnectGrids_.get(connectNumber).add(grid);
                        } else {
                            writeNumericAddressError(grid);
                        }
                        break;
                    case OUT:
                    case OUT_NOT:
                    case OUT_RISING:
                    case OUT_RISING_NOT:
                    case OUT_FALLING:
                    case OUT_FALLING_NOT:
                    case SET:
                    case RESET:
                    case NOT_BITS:
                    case RANDOM:
                    case SCRIPT:
                        address = grid.getAddress();
                        if (!LadderNumber.isNumber(address)) {
                            if (address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                idx = idx_;
                            }
                            registerIo(ioMap, ioTreeTable, idx, address);
                            grid.setInConnectNumber(connectNumber);
                            grid.setOutConnectNumber(0);
                            outGrids_.add(grid);
                            inConnectGrids_.putIfAbsent(connectNumber, new ArrayList<>());
                            inConnectGrids_.get(connectNumber).add(grid);
                        } else {
                            writeNumericAddressError(grid);
                        }
                        break;
                    case AND_BITS:
                    case OR_BITS:
                    case XOR_BITS:
                    case ADDITION:
                    case SUBTRACTION:
                    case MULTIPLICATION:
                    case DIVISION:
                    case AVERAGE:
                    case SHIFT_LEFT_BITS:
                    case SHIFT_RIGHT_BITS:
                    case SIGMOID:
                        address = grid.getAddress();
                        if (!LadderNumber.isNumber(address)) {
                            if (address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                idx = idx_;
                            }
                            registerIo(ioMap, ioTreeTable, idx, address);
                            if (!grid.getBlockFunctions()[0].isNumber()) {
                                functionAddress = grid.getBlockFunctions()[0].getAddress();
                                if (!LadderNumber.isNumber(functionAddress)) {
                                    if (functionAddress.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                        funcIdx = idx_;
                                    }
                                    registerIo(ioMap, ioTreeTable, funcIdx, functionAddress);
                                } else {
                                    writeNumericAddressError(grid);
                                }
                            }
                            if (!grid.getBlockFunctions()[1].isNumber()) {
                                functionAddress = grid.getBlockFunctions()[1].getAddress();
                                if (!LadderNumber.isNumber(functionAddress)) {
                                    if (functionAddress.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                        funcIdx = idx_;
                                    }
                                    registerIo(ioMap, ioTreeTable, funcIdx, functionAddress);
                                } else {
                                    writeNumericAddressError(grid);
                                }
                            }
                            grid.setInConnectNumber(connectNumber);
                            grid.setOutConnectNumber(0);
                            outGrids_.add(grid);
                            inConnectGrids_.putIfAbsent(connectNumber, new ArrayList<>());
                            inConnectGrids_.get(connectNumber).add(grid);
                        } else {
                            writeNumericAddressError(grid);
                        }
                        break;
                    case TIMER:
                    case TIMER_NOT:
                    case COUNTER:
                    case COUNTER_NOT:
                    case MOVE:
                        address = grid.getAddress();
                        if (!LadderNumber.isNumber(address)) {
                            if (address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                idx = idx_;
                            }
                            registerIo(ioMap, ioTreeTable, idx, address);
                            if (!grid.getBlockFunctions()[0].isNumber()) {
                                functionAddress = grid.getBlockFunctions()[0].getAddress();
                                if (!LadderNumber.isNumber(functionAddress)) {
                                    if (functionAddress.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                        funcIdx = idx_;
                                    }
                                    registerIo(ioMap, ioTreeTable, funcIdx, functionAddress);
                                } else {
                                    writeNumericAddressError(grid);
                                }
                            }
                            grid.setInConnectNumber(connectNumber);
                            grid.setOutConnectNumber(0);
                            outGrids_.add(grid);
                            inConnectGrids_.putIfAbsent(connectNumber, new ArrayList<>());
                            inConnectGrids_.get(connectNumber).add(grid);
                        } else {
                            writeNumericAddressError(grid);
                        }
                        break;
                    default:
                        break;
                }
                connectNumbers_[blockIndex(grid)] = connectNumber;
            } else {
                writeConnectError(grid);
            }

            if (lgrid != null) {
                if (lgrid.getBlock() == LadderBlock.CONNECT_LINE) {
                    if (connectNumbers_[blockIndex(lgrid)] == 0) {
                        connectNumberLast = connectRoute(ioMap, ioTreeTable, lgrid, connectNumber,
                                connectNumberLast);
                    } else if (connectNumbers_[blockIndex(lgrid)] != connectNumber) {
                        writeConnectError(grid, lgrid);
                    }
                }
            }

            if (ugrid != null) {
                if (grid.isVertical()) {
                    if (connectNumbers_[blockIndex(ugrid)] == 0) {
                        connectNumberLast = connectRoute(ioMap, ioTreeTable, ugrid, connectNumber, connectNumberLast);
                    } else if (connectNumbers_[blockIndex(ugrid)] != connectNumber) {
                        writeConnectError(grid, ugrid);
                    }
                }
            }

            if (rgrid != null) {
                if (grid.getBlock() == LadderBlock.CONNECT_LINE) {
                    if (connectNumbers_[blockIndex(rgrid)] == 0) {
                        connectNumberLast = connectRoute(ioMap, ioTreeTable, rgrid, connectNumber,
                                connectNumberLast);
                    } else if (connectNumbers_[blockIndex(rgrid)] != connectNumber) {
                        writeConnectError(grid, rgrid);
                    }
                } else if (grid.getBlock().isContact()) {
                    if (connectNumbers_[blockIndex(rgrid)] == 0) {
                        grid.setOutConnectNumber(connectNumberLast + 1);
                        outConnectGrids_.putIfAbsent(connectNumberLast + 1, new ArrayList<>());
                        outConnectGrids_.get(connectNumberLast + 1).add(grid);
                        connectNumberLast = connectRoute(ioMap, ioTreeTable, rgrid, connectNumberLast + 1,
                                connectNumberLast + 1);
                    } else if (grid.getOutConnectNumber() == 0) {
                        grid.setOutConnectNumber(
                                connectNumbers_[blockIndex(rgrid)]);
                        outConnectGrids_.putIfAbsent(
                                connectNumbers_[blockIndex(rgrid)],
                                new ArrayList<>());
                        outConnectGrids_.get(connectNumbers_[blockIndex(rgrid)]).add(grid);
                    }
                }
            }

            if (dgrid != null) {
                if (grid.isVerticalOr()) {
                    if (connectNumbers_[blockIndex(dgrid)] == 0) {
                        connectNumberLast = connectRoute(ioMap, ioTreeTable, dgrid, connectNumber, connectNumberLast);
                    } else if (connectNumbers_[blockIndex(dgrid)] != connectNumber) {
                        writeConnectError(grid, dgrid);
                    }
                }
            }
        }
        return connectNumberLast;
    }

    /**
     *
     * @param ioMap
     * @param cycleTime
     */
    public void run(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap, long cycleTime) {
        LadderGrid grid;
        LadderIo io;
        int idx, funcIdx, index, size;

        Arrays.fill(runConnectNumbers_, 0);
        for (index = 0, size = outGrids_.size(); index < size; index++) {
            grid = outGrids_.get(index);
            idx = addressIndex(grid.getAddress());

            if (!checkRunIo(ioMap, idx, grid)) {
                // the io of the block is not registered : the block is only ignored, the
                // other blocks of the ladder are run
                runConnectNumbers_[blockIndex(grid)] = -1;
                continue;
            }

            // the io of the block is looked up only once : every case of the block uses it
            io = ioMap.get(idx).get(grid.getAddress());

            if (runRouteBegin(ioMap, grid)) {
                switch (grid.getBlock()) {
                    case OUT -> {
                        io.setValue(1);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case OUT_NOT -> {
                        io.setValueNot(0);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case OUT_RISING -> {
                        io.setValueRising(1);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case OUT_RISING_NOT -> {
                        io.setValueRisingNot(0);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case OUT_FALLING -> {
                        io.setValueFalling(1);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case OUT_FALLING_NOT -> {
                        io.setValueFallingNot(0);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case SET -> {
                        io.setValue(1);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case RESET -> {
                        io.resetValue();
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case AND_BITS -> {
                        io.setValue(
                                (long) functionValue(ioMap, grid, 0)
                                        & (long) functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case OR_BITS -> {
                        io.setValue(
                                (long) functionValue(ioMap, grid, 0)
                                        | (long) functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case XOR_BITS -> {
                        io.setValue(
                                (long) functionValue(ioMap, grid, 0)
                                        ^ (long) functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case NOT_BITS -> {
                        io.setValue(~(long) functionValue(ioMap, grid, 0));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case ADDITION -> {
                        io.setValue(
                                functionValue(ioMap, grid, 0) + functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case SUBTRACTION -> {
                        io.setValue(
                                functionValue(ioMap, grid, 0) - functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case MULTIPLICATION -> {
                        io.setValue(
                                functionValue(ioMap, grid, 0) * functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case DIVISION -> {
                        io.setValue(
                                functionValue(ioMap, grid, 0) / functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case AVERAGE -> {
                        io.setValue(
                                (functionValue(ioMap, grid, 0) + functionValue(ioMap, grid, 1))
                                        / 2.0);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case SHIFT_LEFT_BITS -> {
                        io.setValue(
                                (long) functionValue(ioMap, grid, 0)
                                        << (long) functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case SHIFT_RIGHT_BITS -> {
                        io.setValue(
                                (long) functionValue(ioMap, grid, 0)
                                        >> (long) functionValue(ioMap, grid, 1));
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case SIGMOID -> {
                        io.setValue(
                                (Math.tanh(functionValue(ioMap, grid, 0)
                                        * functionValue(ioMap, grid, 1) / 2.0)
                                        + 1.0) / 2.0);
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case RANDOM -> {
                        io.setValue(Math.random());
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case TIMER -> {
                        if (grid.getBlockFunctions()[0].isNumber()) {
                            io.setValueTimer(1,
                                    grid.getBlockFunctions()[0].getValue(), cycleTime);
                        } else {
                            funcIdx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
                            if (grid.getBlockFunctions()[0].getAddress()
                                    .startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                funcIdx = idx_;
                            }
                            io.setValueTimer(1,
                                    ioMap.get(funcIdx).get(grid.getBlockFunctions()[0].getAddress()).getValue(),
                                    cycleTime);
                        }
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case TIMER_NOT -> {
                        if (grid.getBlockFunctions()[0].isNumber()) {
                            io.setValueTimerNot(1,
                                    grid.getBlockFunctions()[0].getValue(), cycleTime);
                        } else {
                            funcIdx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
                            if (grid.getBlockFunctions()[0].getAddress()
                                    .startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                funcIdx = idx_;
                            }
                            io.setValueTimerNot(1,
                                    ioMap.get(funcIdx).get(grid.getBlockFunctions()[0].getAddress()).getValue(),
                                    cycleTime);
                        }
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case COUNTER -> {
                        if (grid.getBlockFunctions()[0].isNumber()) {
                            io.setValueCounter(1,
                                    grid.getBlockFunctions()[0].getValue());
                        } else {
                            funcIdx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
                            if (grid.getBlockFunctions()[0].getAddress()
                                    .startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                funcIdx = idx_;
                            }
                            io.setValueCounter(1,
                                    ioMap.get(funcIdx).get(grid.getBlockFunctions()[0].getAddress()).getValue());
                        }
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case COUNTER_NOT -> {
                        if (grid.getBlockFunctions()[0].isNumber()) {
                            io.setValueCounterNot(1,
                                    grid.getBlockFunctions()[0].getValue());
                        } else {
                            funcIdx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
                            if (grid.getBlockFunctions()[0].getAddress()
                                    .startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                funcIdx = idx_;
                            }
                            io.setValueCounterNot(1,
                                    ioMap.get(funcIdx).get(grid.getBlockFunctions()[0].getAddress()).getValue());
                        }
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case MOVE -> {
                        if (grid.getBlockFunctions()[0].isNumber()) {
                            io.setValue(grid.getBlockFunctions()[0].getValue());
                        } else {
                            funcIdx = LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
                            if (grid.getBlockFunctions()[0].getAddress()
                                    .startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
                                funcIdx = idx_;
                            }
                            io.setValue(
                                    ioMap.get(funcIdx).get(grid.getBlockFunctions()[0].getAddress()).getValue());
                        }
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    case SCRIPT -> {
                        io.setScript(grid.getBlockScript());
                        runConnectNumbers_[blockIndex(grid)] = 1;
                    }
                    default -> {
                    }
                }
            } else {
                switch (grid.getBlock()) {
                    case OUT -> {
                        io.setValue(0);
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    case OUT_NOT -> {
                        io.setValueNot(1);
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    case OUT_RISING -> {
                        io.setValueRising(0);
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    case OUT_RISING_NOT -> {
                        io.setValueRisingNot(1);
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    case OUT_FALLING -> {
                        io.setValueFalling(0);
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    case OUT_FALLING_NOT -> {
                        io.setValueFallingNot(1);
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    case TIMER -> {
                        io.setValueTimer(0, 0, 0);
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    case TIMER_NOT -> {
                        io.setValueTimerNot(0, 0, 0);
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    case SET, RESET, AND_BITS, OR_BITS, XOR_BITS, NOT_BITS, ADDITION,
                            SUBTRACTION, MULTIPLICATION, DIVISION, AVERAGE, SHIFT_LEFT_BITS,
                            SHIFT_RIGHT_BITS, SIGMOID, RANDOM, COUNTER, COUNTER_NOT, MOVE,
                            SCRIPT -> {
                        io.checkEdge();
                        runConnectNumbers_[blockIndex(grid)] = -1;
                    }
                    default -> {
                    }
                }
            }
        }
    }

    /**
     * check the io used by a block before the block is run : an address which is not
     * registered in the io map would make a null pointer error in the cycle of the ladder
     *
     * @param ioMap
     * @param idx
     * @param grid
     * @return true when the block can be run
     */
    private boolean checkRunIo(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap, int idx,
            LadderGrid grid) {
        LadderGrid.BlockFunction[] functions;
        String address;
        int funcIdx, index, size;

        address = grid.getAddress();
        if ((address == null) || address.isEmpty()) {
            // the block uses no address of the io map
            return true;
        }
        if ((idx < 0) || (idx >= ioMap.size()) || (ioMap.get(idx).get(address) == null)) {
            // the address of the block itself is always looked up in the io map : a
            // number used as an address is reported instead of running the block, which
            // would answer no io at all
            return writeRunIoError(address);
        }

        functions = grid.getBlockFunctions();
        for (index = 0, size = functions.length; index < size; index++) {
            if (functions[index].isNumber()) {
                continue;
            }
            address = functions[index].getAddress();
            if ((address == null) || address.isEmpty() || isRunAddressNumber(address)) {
                continue;
            }
            funcIdx = addressIndex(address);
            if ((funcIdx < 0) || (funcIdx >= ioMap.size()) || (ioMap.get(funcIdx).get(address) == null)) {
                return writeRunIoError(address);
            }
        }
        return true;
    }

    /**
     * a real number is never used as an address of the io map : the pattern is only used
     * when the address can be a number, so that the cycle of the ladder is not slowed by
     * a regular expression for every block
     *
     * @param address
     * @return
     */
    private static boolean isRunAddressNumber(String address) {
        return LadderNumber.isNumber(address);
    }

    /**
     * report an io which is not registered only the first time it is found, so that the
     * cycle of the ladder does not write a message at every cycle
     *
     * @param address
     * @return false
     */
    private boolean writeRunIoError(String address) {
        if (runIoErrorSet_.add(address)) {
            writeLog("no io [" + address + "]", true);
        }
        return false;
    }

    private boolean runRouteBegin(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap, LadderGrid grid) {
        if (grid != null) {
            int connectNumber = grid.getInConnectNumber();

            if (connectNumber > 1) {
                LadderGrid ladderGrid;
                int index, size;
                for (index = 0, size = outConnectGrids_.get(connectNumber).size(); index < size; index++) {
                    ladderGrid = outConnectGrids_.get(connectNumber).get(index);
                    if (runConnectNumbers_[blockIndex(ladderGrid)] == 0) {
                        if (runRoute(ioMap, ladderGrid)) {
                            runConnectNumbers_[blockIndex(ladderGrid)] = 1;
                            return true;
                        }
                        runConnectNumbers_[blockIndex(ladderGrid)] = -1;
                    } else if (runConnectNumbers_[blockIndex(ladderGrid)] > 0) {
                        return true;
                    }
                }
            } else if (connectNumber == 1) {
                return true;
            }
        }
        return false;
    }

    private boolean runRoute(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap, LadderGrid grid) {
        if (grid != null) {
            int idx, funcIdx;
            boolean result;
            LadderIo io;

            idx = addressIndex(grid.getAddress());

            if (!checkRunIo(ioMap, idx, grid)) {
                // the io of the block is not registered : the route does not conduct
                return false;
            }

            // the io of the block is looked up only once : every case of the block uses it
            io = ioMap.get(idx).get(grid.getAddress());

            switch (grid.getBlock()) {
                case LOAD -> {
                    result = io.isLd();
                }
                case LOAD_NOT -> {
                    result = !io.isLd();
                }
                case LOAD_RISING -> {
                    result = io.isLdRising();
                }
                case LOAD_RISING_NOT -> {
                    result = !io.isLdRising();
                }
                case LOAD_FALLING -> {
                    result = io.isLdFalling();
                }
                case LOAD_FALLING_NOT -> {
                    result = !io.isLdFalling();
                }
                case COMPARISON_EQUAL -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = io.getValue() == grid.getBlockFunctions()[0].getValue();
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = io.getValue() == ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue();
                    }
                }
                case COMPARISON_NOT_EQUAL -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = io.getValue() != grid.getBlockFunctions()[0].getValue();
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = io.getValue() != ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue();
                    }
                }
                case COMPARISON_LESS -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = io.getValue() < grid.getBlockFunctions()[0].getValue();
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = io.getValue() < ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue();
                    }
                }
                case COMPARISON_LESS_EQUAL -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = io.getValue() <= grid.getBlockFunctions()[0].getValue();
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = io.getValue() <= ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue();
                    }
                }
                case COMPARISON_GREATER -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = io.getValue() > grid.getBlockFunctions()[0].getValue();
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = io.getValue() > ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue();
                    }
                }
                case COMPARISON_GREATER_EQUAL -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = io.getValue() >= grid.getBlockFunctions()[0].getValue();
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = io.getValue() >= ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue();
                    }
                }
                case COMPARISON_AND_BITS -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = (((long) io.getValue())
                                & ((long) grid.getBlockFunctions()[0].getValue())) != 0;
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = (((long) io.getValue()) & ((long) ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue())) != 0;
                    }
                }
                case COMPARISON_OR_BITS -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = (((long) io.getValue())
                                | ((long) grid.getBlockFunctions()[0].getValue())) != 0;
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = (((long) io.getValue()) | ((long) ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue())) != 0;
                    }
                }
                case COMPARISON_XOR_BITS -> {
                    if (grid.getBlockFunctions()[0].isNumber()) {
                        result = (((long) io.getValue())
                                ^ ((long) grid.getBlockFunctions()[0].getValue())) != 0;
                    } else {
                        funcIdx = addressIndex(grid.getBlockFunctions()[0].getAddress());
                        result = (((long) io.getValue()) ^ ((long) ioMap.get(funcIdx)
                                .get(grid.getBlockFunctions()[0].getAddress()).getValue())) != 0;
                    }
                }
                default -> {
                    result = false;
                }
            }

            if (result) {
                int connectNumber = grid.getInConnectNumber();

                if (connectNumber > 1) {
                    LadderGrid ladderGrid;
                    int index, size;

                    for (index = 0, size = outConnectGrids_.get(connectNumber).size(); index < size; index++) {
                        ladderGrid = outConnectGrids_.get(connectNumber).get(index);
                        if (runConnectNumbers_[blockIndex(ladderGrid)] == 0) {
                            if (runRoute(ioMap, ladderGrid)) {
                                runConnectNumbers_[blockIndex(ladderGrid)] = 1;
                                return true;
                            }
                            runConnectNumbers_[blockIndex(ladderGrid)] = -1;
                        } else if (runConnectNumbers_[blockIndex(ladderGrid)] > 0) {
                            return true;
                        }
                    }
                } else {
                    return result;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param column
     * @param row
     * @return
     */
    public LadderGrid findGrid(int column, int row) {
        LadderGrid grid;
        int index, size;

        for (index = 0, size = ladderGrids_.size(); index < size; index++) {
            grid = ladderGrids_.get(index);
            if ((grid.getColumnIndex() == column) && (grid.getRowIndex() == row)) {
                return grid;
            }
        }
        return null;
    }

    /**
     *
     * @param grid
     * @return
     */
    public boolean checkGridOut(LadderGrid grid) {
        LadderGrid rgrid;

        while (grid != null) {
            rgrid = grid.getRightLadderGrid();
            if (rgrid != null) {
                if (rgrid.isVertical() && rgrid.isVerticalOr()) {
                    return false;
                }
            }

            if (grid.getBlock().isContact()) {
                return false;
            }
            grid = rgrid;
        }
        return true;
    }

    /**
     * the value of a function of the block : the value of the number when the function is
     * written as a number, the value of the io of the address of the function otherwise
     *
     * @param ioMap
     * @param grid
     * @param function index of the function of the block
     * @return the value of the function of the block
     */
    private double functionValue(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap, LadderGrid grid,
            int function) {
        if (grid.getBlockFunctions()[function].isNumber()) {
            return grid.getBlockFunctions()[function].getValue();
        }
        return ioMap.get(functionIndex(grid, function)).get(grid.getBlockFunctions()[function].getAddress()).getValue();
    }

    /**
     * the index of the io map which holds the io of the address of a function of the block :
     * the io of an address which begins with the local prefix belongs to the ladder
     *
     * @param grid
     * @param function index of the function of the block
     * @return the index of the io map of the address of the function
     */
    private int functionIndex(LadderGrid grid, int function) {
        if (grid.getBlockFunctions()[function].getAddress().startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
            return idx_;
        }
        return LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
    }

    /**
     * the index of a block in the arrays of the ladder : the blocks of the ladder are kept
     * in a row by row array of column_ blocks
     *
     * @param grid
     * @return the index of the block in the arrays of the ladder
     */
    private int blockIndex(LadderGrid grid) {
        return grid.getColumnIndex() + (grid.getRowIndex() * column_);
    }

    /**
     * the index of the io map which holds the io of an address : an address which begins
     * with the local prefix belongs to the ladder, an other address belongs to the
     * application
     *
     * @param address
     * @return the index of the io map of the address
     */
    private int addressIndex(String address) {
        if (address.startsWith(LadderDefaults.LADDER_LOCAL_ADDRESS_PREFIX)) {
            return idx_;
        }
        return LadderDefaults.LADDER_GLOBAL_ADDRESS_INDEX;
    }

    /**
     * the io of an address used by a block of the ladder is added to the io map and to the
     * tree table of the io when the address is not registered yet
     *
     * @param ioMap
     * @param ioTreeTable
     * @param idx
     * @param address
     */
    private void registerIo(CopyOnWriteArrayList<ConcurrentHashMap<String, LadderIo>> ioMap,
            TreeTableView<LadderTreeTableIo> ioTreeTable, int idx, String address) {
        if (ioMap.get(idx).containsKey(address)) {
            return;
        }
        ioMap.get(idx).put(address, new LadderIo(address));
        ioTreeTable.getRoot().getChildren().get(idx).getChildren().add(new TreeItem<>(new LadderTreeTableIo(address)));
    }

    /**
     * the address of a block which can be a number is written as a number : the block is
     * not connected
     *
     * @param grid
     */
    private void writeNumericAddressError(LadderGrid grid) {
        writeLog("Numeric only error in address" + " [" + grid.getColumnIndex()
                + ":" + grid.getRowIndex() + "]", true);
        isConnectFailed_ = true;
    }

    /**
     * the route of the ladder is broken at the block
     *
     * @param grid
     */
    private void writeConnectError(LadderGrid grid) {
        writeLog("Connection failed" + " [" + grid.getColumnIndex()
                + ":" + grid.getRowIndex() + "]", true);
        isConnectFailed_ = true;
    }

    /**
     * the route of the ladder is broken between two blocks
     *
     * @param grid
     * @param next the block the route goes to
     */
    private void writeConnectError(LadderGrid grid, LadderGrid next) {
        writeLog("Connection failed" + " [" + grid.getColumnIndex()
                + ":" + grid.getRowIndex() + "]->[" + next.getColumnIndex() + ":"
                + next.getRowIndex() + "]", true);
        isConnectFailed_ = true;
    }

    private void writeLog(final String msg, final boolean err) {
        Console.write(Ladder.class.getName(), name_ + " : " + msg, err);
    }
}
