/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.truffle.bf;

import com.oracle.truffle.bf.BFParser.Operation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 *
 * @author chumer
 */
public class BFV0 extends BFImpl {

    private static class Memory {

        int[] cells;

        int index;

        Memory() {
            this.index = 0;
            this.cells = new int[100];
        }
    }

    private Operation[] operations;
    private InputStream in;
    private OutputStream out;

    @Override
    public void prepare(BFParser.Operation[] operations, InputStream in, OutputStream out) {
        this.operations = operations;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() throws IOException {
        run(new Memory(), this.operations, false);
    }

    private void run(Memory memory, Operation[] operations, boolean repeat) throws IOException {
        while (true) {
            if (repeat) {
                if (memory.cells[memory.index] <= 0) {
                    break;
                }
            }
            for (Operation operation : operations) {
                switch (operation.getCode()) {
                    case LEFT:
                        memory.index--;
                        break;
                    case RIGHT:
                        int i = memory.index + 1;
                        if (i >= memory.cells.length) {
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
                        memory.cells[memory.index] = in.read();
                        break;
                    case OUT:
                        out.write(memory.cells[memory.index]);
                        break;
                    case REPEAT:
                        Operation[] children = ((BFParser.Repeat) operation).getChildren();
                        run(memory, children, true);
                        break;
                }

            }
            if (!repeat) {
                break;
            }
        }

    }

}
