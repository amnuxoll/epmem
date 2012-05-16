#!/bin/bash

## Usage: This file must be run from inside the epmem/Ziggurat folder

## Purpose: To automate collection of data, for any map and any number
## of trials

## Output: A folder in parent directory named after arg3. Also a
## info.txt file that describes the test.

## Input: the number of trials, the name of the data set

## @Author: Dustin Dalen
## @Date modified: 4/5/2011
## @Date modified 08 Jul 2011 - made more robust, Linux-ified - :AMN:
## @Date modified 15 May 2012 - ported to the Java Ziggurat - :AMN:
##                          

##Wish List
## 1.  have the script verify that it is in the epmem/Ziggurat
##     directory and refuse to run otherwise
## 2.  Integerate ant into the Zigg build so we can use it to compile
##     the code


NUM_ARGS=2

#arg1 is how many trials
#arg2 is the name of the trial
#arg3 is any add'l args to pass to the MCP

#variables
NUM_TRIALS=$1
NAME=$2

if [ $# -lt $NUM_ARGS ]; then
        echo 'USAGE: gatherData <numTrials> <nameOfDataSet> <java params>'
        echo '!!must be in the epmem/Ziggurat folder!!'
        exit
fi
if [ -d ../$NAME ]; then
        echo "A test named: $NAME, has already been made. ABORTING"
        exit
fi

#Compile the source
javac *.java

#Make the output file and description file.
mkdir ../$NAME
touch ../$NAME/info.txt
echo "Number of trials: $NUM_TRIALS" >> ../$NAME/info.txt
echo "Description of dataset $NAME:" >> ../$NAME/info.txt

#Ask the user for more info
echo "Please enter a description of this data set (press Ctrl-D when finished):"
cat >> ../$NAME/info.txt

#the main loop
for (( j = 0 ; j < $NUM_TRIALS ; j++ )); do
    #run the program with the right args
    echo java Ziggurat.MCP seed=$j $3 $4 $5 $6 $7 $8 $9
    java Ziggurat.MCP seed=$j  $3 $4 $5 $6 $7 $8 $9 > temp

    #parse the output text into a single column of numbers (# steps to goal)
    grep "found after" temp | sed -e 's/s at timestamp.*//g' | sed -e 's/Goal.*found//g' | sed -e 's/[a-z,.]//g' | sed -e 's/ //g'  >> ../$NAME/results.txt
##If env is FlipPredict use this instead:        grep "predict" temp | sed -e 's/predict reward //g' >> ../$NAME/results.txt
    
    echo 'end' >> ../$NAME/results.txt  ##delimeter
done
rm temp

