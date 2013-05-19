Purpose of this project
=======================
This project was created to show how the Google App Engine memcache service can be used to enhance a Google App Engine app. To be specific the project should show how the memcache service can minimize the runtime and the costs of an Google App 
Engine app.

Applications
============
All in all there are three projects that test different aspects of the memcache service. 

High Frequency Trading App
--------------------------
High-frequency trading (HFT) is the use of sophisticated technological tools and computer algorithms to trade securities 
on a rapid basis (source: http://en.wikipedia.org/wiki/High-frequency_trading). Often these securities are only held for
a few milliseconds. Since the updates are that frequent it makes sense to save these updates only in the cache. Writing it
to the database would cost a lot of money and will probably slow down the app.

This app should test how much money could be saved when frequent data updates is saved in the memcache instead of the 
database. Completely replacing the database with the memcache can only be done, if it does not matter if data gets lost.
Since the updates appear this quickely it is assumed that it does not matter if an update gets lost.


OHLC Charts
-----------
When larger sets of data should be displayed it will probably take some time to load this data. If the data does not change over time, it is possible to save this data in the memcache. By doing this it will take less time to load the data.
 
Sport Betting App
-----------------
This application simulates a sport betting portal. It is assumed that there is one big event that most of the people are
interested in. The application tests how long it takes until all users get only cached results. Since the data is static,
i.e. it will not change over time, the results can be cached without taking the risk of presenting obsolete data.

Authors
=======
Joseph Helm (joseph.n.helm@gmail.com)

Markus Vieghofer (markus.vieghofer@gmail.com)
