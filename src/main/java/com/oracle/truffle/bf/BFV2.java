/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.truffle.bf;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RepeatingNode;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.profiles.IntValueProfile;
import com.oracle.truffle.bf.BFParser.OpCode;
import com.oracle.truffle.bf.BFParser.Operation;
import com.oracle.truffle.bf.BFParser.Repeat;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BFV2 extends BFImpl {

    private InputStream in;
    private OutputStream out;
    private CallTarget target;
    @CompilerDirectives.CompilationFinal
    private FrameSlot cellsSlot;
    @CompilerDirectives.CompilationFinal
    private FrameSlot indexSlot;

    public void prepare(Operation[] operations, InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        BFRootNode root = new BFRootNode(prepareNodes(operations));
        this.cellsSlot = root.getFrameDescriptor().addFrameSlot(0, FrameSlotKind.Object);
        this.indexSlot = root.getFrameDescriptor().addFrameSlot(1, FrameSlotKind.Int);
        target = Truffle.getRuntime().createCallTarget(root);
    }

    private OperationNode[] prepareNodes(Operation[] ops) {
        OperationNode[] nodes = new OperationNode[ops.length];
        for (int i = 0; i < nodes.length; i++) {
            Operation operation = ops[i];
            OperationNode[] children = null;
            switch (operation.getCode()) {
                case REPEAT:
                    children = prepareNodes(((Repeat) operation).getChildren());
                    break;
            }
            nodes[i] = new OperationNode(operation.getCode(), children);
        }
        return nodes;
    }

    public void run() throws IOException {
        target.call();
    }

    private class BFRootNode extends RootNode {

        @Children
        private final OperationNode[] children;

        public BFRootNode(OperationNode[] children) {
            super(TruffleLanguage.class, null, null);
            this.children = children;
        }

        void setCells(VirtualFrame frame, int[] cells) {
            frame.setObject(cellsSlot, cells);
        }

        void setIndex(VirtualFrame frame, int index) {
            frame.setInt(indexSlot, index);
        }

        @Override
        @ExplodeLoop
        public Object execute(VirtualFrame frame) {
            setCells(frame, new int[100]);
            setIndex(frame, 0);
            for (OperationNode child : children) {
                child.execute(frame);
            }
            return null;
        }
    }

    private class OperationNode extends Node {

        private final BFParser.OpCode opCode;
        @Children
        private final OperationNode[] children;

        @Child
        private LoopNode loop;

        public OperationNode(OpCode opCode, OperationNode[] children) {
            this.opCode = opCode;
            this.children = children;
            if (opCode == OpCode.REPEAT) {
                this.loop = Truffle.getRuntime().createLoopNode(new BFRepeatingNode());
            }
        }

        private class BFRepeatingNode extends Node implements RepeatingNode {

            @Override
            @ExplodeLoop
            public boolean executeRepeating(VirtualFrame frame) {
                int[] cells = getCells(frame);
                int index = getIndex(frame);
                if (cells[index] > 0) {
                     for (OperationNode child : children) {
                         child.execute(frame);
                     }
                     return true;
                } else {
                    return false;
                }
            }
        }

        int[] getCells(VirtualFrame frame) {
            try {
                return (int[]) frame.getObject(cellsSlot);
            } catch (FrameSlotTypeException ex) {
                CompilerDirectives.transferToInterpreter();
                throw new RuntimeException(ex);
            }
        }

        void setCells(VirtualFrame frame, int[] cells) {
            frame.setObject(cellsSlot, cells);
        }

        int getIndex(VirtualFrame frame) {
            try {
                return frame.getInt(indexSlot);
            } catch (FrameSlotTypeException ex) {
                CompilerDirectives.transferToInterpreter();
                throw new RuntimeException(ex);
            }
        }

        void setIndex(VirtualFrame frame, int index) {
            frame.setInt(indexSlot, index);
        }

        @CompilerDirectives.CompilationFinal
        private boolean seenResize = false;
        
        private final IntValueProfile profile = IntValueProfile.createIdentityProfile();

        public void execute(VirtualFrame frame) {
            try {
                if (opCode != OpCode.REPEAT) {
                    int[] cells = getCells(frame);
                    int index = profile.profile(getIndex(frame));
                    switch (opCode) {
                        case LEFT:
                            setIndex(frame, index - 1);
                            break;
                        case RIGHT:
                            int i = index + 1;
                            if (i >= cells.length) {
                                if (!seenResize) {
                                    CompilerDirectives.transferToInterpreterAndInvalidate();
                                    seenResize = true;
                                }
                                setCells(frame, Arrays.copyOf(cells, cells.length * 2));
                            }
                            setIndex(frame, i);
                            break;
                        case DEC:
                            cells[index]--;
                            break;
                        case INC:
                            cells[index]++;
                            break;
                        case IN:
                            cells[index] = read();
                            break;
                        case OUT:
                            write(cells[index]);
                            break;
                    }
                } else {
                    loop.executeLoop(frame);
                }
            } catch (IOException ex) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                Logger.getLogger(BFV2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @CompilerDirectives.TruffleBoundary
        private void write(int value) throws IOException {
            out.write(value);
        }

        @CompilerDirectives.TruffleBoundary
        private int read() throws IOException {
            return in.read();
        }

    }

}
