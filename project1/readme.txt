Author:		 Alejandro Zepeda
Email:		 zepedaalex25@gmail.com
Student id:	 20173490
Description: In this program, the purpose is to take in a directory and recursively traverse through
			 the sub-directories and build a TreeMap of each word in the files and the locations they
			 are found in. I decided to break up the problem into five classes: ArgumentParser, 
			 DirectoryTraverser, FileParser, InvertedIndex, and Driver. In order to store the data, 
			 I chose to store the files in an ArrayList initially and then words, files, and locations
			 in an inverted index of a TreeMap<String,<TreeMap<String>, ArrayList<Integer>>>.
			 The purpose of this, is to keep the filenames organized so that the files are stored in
			 order. The final structure of the index is a TreeMap with the words as keys with an 
			 inner TreeMap with the file paths as keys and ArrayList<Integer> as the values. I 
			 felt that this was the best method since the words had to have distinct locations for 
			 each file and we could always check if the word is in the file before printing like I 
			 did in the writeResults method in InvertedIndex.java. 