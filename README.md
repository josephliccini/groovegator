groovegator
===========

An Open Source Java Grooveshark Downloader

![Alt text](GrooveGator/src/com/josephliccini/groovegator/GrooveGator_final_About.png?raw=true "Optional Title")


Based on SciLor's Java Grooveshark API, GrooveGator allows you to download songs on Grooveshark.com to your computer.
(please consider donating to SciLor at http://www.scilor.com/donate.html)
As long as you have Java installed on your computer, you will be able to run GrooveGator; this means you can run this on Mac, Linux, and Windows!

Disclaimer: 
I am not responsible fo rany violation of Grooveshark's terms of service
I am not associated with Grooshark.com in any way.
This is more or less a proof of concept


Building from source
====================
Typical maven setup, with one exception:

SciLor has asked specifically not to mirror his softare elsewhere.  In order to build this project, you must first download his GrooveAPI,
which can be found at:
http://www.scilor.com/java-grooveshark-downloader.html, and then get the .zip option

To include this in your local maven repository, first extract the .zip.

Then run:
```
mvn install:install-file -Dfile=/path/to/scilorsgrooveapi.jar -DgroupId=com.scilor -DartifactId=GrooveAPI -Dversion=1.0 -Dpackaging=jar
```
After that, there should be no problems building this using the provided pom.xml


