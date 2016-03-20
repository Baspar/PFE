#!/bin/bash
ant && java -cp lib/neo4j-jdbc-2.3.2-jar-with-dependencies.jar:bin test.testDAO
