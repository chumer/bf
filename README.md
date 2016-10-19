# Truffle BF

A [Brainfuck](https://esolangs.org/wiki/brainfuck) (short BF) implementation using Truffle for the GraalVM.

BF is a famous esoteric language invented by the Swiss Urban Müller in 1993, in an attempt to make a language for which he could write the smallest possible compiler for the Amiga OS, version 2.0. Instead of beeing as small as possible, this implementations aims to be as fast as possible by leveraging dynamic speculation with Truffle and Graal.

This repository is licensed under the permissive UPL licence. Feel free to fork an extend it.

## BF Short Reference

Brainfuck operates on an array of memory cells, also referred to as the tape, each initially set to zero. There is a pointer, initially pointing to the first memory cell. The commands are:

| Command | Description                                                          |
|---------|----------------------------------------------------------------------|
| >       | Move the pointer to the right                                        |
| <       | Move the pointer to the left                                         |
| +       | Increment the memory cell under the pointer                          |
| -       | Decrement the memory cell under the pointer                          |
| .       | Output the character signified by the cell at the pointer            |
| ,       | Input a character and store it in the cell at the pointer            |
| [       | Jump past the matching ] if the cell under the pointer is 0          |
| ]       | Jump back to the matching [ if the cell under the pointer is nonzero |


All characters other than ><+-.,[] should be considered comments and ignored. 

For more information see the [reference](https://esolangs.org/wiki/brainfuck) on Esolangs.

## Prerequisites
* JDK 8
* maven3 

## Installation

* Clone BF repository using
  `git clone https://github.com/chumer/bf`
* Download Graal VM Development Kit from 
  http://www.oracle.com/technetwork/oracle-labs/program-languages/downloads
* Unpack the downloaded `graalvm_*.tar.gz` into `bf/graalvm`. 
* Verify that the file `bf/graalvm/bin/java` exists and is executable
* Execute `mvn package`

## IDE Setup 

### Netbeans
* Tested with Netbeans 8.2
* Open Netbeans
* File -> Open Project -> Select `bf` folder -> Open Project

### Eclipse
* Tested with Eclipse Mars SR2
* Open Eclipse with a new workspace
* Install `m2e` and `m2e-apt` plugins from the Eclipse marketplace (Help -> Eclipse Marketplace...)
* File -> Import... -> Existing Maven Projects -> Select `bf` folder -> Finish

### IntelliJ IDEA
* Tested with IntelliJ 2016.1.3 Community Edition
* Open IntelliJ IDEA
* File -> New -> Project from existing Sources -> Select `bf` folder -> Click next and keep everything default on several screens -> Finish

## Running

* Execute `./bf hello.bf` to run a simple language source file.

## IGV

* Download the Ideal Graph Visualizer (IGV) from
  https://lafo.ssw.uni-linz.ac.at/pub/idealgraphvisualizer/
* Unpack the downloaded `.zip` file  
* Execute `bin/idealgraphvsiualizer` to start IGV
* Execute `./bf -dump tests/SumPrint.sl` to dump graphs to IGV.

## Debugging

* Execute `./bf -debug tests/HelloWorld.sl`.
* Attach a Java remote debugger (like Eclipse) on port 8000.

## Tested Compatibility

Truffle BF is compatible to:

* Truffle-Version: 0.18
* GraalVM-Version: 0.17


## Further information

* [Truffle JavaDoc](http://lafo.ssw.uni-linz.ac.at/javadoc/truffle/latest/)
* [Truffle on Github](http://github.com/graalvm/truffle)
* [Graal on Github](http://github.com/graalvm/graal-core)
* [Graal VM]( http://www.oracle.com/technetwork/oracle-labs/program-languages/overview) on the Oracle Technology Network
* [Publications on Truffle](hhttps://github.com/graalvm/truffle/blob/master/docs/Publications.md)
* [Publications on Graal](https://github.com/graalvm/graal-core/blob/master/docs/Publications.md)

## License

The Truffle framework is licensed under the [GPL 2 with Classpath exception](http://openjdk.java.net/legal/gplv2+ce.html).
Truffle BF is licensed under the [Universal Permissive License (UPL)](http://opensource.org/licenses/UPL).


