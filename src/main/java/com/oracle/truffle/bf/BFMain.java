/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.truffle.bf;

import com.oracle.truffle.bf.BFParser.Operation;
import java.io.IOException;

/**
 *
 * @author chumer
 */
public class BFMain {
    
    
    public static void main(String[] args) throws IOException {
        BFImpl[] impls = new BFImpl[]{new BFV0()};
        String arg = args[0];
        if (arg.equals("-benchmark")) {
            BFBenchmark.benchmark(impls);
            return;
        }
       
        int impl = 0;
        if (args.length > 1) {
            impl = Integer.parseInt(args[1]);
        }
        
        int iterations = 1;
        if (args.length > 2) {
            iterations = Integer.parseInt(args[2]);
        }
        BFImpl bf = impls[impl];
        
        System.out.println("Running with " + bf.getClass().getSimpleName() + " for " + iterations+ " iterations.");
        
        bf.prepare(parse(args[0]), System.in, System.out);
        long time = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            bf.run();
        }
        
        System.out.println("Elapsed " + ( System.currentTimeMillis() - time) + "ms");
    }
    
    private static Operation[] parse(String file) throws IOException {
        return new BFParser().parse(BFMain.class.getResourceAsStream("/test/" + file));
    }
    
    
}
