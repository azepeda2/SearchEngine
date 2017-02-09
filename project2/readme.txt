Author:		 Alejandro Zepeda
Email:		 zepedaalex25@gmail.com
Student id:	 20173490
Description: In this program, the same functionalities and methods were kept 
             from the last project that involved creating the inverted index.
             For project 2, the purpose was to extend the InvertedIndex class
             to support searching of the index. I added a method to the 
             FileParser class that parses the queries by line from a file
             entered by the user and a SearchInvertedIndex class that uses it
             to search the InvertedIndex for words that start with the multiple
             queries. I chose to store the queries by maintaining a 
             HashMap<String, TreeSet<SearchResult>> that mapped queries to 
             TreeSets of SearchResult Objects. The SearchResult object stores  
             the file a words were found in, the frequency of the words, and 
             the first location of the words that matched the query. The 
             TreeSets sorted the results by implementing Comparable and properly
             implementing a compareTo() method to sort the SearchResults. 
             They are first sorted by the highest frequency, and then by 
             earliest initial location of the query. If a query contained 
             multiple words, they were split by white space and the results 
             for those words were stored in the same results for that query.
             I felt that this approach was best because it not only ensured
             the results were sorted as requested but didn't involve storing
             the data twice. The ArrayList of queries is returned by the 
             FileParser class and stored in the SearchIndexClass in order to
             maintain order of queries and not store the list twice.