/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import java.util.*;


/**
 * @author Philip DeCamp
 */
class CFRunLoop {

    private final Thread mThread;

    // Accessible by the thread.
    private CFRef mLoop = null;
    private final List<CFLoopSource> mSourceList = new ArrayList<CFLoopSource>();
    private final List<CFLoopTimer> mTimerList = new ArrayList<CFLoopTimer>();

    // Accesible by caller and thread. Require synchronization.
    // modified by caller
    private boolean mSetRunning = false;
    private boolean mSetKilled = false;

    // modified by thread
    private boolean mRunning = false;
    private boolean mKilled = false;

    // modified by both
    private boolean mStateChanged = true;
    private final Queue<ActionEvent> mActionQueue = new LinkedList<ActionEvent>();


    public CFRunLoop( String threadName ) {
        this( threadName, Thread.NORM_PRIORITY );
    }
    
    public CFRunLoop( String threadName, int threadPriority ) {
        mThread = new LoopThread( threadName, threadPriority );
        mThread.start();
    }



    public synchronized void start() throws CFException {
        if( mSetKilled || mSetRunning ) {
            return;
        }
        mSetRunning = true;
        if( !mRunning ) {
            processStateChange( false );
        }
    }

    public synchronized void stop( boolean block ) throws CFException {
        if( !mSetRunning || mSetKilled ) {
            if( mRunning && block ) {
                processStateChange( true );
            }
            return;
        }

        mSetRunning = false;
        if( mRunning ) {
            processStateChange( block );
        }
    }

    public synchronized void wakeup() throws CFException {
        if( !mRunning ) {
            return;
        }
        runLoopWakeUp( mLoop.pointer() );
    }

    public synchronized void kill( boolean block ) throws CFException {
        if( mSetKilled ) {
            if( !mKilled && block ) {
                processStateChange( true );
            }
            return;
        }

        mSetKilled = true;
        mSetRunning = false;
        if( !mKilled ) {
            processStateChange( block );
        }
    }



    public synchronized void addSources( CFLoopSource... sources ) throws CFException {
        if( mSetKilled || sources == null || sources.length == 0 ) {
            return;
        }
        ActionEvent action = new ActionEvent( Action.ADD_SOURCES, sources );
        mActionQueue.offer( action );
        processStateChange( false );
    }

    public synchronized void addTimers( CFLoopTimer... timers ) throws CFException {
        if( mSetKilled || timers == null || timers.length == 0 ) {
            return;
        }
        ActionEvent action = new ActionEvent( Action.ADD_TIMERS, timers );
        mActionQueue.offer( action );
        processStateChange( false );
    }

    public synchronized void removeSources( CFLoopSource... sources ) throws CFException {
        if( mSetKilled || sources == null || sources.length == 0 ) {
            return;
        }
        ActionEvent action = new ActionEvent( Action.REMOVE_SOURCES, sources );
        mActionQueue.offer( action );
        processStateChange( false );
    }

    public synchronized void removeTimers( CFLoopTimer... timers ) throws CFException {
        if( mSetKilled || timers == null || timers.length == 0 ) {
            return;
        }
        ActionEvent action = new ActionEvent( Action.REMOVE_TIMERS, timers );
        mActionQueue.offer( action );
        processStateChange( false );
    }


    
    private synchronized void processStateChange( boolean block ) throws CFException {
        mStateChanged = true;

        if( mLoop == null ) {
            if( mSetKilled && !mKilled ) {
                releaseSources();
                releaseQueue();
                mKilled = mSetKilled;
            }

            notifyAll();
            return;
        }

        if( block && !mKilled && !mThread.equals( Thread.currentThread() ) ) {
            while( mStateChanged && mLoop != null && !mKilled ) {
                runLoopStop( mLoop.pointer() );
                notifyAll();

                try {
                    wait( 200l );
                } catch( InterruptedException ex ) {}
            }
        } else {
            runLoopStop( mLoop.pointer() );
            notifyAll();
        }
    }



    private void runLoop() {
        synchronized( this ) {
            if( mSetKilled || mKilled ) {
                return;
            }

            mLoop = new CFRef( runLoopGetCurrent() );
        }

        try {
            while( true ) {
                synchronized( this ) {
                    if( mSetKilled ) {
                        return;
                    }

                    processActions();
                    mRunning = mSetRunning;
                    mStateChanged = false;
                    notifyAll();

                    if( !mRunning || mSourceList.isEmpty() && mTimerList.isEmpty() ) {
                        waitForChange();
                        continue;
                    }

                    // System.out.println("Running with: " + mSourceList.size()
                    // + " sources");
                }

                if( runLoopRun() ) {
                    // If loop finished, just wait until something happens.
                    synchronized( this ) {
                        waitForChange();
                    }
                }
            }

        } finally {
            synchronized( this ) {
                mStateChanged = false;
                mSetKilled = true;
                mSetRunning = false;
                mKilled = true;
                notifyAll();

                releaseSources();
                releaseQueue();
                releaseLoop();
            }
        }
    }



    /*=================================================================
     * The following block of private methods are for runLoop() only.
     *=================================================================*/
    
    private synchronized void waitForChange() {
        while( !mSetKilled && !mStateChanged ) {
            try {
                wait();
            } catch( InterruptedException ex ) {}
        }
    }

    
    private void processActions() {
        if( mLoop == null ) {
            return;
        }

        while( !mActionQueue.isEmpty() ) {
            ActionEvent ev = mActionQueue.remove();

            try {
                switch( ev.getAction() ) {
                case ADD_SOURCES:
                    for( CFLoopSource s : (CFLoopSource[])ev.getRefs() ) {
                        if( mSourceList.add( s ) ) {
                            runLoopAddSource( mLoop.pointer(), s.pointer() );
                            s.ref();
                        }
                    }
                    break;

                case ADD_TIMERS:
                    for( CFLoopTimer t : (CFLoopTimer[])ev.getRefs() ) {
                        if( mTimerList.add( t ) ) {
                            runLoopAddTimer( mLoop.pointer(), t.pointer() );
                            t.ref();
                        }
                    }
                    break;

                case REMOVE_SOURCES:
                    for( CFLoopSource s : (CFLoopSource[])ev.getRefs() ) {
                        if( mSourceList.remove( s ) ) {
                            runLoopRemoveSource( mLoop.pointer(), s.pointer() );
                            s.deref();
                        }
                    }
                    break;

                case REMOVE_TIMERS:
                    for( CFLoopTimer t : (CFLoopTimer[])ev.getRefs() ) {
                        if( mTimerList.remove( t ) ) {
                            runLoopRemoveTimer( mLoop.pointer(), t.pointer() );
                            t.deref();
                        }
                    }
                    break;
                }

                ev.deref();

            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
    }


    private void releaseLoop() {
        if( mLoop == null ) {
            return;
        }

        CFRef loop = mLoop;
        mLoop = null;

        try {
            for( CFRef r : mSourceList ) {
                runLoopRemoveSource( loop.pointer(), r.pointer() );
            }

            for( CFRef r : mTimerList ) {
                runLoopRemoveTimer( loop.pointer(), r.pointer() );
            }
        } catch( Exception ex ) {}

        try {
            loop.deref();
        } catch( Exception ex ) {}
    }
    

    private void releaseSources() {
        try {
            for( CFLoopSource r : mSourceList ) {
                r.deref();
            }

            for( CFLoopTimer r : mTimerList ) {
                r.deref();
            }
        } catch( Exception ex ) {}

        mSourceList.clear();
        mTimerList.clear();
    }

    
    private void releaseQueue() {
        try {
            while( !mActionQueue.isEmpty() ) {
                mActionQueue.remove().deref();
            }
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }



    // Calls CFRetain on loop before returning it.
    private static native long runLoopGetCurrent();

    private static native boolean runLoopRun();

    private static native void runLoopWakeUp( long ptr );

    private static native void runLoopStop( long ptr );

    private static native void runLoopAddSource( long ptr, long sourcePtr );

    private static native void runLoopRemoveSource( long ptr, long sourcePtr );

    private static native void runLoopAddTimer( long ptr, long timerPtr );

    private static native void runLoopRemoveTimer( long ptr, long timerPtr );



    private static enum Action {
        ADD_SOURCES,
        ADD_TIMERS,
        REMOVE_SOURCES,
        REMOVE_TIMERS
    }

    

    private static class ActionEvent {

        private final Action mAction;
        private final CFRef[] mRefs;


        public ActionEvent( Action action, CFRef[] refs ) {
            mAction = action;
            mRefs = refs.clone();

            for( int i = 0; i < mRefs.length; i++ ) {
                mRefs[i].ref();
            }
        }



        public Action getAction() {
            return mAction;
        }

        public CFRef[] getRefs() {
            return mRefs;
        }



        public void ref() {
            if( mRefs == null ) {
                return;
            }

            for( CFRef ref : mRefs ) {
                ref.ref();
            }
        }

        public void deref() {
            if( mRefs == null ) {
                return;
            }

            for( CFRef ref : mRefs ) {
                if( ref != null ) {
                    ref.deref();
                }
            }
        }

    }



    private class LoopThread extends Thread {
        public LoopThread( String name, int priority ) {
            super( name );
            setPriority( priority );
            setDaemon( true );
        }

        @Override
        public void run() {
            runLoop();
        }
    }
}
