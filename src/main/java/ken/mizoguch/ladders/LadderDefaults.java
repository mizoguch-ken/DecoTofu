/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the file in the editor.
 */
package ken.mizoguch.ladders;

import java.util.concurrent.TimeUnit;

/**
 * the values which the editor of the ladders uses when a ladder or a block is created, and the
 * marks of the global and of the local io : they belong to no ladder, so every part of the
 * application shares them.
 *
 * @author mizoguch-ken
 */
public final class LadderDefaults {

    /**
     * time of the cycles which are drawn again on the view of the ladders
     */
    public static final long LADDER_VIEW_REFRESH_CYCLE_TIME = TimeUnit.MILLISECONDS.toNanos(100);

    public static final int LADDER_DEFAULT_GRID_COLUMN = 10;
    public static final int LADDER_DEFAULT_GRID_ROW = 10;
    public static final double LADDER_DEFAULT_GRID_MIN_SIZE = 78.912;
    public static final double LADDER_DEFAULT_GRID_MAX_SIZE = LADDER_DEFAULT_GRID_MIN_SIZE * 122.966;
    public static final double LADDER_DEFAULT_GRID_CONTENTS_WIDTH = LADDER_DEFAULT_GRID_MIN_SIZE * 0.382;
    public static final double LADDER_DEFAULT_GRID_CONTENTS_HIGHT = LADDER_DEFAULT_GRID_CONTENTS_WIDTH * 0.618;

    /**
     * the row of the io map which holds the addresses of every ladder
     */
    public static final int LADDER_GLOBAL_ADDRESS_INDEX = 0;

    /**
     * the prefix of an address which belongs to only one ladder
     */
    public static final String LADDER_LOCAL_ADDRESS_PREFIX = ".";

    private LadderDefaults() {
        // the constants of the ladders are not a class which can be used
    }
}
