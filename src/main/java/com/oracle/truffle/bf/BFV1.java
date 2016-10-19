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
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.bf.BFParser.OpCode;
import com.oracle.truffle.bf.BFParser.Operation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chumer
 */
public class BFV1 extends BFImpl {

    private static class Memory {

        int[] cells;

        int index;

        Memory() {
            this.index = 0;
            this.cells = new int[100];
        }
    }

    private CallTarget target;
    private InputStream in;
    private OutputStream out;

    @Override
    public void prepare(BFParser.Operation[] operations, InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        OperationNode[] nodes = prepareNodes(operations);
        BFRootNode root = new BFRootNode(nodes);
        target = Truffle.getRuntime().createCallTarget(root);
    }
    
    @Override
    public void run() throws IOException {
        target.call();
    }
    
    private OperationNode[] prepareNodes(Operation[] operations) {
        OperationNode[] nodes = new OperationNode[operations.length];
        for (int i = 0; i < nodes.length; i++) {
            final OpCode code = operations[i].getCode();
            OperationNode[] children = null;
            if (code == OpCode.REPEAT) {
                children = prepareNodes(((BFParser.Repeat)operations[i]).getChildren());
            }
            nodes[i] = new OperationNode(code, children);
        }
        return nodes;
    }
    

    private static class BFRootNode extends RootNode {

        @Children private final OperationNode[] children;
        
        public BFRootNode(OperationNode[] children) {
            super(TruffleLanguage.class, null, null);
            this.children = children;
        }

        @Override
        @ExplodeLoop
        public Object execute(VirtualFrame frame) {
            Memory memory = new Memory();
            for (OperationNode child : children) {
                child.execute(memory);
            }
            return null;
        }
    }

    private class OperationNode extends Node {

        private final BFParser.OpCode opCode;
        @Children
        private final OperationNode[] children;
        private final BranchProfile expansion = BranchProfile.create();

        OperationNode(BFParser.OpCode opCode, OperationNode[] children) {
            this.children = children;
            this.opCode = opCode;
        }
        
        public void execute(Memory memory) {
            try {
                switch (opCode) {
                    case LEFT:
                        memory.index--;
                        break;
                    case RIGHT:
                        int i = memory.index + 1;
                        if (i >= memory.cells.length) {
                            expansion.enter();
                            memory.cells = Arrays.copyOf(memory.cells, memory.cells.length * 2);
                        }
                        memory.index = i;
                        break;
                    case INC:
                        memory.cells[memory.index]++;
                        break;
                    case DEC:
                        memory.cells[memory.index]--;
                        break;
                    case IN:
                        memory.cells[memory.index] = input();
                        break;
                    case OUT:
                        output(memory.cells[memory.index]);
                        break;
                    case REPEAT:
                        while (memory.cells[memory.index] > 0) {
                            executeChildren(memory);
                        }
                        break;
                }
            } catch (IOException ex) {
                CompilerDirectives.transferToInterpreter();
                Logger.getLogger(BFV1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        @CompilerDirectives.TruffleBoundary
        private int input() throws IOException {
            return in.read();
        }
        
        @CompilerDirectives.TruffleBoundary
        private void output(int value) throws IOException {
            out.write(value);
        }
        
        @ExplodeLoop
        private void executeChildren(Memory memory) {
            for (OperationNode children1 : children) {
                children1.execute(memory);
            }
        }

    }


}
