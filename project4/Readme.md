# WebCrawler

# Table of Contents

* [Introduction](#introduction)
* [Author](#author)
* [Project Demo](#demo)

# <a name="introduction"></a>Introduction
    The purpose of this project was to utilize the methods used in the inverted 
	index project and incorporate them into searching through several websites. 
	Rather than traverse files and directories on a computer, it is given a 
	seed URL and searches through a set amount of URL links found within that 
	webpage and the webpages it links to. It also allows the user to search 
	through the created index for words or phrases found within the webpages.
    
# <a name="author"></a>Author
* "Alejandro Zepeda" <zepedaalex25@gmail.com>

# <a name="demo"></a>Project Demo

	In this program, I added a new class called WebCrawler that 
	handles the fetching and parsing of web pages based on the seed 
	URL. The WebCrawler creates an initial WebCrawlerWorker from the 
	seed URL, which reads in the page line by line, extracting HTML 
	tags, entities, and elements that are not part of the text. If 
	it finds links to other web pages, it checks to see if the link 
	hasn't already been parsed, removes the fragmented portion of the 
	URL and creates a new WebCrawlerWorker for the link. Each worker 
	maintains a personal InvertedIndex that is created for the 
	current web page and adds its data to the main InvertedIndex when
	it is finished. I ensured synchronization by utilizing my 
	MultiReaderLock I implemented. If a worker encounters an error 
	parsing a web page, it simply removes that URL from the list of 
	URLs and waits for more work. There is a maximum of 50 links to be
	that can be parsed as requested and the program checks that the 
	URL hasn't been parsed by calling the sameFile() from URL. I chose
	to include all of the methods for parsing the web pages in my
	private class WebCrawlerWorker since it is the only place where it 
	is utilized in this project. If a user decides to insert a 
	seed directory instead of a seed URL, the program is still able 
	to work so long as they indicate it is a directory by including 
	-d <directory> instead of -u <seed> in the command-line 
	arguments. From the last project, In order to create parallel 
	ready functions, the functions that normally did these tasks were 
	broken down into parsing individual files per thread and storing 
	individual results until parsing is finished. At this point, the 
	partial results are added to the shared inverted index, which is 
	synchronized by the write lock I implemented. As for the 
	searching, individual (single and multi-line) queries are divided 
	into single query per thread and partial results are kept in own
	InvertedIndex until searching is completed. At this point, the 
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