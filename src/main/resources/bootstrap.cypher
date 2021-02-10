CREATE (dave:User {name:"dave"})
CREATE (neo4j:User {name:"neo4j"})
CREATE (sam:User {name:"sam"})

CREATE (users:Group {name:"All Users"})
CREATE (powerusers:Group {name:"Power Users"})
CREATE (superusers:Group {name: "Administrators"})

CREATE (powerusers)-[:MEMBER_OF]->(users)
CREATE (neo4j)-[:MEMBER_OF]->(users)
CREATE (sam)-[:MEMBER_OF]->(users)
CREATE (dave)-[:MEMBER_OF]->(powerusers)
CREATE (neo4j)-[:MEMBER_OF]->(superusers)

CREATE (admin:Role {name:"admin"})
CREATE (reader:Role {name:"reader"})
CREATE (architect:Role {name:"architect"})
CREATE (public:Role {name:"PUBLIC"})

CREATE (users)-[:HAS_ROLE]->(public)
CREATE (users)-[:HAS_ROLE]->(reader)
CREATE (powerusers)-[:HAS_ROLE]->(architect)
CREATE (superusers)-[:HAS_ROLE]->(admin)