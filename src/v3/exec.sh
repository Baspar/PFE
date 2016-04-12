#!/bin/bash
[ -e bin/test/consoleDAO.class ] && java -cp lib/neo4j-jdbc-2.3.2-jar-with-dependencies.jar:bin test.consoleDAO || echo "Veuillez compiler au préalable"
