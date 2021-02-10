FROM neo4j:4.2.3-enterprise
ENV NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
    NEO4J_AUTH=neo4j/password \
    NEO4J_dbms_memory_heap_initial__size=1g \
    NEO4J_dbms_memory_heap_max__size=1g \
    NEO4J_dbms_memory_pagecache_size=512m \
    NEO4J_dbms_security_authorization__providers=plugin-graph-auth
EXPOSE 7687
EXPOSE 7474
COPY build/libs/neo4j-graph-auth-1.0-SNAPSHOT.jar /plugins/
