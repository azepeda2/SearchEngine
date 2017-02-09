# Searchable Inverted Index (w/Parallel Programming)

# Table of Contents

* [Introduction](#introduction)
* [Author](#author)
* [Project Demo](#demo)

# <a name="introduction"></a>Introduction
    The purpose of this project was to modify the seachable inverted index and 
	increase perfomance by using parallel programming techniques. The program 
	traverses through all files and subdirectories within an a user defined 
	directory and creates a list of all words found within the files, 
	filenames, and the location of the word within the file.
    
# <a name="author"></a>Author
* "Alejandro Zepeda" <zepedaalex25@gmail.com>

# <a name="demo"></a>Project Demo

	In this program, similar concepts were used as the last project 
	but there were changes to the way that parsing of files and 
	searching of queries is implemented. In order to create parallel 
	ready functions, the functions that normally did these tasks were 
	broken down into parsing individual files per thread and storing 
	individual results until parsing is finished. At this point, the 
	partial results are added to the shared inverted index, which is 
	synchronized by the write lock I implemented. As for the 
	searching, individual (single and multi-line) queries are divided 
	into single query per thread and partial results are kept in a 
	personal HashMap until searching is completed. At this point, the 
	partial results are added to the shared TreeMap of SearchResults 
	and synchronization is maintained by another instance of the write
	lock. The following is just an overview of the features carried 
	over from the last project:For project 2, the purpose was to 
	extend the InvertedIndex class to support searching of the index. 
	I added a method to the FileParser class that parses the queries 
	by line from a file entered by the user and a SearchInvertedIndex 
	class that uses it to search the InvertedIndex for words that 
	start with the multiple queries. I chose to store the queries by 
	maintaining a HashMap<String, TreeSet<SearchResult>> that mapped 
	queries to TreeSets of SearchResult Objects. The SearchResult 
	object stores the file a words were found in, the frequency of 
	the words, and the first location of the words that matched the 
	query. The TreeSets sorted the results by implementing 
	Comparable and properly implementing a compareTo() method to sort 
	the SearchResults. They are first sorted by the highest frequency,
	and then by earliest initial location of the query. If a query 
	contained multiple words, they were split by white space and the 
	results for those words were stored in the same results for that 
	query. I felt that this approach was best because it not only 
	ensured the results were sorted as requested but didn't involve 
	storing the data twice. The ArrayList of queries is returned by 
	the FileParser class and stored in the SearchIndexClass in order 
	to maintain order of queries and not store the list twice.