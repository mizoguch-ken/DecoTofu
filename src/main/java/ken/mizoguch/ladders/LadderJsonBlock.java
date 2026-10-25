/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.ladders;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mizoguch-ken
 */
public class LadderJsonBlock {

    /**
     * a function of the block of the json representation : the address of an io of the ladder, or
     * the number written in the block with the radix which shows it
     *
     * @param address address of the io of the function of the block
     * @param value value of the number of the function of the block
     * @param radix radix of the number of the function of the block
     */
    public record JsonBlockFunction(String address, Double value, Integer radix) {

        /**
         * the function of the block of an address of the io of the ladder
         *
         * @param address address of the io of the function of the block
         */
        public JsonBlockFunction(String address) {
            this(address, null, null);
        }

        /**
         * the function of the block of a number written in the block
         *
         * @param value value of the number of the function of the block
         * @param radix radix of the number of the function of the block
         */
        public JsonBlockFunction(double value, int radix) {
            this(null, value, radix);
        }
    }

    private final Integer columnIndex;
    private final Integer rowIndex;
    private final String block;
    private final Boolean vertical;
    private final Boolean verticalOr;
    private final String address;
    private final String comment;
    private List<JsonBlockFunction> blockFunctions;
    private String blockScript;

    /**
     *
     * @param columnIndex
     * @param rowIndex
     */
    public LadderJsonBlock(Integer columnIndex, Integer rowIndex) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.block = LadderGrid.LADDER_GRID_INITIAL_BLOCK.toString();
        this.vertical = LadderGrid.LADDER_GRID_INITIAL_VERTICAL;
        this.verticalOr = LadderGrid.LADDER_GRID_INITIAL_VERTICAL_OR;
        this.address = LadderGrid.LADDER_GRID_INITIAL_ADDRESS;
        this.comment = null;
        this.blockFunctions = null;
        this.blockScript = LadderGrid.LADDER_GRID_INITIAL_BLOCK_SCRIPT;
    }

    /**
     *
     * @param columnIndex
     * @param rowIndex
     * @param block
     * @param vertical
     * @param verticalOr
     * @param address
     * @param comment
     * @param blockFunctions
     * @param blockScript
     */
    public LadderJsonBlock(Integer columnIndex, Integer rowIndex, String block, Boolean vertical, Boolean verticalOr,
            String address, String comment, List<LadderGrid.BlockFunction> blockFunctions, String blockScript) {
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.block = block;
        this.vertical = vertical;
        this.verticalOr = verticalOr;
        this.address = address;
        this.comment = comment;
        if (blockFunctions != null) {
            this.blockFunctions = new ArrayList<>();
            for (int i = 0, size = blockFunctions.size(); i < size; i++) {
                if (blockFunctions.get(i).isNumber()
                        && (blockFunctions.get(i).getRadix() != LadderGrid.LADDER_GRID_INITIAL_BLOCK_FUNCTION_RADIX)) {
                    this.blockFunctions.add(
                            new JsonBlockFunction(blockFunctions.get(i).getValue(), blockFunctions.get(i).getRadix()));
                } else if (blockFunctions.get(i).getAddress() != null) {
                    this.blockFunctions.add(new JsonBlockFunction(blockFunctions.get(i).getAddress()));
                } else {
                    break;
                }
            }
        }
        this.blockScript = blockScript;
    }

    /**
     *
     * @return
     */
    public Integer getColumnIndex() {
        return columnIndex;
    }

    /**
     *
     * @return
     */
    public Integer getRowIndex() {
        return rowIndex;
    }

    /**
     *
     * @return
     */
    public String getBlock() {
        return block;
    }

    /**
     *
     * @return
     */
    public Boolean isVertical() {
        return vertical;
    }

    /**
     *
     * @return
     */
    public Boolean isVerticalOr() {
        return verticalOr;
    }

    /**
     *
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     *
     * @return
     */
    public String getComment() {
        return comment;
    }

    /**
     *
     * @return
     */
    public List<JsonBlockFunction> getBlockFunctions() {
        return blockFunctions;
    }

    /**
     *
     * @return
     */
    public String getBlockScript() {
        return blockScript;
    }

    /**
     *
     * @return true when the block holds a script
     */
    public boolean hasScript() {
        return blockScript != null;
    }

    /**
     * remove the script of the block : the script of a ladder which comes from a file
     * is not always wanted, because it is run in the web page with the apis of the
     * application
     */
    public void removeScript() {
        blockScript = null;
    }
}
