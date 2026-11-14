/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ken.mizoguch.soem;

import jnr.ffi.NativeType;
import jnr.ffi.Struct;

/**
 *
 * @author mizoguch-ken
 */
public interface SoemOsal {

    public static final int TRUE = 1;
    public static final int FALSE = 0;

    public class ec_timet extends Struct {

        public final Signed64 tv_sec;
        /* < Seconds elapsed since the Epoch (Jan 1, 1970) */
        public final SignedLong tv_nsec;
        /* < Nanoseconds elapsed since last second boundary */
        @SuppressWarnings("unused")
        private final Padding pad_;

        public ec_timet(jnr.ffi.Runtime runtime) {
            super(runtime);

            tv_sec = new Signed64();
            tv_nsec = new SignedLong();
            pad_ = (runtime.longSize() == 4) ? new Padding(NativeType.UCHAR, 4) : null;
        }
    }

    public final class osal_timert extends Struct {

        public final ec_timet stop_time;

        public osal_timert(jnr.ffi.Runtime runtime) {
            super(runtime);

            stop_time = inner(new ec_timet(runtime));
        }
    }
}
