# COP4520-Assignment3

## To Run Code in the Command Line for Problem 1:
* Make sure you are in the Assignment3-Problem1 directeory
* First type "javac BirthdayPresentsParty.java"
* Then type "java BirthdayPresentsParty"

## To Run Code in the Command Line for Problem 2:
* Make sure you are in the Assignment3-Problem2 directeory
* First type "javac TemperatureModule.java"
* Then type "TemperatureModule"

## Summary and Testing of Problem 1
The way I chose to handle this problem was first figuring out how to add the amount of presents into a sorted linked list and remove the same amount of gift into the list to write the thank you cards. So the way I choose to deal with this issue is by making sure the first presents added into the list would be the first presents to be removed, so that I would know what gifts I already add into the list so that I can remove it. The way this was implemented was using an array list that was filled from 0 to 500,000 and then making sure to shuffle the list so that I can present the unsorted bag of presents. Then using atomic integers the servants would get the index of the gfit they would add to the list and increment the count. And, when the servants would want to remove a gift to write the card, they would follow the same array list but keep track of the atomic integer index for add to make sure it doesn’t overtake it. Also when it comes to adding and removing into the list, the process I chose was using a non-blocking list since it allows all the threads to edit the list without the need of a lock and it's also a quick process. 

When it comes to efficacy and correctness, my implantation fills in those requirements. Since at the very end the servants are able to add 500,000 gifts into the linked list as well as removing the gifts from the list and creating the thank you cards. Also I made sure the servants would more often add or remove a gift but still be able to search for a gift based on the minotaurs request. Also during my testing I made sure the linked list was empty after all the threads ran, so I made the link list print all the keys of its nodes until it reached null which only produced a -1 which was the key of the head of the list. Showing that after all the threads run we are able to empty up the list. When it comes to the efficiency of my implantation after running it a couple of times, the execution is around 2000 milliseconds or just 2 seconds. 

## Summary and Testing for Problem 2
