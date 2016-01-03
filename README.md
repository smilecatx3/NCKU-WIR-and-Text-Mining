# Web Information Retrieval and Text Mining

Implementation of NCKU CSIE 'Web Information Retrieval and Text Mining' course projects using Java  

- Project 1: Small search engine
- Project 2: Implemetation of HITS, PageRank, and SimRank algorithms
- Project 3: Implementation of kNN, k-Means and Hierarchical Clustering algorithms


### Library Dependencies

- [Apache Commons Collections] (http://commons.apache.org/proper/commons-collections/)
- [Apache Commons IO] (http://commons.apache.org/proper/commons-io/)
- [Apache Commons Lang] (http://commons.apache.org/proper/commons-lang/)
- [Apache Commons Math] (http://commons.apache.org/proper/commons-math/)
- [JSON] (https://github.com/douglascrockford/JSON-java)
- [MySQL Connector/J] (https://dev.mysql.com/downloads/connector/j/)
- [MMSeg] (https://github.com/chenlb/mmseg4j-core)
- [JAWS] (http://lyle.smu.edu/~tspell/jaws/)


**NOTE**

For Project 1, you need to prepare the following files to use *MMSeg* and *JAWS* libraries  

- [Chinese words database] (https://github.com/chenlb/mmseg4j-core)    
You should download "chars.dic", "units.dic" and "words.dic". Additionally, you need to append two lines "%" and "/" at the end of "units.dic". (Note that you should convert these files to Chinese Traditional yourself.)  
- [Wordnet database] (https://wordnet.princeton.edu/)

Then you can set the dictionary path in the config file.