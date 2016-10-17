/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.truffle.bf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chumer
 */
public class BFParser {

    public enum OpCode {
        RIGHT,
        LEFT,
        INC,
        DEC,
        OUT,
        IN,
        REPEAT,
    }

    public Operation[] parse(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return parseBlock(reader);
        }
    }

    private Operation[] parseBlock(final BufferedReader reader) throws IOException {
        List<Operation> operations = new ArrayList<>();
        int c;
        outer: while ((c = reader.read()) != -1) {
            Operation operation = null;
            switch (c) {
                case '<':
                    operation = new Operation(OpCode.LEFT);
                    break;
                case '>':
                    operation = new Operation(OpCode.RIGHT);
                    break;
                case '+':
                    operation = new Operation(OpCode.INC);
                    break;
                case '-':
                    operation = new Operation(OpCode.DEC);
                    break;
                case ',':
                    operation = new Operation(OpCode.IN);
                    break;
                case '.':
                    operation = new Operation(OpCode.OUT);
                    break;
                case '[':
                    operation = new Repeat(parseBlock(reader));
                    break;
                case ']':
                    break outer;
            }
            if (operation != null) {
                operations.add(operation);
            }
        }
        return operations.toArray(new Operation[0]);
    }

    public static class Operation {

        private final OpCode code;

        public Operation(OpCode code) {
            this.code = code;
        }

        public OpCode getCode() {
            return code;
        }

    }

    public static class Repeat extends Operation {

        private final Operation[] children;

        public Repeat(Operation[] children) {
            super(OpCode.REPEAT);
            this.children = children;
        }

        public Operation[] getChildren() {
            return children;
        }

    }

}
